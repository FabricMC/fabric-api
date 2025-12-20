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

package net.fabricmc.fabric.api.client.rendering.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when {@link RenderLayer render layers} for a {@link LivingEntityRenderer living entity renderer} are registered.
 *
 * <p>Render layers are typically used for rendering additional objects on an entity, such as armor, an elytra or {@link Deadmau5EarsLayer Deadmau5's ears}.
 * This callback lets developers add additional render layers for use in entity rendering.
 * Listeners should filter out the specific entity renderer they want to hook into, usually through {@code instanceof} checks or filtering by entity type.
 * Once listeners find a suitable entity renderer, they should register their render layer via the registration helper.
 *
 * <p>For example, to register a render layer for a player model, the example below may be used:
 * <blockquote><pre>
 * LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper) -> {
 * 	if (entityRenderer instanceof AvatarRenderer&lt;?&gt; avatarEntityRenderer) {
 * 		registrationHelper.register(new MyRenderLayer(avatarEntityRenderer, context.getModelSet()));
 * 	}
 * });
 * </pre></blockquote>
 */
@FunctionalInterface
public interface LivingEntityRenderLayerRegistrationCallback {
	Event<LivingEntityRenderLayerRegistrationCallback> EVENT = EventFactory.createArrayBacked(
			LivingEntityRenderLayerRegistrationCallback.class, callbacks -> (entityType, entityRenderer, registrationHelper, context) -> {
				for (LivingEntityRenderLayerRegistrationCallback callback : callbacks) {
					callback.registerLayers(entityType, entityRenderer, registrationHelper, context);
				}
			});

	/**
	 * Called when render layers may be registered.
	 *
	 * @param entityType     the entity type of the renderer
	 * @param entityRenderer the entity renderer
	 */
	void registerLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?, ?> entityRenderer, RegistrationHelper registrationHelper, EntityRendererProvider.Context context);

	/**
	 * A delegate object used to help register render layers for an entity renderer.
	 *
	 * <p>This is not meant for implementation by users of the API.
	 */
	@ApiStatus.NonExtendable
	interface RegistrationHelper {
		/**
		 * Adds a render layer to the entity renderer.
		 *
		 * @param renderLayer the render layer
		 * @param <T> the type of entity
		 */
		<T extends EntityRenderState> void register(RenderLayer<T, ? extends EntityModel<T>> renderLayer);
	}
}
