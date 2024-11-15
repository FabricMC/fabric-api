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

package net.fabricmc.fabric.test.item;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.IgniteEnchantmentEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;

public class CustomEnchantmentEffectsTest implements ModInitializer {
	// weird impaling is a copy of impaling used for testing (just in case minecraft changes impaling for some reason)
	public static final RegistryKey<Enchantment> WEIRD_IMPALING = RegistryKey.of(
			RegistryKeys.ENCHANTMENT,
			Identifier.of("fabric-item-api-v1-testmod", "weird_impaling")
	);

	@Override
	public void onInitialize() {
		EnchantmentEvents.MODIFY.register(
				(key, builder, source) -> {
					if (source.isBuiltin() && key == WEIRD_IMPALING) {
						// make impaling set things on fire
						builder.addEffect(
								EnchantmentEffectComponentTypes.POST_ATTACK,
								EnchantmentEffectTarget.ATTACKER,
								EnchantmentEffectTarget.VICTIM,
								new IgniteEnchantmentEffect(EnchantmentLevelBasedValue.linear(4.0f)),
								DamageSourcePropertiesLootCondition.builder(
										DamageSourcePredicate.Builder.create().isDirect(true)
								)
						);

						// add bonus impaling damage to zombie
						builder.addEffect(
								EnchantmentEffectComponentTypes.DAMAGE,
								new AddEnchantmentEffect(EnchantmentLevelBasedValue.linear(2.5f)),
								EntityPropertiesLootCondition.builder(
										LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.create()
												.type(EntityTypePredicate.create(EntityType.ZOMBIE))
								)
						);
					}
				}
		);
	}
}
