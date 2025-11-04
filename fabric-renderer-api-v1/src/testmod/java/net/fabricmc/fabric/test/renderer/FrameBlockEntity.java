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

package net.fabricmc.fabric.test.renderer;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;

public class FrameBlockEntity extends BlockEntity implements RenderDataBlockEntity {
	private static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();

	@Nullable
	private Block block = null;

	public FrameBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(net.fabricmc.fabric.test.renderer.Registration.FRAME_BLOCK_ENTITY_TYPE, blockPos, blockState);
	}

	@Override
	public void loadAdditional(ValueInput data) {
		super.loadAdditional(data);

		block = data.read("block", BLOCK_CODEC).orElse(null);

		if (block == Blocks.AIR) {
			block = null;
		}

		if (this.getLevel() != null && this.getLevel().isClientSide()) {
			// This call forces a chunk remesh.
			level.sendBlockUpdated(worldPosition, null, null, 0);
		}
	}

	@Override
	public void saveAdditional(ValueOutput data) {
		super.saveAdditional(data);

		if (block != null) {
			data.store("block", BLOCK_CODEC, block);
		} else {
			// Always need something in the tag, otherwise S2C syncing will never apply the packet.
			data.store("block", BLOCK_CODEC, Blocks.AIR);
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (this.hasLevel() && !this.getLevel().isClientSide()) {
			((ServerLevel) level).getChunkSource().blockChanged(getBlockPos());
		}
	}

	@Nullable
	public Block getBlock() {
		return this.block;
	}

	public void setBlock(@Nullable Block block) {
		if (block == Blocks.AIR) {
			block = null;
		}

		this.block = block;
		this.setChanged();
	}

	@Nullable
	@Override
	public Block getRenderData() {
		return this.block;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider wrapperLookup) {
		return this.saveCustomOnly(wrapperLookup);
	}
}
