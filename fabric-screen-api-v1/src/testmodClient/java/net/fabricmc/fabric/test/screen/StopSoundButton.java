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

package net.fabricmc.fabric.test.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

class StopSoundButton extends AbstractButton {
	StopSoundButton(int x, int y, int width, int height) {
		super(x, y, width, height, Component.nullToEmpty(""));
	}

	@Override
	protected void renderContents(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, ScreenTests.ARMOR_FULL_TEXTURE, this.getX(), this.getY(), this.width, this.height);

		if (this.isHovered()) {
			drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.literal("Click to stop all sounds"), this.getX(), this.getY());
		}
	}

	@Override
	public void onPress(InputWithModifiers ctx) {
		Minecraft.getInstance().getSoundManager().stop();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationMessageBuilder) {
	}
}
