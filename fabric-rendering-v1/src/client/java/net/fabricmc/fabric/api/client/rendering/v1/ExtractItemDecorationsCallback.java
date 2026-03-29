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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ExtractItemDecorationsCallback {
	/**
	 * Fires at the end of {@link GuiGraphicsExtractor#itemDecorations(Font, ItemStack, int, int, String)} and allows
	 * for drawing custom item stack decorations.
	 *
	 * <p>In vanilla these are: durability bar, cooldown overlay and stack count.
	 */
	Event<ExtractItemDecorationsCallback> EVENT = EventFactory.createArrayBacked(
			ExtractItemDecorationsCallback.class,
			callbacks -> (graphics, font, stack, x, y) -> {
				for (ExtractItemDecorationsCallback callback : callbacks) {
					callback.onExtractItemDecorations(graphics, font, stack, x, y);
				}
			});

	/**
	 * @param graphics     the {@link GuiGraphicsExtractor} instance
	 * @param font         the font
	 * @param stack        the item stack
	 * @param x            the x-position of the item stack
	 * @param y            the y-position of the item stack
	 */
	void onExtractItemDecorations(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y);
}
