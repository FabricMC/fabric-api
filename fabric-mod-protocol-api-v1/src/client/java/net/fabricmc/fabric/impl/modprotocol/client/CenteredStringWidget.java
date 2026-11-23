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

package net.fabricmc.fabric.impl.modprotocol.client;

import java.util.Objects;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class CenteredStringWidget extends StringWidget {
	private int maxWidth;
	private TextOverflow textOverflow = TextOverflow.SCROLLING;

	public CenteredStringWidget(int width, int height, Component message, Font font) {
		super(width, height, message, font);
	}

	@Override
	public StringWidget setMaxWidth(int maxWidth, TextOverflow textOverflow) {
		this.maxWidth = maxWidth;
		this.textOverflow = textOverflow;
		return super.setMaxWidth(maxWidth, textOverflow);
	}

	@Override
	public void visitLines(final ActiveTextCollector output) {
		Component message = this.getMessage();
		Font font = Objects.requireNonNull(this.getFont());

		int maxWidth = this.maxWidth > 0 ? this.maxWidth : this.getWidth();
		int textWidth = font.width(message);
		boolean textOverflow = textWidth > maxWidth;

		int x = this.getX();
		int y = this.getY() + (this.getHeight() - 9) / 2;

		if (textOverflow) {
			switch (this.textOverflow) {
				case TextOverflow.CLAMPED -> output.accept(x, y, ComponentRenderUtils.clipText(message, font, maxWidth));
				case TextOverflow.SCROLLING -> this.extractScrollingStringOverContents(output, message, 2);
			}
		} else {
			output.accept(x + width / 2 - textWidth / 2, y, message.getVisualOrderText());
		}
	}
}
