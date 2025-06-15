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

import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;

/**
 * Armor renderers render worn armor items with custom code.
 * They may be used to render armor with special models or effects.
 *
 * <p>The renderers are registered with {@link net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer#register(ArmorRenderer, ItemConvertible...)}.
 */
@FunctionalInterface
public interface ArmorRenderer {
	/**
	 * Registers the armor renderer for the specified items.
	 * @param renderer	the renderer
	 * @param items		the items
	 * @throws IllegalArgumentException if an item already has a registered armor renderer
	 * @throws NullPointerException if either an item or the renderer is null
	 */
	static void register(ArmorRenderer renderer, ItemConvertible... items) {
		ArmorRendererRegistryImpl.register(renderer, items);
	}

	/**
	 * Helper method for rendering a specific armor model, comes after setting visibility.
	 *
	 * <p>This primarily handles applying glint and the correct {@link RenderLayer}
	 * @param matrices			the matrix stack
	 * @param vertexConsumers	the vertex consumer provider
	 * @param light				packed lightmap coordinates
	 * @param stack				the item stack of the armor item
	 * @param model				the model to be rendered
	 * @param texture			the texture to be applied
	 */
	static void renderPart(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, Model model, Identifier texture) {
		VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(texture), stack.hasGlint());
		model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
	}

	/**
	 * Renders an armor part.
	 *
	 * @param matrices			     the matrix stack
	 * @param vertexConsumers	     the vertex consumer provider
	 * @param stack				     the item stack of the armor item
	 * @param bipedEntityRenderState the render state of the entity
	 * @param slot				     the equipment slot in which the armor stack is worn
	 * @param light				     packed lightmap coordinates
	 * @param contextModel		     the model provided by {@link FeatureRenderer#getContextModel()}
	 */
	void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, BipedEntityRenderState bipedEntityRenderState, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel);

	/**
	 * Checks whether an item stack equipped on the head should also be
	 * rendered as an item. By default, vanilla renders most items with their models (or special item renderers)
	 * around or on top of the entity's head, but this is often unwanted for custom equipment.
	 *
	 * <p>This method only applies to items registered with this renderer.
	 *
	 * <p>Note that the item will never be rendered by vanilla code if it has an armor model defined
	 * by the {@link net.minecraft.component.DataComponentTypes#EQUIPPABLE minecraft:equippable} component.
	 * This method cannot be used to overwrite that check to re-enable also rendering the item model.
	 * See {@link net.minecraft.client.render.entity.feature.ArmorFeatureRenderer#hasModel(ItemStack, EquipmentSlot)}.
	 *
	 * @param entity the equipping entity
	 * @param stack  the item stack equipped on the head
	 * @return {@code true} if the head item should be rendered, {@code false} otherwise
	 */
	default boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
		return true;
	}
}
