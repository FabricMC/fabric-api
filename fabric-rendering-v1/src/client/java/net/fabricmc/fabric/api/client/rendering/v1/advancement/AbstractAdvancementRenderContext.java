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

package net.fabricmc.fabric.api.client.rendering.v1.advancement;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;

@ApiStatus.NonExtendable
public interface AbstractAdvancementRenderContext {
	/**
	 * The graphics instance used for rendering.
	 * @return {@link GuiGraphics} instance
	 */
	GuiGraphics graphics();

	/**
	 * The holder for the advancement.
	 * @return {@link AdvancementHolder} instance
	 */
	AdvancementHolder holder();

	/**
	 * @return The advancement's progress, or {@code null} if there is no progress.
	 */
	@Nullable
	AdvancementProgress progress();

	default Advancement advancement() {
		return holder().value();
	}

	default DisplayInfo display() {
		return advancement().display().orElseThrow();
	}

	/**
	 * @return {@code true} if the advancement has been obtained.
	 */
	default boolean isObtained() {
		AdvancementProgress progress = progress();
		return progress != null && progress.getPercent() >= 1;
	}
}
