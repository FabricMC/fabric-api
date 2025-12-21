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

package net.fabricmc.fabric.mixin.renderer.client.block.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.renderer.ExtendedBlockModelSubmit;
import net.fabricmc.fabric.impl.renderer.ExtendedBlockSubmit;
import net.fabricmc.fabric.impl.renderer.SubmitNodeCollectionExtension;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, SubmitNodeCollectionExtension {
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;
	@Shadow
	private boolean wasUsed;

	@Unique
	private final List<ExtendedBlockSubmit> extendedBlockSubmits = new ArrayList<>();
	@Unique
	private final List<ExtendedBlockModelSubmit> extendedBlockModelSubmits = new ArrayList<>();

	@Override
	public void submitBlock(PoseStack poseStack, BlockState state, int light, int overlay, int outlineColor, BlockAndTintGetter level, BlockPos pos) {
		wasUsed = true;
		extendedBlockSubmits.add(new ExtendedBlockSubmit(poseStack.last().copy(), state, light, overlay, outlineColor,
				level, pos));
		Minecraft.getInstance().getModelManager().specialBlockModelRenderer().renderByBlock(state.getBlock(), ItemDisplayContext.NONE,
				poseStack, submitNodeStorage, light, overlay, outlineColor);
	}

	@Override
	public void submitBlockStateModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		wasUsed = true;
		extendedBlockModelSubmits.add(new ExtendedBlockModelSubmit(
				poseStack.last().copy(),
				renderTypeFunction, model, r, g, b, light, overlay, outlineColor,
				level, pos, state));
	}

	@Override
	public List<ExtendedBlockSubmit> fabric_getExtendedBlockSubmits() {
		return extendedBlockSubmits;
	}

	@Override
	public List<ExtendedBlockModelSubmit> fabric_getExtendedBlockModelSubmits() {
		return extendedBlockModelSubmits;
	}

	@Inject(method = "clear", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		extendedBlockSubmits.clear();
		extendedBlockModelSubmits.clear();
	}
}
