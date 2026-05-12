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

package net.fabricmc.fabric.mixin.holder.component.extension;

import java.util.Map;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentMapBuilder;

@Mixin(DataComponentMap.Builder.class)
public abstract class DataComponentMap$BuilderMixin implements FabricDataComponentMapBuilder {
	@Shadow
	public abstract <T> DataComponentMap.Builder set(DataComponentType<T> type, @Nullable T value);

	@Shadow
	abstract <T> void setUnchecked(DataComponentType<T> type, @Nullable Object value);

	@Shadow
	@Final
	private Reference2ObjectMap<DataComponentType<?>, Object> map;

	@Override
	public DataComponentMap.Builder apply(DataComponentPatch patch) {
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
			entry.getValue().ifPresentOrElse(
					value -> this.setUnchecked(entry.getKey(), value),
					() -> this.set(entry.getKey(), null)
			);
		}

		return (DataComponentMap.Builder) (Object) this;
	}

	@Override
	public DataComponentMap.Builder clear() {
		map.clear();

		return (DataComponentMap.Builder) (Object) this;
	}
}
