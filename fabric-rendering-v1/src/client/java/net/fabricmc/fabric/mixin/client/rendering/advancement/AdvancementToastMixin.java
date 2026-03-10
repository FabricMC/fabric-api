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

package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;

@Mixin(AdvancementToast.class)
abstract class AdvancementToastMixin {
	@Shadow
	@Final
	private AdvancementHolder advancement;

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V"))
	private void extractAdvancementIcon(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
		AdvancementRenderer.IconRenderer iconRenderer = AdvancementRendererRegistryImpl.getIconRenderer(advancement.id());

		if (iconRenderer == null || iconRenderer.shouldRenderOriginalIcon()) {
			original.call(graphics, icon, x, y);
		}

		if (iconRenderer != null) {
			ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
			AdvancementProgress progress = ((ClientAdvancementsAccessor) advancements).fabric_getProgress().get(advancement);
			iconRenderer.extractAdvancementIcon(new AdvancementRenderContextImpl.IconImpl(graphics, advancement, progress, x, y, false, false));
		}
	}
}
