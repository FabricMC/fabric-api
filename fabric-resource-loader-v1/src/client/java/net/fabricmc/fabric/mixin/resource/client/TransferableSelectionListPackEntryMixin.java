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

package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import net.fabricmc.fabric.impl.resource.client.PackTooltipComponent;

@Mixin(TransferableSelectionList.PackEntry.class)
class TransferableSelectionListPackEntryMixin {
	@Shadow
	@Final
	private static int MAX_DESCRIPTION_WIDTH_PIXELS;

	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	@Final
	private StringWidget nameWidget;

	@Shadow
	@Final
	private MultiLineTextWidget descriptionWidget;

	@Shadow
	@Final
	TransferableSelectionList field_62184;

	@Inject(method = "renderContent", at = @At("RETURN"))
	private void onRenderContent(
			GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci
	) {
		if (hovered) {
			Component name = null;
			boolean includeDescription = false;

			if (this.nameWidget.getWidth() < this.minecraft.font.width(this.nameWidget.getMessage().getVisualOrderText())) {
				name = this.nameWidget.getMessage();
			}

			List<FormattedCharSequence> splitDescription = this.minecraft.font.split(
					this.descriptionWidget.getMessage(),
					MAX_DESCRIPTION_WIDTH_PIXELS - (this.field_62184.maxScrollAmount() > 0 ? 6 : 0)
			);

			if (splitDescription.size() > 2) {
				includeDescription = true;
			}

			if (name != null || includeDescription) {
				graphics.setTooltipForNextFrame(
						this.minecraft.font,
						List.of(),
						Optional.of(new PackTooltipComponent(
								Optional.ofNullable(name),
								includeDescription ? Optional.of(splitDescription) : Optional.empty()
						)),
						mouseX, mouseY
				);
			}
		}
	}
}
