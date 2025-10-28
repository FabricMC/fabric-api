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

package net.fabricmc.fabric.mixin.gamerule;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.impl.gamerule.RuleKeyExtensions;

@Mixin(GameRules.Key.class)
public abstract class GameRulesKeyMixin implements RuleKeyExtensions {
	@Unique
	@Nullable
	private CustomGameRuleCategory customCategory;

	@Override
	public CustomGameRuleCategory fabric_getCustomCategory() {
		return this.customCategory;
	}

	@Override
	public void fabric_setCustomCategory(CustomGameRuleCategory customCategory) {
		this.customCategory = customCategory;
	}
}
