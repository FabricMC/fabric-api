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

package net.fabricmc.fabric.api.holder.component.v1.provider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.impl.holder.component.datagen.DataHolderComponentBuilderImpl;

public abstract class DataHolderComponentProvider implements DataProvider {
	protected final FabricPackOutput output;
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;
	private final Map<ResourceKey<?>, DataHolderComponentBuilderImpl> builders = new Reference2ObjectOpenHashMap<>();

	public DataHolderComponentProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		this.output = output;
		this.pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, "fabric/components/");
		this.registriesFuture = registriesFuture;
	}

	protected abstract void generate(HolderLookup.Provider registries);

	protected final Builder builder(ResourceKey<?> key) {
		return this.builders.computeIfAbsent(key, _ -> new DataHolderComponentBuilderImpl());
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.registriesFuture.thenCompose(lookup -> {
			this.builders.clear();
			this.generate(lookup);

			RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (Map.Entry<ResourceKey<?>, DataHolderComponentBuilderImpl> entry : this.builders.entrySet()) {
				JsonObject json = DataHolderComponentBuilderImpl.ENCODER.encodeStart(ops, entry.getValue())
						.getOrThrow(IllegalStateException::new)
						.getAsJsonObject();
				FabricDataGenHelper.addConditions(json, FabricDataGenHelper.consumeConditions(entry.getValue()));
				futures.add(DataProvider.saveStable(cache, json, this.getOutputPath(entry.getKey())));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private Path getOutputPath(ResourceKey<?> key) {
		return this.pathProvider.json(key.identifier().withPrefix(key.registry().getPath() + "/"));
	}

	@Override
	public String getName() {
		return "Data Holder Components";
	}

	public interface Builder {
		Builder replace(boolean replace);

		Builder withConditions(ResourceCondition... conditions);

		PatchBuilder newPatch();
	}

	public interface PatchBuilder {
		PatchBuilder required(boolean required);

		PatchBuilder condition(@Nullable ResourceCondition condition);

		<T> PatchBuilder set(DataComponentType<T> type, T value);

		<T> PatchBuilder set(TypedDataComponent<T> component);

		PatchBuilder set(Iterable<TypedDataComponent<?>> components);

		PatchBuilder remove(DataComponentType<?> type);

		PatchBuilder forceSet(Identifier componentId, Tag data);

		PatchBuilder forceRemove(Identifier componentId);

		PatchBuilder newPatch();
	}
}
