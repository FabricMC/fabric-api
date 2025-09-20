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

import static net.fabricmc.fabric.api.client.particle.v1.ParticleRendererRegistry.getId;

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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor;

public final class ParticleRendererRegistryImpl {
	public static final ParticleRendererRegistryImpl INSTANCE = new ParticleRendererRegistryImpl(ParticleManagerAccessor.getParticleTextureSheets());

	private final List<ParticleTextureSheet> textureSheets;
	private final Map<Identifier, ParticleTextureNode> nodes = new HashMap<>();
	private final IdentityHashMap<ParticleTextureSheet, Function<ParticleManager, ParticleRenderer<?>>> factories = new IdentityHashMap<>();

	@VisibleForTesting
	public ParticleRendererRegistryImpl(List<ParticleTextureSheet> textureSheets) {
		var copyOfTextureSheets = new ArrayList<>(textureSheets);
		this.textureSheets = textureSheets;

		Identifier last = null;

		// Populate the nodes with vanilla texture sheets, to allow sorting with custom sheets later.
		for (ParticleTextureSheet sheet : this.textureSheets) {
			Identifier id = getId(sheet);

			nodes.put(id, new ParticleTextureNode(sheet));

			if (last != null) {
				ParticleTextureNode.link(nodes.get(last), nodes.get(id));
			}

			last = id;
		}

		sort();

		// Just a sanity check to make sure we didn't mess up the order of vanilla texture sheets.
		assertIdentical(textureSheets, copyOfTextureSheets);
	}

	public void register(ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		final Identifier id = getId(textureSheet);

		if (nodes.containsKey(id)) {
			throw new IllegalArgumentException("A ParticleTextureSheet with the id " + id + " has already been registered.");
		}

		if (factories.containsKey(textureSheet)) {
			throw new IllegalArgumentException("The specified ParticleTextureSheet instance has already been registered.");
		}

		var node = new ParticleTextureNode(id, textureSheet);
		nodes.put(id, node);
		textureSheets.add(textureSheet);
		factories.put(textureSheet, function);

		sort();
	}

	public void registerOrdering(Identifier first, Identifier second) {
		Objects.requireNonNull(first);
		Objects.requireNonNull(second);

		ParticleTextureNode firstEntry = nodes.get(first);
		ParticleTextureNode secondEntry = nodes.get(second);

		if (firstEntry == null) {
			throw new IllegalArgumentException("The specified first id " + first + " does not correspond to a registered ParticleTextureSheet.");
		}

		if (secondEntry == null) {
			throw new IllegalArgumentException("The specified second id " + second + " does not correspond to a registered ParticleTextureSheet.");
		}

		ParticleTextureNode.link(firstEntry, secondEntry);
		sort();
	}

	public @Nullable ParticleTextureSheet getParticleTextureSheet(Identifier id) {
		Objects.requireNonNull(id);
		ParticleTextureNode entry = nodes.get(id);
		return entry != null ? entry.textureSheet : null;
	}

	@Nullable
	public Function<ParticleManager, ParticleRenderer<?>> getFactory(ParticleTextureSheet textureSheet) {
		return factories.get(textureSheet);
	}

	private void sort() {
		List<ParticleTextureNode> entries = new ArrayList<>(nodes.values());
		NodeSorting.sort(entries, "particle texture sheets", Comparator.comparing(a -> a.id));

		Reference2IntMap<ParticleTextureSheet> sheets = new Reference2IntLinkedOpenHashMap<>();

		for (int i = 0; i < entries.size(); i++) {
			sheets.put(entries.get(i).textureSheet, i);
		}

		textureSheets.sort(Comparator.comparingInt(sheets::getInt));
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
		final ParticleTextureSheet textureSheet;

		private ParticleTextureNode(Identifier id, ParticleTextureSheet textureSheet) {
			this.id = id;
			this.textureSheet = textureSheet;
		}

		private ParticleTextureNode(ParticleTextureSheet textureSheet) {
			this.id = getId(textureSheet);
			this.textureSheet = textureSheet;
		}

		@Override
		protected String getDescription() {
			return id.toString();
		}
	}
}
