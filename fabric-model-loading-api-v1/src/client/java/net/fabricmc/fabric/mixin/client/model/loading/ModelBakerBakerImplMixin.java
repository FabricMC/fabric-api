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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.ModelBakerHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;

@Mixin(ModelBaker.BakerImpl.class)
abstract class ModelBakerBakerImplMixin {
	@Shadow
	@Final
	private ModelBaker field_40571;

	@WrapOperation(method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/UnbakedModel.bake(Lnet/minecraft/client/render/model/UnbakedModel;Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;"))
	private BakedModel wrapModelBake(UnbakedModel unbakedModel, @Coerce Baker baker, ModelBakeSettings settings, Operation<BakedModel> operation, Identifier id) {
		ModelLoadingEventDispatcher dispatcher = ((ModelBakerHooks) this.field_40571).fabric_getDispatcher();

		if (dispatcher == null) {
			return operation.call(unbakedModel, baker, settings);
		}

		unbakedModel = dispatcher.modifyModelBeforeBake(unbakedModel, id, settings, baker);
		BakedModel model = operation.call(unbakedModel, baker, settings);
		return dispatcher.modifyModelAfterBake(model, id, unbakedModel, settings, baker);
	}
}
