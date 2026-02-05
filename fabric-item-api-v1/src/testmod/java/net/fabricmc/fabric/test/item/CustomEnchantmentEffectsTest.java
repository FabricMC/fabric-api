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

import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;

public class CustomEnchantmentEffectsTest implements ModInitializer {
	// weird impaling is a copy of impaling used for testing (just in case minecraft changes impaling for some reason)
	public static final ResourceKey<Enchantment> WEIRD_IMPALING = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "weird_impaling")
	);

	@Override
	public void onInitialize() {
		EnchantmentEvents.MODIFY.register(
				(key, builder, source) -> {
					if (source.isBuiltin() && key == WEIRD_IMPALING) {
						// make impaling set things on fire
						builder.withEffect(
								EnchantmentEffectComponents.POST_ATTACK,
								EnchantmentTarget.ATTACKER,
								EnchantmentTarget.VICTIM,
								new Ignite(LevelBasedValue.perLevel(4.0f)),
								DamageSourceCondition.hasDamageSource(
										DamageSourcePredicate.Builder.damageType().isDirect(true)
								)
						);

						// add bonus impaling damage to zombie
						builder.withEffect(
								EnchantmentEffectComponents.DAMAGE,
								new AddValue(LevelBasedValue.perLevel(2.5f)),
								LootItemEntityPropertyCondition.hasProperties(
										LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity()
												.entityType(EntityTypePredicate.of(BuiltInRegistries.ENTITY_TYPE, EntityType.ZOMBIE))
								)
						);
					}
				}
		);
	}
}
