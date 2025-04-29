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
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;

public class FrameBlockEntity extends BlockEntity implements RenderDataBlockEntity {
	private static final Codec<Block> BLOCK_CODEC = Registries.BLOCK.getCodec();

	@Nullable
	private Block block = null;

	public FrameBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(Registration.FRAME_BLOCK_ENTITY_TYPE, blockPos, blockState);
	}

	@Override
	public void readData(ReadView data) {
		super.readData(data);

		block = data.read("block", BLOCK_CODEC).orElse(null);

		if (block == Blocks.AIR) {
			block = null;
		}

		if (this.getWorld() != null && this.getWorld().isClient()) {
			// This call forces a chunk remesh.
			world.updateListeners(pos, null, null, 0);
		}
	}

	@Override
	public void writeData(WriteView data) {
		super.writeData(data);

		if (block != null) {
			data.put("block", BLOCK_CODEC, block);
		} else {
			// Always need something in the tag, otherwise S2C syncing will never apply the packet.
			data.put("block", BLOCK_CODEC, Blocks.AIR);
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();

		if (this.hasWorld() && !this.getWorld().isClient()) {
			((ServerWorld) world).getChunkManager().markForUpdate(getPos());
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
		this.markDirty();
	}

	@Nullable
	@Override
	public Block getRenderData() {
		return this.block;
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
		return this.createComponentlessNbt(wrapperLookup);
	}
}
