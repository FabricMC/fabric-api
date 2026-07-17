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

package net.fabricmc.fabric.mixin.advancement;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;

@Mixin(Advancement.Builder.class)
public interface AdvancementBuilderAccessor {
	@Accessor("parent")
	Optional<Identifier> fabric_getParent();

	@Accessor("display")
	Optional<DisplayInfo> fabric_getDisplay();

	@Accessor("rewards")
	AdvancementRewards fabric_getRewards();

	@Accessor("criteria")
	ImmutableMap.Builder<String, Criterion<?>> fabric_getCriteriaBuilder();

	@Accessor("requirements")
	Optional<AdvancementRequirements> fabric_getRequirements();

	@Accessor("sendsTelemetryEvent")
	boolean fabric_getSendsTelemetryEvent();
}
