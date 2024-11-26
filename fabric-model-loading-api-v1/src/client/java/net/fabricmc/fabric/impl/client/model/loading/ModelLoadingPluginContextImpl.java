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

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ModelLoadingPluginContextImpl implements ModelLoadingPlugin.Context {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoadingPluginContextImpl.class);

	final Set<Identifier> extraModels = new LinkedHashSet<>();
	final Map<Block, BlockStateResolver> blockStateResolvers = new IdentityHashMap<>();

	private static final Identifier[] MODEL_MODIFIER_PHASES = new Identifier[] { ModelModifier.OVERRIDE_PHASE, ModelModifier.DEFAULT_PHASE, ModelModifier.WRAP_PHASE, ModelModifier.WRAP_LAST_PHASE };

	private final Event<ModelModifier.OnLoad> onLoadModifiers = EventFactory.createWithPhases(ModelModifier.OnLoad.class, modifiers -> (model, context) -> {
		for (ModelModifier.OnLoad modifier : modifiers) {
			try {
				model = modifier.modifyModelOnLoad(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify unbaked model on load", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);
	private final Event<ModelModifier.BeforeBake> beforeBakeModifiers = EventFactory.createWithPhases(ModelModifier.BeforeBake.class, modifiers -> (model, context) -> {
		for (ModelModifier.BeforeBake modifier : modifiers) {
			try {
				model = modifier.modifyModelBeforeBake(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify unbaked model before bake", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);
	private final Event<ModelModifier.AfterBake> afterBakeModifiers = EventFactory.createWithPhases(ModelModifier.AfterBake.class, modifiers -> (model, context) -> {
		for (ModelModifier.AfterBake modifier : modifiers) {
			try {
				model = modifier.modifyModelAfterBake(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify baked model after bake", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);
	private final Event<ModelModifier.OnLoadBlock> onLoadBlockModifiers = EventFactory.createWithPhases(ModelModifier.OnLoadBlock.class, modifiers -> (model, context) -> {
		for (ModelModifier.OnLoadBlock modifier : modifiers) {
			try {
				model = modifier.modifyModelOnLoad(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify unbaked block model on load", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);
	private final Event<ModelModifier.BeforeBakeBlock> beforeBakeBlockModifiers = EventFactory.createWithPhases(ModelModifier.BeforeBakeBlock.class, modifiers -> (model, context) -> {
		for (ModelModifier.BeforeBakeBlock modifier : modifiers) {
			try {
				model = modifier.modifyModelBeforeBake(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify unbaked block model before bake", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);
	private final Event<ModelModifier.AfterBakeBlock> afterBakeBlockModifiers = EventFactory.createWithPhases(ModelModifier.AfterBakeBlock.class, modifiers -> (model, context) -> {
		for (ModelModifier.AfterBakeBlock modifier : modifiers) {
			try {
				model = modifier.modifyModelAfterBake(model, context);
			} catch (Exception exception) {
				LOGGER.error("Failed to modify baked block model after bake", exception);
			}
		}

		return model;
	}, MODEL_MODIFIER_PHASES);

	@Override
	public void addModels(Identifier... ids) {
		for (Identifier id : ids) {
			extraModels.add(id);
		}
	}

	@Override
	public void addModels(Collection<? extends Identifier> ids) {
		extraModels.addAll(ids);
	}

	@Override
	public void registerBlockStateResolver(Block block, BlockStateResolver resolver) {
		Objects.requireNonNull(block, "block cannot be null");
		Objects.requireNonNull(resolver, "resolver cannot be null");

		Optional<RegistryKey<Block>> optionalKey = Registries.BLOCK.getKey(block);

		if (optionalKey.isEmpty()) {
			throw new IllegalArgumentException("Received unregistered block");
		}

		if (blockStateResolvers.put(block, resolver) != null) {
			throw new IllegalArgumentException("Duplicate block state resolver for " + block);
		}
	}

	@Override
	public Event<ModelModifier.OnLoad> modifyModelOnLoad() {
		return onLoadModifiers;
	}

	@Override
	public Event<ModelModifier.BeforeBake> modifyModelBeforeBake() {
		return beforeBakeModifiers;
	}

	@Override
	public Event<ModelModifier.AfterBake> modifyModelAfterBake() {
		return afterBakeModifiers;
	}

	@Override
	public Event<ModelModifier.OnLoadBlock> modifyBlockModelOnLoad() {
		return onLoadBlockModifiers;
	}

	@Override
	public Event<ModelModifier.BeforeBakeBlock> modifyBlockModelBeforeBake() {
		return beforeBakeBlockModifiers;
	}

	@Override
	public Event<ModelModifier.AfterBakeBlock> modifyBlockModelAfterBake() {
		return afterBakeBlockModifiers;
	}
}
