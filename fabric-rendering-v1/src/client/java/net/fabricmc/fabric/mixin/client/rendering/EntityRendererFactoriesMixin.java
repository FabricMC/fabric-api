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

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;

@Mixin(EntityRendererFactories.class)
public abstract class EntityRendererFactoriesMixin {
	@Shadow()
	@Final
	private static Map<EntityType<?>, EntityRendererFactory<?>> RENDERER_FACTORIES;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Inject(method = "<clinit>*", at = @At(value = "RETURN"))
	private static void onRegisterRenderers(CallbackInfo info) {
		EntityRendererRegistryImpl.setup(((t, factory) -> RENDERER_FACTORIES.put(t, factory)));
	}

	// synthetic lambda in reloadEntityRenderers
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "method_32174", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRendererFactory;create(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;)Lnet/minecraft/client/render/entity/EntityRenderer;"))
	private static EntityRenderer<?, ?> createEntityRenderer(EntityRendererFactory<?> entityRendererFactory, EntityRendererFactory.Context context, ImmutableMap.Builder builder, EntityRendererFactory.Context context2, EntityType<?> entityType) {
		EntityRenderer<?, ?> entityRenderer = entityRendererFactory.create(context);

		if (entityRenderer instanceof LivingEntityRenderer) { // Must be living for features
			LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
			LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers((EntityType<? extends LivingEntity>) entityType, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(accessor::callAddFeature), context);
		}

		return entityRenderer;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@WrapOperation(method = "reloadPlayerRenderers", at = @At(value = "NEW", target = "(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)Lnet/minecraft/client/render/entity/PlayerEntityRenderer;"))
	private static PlayerEntityRenderer createPlayerEntityRenderer(EntityRendererFactory.Context context, boolean slim, Operation<PlayerEntityRenderer> original) {
		PlayerEntityRenderer entityRenderer = original.call(context, slim);

		LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) entityRenderer;
		LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker().registerRenderers(EntityType.PLAYER, (LivingEntityRenderer) entityRenderer, new RegistrationHelperImpl(accessor::callAddFeature), context);

		return entityRenderer;
	}
}
