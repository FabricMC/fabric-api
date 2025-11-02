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

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;

/**
 * Armor renderers render worn armor items with custom code.
 * They may be used to render armor with special models or effects.
 *
 * <p>The renderers are registered with {@link net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer#register(Factory, ItemConvertible...)}
 * or {@link net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer#register(ArmorRenderer, ItemConvertible...)}.
 */
@FunctionalInterface
public interface ArmorRenderer {
	/**
	 * Registers the armor renderer for the specified items.
	 * @param factory   the renderer factory
	 * @param items     the items
	 * @throws IllegalArgumentException if an item already has a registered armor renderer
	 * @throws NullPointerException if either an item or the factory is null
	 */
	static void register(ArmorRenderer.Factory factory, ItemConvertible... items) {
		ArmorRendererRegistryImpl.register(factory, items);
	}

	/**
	 * Registers the armor renderer for the specified items.
	 * @param renderer  the renderer
	 * @param items     the items
	 * @throws IllegalArgumentException if an item already has a registered armor renderer
	 * @throws NullPointerException if either an item or the renderer is null
	 */
	static void register(ArmorRenderer renderer, ItemConvertible... items) {
		ArmorRendererRegistryImpl.register(renderer, items);
	}

	/**
	 * Helper method for rendering a {@link TransformCopyingModel}, which will copy transforms from a source model to
	 * a delegate model when it is rendered.
	 * @param sourceModel           the model whose transforms will be copied
	 * @param sourceModelState      the model state of the source model
	 * @param delegateModel         the model that will be rendered with transforms copied from the source model
	 * @param delegateModelState    the model state of the delegate model
	 * @param setDelegateAngles     {@code true} if the {@link Model#setAngles(Object)} method should be called for the
	 *                                             delegate model after it is called for the source model
	 * @param queue                 the {@link RenderCommandQueue}
	 * @param matrices              the matrix stack
	 * @param renderLayer           the render layer
	 * @param light                 packed lightmap coordinates
	 * @param overlay               packed overlay texture coordinates
	 * @param tintedColor           the color to tint the model with
	 * @param sprite                the sprite to render the model with, or {@code null} to use the render layer instead
	 * @param outlineColor          the outline color of the model
	 * @param crumblingOverlay      the crumbling overlay, or {@code null} for no crumbling overlay
	 * @param <S>                   state type of the source model
	 * @param <D>                   state type of the delegate model
	 */
	static <S, D> void submitTransformCopyingModel(Model<? super S> sourceModel, S sourceModelState, Model<? super D> delegateModel, D delegateModelState, boolean setDelegateAngles, RenderCommandQueue queue, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int tintedColor, @Nullable Sprite sprite, int outlineColor, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
		queue.submitModel(TransformCopyingModel.create(sourceModel, delegateModel, setDelegateAngles), Pair.of(sourceModelState, delegateModelState), matrices, renderLayer, light, overlay, tintedColor, sprite, outlineColor, crumblingOverlay);
	}

	/**
	 * Helper method for rendering a {@link TransformCopyingModel}, which will copy transforms from its source model to
	 * its delegate model when it is rendered.
	 * @param sourceModel           the model whose transforms will be copied
	 * @param sourceModelState      the model state of the source model
	 * @param delegateModel         the model that will be rendered with transforms copied from the source model
	 * @param delegateModelState    the model state of the delegate model
	 * @param setDelegateAngles     {@code true} if the {@link Model#setAngles(Object)} method should be called for the
	 *                                             delegate model after it is called for the source model
	 * @param queue                 the {@link RenderCommandQueue}
	 * @param matrices              the matrix stack
	 * @param renderLayer           the render layer
	 * @param light                 packed lightmap coordinates
	 * @param overlay               packed overlay texture coordinates
	 * @param outlineColor          the outline color of the model
	 * @param crumblingOverlay      the crumbling overlay, or {@code null} for no crumbling overlay
	 * @param <S>                   state type of the source model
	 * @param <D>                   state type of the delegate model
	 */
	static <S, D> void submitTransformCopyingModel(Model<? super S> sourceModel, S sourceModelState, Model<? super D> delegateModel, D delegateModelState, boolean setDelegateAngles, RenderCommandQueue queue, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int outlineColor, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
		queue.submitModel(TransformCopyingModel.create(sourceModel, delegateModel, setDelegateAngles), Pair.of(sourceModelState, delegateModelState), matrices, renderLayer, light, overlay, outlineColor, crumblingOverlay);
	}

	/**
	 * Renders an armor part.
	 *
	 * @param matrices                  the matrix stack
	 * @param orderedRenderCommandQueue the {@link OrderedRenderCommandQueue} instance
	 * @param stack                     the item stack of the armor item
	 * @param bipedEntityRenderState    the render state of the entity
	 * @param slot                      the equipment slot in which the armor stack is worn
	 * @param light                     packed lightmap coordinates
	 * @param contextModel              the model provided by {@link FeatureRenderer#getContextModel()}
	 */
	void render(MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ItemStack stack, BipedEntityRenderState bipedEntityRenderState, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel);

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

	/**
	 * A factory to create an {@link ArmorRenderer} instance.
	 */
	@FunctionalInterface
	interface Factory {
		ArmorRenderer createArmorRenderer(EntityRendererFactory.Context context);
	}
}
