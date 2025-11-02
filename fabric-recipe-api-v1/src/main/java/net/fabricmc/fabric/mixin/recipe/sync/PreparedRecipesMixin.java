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

package net.fabricmc.fabric.mixin.recipe.sync;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;

import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwarePreparedRecipe;

@Mixin(PreparedRecipes.class)
public class PreparedRecipesMixin implements SyncedSerializerAwarePreparedRecipe {
	@Unique
	private Map<RecipeSerializer<?>, List<RecipeEntry<?>>> bySyncedSerializer;

	@Inject(method = "of", at = @At("HEAD"))
	private static void provideSerializerMap(Iterable<RecipeEntry<?>> recipes, CallbackInfoReturnable<PreparedRecipes> cir,
											@Share("bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeEntry<?>>>> bySerializer) {
		var map = new IdentityHashMap<RecipeSerializer<?>, List<RecipeEntry<?>>>();

		for (RecipeSerializer<?> serializer : RecipeSyncImpl.getSyncedSerializers()) {
			map.put(serializer, new ArrayList<>());
		}

		bySerializer.set(map);
	}

	@Inject(method = "of", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;"))
	private static void fillSerializerMap(Iterable<RecipeEntry<?>> recipes, CallbackInfoReturnable<PreparedRecipes> cir, @Local RecipeEntry<?> entry,
										@Share("bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeEntry<?>>>> bySerializer) {
		List<RecipeEntry<?>> list = bySerializer.get().get(entry.value().getSerializer());

		if (list != null) {
			list.add(entry);
		}
	}

	@ModifyReturnValue(method = "of", at = @At("RETURN"))
	private static PreparedRecipes attachSerializerMap(PreparedRecipes original,
													@Share("bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeEntry<?>>>> bySerializer) {
		((PreparedRecipesMixin) (Object) original).bySyncedSerializer = bySerializer.get();
		return original;
	}

	@Override
	public @Nullable List<RecipeEntry<?>> fabric_getRecipesBySyncedSerializer(RecipeSerializer<?> serializer) {
		//noinspection unchecked
		return this.bySyncedSerializer.get(serializer);
	}
}
