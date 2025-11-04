/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.test.transfer.unittests;

import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.test.transfer.ingame.TransferTestInitializer;

class FluidTests extends AbstractTransferApiTest {
	private static FluidVariant TAGGED_WATER, TAGGED_WATER_2, WATER, LAVA;
	private static int finalCommitCount = 0;
	public static DataComponentType<Integer> TEST;
	private static SingleSlotStorage<FluidVariant> createWaterStorage() {
		return new SingleVariantStorage<>() {
			@Override
			protected FluidVariant getBlankVariant() {
				return FluidVariant.blank();
			}

			@Override
			protected long getCapacity(FluidVariant fluidVariant) {
				return BUCKET * 2;
			}

			@Override
			protected boolean canInsert(FluidVariant fluidVariant) {
				return fluidVariant.isOf(Fluids.WATER);
			}

			@Override
			protected void onFinalCommit() {
				finalCommitCount++;
			}
		};
	}

	@BeforeAll
	static void beforeAll() {
		bootstrap();

		DataComponentPatch components = DataComponentPatch.builder()
				.set(TEST, 1)
				.build();
		TAGGED_WATER = FluidVariant.of(Fluids.WATER, components);
		TAGGED_WATER_2 = FluidVariant.of(Fluids.WATER, components);
		WATER = FluidVariant.of(Fluids.WATER);
		LAVA = FluidVariant.of(Fluids.LAVA);
		TEST = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(TransferTestInitializer.MOD_ID, "test"),
								DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
	}

	@Test
	public void testFluidStorage() {
		SingleSlotStorage<FluidVariant> waterStorage = createWaterStorage();

		// Test content
		if (!waterStorage.isResourceBlank()) throw new AssertionError("Should have been blank");

		// Test some insertions
		try (Transaction tx = Transaction.openOuter()) {
			// Should not allow lava (canInsert returns false)
			if (waterStorage.insert(LAVA, BUCKET, tx) != 0) throw new AssertionError("Lava inserted");
			// Should allow insert, but without mutating the storage.
			if (StorageUtil.simulateInsert(waterStorage, WATER, BUCKET, tx) != BUCKET) throw new AssertionError("Simulated insert failed");
			// Should allow insert
			if (waterStorage.insert(TAGGED_WATER, BUCKET, tx) != BUCKET) throw new AssertionError("Tagged water insert 1 failed");
			// Variants are different, should not allow insert
			if (waterStorage.insert(WATER, BUCKET, tx) != 0) throw new AssertionError("Water inserted");
			// Should allow insert again even if the variant is different cause they are equal
			if (waterStorage.insert(TAGGED_WATER_2, BUCKET, tx) != BUCKET) throw new AssertionError("Tagged water insert 2 failed");
			// Should not allow further insertion because the storage is full
			if (waterStorage.insert(TAGGED_WATER, BUCKET, tx) != 0) throw new AssertionError("Storage full, yet something was inserted");
			// Should allow extraction
			if (waterStorage.extract(TAGGED_WATER_2, BUCKET, tx) != BUCKET) throw new AssertionError("Extraction failed");
			// Simulated extraction should succeed but do nothing
			if (StorageUtil.simulateExtract(waterStorage, TAGGED_WATER, Long.MAX_VALUE, tx) != BUCKET) throw new AssertionError("Simulated extraction failed");
			// Re-insert
			if (waterStorage.insert(TAGGED_WATER_2, BUCKET, tx) != BUCKET) throw new AssertionError("Tagged water insert 3 failed");
			// Test contents
			if (waterStorage.getAmount() != BUCKET * 2 || !waterStorage.getResource().equals(TAGGED_WATER_2)) throw new AssertionError("Contents are wrong");
			// No commit -> will abort
		}

		// Test content again to make sure the rollback worked as expected
		if (!waterStorage.isResourceBlank()) throw new AssertionError("Should have been blank");

		// Test highly nested commit
		try (Transaction tx = Transaction.openOuter()) {
			if (waterStorage.getAmount() != 0) throw new AssertionError("Initial amount is wrong");
			if (waterStorage.insert(WATER, BUCKET, tx) != BUCKET) throw new AssertionError("Water insertion failed");

			try (Transaction nested1 = tx.openNested()) {
				try (Transaction nested2 = nested1.openNested()) {
					if (waterStorage.insert(WATER, BUCKET, nested2) != BUCKET) throw new AssertionError("Nested insertion failed");
					if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Two buckets have been inserted");
					nested2.commit();
				}

				if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Nested no 1 was committed, so we should still have two buckets");
				nested1.commit();
			}

			if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Nested no 1 was committed, so we should still have two buckets");
		}

		if (waterStorage.getAmount() != 0) throw new AssertionError("Amount should have been reverted to zero");

		// Test nested commit to make sure it behaves as expected

		// Without outer commit
		insertWaterWithNesting(waterStorage, false);
		if (waterStorage.getAmount() != 0) throw new AssertionError("Amount should have been reverted to zero");
		if (finalCommitCount != 0) throw new AssertionError("Nothing should have called onFinalCommit() yet (no outer commit)");

		// With outer commit
		insertWaterWithNesting(waterStorage, true);
		if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Outer was committed, so we should still have two buckets");
		if (finalCommitCount != 1) throw new AssertionError("onFinalCommit() should have been called exactly once.");
	}

	@Test
	void testPacketCodec() {
		FluidVariant variant = FluidVariant.of(Fluids.WATER, DataComponentPatch.builder().set(TEST, 1).build());
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		RegistryFriendlyByteBuf rbuf = new RegistryFriendlyByteBuf(buf, staticDrm());
		FluidVariant.PACKET_CODEC.encode(rbuf, variant);

		FluidVariant decoded = FluidVariant.PACKET_CODEC.decode(rbuf);
		Assertions.assertTrue(variant.equals(decoded));
	}

	private static void insertWaterWithNesting(SingleSlotStorage<FluidVariant> waterStorage, boolean doOuterCommit) {
		try (Transaction tx = Transaction.openOuter()) {
			if (waterStorage.getAmount() != 0) throw new AssertionError("Initial amount is wrong");
			if (waterStorage.insert(WATER, BUCKET, tx) != BUCKET) throw new AssertionError("Water insertion failed");

			try (Transaction nested = tx.openNested()) {
				if (waterStorage.insert(WATER, BUCKET, nested) != BUCKET) throw new AssertionError("Nested insertion failed");
				if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Two buckets have been inserted");
				nested.commit();
			}

			if (waterStorage.getAmount() != 2 * BUCKET) throw new AssertionError("Nested was committed, so we should still have two buckets");

			if (doOuterCommit) {
				tx.commit();
			}
		}
	}
}
