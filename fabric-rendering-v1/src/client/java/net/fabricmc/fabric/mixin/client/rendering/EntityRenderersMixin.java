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

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;

@Mixin(EntityRenderers.class)
abstract class EntityRenderersMixin {
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "createEntityRenderers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;"))
	private static ImmutableMap.Builder createEntityRenderer(ImmutableMap.Builder builder, Object type, Object renderer, EntityRendererProvider.Context context) {
		EntityRenderer<?, ?> entityRenderer = (EntityRenderer<?, ?>) renderer;
		EntityType<?> entityType = (EntityType<?>) type;

		if (entityRenderer instanceof LivingEntityRenderer) { // Must be living for features
			LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
			LivingEntityRenderLayerRegistrationCallback.EVENT.invoker().registerLayers((EntityType<? extends LivingEntity>) entityType, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(accessor::callAddLayer), context);
		}

		return builder.put(entityType, entityRenderer);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@WrapOperation(method = "createAvatarRenderers", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"))
	private static AvatarRenderer createAvatarRenderer(EntityRendererProvider.Context context, boolean slim, Operation<AvatarRenderer> original) {
		AvatarRenderer entityRenderer = original.call(context, slim);

		LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
		LivingEntityRenderLayerRegistrationCallback.EVENT.invoker().registerLayers(EntityTypes.PLAYER, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(accessor::callAddLayer), context);

		return entityRenderer;
	}
}
