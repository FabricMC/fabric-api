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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;

import net.fabricmc.fabric.impl.recipe.book.RecipeBookImpl;
import net.fabricmc.fabric.impl.recipe.book.RecipeBookSettingsHooks;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin implements RecipeBookSettingsHooks {
	@Unique
	public final Map<RecipeBookType, RecipeBookSettings.TypeSettings> typeSettings = new HashMap<>();

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
	private static MapCodec<RecipeBookSettings> modifyRecipeBookSettingsCodecs(MapCodec<RecipeBookSettings> original) {
		return RecipeBookImpl.modifyRecipeBookSettingsCodec(original);
	}

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void createTypeSettings(CallbackInfo ci) {
		for (RecipeBookType type : RecipeBookImpl.TYPE_TO_ID.keySet()) {
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
		((RecipeBookSettingsHooks) (Object) original).fabric_getTypeSettings().putAll(this.typeSettings);
		return original;
	}

	@Inject(method = "replaceFrom", at = @At("TAIL"))
	private void enchiridion$replaceTypeSettings(RecipeBookSettings other, CallbackInfo ci) {
		this.typeSettings.putAll(((RecipeBookSettingsHooks) (Object) other).fabric_getTypeSettings());
	}

	@Override
	public Map<RecipeBookType, RecipeBookSettings.TypeSettings> fabric_getTypeSettings() {
		return typeSettings;
	}
}
