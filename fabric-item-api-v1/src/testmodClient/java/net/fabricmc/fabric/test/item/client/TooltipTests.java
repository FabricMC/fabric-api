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

package net.fabricmc.fabric.test.item.client;

import java.util.Optional;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class TooltipTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Adds a tooltip to all items with the name of the mod they come from.
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			String modName = stack.getCreatorNamespace();
			Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modName);

			if (modContainer.isPresent()) {
				modName = modContainer.get().getMetadata().getName();
			}

			lines.add(Text.literal(modName).formatted(Formatting.BLUE, Formatting.ITALIC));
		});
	}
}
