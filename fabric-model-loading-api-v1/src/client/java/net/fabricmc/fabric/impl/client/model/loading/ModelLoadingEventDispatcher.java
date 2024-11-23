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

import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.GroupableModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

public class ModelLoadingEventDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoadingEventDispatcher.class);
	public static final ThreadLocal<ModelLoadingEventDispatcher> CURRENT = new ThreadLocal<>();

	private final ModelLoadingPluginContextImpl pluginContext;

	private final OnLoadModifierContext onLoadModifierContext = new OnLoadModifierContext();
	private final ObjectArrayList<BeforeBakeModifierContext> beforeBakeModifierContextStack = new ObjectArrayList<>();
	private final ObjectArrayList<AfterBakeModifierContext> afterBakeModifierContextStack = new ObjectArrayList<>();
	private final BeforeBakeBlockModifierContext beforeBakeBlockModifierContext = new BeforeBakeBlockModifierContext();
	private final AfterBakeBlockModifierContext afterBakeBlockModifierContext = new AfterBakeBlockModifierContext();

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

	public UnbakedModel modifyModelBeforeBake(UnbakedModel model, Identifier id, ModelBakeSettings settings, Baker baker) {
		if (beforeBakeModifierContextStack.isEmpty()) {
			beforeBakeModifierContextStack.add(new BeforeBakeModifierContext());
		}

		BeforeBakeModifierContext context = beforeBakeModifierContextStack.pop();
		context.prepare(id, settings, baker);

		model = pluginContext.modifyModelBeforeBake().invoker().modifyModelBeforeBake(model, context);

		beforeBakeModifierContextStack.push(context);
		return model;
	}

	public BakedModel modifyModelAfterBake(BakedModel model, Identifier id, UnbakedModel sourceModel, ModelBakeSettings settings, Baker baker) {
		if (afterBakeModifierContextStack.isEmpty()) {
			afterBakeModifierContextStack.add(new AfterBakeModifierContext());
		}

		AfterBakeModifierContext context = afterBakeModifierContextStack.pop();
		context.prepare(id, sourceModel, settings, baker);

		model = pluginContext.modifyModelAfterBake().invoker().modifyModelAfterBake(model, context);

		afterBakeModifierContextStack.push(context);
		return model;
	}

	public GroupableModel modifyBlockModelBeforeBake(GroupableModel model, ModelIdentifier id, Baker baker) {
		beforeBakeBlockModifierContext.prepare(id, baker);
		return pluginContext.modifyBlockModelBeforeBake().invoker().modifyModelBeforeBake(model, beforeBakeBlockModifierContext);
	}

	public BakedModel modifyBlockModelAfterBake(BakedModel model, ModelIdentifier id, GroupableModel sourceModel, Baker baker) {
		afterBakeBlockModifierContext.prepare(id, sourceModel, baker);
		return pluginContext.modifyBlockModelAfterBake().invoker().modifyModelAfterBake(model, afterBakeBlockModifierContext);
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

	private static class BeforeBakeModifierContext implements ModelModifier.BeforeBake.Context {
		private Identifier id;
		private ModelBakeSettings settings;
		private Baker baker;

		private void prepare(Identifier id, ModelBakeSettings settings, Baker baker) {
			this.id = id;
			this.settings = settings;
			this.baker = baker;
		}

		@Override
		public Identifier id() {
			return id;
		}

		@Override
		public ModelBakeSettings settings() {
			return settings;
		}

		@Override
		public Baker baker() {
			return baker;
		}
	}

	private static class AfterBakeModifierContext implements ModelModifier.AfterBake.Context {
		private Identifier id;
		private UnbakedModel sourceModel;
		private ModelBakeSettings settings;
		private Baker baker;

		private void prepare(Identifier id, UnbakedModel sourceModel, ModelBakeSettings settings, Baker baker) {
			this.id = id;
			this.sourceModel = sourceModel;
			this.settings = settings;
			this.baker = baker;
		}

		@Override
		public Identifier id() {
			return id;
		}

		@Override
		public UnbakedModel sourceModel() {
			return sourceModel;
		}

		@Override
		public ModelBakeSettings settings() {
			return settings;
		}

		@Override
		public Baker baker() {
			return baker;
		}
	}

	private static class BeforeBakeBlockModifierContext implements ModelModifier.BeforeBakeBlock.Context {
		private ModelIdentifier id;
		private Baker baker;

		private void prepare(ModelIdentifier id, Baker baker) {
			this.id = id;
			this.baker = baker;
		}

		@Override
		public ModelIdentifier id() {
			return id;
		}

		@Override
		public Baker baker() {
			return baker;
		}
	}

	private static class AfterBakeBlockModifierContext implements ModelModifier.AfterBakeBlock.Context {
		private ModelIdentifier id;
		private GroupableModel sourceModel;
		private Baker baker;

		private void prepare(ModelIdentifier id, GroupableModel sourceModel, Baker baker) {
			this.id = id;
			this.sourceModel = sourceModel;
			this.baker = baker;
		}

		@Override
		public ModelIdentifier id() {
			return id;
		}

		@Override
		public GroupableModel sourceModel() {
			return sourceModel;
		}

		@Override
		public Baker baker() {
			return baker;
		}
	}
}
