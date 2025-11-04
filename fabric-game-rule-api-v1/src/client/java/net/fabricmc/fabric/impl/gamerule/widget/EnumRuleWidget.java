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

package net.fabricmc.fabric.impl.gamerule.widget;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.mixin.gamerule.client.EditGameRulesScreenAccessor;

public final class EnumRuleWidget<E extends Enum<E>> extends EditGameRulesScreen.GameRuleEntry {
	private final Button buttonWidget;
	private final String rootTranslationKey;

	public EnumRuleWidget(EditGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, final String ruleName, GameRule<E> enumRule, String translationKey) {
		gameRuleScreen.super(description, name);
		EditGameRulesScreenAccessor accessor = (EditGameRulesScreenAccessor) gameRuleScreen;

		// Overwrite line wrapping to account for button larger than vanilla's by 44 pixels.
		this.label = Minecraft.getInstance().font.split(name, 175 - 44);

		// Base translation key needs to be set before the button widget is created.
		this.rootTranslationKey = translationKey;
		this.buttonWidget = Button.builder(this.getValueText(accessor.getGameRules().get(enumRule)), (buttonWidget) -> {
			accessor.getGameRules().set(enumRule,
					((RuleTypeExtensions) (Object) enumRule).fabric_enumCycle(
							accessor.getGameRules().get(enumRule)
					),
					null);
			buttonWidget.setMessage(this.getValueText(accessor.getGameRules().get(enumRule)));
		}).bounds(10, 5, 42, 20).build();

		this.children.add(this.buttonWidget);
	}

	public Component getValueText(E value) {
		final String key = this.rootTranslationKey + "." + value.name().toLowerCase(Locale.ROOT);
		return Component.translatableWithFallback(key, value.toString());
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		this.renderLabel(context, this.getContentY(), this.getContentX());

		this.buttonWidget.setX(this.getContentRight() - 44);
		this.buttonWidget.setY(this.getContentY());
		this.buttonWidget.render(context, mouseX, mouseY, tickDelta);
	}
}
