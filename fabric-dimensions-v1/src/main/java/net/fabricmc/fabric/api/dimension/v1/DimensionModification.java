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

package net.fabricmc.fabric.api.dimension.v1;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.dimension.modification.DimensionModificationImpl;

/**
 * Provides methods for modifying dimensions. To create an instance, call
 * {@link DimensionModifications#create(Identifier)}.
 *
 * @see DimensionModifications
 */
public class DimensionModification {
	private final Identifier id;

	@ApiStatus.Internal
	DimensionModification(Identifier id) {
		this.id = id;
	}

	/**
	 * Adds a modifier that is not sensitive to the current state of the dimension when it is applied, examples
	 * for this are modifiers that simply add or remove features unconditionally, or change other values
	 * to constants.
	 */
	public DimensionModification add(ModificationPhase phase, Predicate<DimensionSelectionContext> selector, Consumer<DimensionModificationContext> modifier) {
		DimensionModificationImpl.INSTANCE.addModifier(id, phase, selector, modifier);
		return this;
	}

	/**
	 * Adds a modifier that is sensitive to the current state of the dimension when it is applied.
	 * Examples for this are modifiers that apply scales to existing values (e.g. half the temperature).
	 *
	 * <p>For modifiers that should only be applied if a given condition is met for a dimension, please add these
	 * conditions to the selector, and use a context-free modifier instead, as this will greatly help
	 * with debugging world generation issues.
	 */
	public DimensionModification add(ModificationPhase phase, Predicate<DimensionSelectionContext> selector, BiConsumer<DimensionSelectionContext, DimensionModificationContext> modifier) {
		DimensionModificationImpl.INSTANCE.addModifier(id, phase, selector, modifier);
		return this;
	}
}
