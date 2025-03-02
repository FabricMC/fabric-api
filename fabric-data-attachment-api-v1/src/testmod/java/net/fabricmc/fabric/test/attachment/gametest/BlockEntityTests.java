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

package net.fabricmc.fabric.test.attachment.gametest;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;
import net.fabricmc.fabric.test.attachment.mixin.BlockEntityTypeAccessor;

public class BlockEntityTests {
	private static final Logger LOGGER = LogUtils.getLogger();

	@GameTest
	public void testBlockEntitySync(TestContext context) {
		BlockPos pos = BlockPos.ORIGIN.up();

		for (RegistryEntry<BlockEntityType<?>> entry : Registries.BLOCK_ENTITY_TYPE.getIndexedEntries()) {
			Block supportBlock = ((BlockEntityTypeAccessor) entry.value()).getBlocks().iterator().next();

			if (!supportBlock.isEnabled(context.getWorld().getEnabledFeatures())) {
				LOGGER.info("Skipped disabled feature {}", entry);
				continue;
			}

			BlockEntity be = entry.value().instantiate(pos, supportBlock.getDefaultState());

			if (be == null) {
				LOGGER.info("Couldn't get a block entity for type " + entry);
				continue;
			}

			be.setWorld(context.getWorld());
			be.setAttached(AttachmentTestMod.PERSISTENT, "test");
			Packet<ClientPlayPacketListener> packet = be.toUpdatePacket();

			if (packet == null) {
				// Doesn't send update packets, fine
				continue;
			}

			if (!(packet instanceof BlockEntityUpdateS2CPacket)) {
				LOGGER.warn("Not a BE packet for {}, instead {}", entry, packet);
				continue;
			}

			NbtCompound nbt = ((BlockEntityUpdateS2CPacket) packet).getNbt();

			if (nbt != null && nbt.contains(AttachmentTarget.NBT_ATTACHMENT_KEY)) {
				// Note: this is a vanilla bug (it called createNbt, instead of the correct createComponentlessNbt)
				throw context.createError("Packet NBT for " + entry + " had persistent data: " + nbt);
			}
		}

		context.complete();
	}
}
