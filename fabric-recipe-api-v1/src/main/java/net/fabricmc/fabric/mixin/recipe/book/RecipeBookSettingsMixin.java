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

package net.fabricmc.fabric.mixin.recipe.book;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.fabric.impl.recipe.book.RecipeBookImpl;
import net.fabricmc.fabric.impl.recipe.book.RecipeBookSettingsExtension;
import net.fabricmc.fabric.impl.recipe.util.WrapperMapCodec;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin implements RecipeBookSettingsExtension {
	@Shadow
	@Final
	@Mutable
	public static MapCodec<RecipeBookSettings> MAP_CODEC;
	@Unique
	public final Map<RecipeBookType, RecipeBookSettings.TypeSettings> typeSettings = new HashMap<>();

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void modifyRecipeBookSettingsCodecs(CallbackInfo ci) {
		MAP_CODEC = new WrapperMapCodec<>(MAP_CODEC, new WrapperMapCodec.Wrapper<>() {
			@Override
			public <T> RecordBuilder<T> encode(RecipeBookSettings input, DynamicOps<T> ops, RecordBuilder<T> prefix, MapEncoder<RecipeBookSettings> wrapped) {
				RecordBuilder<T> builder = wrapped.encode(input, ops, prefix);

				Map<RecipeBookType, RecipeBookSettings.TypeSettings> typeSettings = new HashMap<>(((RecipeBookSettingsExtension) (Object) input).fabric_getTypeSettings());
				// Remove anything that should be handled by default.
				typeSettings.values().removeIf(settings -> !settings.open() && !settings.filtering());

				if (!typeSettings.isEmpty()) {
					return RecipeBookImpl.FABRIC_SETTINGS_MAP_CODEC.encode(typeSettings, ops, builder);
				}

				return builder;
			}

			@Override
			public <T> DataResult<RecipeBookSettings> decode(DynamicOps<T> ops, MapLike<T> input, MapDecoder<RecipeBookSettings> wrapped) {
				return wrapped.decode(ops, input).flatMap(recipeBookSettings -> {
					DataResult<RecipeBookSettings> dataResult = DataResult.success(recipeBookSettings);
					return RecipeBookImpl.FABRIC_SETTINGS_MAP_CODEC.decode(ops, input).mapOrElse(
							fabricSettings -> dataResult.map(settings -> {
								((RecipeBookSettingsExtension) (Object) settings).fabric_getTypeSettings().putAll(fabricSettings);
								return settings;
							}),
							// The below will return if everything is the default value, ignore the error.
							error -> DataResult.success(recipeBookSettings)
					);
				});
			}
		});
	}

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void createTypeSettings(CallbackInfo ci) {
		for (RecipeBookType type : RecipeBookImpl.ENTRIES.keySet()) {
			typeSettings.put(type, RecipeBookSettings.TypeSettings.DEFAULT);
		}
	}

	@Inject(method = "getSettings", at = @At("HEAD"), cancellable = true)
	private void enchiridion$getCookingBookSettings(RecipeBookType type, CallbackInfoReturnable<RecipeBookSettings.TypeSettings> cir) {
		if (typeSettings.containsKey(type)) {
			cir.setReturnValue(typeSettings.get(type));
		}
	}

	@Inject(method = "updateSettings", at = @At("HEAD"), cancellable = true)
	private void enchiridion$updateCookingBookSettings(RecipeBookType recipeBookType, UnaryOperator<RecipeBookSettings.TypeSettings> operator, CallbackInfo ci) {
		if (typeSettings.containsKey(recipeBookType)) {
			this.typeSettings.compute(recipeBookType, (bookType, settings) -> operator.apply(settings));
			ci.cancel();
		}
	}

	@ModifyReturnValue(method = "copy", at = @At("RETURN"))
	private RecipeBookSettings copyTypeSettings(RecipeBookSettings original) {
		((RecipeBookSettingsExtension) (Object) original).fabric_getTypeSettings().putAll(this.typeSettings);
		return original;
	}

	@Inject(method = "replaceFrom", at = @At("TAIL"))
	private void enchiridion$replaceTypeSettings(RecipeBookSettings other, CallbackInfo ci) {
		this.typeSettings.putAll(((RecipeBookSettingsExtension) (Object) other).fabric_getTypeSettings());
	}

	@Override
	public Map<RecipeBookType, RecipeBookSettings.TypeSettings> fabric_getTypeSettings() {
		return typeSettings;
	}
}
