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

package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;

import net.fabricmc.fabric.impl.client.rendering.GuiRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.LevelRendererExtensions;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private GuiRenderer guiRenderer;

	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void guiRendererReady(Minecraft client, ItemInHandRenderer firstPersonHeldItemRenderer, RenderBuffers buffers, BlockRenderDispatcher blockRenderManager, CallbackInfo ci) {
		GuiRendererExtensions guiRenderer = (GuiRendererExtensions) this.guiRenderer;
		guiRenderer.fabric_onReady(this.submitNodeStorage);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;extractLevel(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V"))
	private void beforeExtract(DeltaTracker deltaTracker, CallbackInfo ci, @Local(name = "renderOutline") boolean renderOutline, @Local(name = "modelViewMatrix") Matrix4f modelViewMatrix, @Local(name = "cameraState") CameraRenderState cameraState) {
		((LevelRendererExtensions) (Object) minecraft.levelRenderer).fabric_prepareLevelExtractionContext(
				deltaTracker,
				renderOutline,
				modelViewMatrix,
				cameraState
		);
	}
}
