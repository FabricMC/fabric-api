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

package net.fabricmc.fabric.mixin.content.registry;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockAbstractBlockStateMixin extends State<Block, BlockState> implements OxidizableBlocksRegistryImpl.RandomTickCacheRefresher {
	@Shadow
	private boolean ticksRandomly;

	private AbstractBlockAbstractBlockStateMixin(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
		super(owner, propertyMap, codec);
	}

	@Override
	public void fabric_api$refreshRandomTickCache() {
		this.ticksRandomly = ((AbstractBlockAccessor) this.owner).callHasRandomTicks(this.asBlockState());
	}

	@Shadow
	protected BlockState asBlockState() {
		return null;
	}
}
