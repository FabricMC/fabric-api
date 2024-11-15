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

package net.fabricmc.fabric.impl.client.itemgroup;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupImpl;

public class FabricCreativeGuiComponents {
	private static final Identifier BUTTON_TEX = Identifier.of("fabric", "textures/gui/creative_buttons.png");
	private static final double TABS_PER_PAGE = FabricItemGroupImpl.TABS_PER_PAGE;
	public static final Set<ItemGroup> COMMON_GROUPS = Set.of(ItemGroups.SEARCH, ItemGroups.INVENTORY, ItemGroups.HOTBAR, ItemGroups.OPERATOR).stream()
			.map(Registries.ITEM_GROUP::getOrThrow)
			.collect(Collectors.toSet());

	public static int getPageCount() {
		return (int) Math.ceil((ItemGroups.getGroupsToDisplay().size() - COMMON_GROUPS.stream().filter(ItemGroup::shouldDisplay).count()) / TABS_PER_PAGE);
	}

	public static class ItemGroupButtonWidget extends ButtonWidget {
		final CreativeInventoryScreen screen;
		final Type type;

		public ItemGroupButtonWidget(int x, int y, Type type, CreativeInventoryScreen screen) {
			super(x, y, 11, 12, type.text, (bw) -> type.clickConsumer.accept(screen), ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			this.type = type;
			this.screen = screen;
		}

		@Override
		protected void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
			this.active = type.isEnabled.test(screen);
			this.visible = screen.hasAdditionalPages();

			if (!this.visible) {
				return;
			}

			int u = active && this.isHovered() ? 22 : 0;
			int v = active ? 0 : 12;
			drawContext.drawTexture(BUTTON_TEX, this.getX(), this.getY(), u + (type == Type.NEXT ? 11 : 0), v, 11, 12);

			if (this.isHovered()) {
				drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("fabric.gui.creativeTabPage", screen.getCurrentPage() + 1, getPageCount()), mouseX, mouseY);
			}
		}
	}

	public enum Type {
		NEXT(Text.literal(">"), CreativeInventoryScreen::switchToNextPage, screen -> screen.getCurrentPage() + 1 < screen.getPageCount()),
		PREVIOUS(Text.literal("<"), CreativeInventoryScreen::switchToPreviousPage, screen -> screen.getCurrentPage() != 0);

		final Text text;
		final Consumer<CreativeInventoryScreen> clickConsumer;
		final Predicate<CreativeInventoryScreen> isEnabled;

		Type(Text text, Consumer<CreativeInventoryScreen> clickConsumer, Predicate<CreativeInventoryScreen> isEnabled) {
			this.text = text;
			this.clickConsumer = clickConsumer;
			this.isEnabled = isEnabled;
		}
	}
}
