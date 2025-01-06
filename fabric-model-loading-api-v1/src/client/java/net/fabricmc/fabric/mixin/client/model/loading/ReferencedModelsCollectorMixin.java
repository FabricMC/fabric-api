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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;

@Mixin(ReferencedModelsCollector.class)
abstract class ReferencedModelsCollectorMixin {
	@Unique
	@Nullable
	private ModelLoadingEventDispatcher fabric_eventDispatcher;

	@Shadow
	public abstract void add(ResolvableModel model);

	@Shadow
	abstract UnbakedModel computeResolvedModel(Identifier id);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onReturnInit(CallbackInfo ci) {
		fabric_eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();

		if (fabric_eventDispatcher != null) {
			fabric_eventDispatcher.forEachExtraModel(id -> add(computeResolvedModel(id)));
		}
	}

	@ModifyVariable(method = "getModel", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	@Nullable
	private UnbakedModel onLoadModel(@Nullable UnbakedModel model, Identifier id) {
		if (fabric_eventDispatcher == null) {
			return model;
		}

		return fabric_eventDispatcher.modifyModelOnLoad(model, id);
	}
}
