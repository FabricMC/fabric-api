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
import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;

@Mixin(AdvancementsScreen.class)
abstract class AdvancementsScreenMixin {
	@Shadow
	private @Nullable AdvancementTab selectedTab;

	@WrapOperation(method = "extractWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;extractIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
	private void wrapDrawIcon(AdvancementTab tab, GuiGraphicsExtractor graphics, int xo, int yo, Operation<Void> original, @Local(name = "mouseX") int mouseX, @Local(name = "mouseY") int mouseY) {
		AdvancementHolder holder = tab.getRootNode().holder();

		if (AdvancementRendererRegistryImpl.getIconRenderer(holder.id()) != null) {
			boolean hovered = tab.isMouseOver(xo, yo, mouseX, mouseY);
			boolean selected = selectedTab == tab;
			AdvancementProgress progress = ((AdvancementWidgetAccessor) ((AdvancementTabAccessor) tab).fabric_getRoot()).fabric_getProgress();
			ScopedValue.where(
					AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT,
					new AdvancementRenderContextImpl.IconImpl(graphics, holder, progress, hovered, selected)
			).call(() -> original.call(tab, graphics, xo, yo));
		} else {
			original.call(tab, graphics, xo, yo);
		}
	}
}
