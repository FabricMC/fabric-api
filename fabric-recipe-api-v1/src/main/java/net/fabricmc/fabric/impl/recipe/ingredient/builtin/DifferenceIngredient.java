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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class DifferenceIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<DifferenceIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final Ingredient subtracted;

	public DifferenceIngredient(Ingredient base, Ingredient subtracted) {
		this.base = base;
		this.subtracted = subtracted;
	}

	@Override
	public boolean test(ItemStack stack) {
		return base.test(stack) && !subtracted.test(stack);
	}

	@Override
	public Stream<RegistryEntry<Item>> getMatchingItems() {
		final List<RegistryEntry<Item>> subtractedMatchingItems = subtracted.getMatchingItems().toList();
		return base.getMatchingItems()
				.filter(registryEntry -> !subtractedMatchingItems.contains(registryEntry));
	}

	@Override
	public boolean requiresTesting() {
		return base.requiresTesting() || subtracted.requiresTesting();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	private Ingredient getSubtracted() {
		return subtracted;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DifferenceIngredient that = (DifferenceIngredient) o;
		return base.equals(that.base) && subtracted.equals(that.subtracted);
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, subtracted);
	}

	private static class Serializer implements CustomIngredientSerializer<DifferenceIngredient> {
		private static final Identifier ID = Identifier.of("fabric", "difference");
		private static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						Ingredient.CODEC.fieldOf("base").forGetter(DifferenceIngredient::getBase),
						Ingredient.CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted)
				).apply(instance, DifferenceIngredient::new)
		);
		private static final PacketCodec<RegistryByteBuf, DifferenceIngredient> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, DifferenceIngredient::getBase,
				Ingredient.PACKET_CODEC, DifferenceIngredient::getSubtracted,
				DifferenceIngredient::new
		);

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<DifferenceIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, DifferenceIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
