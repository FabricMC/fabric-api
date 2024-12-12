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

package net.fabricmc.fabric.impl.client.model.loading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.GroupableModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

public class ModelLoadingEventDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoadingEventDispatcher.class);
	public static final ThreadLocal<ModelLoadingEventDispatcher> CURRENT = new ThreadLocal<>();

	private final ModelLoadingPluginContextImpl pluginContext;

	private final BlockStateResolverContext blockStateResolverContext = new BlockStateResolverContext();

	private final OnLoadModifierContext onLoadModifierContext = new OnLoadModifierContext();
	private final OnLoadBlockModifierContext onLoadBlockModifierContext = new OnLoadBlockModifierContext();

	public ModelLoadingEventDispatcher(List<ModelLoadingPlugin> plugins) {
		this.pluginContext = new ModelLoadingPluginContextImpl();

		for (ModelLoadingPlugin plugin : plugins) {
			try {
				plugin.initialize(pluginContext);
			} catch (Exception exception) {
				LOGGER.error("Failed to initialize model loading plugin", exception);
			}
		}
	}

	public void forEachExtraModel(Consumer<Identifier> extraModelConsumer) {
		pluginContext.extraModels.forEach(extraModelConsumer);
	}

	@Nullable
	public UnbakedModel modifyModelOnLoad(@Nullable UnbakedModel model, Identifier id) {
		onLoadModifierContext.prepare(id);
		return pluginContext.modifyModelOnLoad().invoker().modifyModelOnLoad(model, onLoadModifierContext);
	}

	public BlockStatesLoader.BlockStateDefinition modifyBlockModelsOnLoad(BlockStatesLoader.BlockStateDefinition models) {
		Map<ModelIdentifier, BlockStatesLoader.BlockModel> map = models.models();

		if (!(map instanceof HashMap)) {
			map = new HashMap<>(map);
			models = new BlockStatesLoader.BlockStateDefinition(map);
		}

		putResolvedBlockStates(map);

		map.replaceAll((id, blockModel) -> {
			GroupableModel original = blockModel.model();
			GroupableModel modified = modifyBlockModelOnLoad(original, id, blockModel.state());

			if (original != modified) {
				return new BlockStatesLoader.BlockModel(blockModel.state(), modified);
			}

			return blockModel;
		});

		return models;
	}

	private void putResolvedBlockStates(Map<ModelIdentifier, BlockStatesLoader.BlockModel> map) {
		pluginContext.blockStateResolvers.forEach((block, resolver) -> {
			Optional<RegistryKey<Block>> optionalKey = Registries.BLOCK.getKey(block);

			if (optionalKey.isEmpty()) {
				return;
			}

			Identifier blockId = optionalKey.get().getValue();

			resolveBlockStates(resolver, block, (state, model) -> {
				ModelIdentifier modelId = BlockModels.getModelId(blockId, state);
				map.put(modelId, new BlockStatesLoader.BlockModel(state, model));
			});
		});
	}

	private void resolveBlockStates(BlockStateResolver resolver, Block block, BiConsumer<BlockState, GroupableModel> output) {
		BlockStateResolverContext context = blockStateResolverContext;
		context.prepare(block);

		Reference2ReferenceMap<BlockState, GroupableModel> resolvedModels = context.models;
		ImmutableList<BlockState> allStates = block.getStateManager().getStates();
		boolean thrown = false;

		try {
			resolver.resolveBlockStates(context);
		} catch (Exception e) {
			LOGGER.error("Failed to resolve block state models for block {}. Using missing model for all states.", block, e);
			thrown = true;
		}

		if (!thrown) {
			if (resolvedModels.size() == allStates.size()) {
				// If there are as many resolved models as total states, all states have
				// been resolved and models do not need to be null-checked.
				resolvedModels.forEach(output);
			} else {
				for (BlockState state : allStates) {
					@Nullable
					GroupableModel model = resolvedModels.get(state);

					if (model == null) {
						LOGGER.error("Block state resolver did not provide a model for state {} in block {}. Using missing model.", state, block);
					} else {
						output.accept(state, model);
					}
				}
			}
		}

		resolvedModels.clear();
	}

	private GroupableModel modifyBlockModelOnLoad(GroupableModel model, ModelIdentifier id, BlockState state) {
		onLoadBlockModifierContext.prepare(id, state);
		return pluginContext.modifyBlockModelOnLoad().invoker().modifyModelOnLoad(model, onLoadBlockModifierContext);
	}

	private static class BlockStateResolverContext implements BlockStateResolver.Context {
		private Block block;
		private final Reference2ReferenceMap<BlockState, GroupableModel> models = new Reference2ReferenceOpenHashMap<>();

		private void prepare(Block block) {
			this.block = block;
			models.clear();
		}

		@Override
		public Block block() {
			return block;
		}

		@Override
		public void setModel(BlockState state, GroupableModel model) {
			Objects.requireNonNull(model, "state cannot be null");
			Objects.requireNonNull(model, "model cannot be null");

			if (!state.isOf(block)) {
				throw new IllegalArgumentException("Attempted to set model for state " + state + " on block " + block);
			}

			if (models.putIfAbsent(state, model) != null) {
				throw new IllegalStateException("Duplicate model for state " + state + " on block " + block);
			}
		}
	}

	private static class OnLoadModifierContext implements ModelModifier.OnLoad.Context {
		private Identifier id;

		private void prepare(Identifier id) {
			this.id = id;
		}

		@Override
		public Identifier id() {
			return id;
		}
	}

	private static class OnLoadBlockModifierContext implements ModelModifier.OnLoadBlock.Context {
		private ModelIdentifier id;
		private BlockState state;

		private void prepare(ModelIdentifier id, BlockState state) {
			this.id = id;
			this.state = state;
		}

		@Override
		public ModelIdentifier id() {
			return id;
		}

		@Override
		public BlockState state() {
			return state;
		}
	}
}
