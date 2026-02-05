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

package net.fabricmc.fabric.api.transfer.v1.storage;

import java.util.Objects;

import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;

/**
 * An immutable association of an immutable object instance (for example {@code Item} or {@code Fluid}) and data components.
 *
 * <p>This is exposed for convenience for code that needs to be generic across multiple transfer variants,
 * but note that a {@link Storage} is not necessarily bound to {@code TransferVariant}. Its generic parameter can be any immutable object.
 *
 * <p><b>Transfer variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the components.
 *
 * @param <O> The type of the immutable object instance, for example {@code Item} or {@code Fluid}.
 */
public interface TransferVariant<O> extends DataComponentHolder, TypedInstance<O> {
	/**
	 * Return true if this variant is blank, and false otherwise.
	 */
	boolean isBlank();

	/**
	 * Return the immutable object instance of this variant.
	 */
	O getObject();

	/**
	 * @return The {@link DataComponentPatch} of this variant.
	 */
	DataComponentPatch getComponentsPatch();

	/**
	 * @return The {@link DataComponentMap} of this variant.
	 */
	@Override
	DataComponentMap getComponents();

	/**
	 * Return true if this variant has a component patch.
	 */
	default boolean hasComponents() {
		return !getComponentsPatch().isEmpty();
	}

	/**
	 * Return true if the tag of this variant matches the passed tag, and false otherwise.
	 *
	 * <p>Note: True is returned if both tags are {@code null}.
	 */
	default boolean componentsMatch(DataComponentPatch other) {
		return Objects.equals(getComponentsPatch(), other);
	}

	/**
	 * Return {@code true} if the object of this variant matches the passed fluid.
	 */
	default boolean isOf(O object) {
		return getObject() == object;
	}

	/**
	 * Creates a copy of this TransferVariant with the provided component patch applied.
	 * @param patch the patch to apply
	 * @return the new variant with the patch applied
	 */
	default TransferVariant<O> withComponents(DataComponentPatch patch) {
		throw new UnsupportedOperationException("withComponents is not supported by this TransferVariant");
	}
}
