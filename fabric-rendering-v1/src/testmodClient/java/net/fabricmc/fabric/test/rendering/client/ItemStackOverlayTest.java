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

package net.fabricmc.fabric.test.rendering.client;

import net.minecraft.ChatFormatting;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DrawItemStackOverlayCallback;

public class ItemStackOverlayTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DrawItemStackOverlayCallback.EVENT.register((context, textRenderer, stack, x, y) -> {
			// renders a plus sign on all shulker boxes where the stack count would usually be
			if (stack.is(ItemTags.SHULKER_BOXES)) {
				String s = "+";
				context.pose().pushMatrix();
				context.drawString(textRenderer,
						s,
						x + 19 - 2 - textRenderer.width(s),
						y + 6 + 3,
						ARGB.opaque(ChatFormatting.YELLOW.getColor()),
						true);
				context.pose().popMatrix();
			}
		});
	}
}
