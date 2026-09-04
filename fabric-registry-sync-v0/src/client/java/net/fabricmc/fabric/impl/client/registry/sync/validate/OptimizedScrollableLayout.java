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

package net.fabricmc.fabric.impl.client.registry.sync.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;

/// Based on {@link net.minecraft.client.gui.components.ScrollableLayout}, but optimized to render faster
public class OptimizedScrollableLayout implements Layout {
	private static final int DEFAULT_SCROLLBAR_SPACING = 4;
	private final Layout content;
	private final Container container;
	private final int scrollbarSpacing;
	private final int maxHeight;

	public OptimizedScrollableLayout(final Minecraft minecraft, final Layout content, final int maxHeight) {
		this.scrollbarSpacing = DEFAULT_SCROLLBAR_SPACING;
		this.content = content;
		this.maxHeight = maxHeight;
		this.container = new Container(minecraft, 0, maxHeight, AbstractScrollArea.defaultSettings(10));
	}

	@Override
	public void arrangeElements() {
		this.content.arrangeElements();

		this.container.setWidth(this.content.getWidth() + this.container.scrollbarReserve());
		this.container.setHeight(Math.clamp(this.container.getHeight(), 0, this.maxHeight));
		this.container.refreshChildren();
		this.container.refreshScrollAmount();
	}

	@Override
	public void visitChildren(final Consumer<LayoutElement> layoutElementVisitor) {
		layoutElementVisitor.accept(this.container);
	}

	@Override
	public void removeChildren() {
		this.container.children().clear();
		this.content.removeChildren();
	}

	@Override
	public void setX(final int x) {
		this.container.setX(x);
	}

	@Override
	public void setY(final int y) {
		this.container.setY(y);
	}

	@Override
	public int getX() {
		return this.container.getX();
	}

	@Override
	public int getY() {
		return this.container.getY();
	}

	@Override
	public int getWidth() {
		return this.container.getWidth();
	}

	@Override
	public int getHeight() {
		return this.container.getHeight();
	}

	private class Container extends AbstractContainerWidget {
		private final Minecraft minecraft;
		private final List<AbstractWidget> children;

		Container(final Minecraft minecraft, final int width, final int height, final ScrollbarSettings scrollbarSettings) {
			super(0, 0, width, height, CommonComponents.EMPTY, scrollbarSettings);
			this.children = new ArrayList<>();
			this.minecraft = minecraft;
			this.refreshChildren();
		}

		public void refreshChildren() {
			this.children.clear();
			OptimizedScrollableLayout.this.content.visitWidgets(this.children::add);
		}

		@Override
		protected int contentHeight() {
			return OptimizedScrollableLayout.this.content.getHeight();
		}

		@Override
		protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
			graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);

			for (AbstractWidget child : this.children) {
				if (child.getY() + child.getHeight() < this.getY() || child.getY() > this.getY() + this.getHeight()) {
					continue;
				}

				child.extractRenderState(graphics, mouseX, mouseY, a);
			}

			graphics.disableScissor();
			this.extractScrollbar(graphics, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(final NarrationElementOutput output) {
		}

		@Override
		public ScreenRectangle getBorderForArrowNavigation(final ScreenDirection opposite) {
			GuiEventListener focused = this.getFocused();
			return focused != null ? focused.getBorderForArrowNavigation(opposite) : (new ScreenRectangle(this.getX(), this.getY(), this.width, this.contentHeight())).getBorder(opposite);
		}

		@Override
		public void setFocused(final @Nullable GuiEventListener focused) {
			super.setFocused(focused);

			if (focused != null && this.minecraft.getLastInputType().isKeyboard()) {
				ScreenRectangle area = this.getRectangle();
				ScreenRectangle focusedRect = focused.getRectangle();
				int topDelta = focusedRect.top() - area.top();
				int bottomDelta = focusedRect.bottom() - area.bottom();
				double scrollRate = this.scrollRate();

				if (topDelta < 0) {
					this.setScrollAmount(this.scrollAmount() + (double) topDelta - scrollRate);
				} else if (bottomDelta > 0) {
					this.setScrollAmount(this.scrollAmount() + (double) bottomDelta + scrollRate);
				}
			}
		}

		@Override
		public void setX(final int x) {
			super.setX(x);
			OptimizedScrollableLayout.this.content.setX(x);
		}

		@Override
		public void setY(final int y) {
			super.setY(y);
			OptimizedScrollableLayout.this.content.setY(y - (int) this.scrollAmount());
		}

		private int scrollbarReserve() {
			return OptimizedScrollableLayout.this.scrollbarSpacing + this.scrollbarWidth();
		}

		@Override
		public void setScrollAmount(final double scrollAmount) {
			super.setScrollAmount(scrollAmount);
			OptimizedScrollableLayout.this.content.setY(this.getRectangle().top() - (int) this.scrollAmount());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}

		@Override
		public Collection<? extends NarratableEntry> getNarratables() {
			return this.children;
		}
	}
}
