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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;

/**
 * Helper class for registering BlockEntityRenderers.
 *
 * <p>Use {@link net.minecraft.client.render.block.entity.BlockEntityRendererFactories#register(BlockEntityType, BlockEntityRendererFactory)} instead.
 *
 * @deprecated Replaced with transitive access wideners in Fabric Transitive Access Wideners (v1).
 */
@Deprecated
public final class BlockEntityRendererRegistry {
	/**
	 * Register a BlockEntityRenderer for a BlockEntityType. Can be called clientside before the world is rendered.
	 *
	 * @param blockEntityType the {@link BlockEntityType} to register a renderer for
	 * @param blockEntityRendererFactory a {@link BlockEntityRendererFactory} that creates a {@link BlockEntityRenderer}, called
	 *                            when {@link BlockEntityRenderManager} is initialized or immediately if the dispatcher
	 *                            class is already loaded
	 * @param <E> the {@link BlockEntity}
	 */
	public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<E> blockEntityType, BlockEntityRendererFactory<? super E, ? super S> blockEntityRendererFactory) {
		BlockEntityRendererRegistryImpl.register(blockEntityType, blockEntityRendererFactory);
	}

	private BlockEntityRendererRegistry() {
	}
}
