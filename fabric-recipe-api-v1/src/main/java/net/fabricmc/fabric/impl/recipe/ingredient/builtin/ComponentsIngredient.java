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

package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class ComponentsIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<ComponentsIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final ComponentChanges components;

	public ComponentsIngredient(Ingredient base, ComponentChanges components) {
		if (components.isEmpty()) {
			throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
		}

		this.base = base;
		this.components = components;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (!base.test(stack)) return false;

		// None strict matching
		for (Map.Entry<ComponentType<?>, Optional<?>> entry : components.entrySet()) {
			final ComponentType<?> type = entry.getKey();
			final Optional<?> value = entry.getValue();

			if (value.isPresent()) {
				// Expect the stack to contain a matching component
				if (!stack.contains(type)) {
					return false;
				}

				if (!Objects.equals(value.get(), stack.get(type))) {
					return false;
				}
			} else {
				// Expect the target stack to not contain this component
				if (stack.contains(type)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public Stream<RegistryEntry<Item>> getMatchingItems() {
		return base.getMatchingItems();
	}

	@Override
	public SlotDisplay toDisplay() {
		return new SlotDisplay.CompositeSlotDisplay(
			base.getMatchingItems().map(this::createEntryDisplay).toList()
		);
	}

	private SlotDisplay createEntryDisplay(RegistryEntry<Item> entry) {
		ItemStack stack = entry.value().getDefaultStack();
		stack.applyChanges(components);
		return new SlotDisplay.StackSlotDisplay(stack);
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	@Nullable
	private ComponentChanges getComponents() {
		return components;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComponentsIngredient that = (ComponentsIngredient) o;
		return base.equals(that.base) && components.equals(that.components);
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, components);
	}

	private static class Serializer implements CustomIngredientSerializer<ComponentsIngredient> {
		private static final Identifier ID = Identifier.of("fabric", "components");
		private static final MapCodec<ComponentsIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Ingredient.CODEC.fieldOf("base").forGetter(ComponentsIngredient::getBase),
						ComponentChanges.CODEC.fieldOf("components").forGetter(ComponentsIngredient::getComponents)
				).apply(instance, ComponentsIngredient::new)
		);
		private static final PacketCodec<RegistryByteBuf, ComponentsIngredient> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, ComponentsIngredient::getBase,
				ComponentChanges.PACKET_CODEC, ComponentsIngredient::getComponents,
				ComponentsIngredient::new
		);

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<ComponentsIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, ComponentsIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
