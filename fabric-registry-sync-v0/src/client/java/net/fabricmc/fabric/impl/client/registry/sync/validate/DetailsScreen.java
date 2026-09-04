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

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.registry.sync.validate.Details;

public class DetailsScreen extends Screen {
	private static final Identifier SCREEN_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

	private final Details details;
	private final Screen lastScreen;
	private HeaderAndFooterLayout layout;

	protected DetailsScreen(Component title, Supplier<Details> detailsSupplier, Screen lastScreen) {
		super(title);
		this.details = detailsSupplier.get();
		this.lastScreen = lastScreen;
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		this.rebuildWidgets();
	}

	protected void init() {
		this.layout = new HeaderAndFooterLayout(this);
		this.addTitle();
		this.addContents();
		this.addFooter();
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	protected void addTitle() {
		this.layout.addTitleHeader(this.title, this.font);
	}

	protected void addContents() {
		LinearLayout body = LinearLayout.vertical().spacing(4);

		for (Details.Section section : this.details.sections()) {
			body.addChild(FocusableTextWidget.builder(section.header(), font, 4).maxWidth(this.width - 40).alwaysShowBorder(false)
					.backgroundFill(FocusableTextWidget.BackgroundFill.NEVER).build().setCentered(true));

			for (Component line : section.lines()) {
				body.addChild(FocusableTextWidget.builder(line, font, 2).maxWidth(this.width - 40).alwaysShowBorder(false)
						.backgroundFill(FocusableTextWidget.BackgroundFill.NEVER).build().setCentered(false));
			}
		}

		var scrollable = new OptimizedScrollableLayout(minecraft, body, this.layout.getContentHeight());
		body.arrangeElements();
		this.layout.addToContents(scrollable);
	}

	protected void addFooter() {
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (_) -> this.onClose()).width(200).build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);

		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				SCREEN_BACKGROUND,
				0,
				this.layout.getHeaderHeight(),
				this.width, 0,
				width,
				this.layout.getContentHeight(),
				32,
				32
		);

		graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, width, 2, 32, 2);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.layout.getHeaderHeight() + this.layout.getContentHeight(), 0.0F, 0.0F, width, 2, 32, 2);
	}

	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
