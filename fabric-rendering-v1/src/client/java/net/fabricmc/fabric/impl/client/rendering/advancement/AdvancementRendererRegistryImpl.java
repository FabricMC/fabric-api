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

package net.fabricmc.fabric.impl.client.rendering.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;

public final class AdvancementRendererRegistryImpl {
	public static final ScopedValue<AdvancementRenderContextImpl.IconImpl> TAB_ICON_RENDER_CONTEXT = ScopedValue.newInstance();
	private static final Map<Identifier, AdvancementRenderer.IconRenderer> ICONS = new HashMap<>();
	private static final Map<Identifier, AdvancementRenderer.FrameRenderer> FRAMES = new HashMap<>();
	private static final Map<Identifier, AdvancementRenderer.BackgroundRenderer> BACKGROUNDS = new HashMap<>();

	public static void registerIcon(AdvancementRenderer.IconRenderer iconRenderer, Identifier... advancementIds) {
		registerRenderer("Icon", ICONS, iconRenderer, advancementIds);
	}

	public static void registerFrame(AdvancementRenderer.FrameRenderer frameRenderer, Identifier... advancementIds) {
		registerRenderer("Frame", FRAMES, frameRenderer, advancementIds);
	}

	public static void registerBackground(AdvancementRenderer.BackgroundRenderer backgroundRenderer, Identifier... advancementIds) {
		registerRenderer("Background", BACKGROUNDS, backgroundRenderer, advancementIds);
	}

	public static AdvancementRenderer.@Nullable IconRenderer getIconRenderer(Identifier advancementId) {
		return ICONS.get(advancementId);
	}

	public static AdvancementRenderer.@Nullable FrameRenderer getFrameRenderer(Identifier advancementId) {
		return FRAMES.get(advancementId);
	}

	public static AdvancementRenderer.@Nullable BackgroundRenderer getBackgroundRenderer(Identifier advancementId) {
		return BACKGROUNDS.get(advancementId);
	}

	private static <T> void registerRenderer(String type, Map<Identifier, T> renderers, T renderer, Identifier... advancementIds) {
		Objects.requireNonNull(renderer, type + " renderer is null");

		if (advancementIds.length == 0) {
			throw new IllegalArgumentException(type + " advancement renderer registered for no advancements");
		}

		for (Identifier advancementId : advancementIds) {
			Objects.requireNonNull(advancementId, " advancement id is null");

			if (renderers.putIfAbsent(advancementId, renderer) != null) {
				throw new IllegalArgumentException(type + " advancement renderer already exists for " + advancementId);
			}
		}
	}

	private AdvancementRendererRegistryImpl() {
	}
}
