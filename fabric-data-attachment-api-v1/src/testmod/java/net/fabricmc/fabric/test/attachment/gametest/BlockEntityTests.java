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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;
import net.fabricmc.fabric.test.attachment.mixin.BlockEntityTypeAccessor;

public class BlockEntityTests {
	private static final Logger LOGGER = LogUtils.getLogger();

	@GameTest
	public void testBlockEntitySync(GameTestHelper helper) {
		BlockPos pos = BlockPos.ZERO.above();

		for (Holder<BlockEntityType<?>> holder : BuiltInRegistries.BLOCK_ENTITY_TYPE.asHolderIdMap()) {
			Block supportBlock = ((BlockEntityTypeAccessor) holder.value()).getBlocks().iterator().next();

			if (!supportBlock.isEnabled(helper.getLevel().enabledFeatures())) {
				LOGGER.info("Skipped disabled feature {}", holder);
				continue;
			}

			BlockEntity be = holder.value().create(pos, supportBlock.defaultBlockState());

			if (be == null) {
				LOGGER.info("Couldn't get a block entity for type " + holder);
				continue;
			}

			be.setLevel(helper.getLevel());
			be.setAttached(AttachmentTestMod.PERSISTENT, "test");
			Packet<ClientGamePacketListener> packet = be.getUpdatePacket();

			if (packet == null) {
				// Doesn't send update packets, fine
				continue;
			}

			if (!(packet instanceof ClientboundBlockEntityDataPacket)) {
				LOGGER.warn("Not a BE packet for {}, instead {}", holder, packet);
				continue;
			}

			CompoundTag tag = ((ClientboundBlockEntityDataPacket) packet).getTag();

			if (tag != null && tag.contains(AttachmentTarget.NBT_ATTACHMENT_KEY)) {
				// Note: this is a vanilla bug (it called createNbt, instead of the correct createComponentlessNbt)
				throw helper.assertionException("Packet NBT for " + holder + " had persistent data: " + tag);
			}
		}

		helper.succeed();
	}
}
