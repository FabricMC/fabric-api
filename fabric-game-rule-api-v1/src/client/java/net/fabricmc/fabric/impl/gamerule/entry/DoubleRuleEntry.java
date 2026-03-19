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

package net.fabricmc.fabric.impl.gamerule.entry;

import java.util.List;

import com.mojang.serialization.DataResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.mixin.gamerule.client.AbstractGameRulesScreenAccessor;

public final class DoubleRuleEntry extends AbstractGameRulesScreen.GameRuleEntry {
	private final EditBox input;

	public DoubleRuleEntry(AbstractGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, final String ruleName, final GameRule<Double> doubleRule) {
		gameRuleScreen.super(description, name);
		AbstractGameRulesScreenAccessor accessor = (AbstractGameRulesScreenAccessor) gameRuleScreen;

		this.input = new EditBox(Minecraft.getInstance().font, 10, 5, 42, 20,
				name.copy()
				.append(CommonComponents.NEW_LINE)
				.append(ruleName)
				.append(CommonComponents.NEW_LINE)
		);

		this.input.setValue(accessor.getGameRules().getAsString(doubleRule));
		this.input.setResponder(value -> {
			DataResult<Double> dataResult = doubleRule.deserialize(value);

			if (dataResult.isSuccess()) {
				this.input.setTextColor(0xFFE0E0E0);
				accessor.callClearInvalid(this);
				accessor.getGameRules().set(doubleRule, dataResult.getOrThrow(), null);
			} else {
				this.input.setTextColor(0xFFFF0000);
				accessor.callMarkInvalid(this);
			}
		});

		this.children.add(this.input);
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		this.extractLabel(graphics, this.getContentY(), this.getContentX());

		this.input.setX(this.getContentRight() - 44);
		this.input.setY(this.getContentY());
		this.input.extractRenderState(graphics, mouseX, mouseY, tickDelta);
	}
}
