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

package net.fabricmc.fabric.impl.client.particle;

import static net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry.getId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.mixin.client.particle.ParticleEngineAccessor;

public final class ParticleGroupRegistryImpl {
	public static final ParticleGroupRegistryImpl INSTANCE = new ParticleGroupRegistryImpl(ParticleEngineAccessor.getParticleRenderTypes());

	private final List<ParticleRenderType> renderTypes;
	private final Map<Identifier, ParticleTextureNode> nodes = new HashMap<>();
	private final IdentityHashMap<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> factories = new IdentityHashMap<>();

	@VisibleForTesting
	public ParticleGroupRegistryImpl(List<ParticleRenderType> renderTypes) {
		var copyOfRenderTypes = new ArrayList<>(renderTypes);
		this.renderTypes = renderTypes;

		Identifier last = null;

		// Populate the nodes with vanilla texture sheets, to allow sorting with custom sheets later.
		for (ParticleRenderType renderType : this.renderTypes) {
			Identifier id = getId(renderType);

			nodes.put(id, new ParticleTextureNode(renderType));

			if (last != null) {
				ParticleTextureNode.link(nodes.get(last), nodes.get(id));
			}

			last = id;
		}

		sort();

		// Just a sanity check to make sure we didn't mess up the order of vanilla texture sheets.
		assertIdentical(renderTypes, copyOfRenderTypes);
	}

	public void register(ParticleRenderType renderType, Function<ParticleEngine, ParticleGroup<?>> function) {
		final Identifier id = getId(renderType);

		if (nodes.containsKey(id)) {
			throw new IllegalArgumentException("A ParticleRenderType with the id " + id + " has already been registered.");
		}

		if (factories.containsKey(renderType)) {
			throw new IllegalArgumentException("The specified ParticleRenderType instance has already been registered.");
		}

		var node = new ParticleTextureNode(id, renderType);
		nodes.put(id, node);
		renderTypes.add(renderType);
		factories.put(renderType, function);

		sort();
	}

	public void registerOrdering(Identifier first, Identifier second) {
		Objects.requireNonNull(first);
		Objects.requireNonNull(second);

		ParticleTextureNode firstEntry = nodes.get(first);
		ParticleTextureNode secondEntry = nodes.get(second);

		if (firstEntry == null) {
			throw new IllegalArgumentException("The specified first id " + first + " does not correspond to a registered ParticleRenderType.");
		}

		if (secondEntry == null) {
			throw new IllegalArgumentException("The specified second id " + second + " does not correspond to a registered ParticleRenderType.");
		}

		ParticleTextureNode.link(firstEntry, secondEntry);
		sort();
	}

	public @Nullable ParticleRenderType getParticleRenderType(Identifier id) {
		Objects.requireNonNull(id);
		ParticleTextureNode entry = nodes.get(id);
		return entry != null ? entry.renderType : null;
	}

	@Nullable
	public Function<ParticleEngine, ParticleGroup<?>> getFactory(ParticleRenderType renderType) {
		return factories.get(renderType);
	}

	private void sort() {
		List<ParticleTextureNode> entries = new ArrayList<>(nodes.values());
		NodeSorting.sort(entries, "particle texture sheets", Comparator.comparing(a -> a.id));

		Reference2IntMap<ParticleRenderType> sheets = new Reference2IntLinkedOpenHashMap<>();

		for (int i = 0; i < entries.size(); i++) {
			sheets.put(entries.get(i).renderType, i);
		}

		renderTypes.sort(Comparator.comparingInt(sheets::getInt));
	}

	private static void assertIdentical(List<?> a, List<?> b) {
		if (a.size() != b.size()) {
			throw new AssertionError("Lists differ in size: " + a.size() + " != " + b.size());
		}

		for (int i = 0; i < a.size(); i++) {
			if (a.get(i) != b.get(i)) {
				throw new AssertionError("Lists differ at index " + i + ": " + a.get(i) + " != " + b.get(i));
			}
		}
	}

	private static class ParticleTextureNode extends SortableNode<ParticleTextureNode> {
		final Identifier id;
		final ParticleRenderType renderType;

		private ParticleTextureNode(Identifier id, ParticleRenderType renderType) {
			this.id = id;
			this.renderType = renderType;
		}

		private ParticleTextureNode(ParticleRenderType renderType) {
			this.id = getId(renderType);
			this.renderType = renderType;
		}

		@Override
		protected String getDescription() {
			return id.toString();
		}
	}
}
