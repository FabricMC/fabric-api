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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

@Mixin(InventoryScreen.class)
abstract class InventoryScreenMixin {
	@Redirect(
			method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;")
	)
	private static <T extends Entity, S extends EntityRenderState> S createNewRenderState(EntityRenderer<T, S> renderer, T entity, float tickProgress) {
		// Vanilla reuses the same render state rather than creating a new one, which causes all entities of the same
		// type rendered by this method to be rendered the same. Not good! We create a new render state each time here.
		S newState = renderer.createRenderState();
		renderer.updateRenderState(entity, newState, tickProgress);
		return newState;
	}
}
