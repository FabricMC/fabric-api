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

package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;

@Mixin(CapeFeatureRenderer.class)
public class CapeFeatureRendererMixin {
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/CapeFeatureRenderer;hasCustomModelForLayer(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;)Z"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V")
	public boolean injectCapeRenderCheck(CapeFeatureRenderer instance, ItemStack itemStack, EquipmentModel.LayerType layerType, Operation<Boolean> original, @Local(argsOnly = true) PlayerEntityRenderState state) {
		if (!LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.invoker().allowCapeRender(state)) {
			return false;
		}

		return original.call(instance, itemStack, layerType);
	}
}
