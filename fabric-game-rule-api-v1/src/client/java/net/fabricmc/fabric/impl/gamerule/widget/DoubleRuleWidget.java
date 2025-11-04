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

import com.mojang.serialization.DataResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.mixin.gamerule.client.EditGameRulesScreenAccessor;

public final class DoubleRuleWidget extends EditGameRulesScreen.GameRuleEntry {
	private final EditBox textFieldWidget;

	public DoubleRuleWidget(EditGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, final String ruleName, final GameRule<Double> doubleRule) {
		gameRuleScreen.super(description, name);
		EditGameRulesScreenAccessor accessor = (EditGameRulesScreenAccessor) gameRuleScreen;

		this.textFieldWidget = new EditBox(Minecraft.getInstance().font, 10, 5, 42, 20,
				name.copy()
				.append(CommonComponents.NEW_LINE)
				.append(ruleName)
				.append(CommonComponents.NEW_LINE)
		);

		this.textFieldWidget.setValue(accessor.getGameRules().getAsString(doubleRule));
		this.textFieldWidget.setResponder(value -> {
			DataResult<Double> dataResult = doubleRule.deserialize(value);

			if (dataResult.isSuccess()) {
				this.textFieldWidget.setTextColor(0xFFE0E0E0);
				accessor.callMarkValid(this);
				accessor.getGameRules().set(doubleRule, dataResult.getOrThrow(), null);
			} else {
				this.textFieldWidget.setTextColor(0xFFFF0000);
				accessor.callMarkInvalid(this);
			}
		});

		this.children.add(this.textFieldWidget);
	}

	@Override
	public void renderContent(GuiGraphics drawContext, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		this.renderLabel(drawContext, this.getContentY(), this.getContentX());

		this.textFieldWidget.setX(this.getContentRight() - 44);
		this.textFieldWidget.setY(this.getContentY());
		this.textFieldWidget.render(drawContext, mouseX, mouseY, tickDelta);
	}
}
