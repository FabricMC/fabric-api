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

package net.fabricmc.fabric.mixin.gamerule.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;

// For any future maintainers who wonder why this class does not compile because of jsr305, please reload gradle using `--refresh-dependencies`.
@Mixin(EditGameRulesScreen.RuleList.class)
public abstract class EditGameRulesScreenRuleListMixin extends AbstractSelectionList<EditGameRulesScreen.RuleEntry> {
	@Unique
	private final Map<CustomGameRuleCategory, List<EditGameRulesScreen.RuleEntry>> fabricCategories = new HashMap<>();

	public EditGameRulesScreenRuleListMixin(Minecraft client, int width, int height, int top, int itemHeight) {
		super(client, width, height, top, itemHeight);
	}

	// EditGameRulesScreen is effectively a synthetic parameter
	@Inject(method = "<init>(Lnet/minecraft/client/gui/screens/worldselection/EditGameRulesScreen;Lnet/minecraft/world/level/gamerules/GameRules;)V", at = @At("TAIL"))
	private void initializeFabricGameruleCategories(EditGameRulesScreen screen, GameRules gameRules, CallbackInfo ci) {
		this.fabricCategories.forEach((category, widgetList) -> {
			this.addEntry(screen.new CategoryRuleEntry(category.getName()));

			for (EditGameRulesScreen.RuleEntry widget : widgetList) {
				this.addEntry(widget);
			}
		});
	}

	// Synthetic method
	@Inject(method = "method_27638(Ljava/util/Map$Entry;)V", at = @At("HEAD"), cancellable = true)
	private void ignoreKeysWithCustomCategories(Map.Entry<GameRule<?>, EditGameRulesScreen.RuleEntry> entry, CallbackInfo ci) {
		final GameRule<?> rule = entry.getKey();
		CustomGameRuleCategory.getCategory(rule).ifPresent(category -> {
			this.fabricCategories.computeIfAbsent(category, c -> new ArrayList<>()).add(entry.getValue());
			ci.cancel();
		});
	}
}
