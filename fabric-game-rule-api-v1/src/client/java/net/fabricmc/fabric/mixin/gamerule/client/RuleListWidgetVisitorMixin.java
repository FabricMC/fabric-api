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

package net.fabricmc.fabric.mixin.gamerule.client;

import java.util.Locale;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleVisitor;

import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleVisitor;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.widget.DoubleRuleWidget;
import net.fabricmc.fabric.impl.gamerule.widget.EnumRuleWidget;

@Mixin(targets = "net/minecraft/client/gui/screen/world/EditGameRulesScreen$RuleListWidget$1")
public abstract class RuleListWidgetVisitorMixin implements GameRuleVisitor, FabricGameRuleVisitor {
	@Final
	@Shadow
	private EditGameRulesScreen field_24314;
	@Shadow
	protected abstract <T> void createRuleWidget(GameRule<T> key, EditGameRulesScreen.RuleWidgetFactory<T> widgetFactory);

	@Override
	public void visitDouble(GameRule<Double> doubleRule) {
		this.createRuleWidget(doubleRule, (name, description, ruleName, rule) -> {
			return new DoubleRuleWidget(this.field_24314, name, description, ruleName, rule);
		});
	}

	@Override
	public <E extends Enum<E>> void visitEnum(GameRule<E> enumRule) {
		this.createRuleWidget(enumRule, (name, description, ruleName, rule) -> {
			return new EnumRuleWidget<>(this.field_24314, name, description, ruleName, rule, enumRule.getTranslationKey());
		});
	}

	/**
	 * @reason We need to display an enum rule's default value as translated.
	 */
	@WrapOperation(method = "createRuleWidget(Lnet/minecraft/world/rule/GameRule;Lnet/minecraft/client/gui/screen/world/EditGameRulesScreen$RuleWidgetFactory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/rule/GameRule;getValueName(Ljava/lang/Object;)Ljava/lang/String;"))
	private <T> String displayProperEnumName(GameRule<T> instance, T value, Operation<String> original) {
		String valueName = original.call(instance, value);

		if (((RuleTypeExtensions) (Object) instance).fabric_getType() != FabricGameRuleType.ENUM) {
			return valueName;
		}

		String translationKey = instance.getTranslationKey() + "." + valueName.toLowerCase(Locale.ROOT);

		if (I18n.hasTranslation(translationKey)) {
			return I18n.translate(translationKey);
		}

		return valueName;
	}
}
