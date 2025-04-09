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

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.impl.dimension.TaggedChoiceTypeExtension;

@Mixin(value = TaggedChoice.TaggedChoiceType.class, remap = false)
public class TaggedChoiceTaggedChoiceTypeMixin<K> implements TaggedChoiceTypeExtension {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("TaggedChoiceType_DimDataFix");

	@Shadow(remap = false)
	@Final
	protected Object2ObjectMap<K, Type<?>> types;

	@Unique
	private boolean failSoft;

	/**
	 * Make the DSL.taggedChoiceLazy to ignore mod custom generator types and not cause deserialization failure.
	 * The Codec.PASSTHROUGH will not make Dynamic to be deserialized and serialized to Dynamic.
	 * This will avoid deserialization failure from DFU when upgrading level.dat that contains mod custom generator types.
	 */
	@Inject(
			method = "getMapCodec", at = @At("HEAD"), cancellable = true, remap = false
	)
	private void onGetCodec(K k, CallbackInfoReturnable<DataResult<? extends Codec<?>>> cir) {
		if (failSoft) {
			if (!types.containsKey(k)) {
				LOGGER.warn("Not recognizing key {}. Using pass-through codec. {}", k, this);
				cir.setReturnValue(DataResult.success(Codec.PASSTHROUGH));
			}
		}
	}

	@Override
	public void fabric$setFailSoft(boolean cond) {
		failSoft = cond;
	}
}
