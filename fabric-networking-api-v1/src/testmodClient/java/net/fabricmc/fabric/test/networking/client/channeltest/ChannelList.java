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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class ChannelList extends AbstractSelectionList<ChannelList.Entry> {
	ChannelList(Minecraft client, int width, int height, int top, int itemHeight) {
		super(client, width, height, top, itemHeight);
	}

	@Override
	public int addEntry(Entry entry) {
		return super.addEntry(entry);
	}

	void clear() {
		this.clearEntries();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput arg) {
		// TODO seems to be possibly accessibility related
	}

	class Entry extends AbstractSelectionList.Entry<Entry> {
		private final Identifier channel;

		Entry(Identifier channel) {
			this.channel = channel;
		}

		@Override
		public void renderContent(GuiGraphics drawContext, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawContext.setTooltipForNextFrame(ChannelList.this.minecraft.font, Component.literal(this.channel.toString()).withStyle(ChatFormatting.WHITE), getContentX(), getContentY());
		}
	}
}
