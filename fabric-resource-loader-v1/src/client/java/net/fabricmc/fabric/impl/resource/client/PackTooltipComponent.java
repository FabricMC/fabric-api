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

package net.fabricmc.fabric.impl.resource.client;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PackTooltipComponent(Optional<Component> name, Optional<List<FormattedCharSequence>> description)
		implements TooltipComponent, ClientTooltipComponent {
	@Override
	public int getHeight(Font font) {
		int height = 0;

		if (this.name.isPresent()) {
			height += font.lineHeight + 2;
		}

		if (this.description.isPresent()) {
			height += this.description.get().size() * font.lineHeight + 3;
		}

		if (this.name.isPresent() && this.description.isPresent()) {
			height += font.lineHeight;
		}

		return height;
	}

	@Override
	public int getWidth(Font font) {
		return Math.max(
				this.name.map(font::width).orElse(0),
				this.description
						.map(description -> description.stream().mapToInt(font::width).max().orElse(0))
						.orElse(0)
		);
	}

	@Override
	public void renderText(GuiGraphics graphics, Font font, int x, int y) {
		if (this.name.isPresent()) {
			graphics.drawString(font, this.name.get(), x, y, 0xffffffff, true);
			y += font.lineHeight + 1;

			if (this.description.isPresent()) {
				y += font.lineHeight;
			}
		}

		if (this.description.isPresent()) {
			for (FormattedCharSequence line : this.description.get()) {
				graphics.drawString(font, line, x, y, 0xffffffff, true);
				y += font.lineHeight + 1;
			}
		}
	}

	@Override
	public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
		if (this.name.isPresent() && this.description.isPresent()) {
			graphics.fill(
					x, y + font.lineHeight + 4,
					x + this.getWidth(font), y + font.lineHeight + 5,
					0xff000000 | ChatFormatting.GRAY.getColor()
			);
		}
	}
}
