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

package net.fabricmc.fabric.mixin.recipe.cooking;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.util.dynamic.Codecs;

/**
 * A Mixin class to allow cooking recipes to interpret different Json formats for the "result" item.
 *
 * <p>In Vanilla (1.20.4), cooking recipes can deserialise the following Json format for their result:
 * <pre>
 * "result": "minecraft:example"
 * </pre>
 *
 * <p>With this mixin, the following format is added as a valid option.
 * <pre>
 * "result": {
 *     "item": "minecraft:example",     (required)
 *     "count": 1                       (optional)
 * }
 * </pre>
 *
 * <p>Additionally, the mixin alters cooking recipes to always prefer to be serialised in the following manner:
 * <pre>
 * "result": {
 *     "item": minecraft:example",
 *     "count": 1
 * }
 * </pre>
 */
@Mixin(CookingRecipeSerializer.class)
public abstract class CookingRecipeSerializerMixin {
	/**
	 * Modifies the original codec to include an alternative (and now primary) Codec that allows for both identifier
	 * and counting of furnace recipe items.
	 *
	 * <p>If you wanted to alter this to allow users to extend the additional Codec, you could instead read the codec from
	 * a more customisable data source.
	 * @param originalCodec the original Codec for Item strings.
	 * @param fieldName the name of the field that was used to build the MapCodec. In 1.20.4 this was "result"
	 * @return a new Codec built with the <code>Codecs.alternatively()</code> function that allows the aforementioned
	 * 		   serialisation/deserialisation functionality.
	 */
	@ModifyReceiver(
			method = "method_53766(ILnet/minecraft/recipe/AbstractCookingRecipe$RecipeFactory;Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/serialization/Codec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;",
					ordinal = 1,
					remap = false
			)
	)
	private static Codec<? extends ItemStack> addAlternativeCodec(Codec<? extends ItemStack> originalCodec, String fieldName) {
		return Codecs.alternatively(
				ItemStack.RECIPE_RESULT_CODEC, // doesn't work with nbt - a custom codec would be required here
				originalCodec);
	}
}
