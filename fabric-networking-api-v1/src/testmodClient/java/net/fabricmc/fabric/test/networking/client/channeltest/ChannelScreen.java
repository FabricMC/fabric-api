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

package net.fabricmc.fabric.test.networking.client.channeltest;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

final class ChannelScreen extends Screen {
	private final NetworkingChannelClientTest mod;
	private Button s2cButton;
	private Button c2sButton;
	private Button closeButton;
	private ChannelList channelList;

	ChannelScreen(NetworkingChannelClientTest mod) {
		super(Component.literal("TODO"));
		this.mod = mod;
	}

	@Override
	protected void init() {
		this.s2cButton = this.addRenderableWidget(Button.builder(Component.literal("S2C"), this::toS2C)
				.pos(this.width / 2 - 55, 5)
				.size(50, 20)
				.tooltip(Tooltip.create(Component.literal("Packets this client can receive")))
				.build());
		this.c2sButton = this.addRenderableWidget(Button.builder(Component.literal("C2S"), this::toC2S)
				.pos(this.width / 2 + 5, 5)
				.size(50, 20)
				.tooltip(Tooltip.create(Component.literal("Packets the server can receive")))
				.build());
		this.closeButton = this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
				.pos(this.width / 2 - 60, this.height - 25)
				.size(120, 20)
				.build());
		this.channelList = this.addRenderableOnly(new ChannelList(this.minecraft, this.width, this.height - 60, 30, this.font.lineHeight + 2));
	}

	@Override
	public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
		this.renderBackground(drawContext, mouseX, mouseY, delta);
		this.channelList.render(drawContext, mouseX, mouseY, delta);
		super.render(drawContext, mouseX, mouseY, delta);

		if (this.s2cButton.active && this.c2sButton.active) {
			final Component clickMe = Component.literal("Click S2C or C2S to view supported channels").withStyle(ChatFormatting.YELLOW);

			final int textWidth = this.font.width(clickMe);
			drawContext.setTooltipForNextFrame(
					this.font,
					clickMe,
					(int) (this.width / 2.0F - (textWidth / 2.0F)),
					60
			);
		}
	}

	void refresh() {
		if (!this.c2sButton.active && this.s2cButton.active) {
			this.toC2S(this.c2sButton);
		}
	}

	private void toC2S(Button button) {
		this.s2cButton.active = true;
		button.active = false;
		this.channelList.clear();

		for (Identifier receiver : ClientPlayNetworking.getSendable()) {
			this.channelList.addEntry(this.channelList.new Entry(receiver));
		}
	}

	private void toS2C(Button button) {
		this.c2sButton.active = true;
		button.active = false;
		this.channelList.clear();

		for (Identifier receiver : ClientPlayNetworking.getReceived()) {
			this.channelList.addEntry(this.channelList.new Entry(receiver));
		}
	}
}
