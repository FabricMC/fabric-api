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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;

@Mixin(BlockModelRenderer.class)
abstract class BlockModelRendererMixin {
	@Overwrite
	public static void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, int light, int overlay) {
		FabricBlockModelRenderer.render(entry, layer -> vertexConsumer, model, red, green, blue, light, overlay, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());
	}
}
