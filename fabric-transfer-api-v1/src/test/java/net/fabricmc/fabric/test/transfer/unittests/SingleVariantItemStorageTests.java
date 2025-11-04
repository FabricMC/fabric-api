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
import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.test.transfer.ingame.TransferTestInitializer;

public class SingleVariantItemStorageTests extends AbstractTransferApiTest {
	private static FluidVariant LAVA;
	public static DataComponentType<FluidData> FLUID;

	@BeforeAll
	static void beforeAll() {
		bootstrap();

		LAVA = FluidVariant.of(Fluids.LAVA);
		FLUID = Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(TransferTestInitializer.MOD_ID, "fluid"),
				DataComponentType.<FluidData>builder().persistent(FluidData.CODEC).networkSynchronized(FluidData.PACKET_CODEC).build());
	}

	@Test
	public void testWaterTank() {
		SimpleContainer inv = new SimpleContainer(new ItemStack(Items.DIAMOND, 2), ItemStack.EMPTY);
		ContainerItemContext ctx = new InventoryContainerItemContext(inv);

		Storage<FluidVariant> storage = createTankStorage(ctx);

		try (Transaction tx = Transaction.openOuter()) {
			// Insertion should succeed and transfer an item into the second slot.
			assertEquals(BUCKET, storage.insert(LAVA, BUCKET, tx));
			// Insertion should create a new stack.
			assertEquals(1, inv.getItem(0).getCount());
			assertEquals(DataComponentPatch.EMPTY, inv.getItem(0).getComponentsPatch());
			assertEquals(1, inv.getItem(1).getCount());
			assertEquals(LAVA, getFluid(inv.getItem(1)));
			assertEquals(BUCKET, getAmount(inv.getItem(1)));

			// Second insertion should just insert in place as the count is now 1.
			assertEquals(BUCKET, storage.insert(LAVA, BUCKET, tx));

			for (int slot = 0; slot < 2; ++slot) {
				assertEquals(LAVA, getFluid(inv.getItem(slot)));
				assertEquals(BUCKET, getAmount(inv.getItem(slot)));
			}

			tx.commit();
		}

		// Make sure other components are kept.
		Component customName = Component.literal("Lava-containing diamond!");
		inv.getItem(0).set(DataComponents.CUSTOM_NAME, customName);

		try (Transaction tx = Transaction.openOuter()) {
			// Test extract along the way.
			assertEquals(BUCKET, storage.extract(LAVA, 10 * BUCKET, tx));

			tx.commit();
		}

		// Check custom name.
		assertEquals(customName, inv.getItem(0).getHoverName());
		assertEquals(FluidVariant.blank(), getFluid(inv.getItem(0)));
		assertEquals(0L, getAmount(inv.getItem(0)));
	}

	@Test
	public void writeNbtTest() {
		SingleItemStorage storage = new SingleItemStorage() {
			@Override
			protected long getCapacity(ItemVariant variant) {
				return 10;
			}
		};

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx);
			tx.commit();
		}

		TagValueOutput writeView = TagValueOutput.createWithoutContext(null);
		storage.writeData(writeView);
		assertEquals("{amount:1L,variant:{item:\"minecraft:diamond\"}}", writeView.buildResult().toString());
	}

	@Test
	public void writeNbtWithComponentTest() {
		SingleItemStorage storage = new SingleItemStorage() {
			@Override
			protected long getCapacity(ItemVariant variant) {
				return 10;
			}
		};

		try (Transaction tx = Transaction.openOuter()) {
			ItemStack stack = new ItemStack(Items.DIAMOND);
			stack.set(DataComponents.CUSTOM_NAME, Component.literal("test name"));
			storage.insert(ItemVariant.of(stack), 1, tx);
			tx.commit();
		}

		TagValueOutput writeView = TagValueOutput.createWithoutContext(null);
		storage.writeData(writeView);
		assertEquals("{amount:1L,variant:{components:{\"minecraft:custom_name\":\"test name\"},item:\"minecraft:diamond\"}}", writeView.buildResult().toString());
	}

	@Test
	public void readNbtTest() {
		SingleItemStorage storage = new SingleItemStorage() {
			@Override
			protected long getCapacity(ItemVariant variant) {
				return 10;
			}
		};

		CompoundTag variantNbt = new CompoundTag();
		variantNbt.putString("item", "minecraft:diamond");
		variantNbt.put("components", new CompoundTag());
		CompoundTag nbt = new CompoundTag();
		nbt.putLong("amount", 1);
		nbt.put("variant", variantNbt);

		storage.readData(TagValueInput.create(ProblemReporter.DISCARDING, staticDrm(), nbt));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1L, storage.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}
	}

	@Test
	public void readInvalidNbtTest() {
		SingleItemStorage storage = new SingleItemStorage() {
			@Override
			protected long getCapacity(ItemVariant variant) {
				return 10;
			}
		};

		// Test that invalid NBT defaults to empty.
		CompoundTag variantNbt = new CompoundTag();
		variantNbt.putString("id", "minecraft:diamond");
		CompoundTag nbt = new CompoundTag();
		nbt.putLong("amount", 1);
		nbt.put("variant", variantNbt);

		storage.readData(TagValueInput.create(ProblemReporter.DISCARDING, staticDrm(), nbt));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0L, storage.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}
	}

	private static FluidVariant getFluid(ItemStack stack) {
		return stack.getOrDefault(FLUID, FluidData.DEFAULT).variant();
	}

	private static long getAmount(ItemStack stack) {
		return stack.getOrDefault(FLUID, FluidData.DEFAULT).amount();
	}

	private static void setContents(ItemStack stack, FluidVariant newResource, long newAmount) {
		if (newAmount > 0) {
			FluidData fluidData = new FluidData(newResource, newAmount);
			stack.set(FLUID, fluidData);
		} else {
			// Make sure emptied tanks can stack with tanks without components.
			// Note: because we use a vanilla item (diamond), we need to remove;
			// a custom item should instead set the fluid to the default value as specified in the item settings.
			stack.remove(FLUID);
		}
	}

	public record FluidData(FluidVariant variant, long amount) {
		public static Codec<FluidData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				FluidVariant.CODEC.fieldOf("variant").forGetter(FluidData::variant),
				Codec.LONG.fieldOf("amount").forGetter(FluidData::amount)
		).apply(instance, FluidData::new));
		public static StreamCodec<RegistryFriendlyByteBuf, FluidData> PACKET_CODEC = StreamCodec.composite(
				FluidVariant.PACKET_CODEC, FluidData::variant,
				ByteBufCodecs.VAR_LONG, FluidData::amount,
				FluidData::new
		);

		public static FluidData DEFAULT = new FluidData(FluidVariant.blank(), 0);
	}

	private static Storage<FluidVariant> createTankStorage(ContainerItemContext ctx) {
		return new SingleVariantItemStorage<>(ctx) {
			@Override
			protected FluidVariant getBlankResource() {
				return FluidVariant.blank();
			}

			@Override
			protected FluidVariant getResource(ItemVariant currentVariant) {
				return getFluid(currentVariant.toStack());
			}

			@Override
			protected long getAmount(ItemVariant currentVariant) {
				return SingleVariantItemStorageTests.getAmount(currentVariant.toStack());
			}

			@Override
			protected long getCapacity(FluidVariant variant) {
				return 2 * BUCKET;
			}

			@Override
			protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
				// Operate on the stack directly to keep any other components such as a custom name or enchant.
				ItemStack stack = currentVariant.toStack();
				setContents(stack, newResource, newAmount);
				return ItemVariant.of(stack);
			}
		};
	}

	private static class InventoryContainerItemContext implements ContainerItemContext {
		private final InventoryStorage storage;

		private InventoryContainerItemContext(Container inventory) {
			this.storage = InventoryStorage.of(inventory, null);
		}

		@Override
		public SingleSlotStorage<ItemVariant> getMainSlot() {
			return storage.getSlot(0);
		}

		@Override
		public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
			return storage.insert(itemVariant, maxAmount, transactionContext);
		}

		@Override
		public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
			return storage.getSlots();
		}
	}
}
