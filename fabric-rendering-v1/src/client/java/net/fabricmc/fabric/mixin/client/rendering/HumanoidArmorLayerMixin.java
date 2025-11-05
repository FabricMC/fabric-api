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

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
	@Unique
	private HumanoidRenderState bipedRenderState;

	public HumanoidArmorLayerMixin(RenderLayerParent<S, M> featureRendererContext) {
		super(featureRendererContext);
	}

	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
	private void render(PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, int i, S bipedEntityRenderState, float f, float g, CallbackInfo ci) {
		this.bipedRenderState = bipedEntityRenderState;
	}

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void renderArmor(PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, ItemStack stack, EquipmentSlot armorSlot, int light, S bipedEntityRenderState, CallbackInfo ci) {
		ArmorRenderer renderer = ArmorRendererRegistryImpl.get(stack.getItem());

		if (renderer != null) {
			renderer.render(matrices, orderedRenderCommandQueue, stack, bipedRenderState, armorSlot, light, (HumanoidModel<HumanoidRenderState>) getParentModel());
			ci.cancel();
		}
	}
}
