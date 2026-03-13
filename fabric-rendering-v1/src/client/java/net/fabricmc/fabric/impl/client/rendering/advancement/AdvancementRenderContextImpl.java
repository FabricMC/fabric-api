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

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderContext;

public final class AdvancementRenderContextImpl {
	public static final class IconImpl implements AdvancementRenderContext.Icon {
		private final GuiGraphicsExtractor graphics;
		private final AdvancementHolder holder;
		@Nullable
		private final AdvancementProgress progress;
		private int x;
		private int y;
		private final boolean hovered;
		private final boolean selected;

		public IconImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, int x, int y, boolean hovered, boolean selected) {
			this.graphics = graphics;
			this.holder = holder;
			this.progress = progress;
			this.x = x;
			this.y = y;
			this.hovered = hovered;
			this.selected = selected;
		}

		public IconImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, boolean hovered, boolean selected) {
			this(graphics, holder, progress, 0, 0, hovered, selected);
		}

		@Override
		public GuiGraphicsExtractor graphics() {
			return graphics;
		}

		@Override
		public AdvancementHolder holder() {
			return holder;
		}

		@Override
		public @Nullable AdvancementProgress progress() {
			return progress;
		}

		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}

		@Override
		public boolean isHovered() {
			return hovered;
		}

		@Override
		public boolean isSelected() {
			return selected;
		}

		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public record FrameImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, int x, int y, boolean isHovered) implements AdvancementRenderContext.Frame {
	}

	public record BackgroundImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, ScreenRectangle bounds, double scrollX, double scrollY) implements AdvancementRenderContext.Background {
	}

	private AdvancementRenderContextImpl() {
	}
}
