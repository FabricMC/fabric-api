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

package net.fabricmc.fabric.mixin.dimension;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.datafixer.schema.Schema2832;

import net.fabricmc.fabric.impl.dimension.TaggedChoiceExtension;

@Mixin(Schema2832.class)
public class Schema2832Mixin {
	/**
	 * Make the DSL.taggedChoiceLazy to ignore mod custom generator types and not cause deserialization failure.
	 */
	@Redirect(
			method = {
					"method_38837", "method_38838"
			},
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/DSL;taggedChoiceLazy(Ljava/lang/String;Lcom/mojang/datafixers/types/Type;Ljava/util/Map;)Lcom/mojang/datafixers/types/templates/TaggedChoice;"
			)
	)
	private static <K> TaggedChoice<K> redirectTaggedChoiceLazy(
			String name, Type<K> keyType, Map<K, Supplier<TypeTemplate>> templates
	) {
		TaggedChoice<K> result = DSL.taggedChoiceLazy(name, keyType, templates);
		((TaggedChoiceExtension) (Object) result).fabric$setFailSoft(true);
		return result;
	}
}
