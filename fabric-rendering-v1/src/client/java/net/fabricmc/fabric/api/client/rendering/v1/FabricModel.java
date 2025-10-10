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

package net.fabricmc.fabric.api.client.rendering.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

/**
 * General purpose Fabric extensions to the {@link Model} class.
 *
 * <p>Note: This interface is automatically implemented on all {@link Model} instances via Mixin and interface injection.
 */
@ApiStatus.NonExtendable
public interface FabricModel<S> {
	/**
	 * Returns a child model part of the given name, or {@code null} if one is not found.
	 * @param name the name of the child model part
	 * @return the child model part that corresponds to the name parameter, or {@code null} if it is not found.
	 */
	@Nullable
	default ModelPart getChildPart(String name) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Copies transforms of child model parts of the model to child model parts of this model whose names match.
	 * @param model the model to copy transforms from
	 */
	default void copyTransforms(Model<?> model) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
