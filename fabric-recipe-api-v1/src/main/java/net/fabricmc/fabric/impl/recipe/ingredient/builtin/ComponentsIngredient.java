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
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class ComponentsIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<ComponentsIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final DataComponentPatch components;

	public ComponentsIngredient(Ingredient base, DataComponentPatch components) {
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
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
			final DataComponentType<?> type = entry.getKey();
			final Optional<?> value = entry.getValue();

			if (value.isPresent()) {
				// Expect the stack to contain a matching component
				if (!stack.has(type)) {
					return false;
				}

				if (!Objects.equals(value.get(), stack.get(type))) {
					return false;
				}
			} else {
				// Expect the target stack to not contain this component
				if (stack.has(type)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public Stream<Holder<Item>> getMatchingItems() {
		return base.items();
	}

	@Override
	public SlotDisplay toDisplay() {
		return new SlotDisplay.Composite(
			base.items().map(this::createEntryDisplay).toList()
		);
	}

	private SlotDisplay createEntryDisplay(Holder<Item> entry) {
		ItemStack stack = entry.value().getDefaultInstance();
		stack.applyComponentsAndValidate(components);
		return new SlotDisplay.ItemStackSlotDisplay(stack);
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
	private DataComponentPatch getComponents() {
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
		private static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "components");
		private static final MapCodec<ComponentsIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Ingredient.CODEC.fieldOf("base").forGetter(ComponentsIngredient::getBase),
						DataComponentPatch.CODEC.fieldOf("components").forGetter(ComponentsIngredient::getComponents)
				).apply(instance, ComponentsIngredient::new)
		);
		private static final StreamCodec<RegistryFriendlyByteBuf, ComponentsIngredient> PACKET_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC, ComponentsIngredient::getBase,
				DataComponentPatch.STREAM_CODEC, ComponentsIngredient::getComponents,
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
		public StreamCodec<RegistryFriendlyByteBuf, ComponentsIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
