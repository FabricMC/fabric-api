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

package net.fabricmc.fabric.mixin.debug.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

import net.fabricmc.fabric.impl.debug.client.renderer.DebugRendererRegistryImpl;

@Mixin(DebugRenderer.class)
abstract class DebugRendererMixin {
	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> renderers;

	private DebugRendererMixin() {
	}

	@Inject(method = "refreshRendererList", at = @At("RETURN"))
	private void registerRenderers(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		for (DebugRendererRegistryImpl.Entry entry : DebugRendererRegistryImpl.RENDERERS) {
			// a Stream#map would make the most sense here, but they're banned
			// so you have to suffer with me now
			renderers.add(entry.rendererFactory().create(minecraft));
		}
	}
}
