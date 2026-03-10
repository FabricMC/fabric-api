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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@ApiStatus.NonExtendable
public sealed interface AdvancementRenderContext permits AdvancementRenderContext.Icon, AdvancementRenderContext.Frame, AdvancementRenderContext.Background {
	/**
	 * The graphics instance used for rendering.
	 * @return {@link GuiGraphicsExtractor} instance
	 */
	GuiGraphicsExtractor graphics();

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

	/**
	 * The advancement being rendered.
	 * @return {@link Advancement} instance
	 */
	default Advancement advancement() {
		return holder().value();
	}

	/**
	 * The display info of the advancement.
	 * @return {@link DisplayInfo} instance
	 */
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

	@ApiStatus.NonExtendable
	non-sealed interface Icon extends AdvancementRenderContext {
		/**
		 * @return The x coordinate of the icon's top-left corner.
		 */
		int x();

		/**
		 * @return The y coordinate of the icon's top-left corner.
		 */
		int y();

		/**
		 * @return {@code true} if the mouse is hovered over the icon.
		 */
		boolean isHovered();

		/**
		 * @return {@code true} if the icon is rendered as a selected tab.
		 */
		boolean isSelected();
	}

	@ApiStatus.NonExtendable
	non-sealed interface Frame extends AdvancementRenderContext {
		/**
		 * @return The x coordinate of the frame's top-left corner.
		 */
		int x();

		/**
		 * @return The y coordinate of the frame's top-left corner.
		 */
		int y();

		/**
		 * @return {@code true} if the mouse is hovered over the frame.
		 */
		boolean isHovered();
	}

	@ApiStatus.NonExtendable
	non-sealed interface Background extends AdvancementRenderContext {
		/**
		 * @return the {@link ScreenRectangle} that the background is contained within.
		 * @apiNote use {@link ScreenRectangle#left()} and {@link ScreenRectangle#top()} for the starting coordinates of the background.
		 */
		ScreenRectangle bounds();

		/**
		 * @return the background's x scroll offset.
		 */
		double scrollX();

		/**
		 * @return the background's y scroll offset.
		 */
		double scrollY();
	}
}
