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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.function.BiConsumer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

public final class BlockEntityRendererRegistryImpl {
	private static final HashMap<BlockEntityType<?>, BlockEntityRendererFactory<?, ?>> MAP = new HashMap<>();
	private static BiConsumer<BlockEntityType<?>, BlockEntityRendererFactory<?, ?>> handler = (type, function) -> MAP.put(type, function);

	public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<E> blockEntityType, BlockEntityRendererFactory<? super E, ? super S> blockEntityRendererFactory) {
		handler.accept(blockEntityType, blockEntityRendererFactory);
	}

	public static void setup(BiConsumer<BlockEntityType<?>, BlockEntityRendererFactory<?, ?>> vanillaHandler) {
		MAP.forEach(vanillaHandler);
		handler = vanillaHandler;
	}

	private BlockEntityRendererRegistryImpl() {
	}
}
