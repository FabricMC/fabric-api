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
import net.minecraft.client.resources.model.ModelManager;

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
	private void guiRendererReady(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, RenderBuffers renderBuffers, ModelManager modelManager, CallbackInfo ci) {
		GuiRendererExtensions guiRenderer = (GuiRendererExtensions) this.guiRenderer;
		guiRenderer.fabric_onReady(this.submitNodeStorage);
	}

	@Inject(method = "extract", at = @At(value = "HEAD"))
	private void beforeExtract(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		((LevelRendererExtensions) (Object) minecraft.levelRenderer).fabric_prepareLevelExtractionContext(deltaTracker);
	}
}
