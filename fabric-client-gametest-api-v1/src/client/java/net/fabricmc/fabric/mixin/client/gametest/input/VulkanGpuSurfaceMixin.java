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

package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkOffset3D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;

@Mixin(targets = "com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface")
public class VulkanGpuSurfaceMixin {
	@WrapOperation(method = "blitFromTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageBlit$Buffer;dstOffsets(Lorg/lwjgl/vulkan/VkOffset3D$Buffer;)Lorg/lwjgl/vulkan/VkImageBlit$Buffer;"))
	private VkImageBlit.Buffer blitFrameBuffer(VkImageBlit.Buffer blitRegion, VkOffset3D.Buffer dstOffsets, Operation<VkImageBlit.Buffer> original, @Local(argsOnly = true) GpuTextureView gpuTextureView) {
		if (gpuTextureView.texture() == Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture()) {
			WindowHooks window = ((WindowHooks) (Object) Minecraft.getInstance().getWindow());
			dstOffsets.position(0);
			dstOffsets.x(0).y(window.fabric_getRealFramebufferHeight()).z(0);
			dstOffsets.position(1);
			dstOffsets.x(window.fabric_getRealFramebufferWidth()).y(0).z(1);
			dstOffsets.position(0);
		}

		return original.call(blitRegion, dstOffsets);
	}
}
