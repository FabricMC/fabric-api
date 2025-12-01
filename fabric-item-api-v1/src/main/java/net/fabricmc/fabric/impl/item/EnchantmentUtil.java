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

package net.fabricmc.fabric.impl.item;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.item.v1.EnchantmentSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.fabric.mixin.item.EnchantmentBuilderAccessor;

public class EnchantmentUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnchantmentUtil.class);

	@SuppressWarnings("unchecked")
	@Nullable
	public static Enchantment modify(ResourceKey<Enchantment> key, Enchantment originalEnchantment, EnchantmentSource source) {
		Enchantment.Builder builder = Enchantment.enchantment(originalEnchantment.definition());
		EnchantmentBuilderAccessor accessor = (EnchantmentBuilderAccessor) builder;
		BuilderExtensions builderExtensions = (BuilderExtensions) builder;

		builder.exclusiveWith(originalEnchantment.exclusiveSet());
		accessor.getEffectMap().addAll(originalEnchantment.effects());

		originalEnchantment.effects().stream()
				.forEach(component -> {
					if (component.value() instanceof List<?> valueList) {
						// component type cast is checked by the value
						accessor.invokeGetEffectsList((DataComponentType<List<Object>>) component.type())
								.addAll(valueList);
					}
				});

		// Reset the modified flag before invoking the event as we setup the builder above
		builderExtensions.fabric$resetModified();

		EnchantmentEvents.MODIFY.invoker().modify(key, builder, source);

		if (builderExtensions.fabric$didModify()) {
			LOGGER.debug("Enchantment {} was modified", key.identifier());

			return new Enchantment(
					originalEnchantment.description(),
					accessor.getDefinition(),
					accessor.getExclusiveSet(),
					accessor.getEffectMap().build()
			);
		}

		return null;
	}

	public static EnchantmentSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = resource.getFabricPackSource();

			if (packSource == PackSource.BUILT_IN) {
				return EnchantmentSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModResourcePackSource) {
				return EnchantmentSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual enchantment injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return EnchantmentSource.DATA_PACK;
	}

	private EnchantmentUtil() { }

	public interface BuilderExtensions {
		void fabric$resetModified();
		boolean fabric$didModify();
	}
}
