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

package net.fabricmc.fabric.mixin.recipe.ingredient;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientPacketCodec;
import net.fabricmc.fabric.impl.recipe.ingredient.OptionalCustomIngredientPacketCodec;

@Mixin(Ingredient.class)
public class IngredientMixin implements FabricIngredient {
	@Mutable
	@Shadow
	@Final
	public static Codec<Ingredient> CODEC;

	@Shadow
	@Final
	private HolderSet<Item> values;

	@ModifyExpressionValue(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 0
			)
	)
	private static StreamCodec<RegistryFriendlyByteBuf, Ingredient> useCustomIngredientPacketCodec(StreamCodec<RegistryFriendlyByteBuf, Ingredient> original) {
		return new CustomIngredientPacketCodec(original);
	}

	@ModifyExpressionValue(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;",
					ordinal = 1
			)
	)
	private static StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> useOptionalCustomIngredientPacketCodec(StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> original) {
		return new OptionalCustomIngredientPacketCodec(original);
	}

	@Inject(method = "<clinit>", at = @At("TAIL"), cancellable = true)
	private static void injectCodec(CallbackInfo ci) {
		Codec<CustomIngredient> customIngredientCodec = CustomIngredientImpl.CODEC.dispatch(
				CustomIngredientImpl.TYPE_KEY,
				CustomIngredient::getSerializer,
				CustomIngredientSerializer::getCodec);

		CODEC = Codec.either(customIngredientCodec, CODEC).xmap(
				either -> either.map(CustomIngredient::toVanilla, ingredient -> ingredient),
				ingredient -> {
					CustomIngredient customIngredient = ingredient.getCustomIngredient();
					return customIngredient == null ? Either.right(ingredient) : Either.left(customIngredient);
				}
		);
	}

	// Targets the lambdas in the codecs which extract the entries from an ingredient.
	// For custom ingredients, these lambdas will only be invoked when the client does not support this ingredient.
	// In this case, use CustomIngredientImpl#getCustomMatchingItems, which as close as we can get.
	@Inject(method = {"method_61673", "method_61677", "method_61680"}, at = @At("HEAD"), cancellable = true)
	private static void onGetEntries(Ingredient ingredient, CallbackInfoReturnable<HolderSet<Item>> cir) {
		if (ingredient instanceof CustomIngredientImpl customIngredient) {
			cir.setReturnValue(HolderSet.direct(customIngredient.getCustomMatchingItems()));
		}
	}

	@Inject(method = "equals(Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
	private void onHeadEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
		if (obj instanceof CustomIngredientImpl) {
			// This will only get called when this isn't custom and other is custom, in which case the
			// ingredients can never be equal.
			cir.setReturnValue(false);
		}
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
