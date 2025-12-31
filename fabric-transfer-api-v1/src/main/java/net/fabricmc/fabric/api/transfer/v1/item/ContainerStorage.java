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

package net.fabricmc.fabric.api.transfer.v1.item;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.item.ContainerStorageImpl;

/// An implementation of `Storage<ItemVariant>` for vanilla's [Container], [net.minecraft.world.WorldlyContainer] and [net.minecraft.world.entity.player.Inventory].
///
/// [Container] is often nicer to implement than `Storage<ItemVariant>`, but harder to use for item transfer.
/// This wrapper allows one to have the best of both worlds, for example by storing a subclass of [net.minecraft.world.SimpleContainer] in a block entity class,
/// while exposing it as a `Storage<ItemVariant>` to {@linkplain ItemStorage#SIDED the item transfer API}.
///
/// In particular, note that [#getSlots] can be combined with [net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage] to retrieve a wrapper around a specific range of slots.
///
/// **Important note:** This wrapper assumes that the container owns its slots.
/// If the container does not own its slots, for example because it delegates to another container, this wrapper should not be used!
@ApiStatus.NonExtendable
public interface ContainerStorage extends SlottedStorage<ItemVariant> {
	/// Return a wrapper around an [Container].
	///
	/// If the container is a [net.minecraft.world.WorldlyContainer] and the direction is nonnull, the wrapper wraps the sided container from the given direction.
	/// The returned wrapper contains only the slots with the indices returned by [net.minecraft.world.WorldlyContainer#getSlotsForFace] at query time.
	///
	/// @param container The container to wrap.
	/// @param direction The direction to use if the access is sided, or `null` if the access is not sided.
	static ContainerStorage of(Container container, @Nullable Direction direction) {
		Objects.requireNonNull(container, "Null container is not supported.");
		return ContainerStorageImpl.of(container, direction);
	}

	/// Retrieve an unmodifiable list of the wrappers for the slots in this container.
	/// Each wrapper corresponds to a single slot in the container.
	@Override
	@UnmodifiableView
	List<SingleSlotStorage<ItemVariant>> getSlots();

	@Override
	default int getSlotCount() {
		return getSlots().size();
	}

	@Override
	default SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return getSlots().get(slot);
	}
}
