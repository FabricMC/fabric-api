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

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;

@Mixin(ModelBaker.class)
abstract class ModelBakerMixin {
	@Shadow
	@Final
	static Logger LOGGER;

	@Unique
	@Nullable
	private ModelLoadingEventDispatcher fabric_eventDispatcher;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onReturnInit(CallbackInfo ci) {
		fabric_eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
	}

	@Inject(method = "bake", at = @At("RETURN"))
	private void onReturnBake(ModelBaker.ErrorCollectingSpriteGetter spriteGetter, CallbackInfoReturnable<ModelBaker.BakedModels> cir) {
		if (fabric_eventDispatcher == null) {
			return;
		}

		ModelBaker.BakedModels models = cir.getReturnValue();
		Map<Identifier, BakedModel> extraModels = new HashMap<>();
		fabric_eventDispatcher.forEachExtraModel(id -> {
			try {
				BakedModel model = ((ModelBaker) (Object) this).new BakerImpl(spriteGetter, id::toString).bake(id, ModelRotation.X0_Y0);
				extraModels.put(id, model);
			} catch (Exception e) {
				LOGGER.warn("Unable to bake extra model: '{}': {}", id, e);
			}
		});
		((BakedModelsHooks) (Object) models).fabric_setExtraModels(extraModels);
	}
}
