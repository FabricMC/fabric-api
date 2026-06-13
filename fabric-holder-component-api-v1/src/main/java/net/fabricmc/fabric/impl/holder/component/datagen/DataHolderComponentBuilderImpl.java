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

package net.fabricmc.fabric.impl.holder.component.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import net.fabricmc.fabric.api.holder.component.v1.provider.DataHolderComponentProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.impl.holder.component.data.DataHolderComponentFile;

public final class DataHolderComponentBuilderImpl implements DataHolderComponentProvider.Builder {
	public static final Encoder<DataHolderComponentBuilderImpl> ENCODER = DataHolderComponentBuilderImpl::encode;
	private boolean replace = false;
	private final List<PatchBuilderImpl> patches = new ArrayList<>();

	@Override
	public DataHolderComponentBuilderImpl replace(boolean replace) {
		this.replace = replace;
		return this;
	}

	@Override
	public DataHolderComponentBuilderImpl withConditions(ResourceCondition... conditions) {
		FabricDataGenHelper.addConditions(this, conditions);
		return this;
	}

	@Override
	public PatchBuilderImpl newPatch() {
		PatchBuilderImpl patch = new PatchBuilderImpl();
		this.patches.add(patch);
		return patch;
	}

	private static <T> DataResult<T> encode(DataHolderComponentBuilderImpl input, DynamicOps<T> ops, T prefix) {
		RecordBuilder<T> builder = ops.mapBuilder();

		if (input.replace) {
			builder.add("replace", ops.createBoolean(true));
		}

		ListBuilder<T> patches = ops.listBuilder();

		for (PatchBuilderImpl patch : input.patches) {
			patches.add(patch, PatchBuilderImpl::encode);
		}

		builder.add("patches", patches.build(ops.empty()));

		return builder.build(prefix);
	}

	public final class PatchBuilderImpl implements DataHolderComponentProvider.PatchBuilder {
		private final DataComponentPatch.Builder components = DataComponentPatch.builder();
		private final Map<Identifier, Optional<Tag>> forcedComponents = new Object2ObjectOpenHashMap<>();
		private boolean required = true;
		@Nullable
		private ResourceCondition condition;

		@Override
		public PatchBuilderImpl required(boolean required) {
			this.required = required;
			return this;
		}

		@Override
		public PatchBuilderImpl condition(@Nullable ResourceCondition condition) {
			this.condition = condition;
			return this;
		}

		@Override
		public <T> PatchBuilderImpl set(DataComponentType<T> type, T value) {
			this.components.set(type, value);
			return this;
		}

		@Override
		public PatchBuilderImpl remove(DataComponentType<?> type) {
			this.components.remove(type);
			return this;
		}

		@Override
		public <T> PatchBuilderImpl set(TypedDataComponent<T> component) {
			this.components.set(component);
			return this;
		}

		@Override
		public PatchBuilderImpl set(Iterable<TypedDataComponent<?>> components) {
			this.components.set(components);
			return this;
		}

		@Override
		public PatchBuilderImpl forceSet(Identifier componentId, Tag data) {
			this.forcedComponents.put(componentId, Optional.of(data));
			return this;
		}

		@Override
		public PatchBuilderImpl forceRemove(Identifier componentId) {
			this.forcedComponents.put(componentId, Optional.empty());
			return this;
		}

		@Override
		public PatchBuilderImpl newPatch() {
			return DataHolderComponentBuilderImpl.this.newPatch();
		}

		private static <T> DataResult<T> encode(PatchBuilderImpl input, DynamicOps<T> ops, T prefix) {
			RecordBuilder<T> componentsBuilder = MapCodec.assumeMapUnsafe(DataComponentPatch.CODEC)
					.encode(input.components.build(), ops, ops.mapBuilder());

			for (Map.Entry<Identifier, Optional<Tag>> entry : input.forcedComponents.entrySet()) {
				String key = entry.getValue().isPresent() ? entry.getKey().toString() : "!" + entry.getKey().toString();
				DataResult<T> value = entry.getValue()
						.map(tag -> ExtraCodecs.NBT.encodeStart(ops, tag))
						.orElseGet(() -> ops.mergeToMap(ops.empty(), MapLike.empty()));
				componentsBuilder.add(key, value);
			}

			RecordBuilder<T> builder = ops.mapBuilder();
			builder.add("components", componentsBuilder.build(ops.empty()));
			DataHolderComponentFile.Patch.REQUIRED_MAP_CODEC.encode(input.required, ops, builder);
			DataHolderComponentFile.Patch.CONDITION_MAP_CODEC.encode(Optional.ofNullable(input.condition), ops, builder);

			return builder.build(prefix);
		}

		private PatchBuilderImpl() {
		}
	}
}
