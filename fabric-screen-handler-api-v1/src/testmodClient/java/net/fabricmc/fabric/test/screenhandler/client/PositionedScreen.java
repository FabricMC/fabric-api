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

package net.fabricmc.fabric.test.screenhandler.client;

import java.util.Optional;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.test.screenhandler.screen.PositionedScreenHandler;

public class PositionedScreen extends AbstractContainerScreen<AbstractContainerMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

	public PositionedScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, getPositionText(handler).orElse(title));
	}

	private static Optional<Component> getPositionText(AbstractContainerMenu handler) {
		if (handler instanceof PositionedScreenHandler) {
			BlockPos pos = ((PositionedScreenHandler) handler).getPos();
			return pos != null ? Optional.of(Component.literal("(" + pos.toShortString() + ")")) : Optional.empty();
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		renderBackground(drawContext, mouseX, mouseY, delta);
		super.render(drawContext, mouseX, mouseY, delta);
		renderTooltip(drawContext, mouseX, mouseY);
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleLabelX = (imageWidth - font.width(title)) / 2;
	}

	@Override
	protected void renderBg(GuiGraphics drawContext, float delta, int mouseX, int mouseY) {
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		drawContext.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
	}
}
