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
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;

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

	public Map<ExtraModelKey<?>, UnbakedExtraModel<?>> getExtraModels() {
		return pluginContext.extraModels;
	}

	public Map<Identifier, UnbakedModel> modifyModelsOnLoad(Map<Identifier, UnbakedModel> models) {
		if (!(models instanceof HashMap)) {
			models = new HashMap<>(models);
		}

		models.replaceAll(this::modifyModelOnLoad);
		return models;
	}

	private UnbakedModel modifyModelOnLoad(Identifier id, UnbakedModel model) {
		onLoadModifierContext.prepare(id);
		return pluginContext.modifyModelOnLoad().invoker().modifyModelOnLoad(model, onLoadModifierContext);
	}

	public BlockStatesLoader.LoadedModels modifyBlockModelsOnLoad(BlockStatesLoader.LoadedModels models) {
		Map<BlockState, BlockStateModel.UnbakedGrouped> map = models.models();

		if (!(map instanceof HashMap)) {
			map = new HashMap<>(map);
			models = new BlockStatesLoader.LoadedModels(map);
		}

		putResolvedBlockStates(map);
		map.replaceAll(this::modifyBlockModelOnLoad);

		return models;
	}

	private void putResolvedBlockStates(Map<BlockState, BlockStateModel.UnbakedGrouped> map) {
		pluginContext.blockStateResolvers.forEach((block, resolver) -> {
			resolveBlockStates(resolver, block, map::put);
		});
	}

	private void resolveBlockStates(BlockStateResolver resolver, Block block, BiConsumer<BlockState, BlockStateModel.UnbakedGrouped> output) {
		BlockStateResolverContext context = blockStateResolverContext;
		context.prepare(block);

		Reference2ReferenceMap<BlockState, BlockStateModel.UnbakedGrouped> resolvedModels = context.models;
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
					BlockStateModel.UnbakedGrouped model = resolvedModels.get(state);

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

	private BlockStateModel.UnbakedGrouped modifyBlockModelOnLoad(BlockState state, BlockStateModel.UnbakedGrouped model) {
		onLoadBlockModifierContext.prepare(state);
		return pluginContext.modifyBlockModelOnLoad().invoker().modifyModelOnLoad(model, onLoadBlockModifierContext);
	}

	public BlockStateModel modifyBlockModel(BlockStateModel.UnbakedGrouped unbakedModel, BlockState state, Baker baker, Operation<BlockStateModel> bakeOperation) {
		BakeBlockModifierContext modifierContext = new BakeBlockModifierContext(state, baker);
		unbakedModel = pluginContext.modifyBlockModelBeforeBake().invoker().modifyModelBeforeBake(unbakedModel, modifierContext);
		BlockStateModel model = bakeOperation.call(unbakedModel, state, baker);
		modifierContext.prepareAfterBake(unbakedModel);
		return pluginContext.modifyBlockModelAfterBake().invoker().modifyModelAfterBake(model, modifierContext);
	}

	public ItemModel modifyItemModel(ItemModel.Unbaked unbakedModel, Identifier itemId, ItemModel.BakeContext bakeContext, Operation<ItemModel> bakeOperation) {
		BakeItemModifierContext modifierContext = new BakeItemModifierContext(itemId, bakeContext);
		unbakedModel = pluginContext.modifyItemModelBeforeBake().invoker().modifyModelBeforeBake(unbakedModel, modifierContext);
		ItemModel model = bakeOperation.call(unbakedModel, bakeContext);
		modifierContext.prepareAfterBake(unbakedModel);
		return pluginContext.modifyItemModelAfterBake().invoker().modifyModelAfterBake(model, modifierContext);
	}

	private static class BlockStateResolverContext implements BlockStateResolver.Context {
		private Block block;
		private final Reference2ReferenceMap<BlockState, BlockStateModel.UnbakedGrouped> models = new Reference2ReferenceOpenHashMap<>();

		private void prepare(Block block) {
			this.block = block;
			models.clear();
		}

		@Override
		public Block block() {
			return block;
		}

		@Override
		public void setModel(BlockState state, BlockStateModel.UnbakedGrouped model) {
			Objects.requireNonNull(state, "state cannot be null");
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
		private BlockState state;

		private void prepare(BlockState state) {
			this.state = state;
		}

		@Override
		public BlockState state() {
			return state;
		}
	}

	private static class BakeBlockModifierContext implements ModelModifier.BeforeBakeBlock.Context, ModelModifier.AfterBakeBlock.Context {
		private final BlockState state;
		private final Baker baker;
		private BlockStateModel.UnbakedGrouped sourceModel;

		private BakeBlockModifierContext(BlockState state, Baker baker) {
			this.state = state;
			this.baker = baker;
		}

		private void prepareAfterBake(BlockStateModel.UnbakedGrouped sourceModel) {
			this.sourceModel = sourceModel;
		}

		@Override
		public BlockState state() {
			return state;
		}

		@Override
		public Baker baker() {
			return baker;
		}

		@Override
		public BlockStateModel.UnbakedGrouped sourceModel() {
			return sourceModel;
		}
	}

	private static class BakeItemModifierContext implements ModelModifier.BeforeBakeItem.Context, ModelModifier.AfterBakeItem.Context {
		private final Identifier itemId;
		private final ItemModel.BakeContext bakeContext;
		private ItemModel.Unbaked sourceModel;

		private BakeItemModifierContext(Identifier itemId, ItemModel.BakeContext bakeContext) {
			this.itemId = itemId;
			this.bakeContext = bakeContext;
		}

		private void prepareAfterBake(ItemModel.Unbaked sourceModel) {
			this.sourceModel = sourceModel;
		}

		@Override
		public Identifier itemId() {
			return itemId;
		}

		@Override
		public ItemModel.BakeContext bakeContext() {
			return bakeContext;
		}

		@Override
		public ItemModel.Unbaked sourceModel() {
			return sourceModel;
		}
	}
}
