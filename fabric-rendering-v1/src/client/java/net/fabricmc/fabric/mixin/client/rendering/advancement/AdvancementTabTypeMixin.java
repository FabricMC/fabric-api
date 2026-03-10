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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;

@Mixin(targets = "net/minecraft/client/gui/screens/advancements/AdvancementTabType")
abstract class AdvancementTabTypeMixin {
	@WrapOperation(method = "extractIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V"))
	private void extractAdvancementIcon(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
		if (AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.isBound()) {
			final AdvancementRenderContextImpl.IconImpl context = AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.get();
			context.setPos(x, y);
			AdvancementRenderer.IconRenderer iconRenderer = AdvancementRendererRegistryImpl.getIconRenderer(context.holder().id());

			if (iconRenderer.shouldRenderOriginalIcon()) {
				original.call(graphics, icon, x, y);
			}

			iconRenderer.extractAdvancementIcon(context);
		} else {
			original.call(graphics, icon, x, y);
		}
	}
}
