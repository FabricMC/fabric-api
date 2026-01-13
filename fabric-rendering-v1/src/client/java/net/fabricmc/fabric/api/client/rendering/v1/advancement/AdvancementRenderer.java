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

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;

/**
 * Advancement renderers allow for custom advancement icons, frames, and backgrounds
 * which render in the {@link net.minecraft.client.gui.screens.advancements.AdvancementsScreen advancements screen}
 * and {@link net.minecraft.client.gui.components.toasts.AdvancementToast advancement toasts}.
 */
public final class AdvancementRenderer {
	/**
	 * Registers an {@link IconRenderer} for advancement icons that show on advancement widgets, tabs, and toasts.
	 * @param iconRenderer the icon renderer
	 * @param advancementIds identifiers of the advancements
	 * @throws IllegalArgumentException if an advancement already has a registered icon renderer
	 * @throws NullPointerException if either an advancement id or the icon renderer is null
	 */
	public static void registerIcon(IconRenderer iconRenderer, Identifier... advancementIds) {
		AdvancementRendererRegistryImpl.registerIcon(iconRenderer, advancementIds);
	}

	/**
	 * Registers a {@link FrameRenderer} for advancement frames that show on advancement widgets.
	 * @param frameRenderer the frame renderer
	 * @param advancementIds identifiers of the advancements
	 * @throws IllegalArgumentException if an advancement already has a registered frame renderer
	 * @throws NullPointerException if either an advancement id or the frame renderer is null
	 */
	public static void registerFrame(FrameRenderer frameRenderer, Identifier... advancementIds) {
		AdvancementRendererRegistryImpl.registerFrame(frameRenderer, advancementIds);
	}

	/**
	 * Registers a {@link BackgroundRenderer} for the backgrounds of advancement tabs.
	 *
	 * <p>Only root advancements render their backgrounds.
	 * @param backgroundRenderer the frame renderer
	 * @param advancementIds identifiers of the advancements
	 * @throws IllegalArgumentException if an advancement already has a registered background renderer
	 * @throws NullPointerException if either an advancement id or the background renderer is null
	 */
	public static void registerBackground(BackgroundRenderer backgroundRenderer, Identifier... advancementIds) {
		AdvancementRendererRegistryImpl.registerBackground(backgroundRenderer, advancementIds);
	}

	@FunctionalInterface
	public interface IconRenderer {
		void renderAdvancementIcon(AdvancementRenderContext.Icon context);

		/**
		 * @return {@code true} if the original advancement icon should render alongside this icon renderer.
		 */
		default boolean shouldRenderOriginalIcon() {
			return false;
		}
	}

	@FunctionalInterface
	public interface FrameRenderer {
		void renderAdvancementFrame(AdvancementRenderContext.Frame context);

		/**
		 * @return {@code true} if the original advancement frame should render alongside this frame renderer.
		 */
		default boolean shouldRenderOriginalFrame() {
			return false;
		}

		/**
		 * @return {@code true} if the tooltip of a hovered advancement widget should render.
		 */
		default boolean shouldRenderTooltip() {
			return true;
		}
	}

	@FunctionalInterface
	public interface BackgroundRenderer {
		void renderAdvancementBackground(AdvancementRenderContext.Background context);

		/**
		 * @return {@code true} if the original advancement background should render alongside this background renderer.
		 */
		default boolean shouldRenderOriginalBackground() {
			return false;
		}
	}

	private AdvancementRenderer() {
	}
}
