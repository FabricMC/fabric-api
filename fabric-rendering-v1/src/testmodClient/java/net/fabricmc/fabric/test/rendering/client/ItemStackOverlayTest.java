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

import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DrawItemStackOverlayCallback;

public class ItemStackOverlayTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DrawItemStackOverlayCallback.EVENT.register((context, textRenderer, stack, x, y) -> {
			// renders a plus sign on all shulker boxes where the stack count would usually be
			if (stack.isIn(ItemTags.SHULKER_BOXES)) {
				String s = "+";
				context.getMatrices().pushMatrix();
				context.drawText(textRenderer,
						s,
						x + 19 - 2 - textRenderer.getWidth(s),
						y + 6 + 3,
						ColorHelper.fullAlpha(Formatting.YELLOW.getColorValue()),
						true);
				context.getMatrices().popMatrix();
			}
		});
	}
}
