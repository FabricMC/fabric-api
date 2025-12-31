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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.impl.transfer.item.BundleContentsStorage;
import net.fabricmc.fabric.impl.transfer.item.ComposterWrapper;
import net.fabricmc.fabric.impl.transfer.item.ItemContainerContentsStorage;
import net.fabricmc.fabric.mixin.transfer.CompoundContainerAccessor;

/// Access to [Storage&lt;ItemVariant&gt;][Storage] instances.
public final class ItemStorage {
	/// Sided block access to item variant storages.
	/// The `Direction` parameter may be null, meaning that the full storage (ignoring side restrictions) should be queried.
	/// Refer to [BlockApiLookup] for documentation on how to use this field.
	///
	/// When the operations supported by a storage change,
	/// that is if the return value of [Storage#supportsInsertion] or [Storage#supportsExtraction] changes,
	/// the storage should notify its neighbors with a block update so that they can refresh their connections if necessary.
	///
	/// Block entities directly implementing [Container] or [WorldlyContainer] are automatically handled by a fallback provider,
	/// and don't need to do anything.
	/// Blocks that implement [WorldlyContainerHolder] and whose returned container is constant (it's the same for two subsequent calls)
	/// are also handled automatically and don't need to do anything.
	/// The fallback provider assumes that the [Container] "owns" its contents. If that's not the case,
	/// for example because it redirects all function calls to another container, then implementing [Container] should be avoided.
	///
	/// Hoppers and droppers will interact with storages exposed through this lookup, thus implementing one of the vanilla APIs is not necessary.
	///
	/// Depending on the use case, the following strategies can be used to offer a `Storage<ItemVariant>` implementation:
	///
	///   - Directly implementing [Container] or [WorldlyContainer] on a block entity - it will be wrapped automatically.
	///   - Storing a container inside a block entity field, and converting it manually with [ContainerStorage#of].
	///     [net.minecraft.world.SimpleContainer] can be used for easy implementation.
	///   - [net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage] can also be used for more flexibility. Multiple of them can be combined with [net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage].
	///   - Directly providing a custom implementation of `Storage<ItemVariant>` is also possible.
	///
	///
	/// A simple way to expose item variant storages for a block entity hierarchy is to extend [SidedStorageBlockEntity].
	///
	/// This may be queried safely both on the logical server and on the logical client threads.
	/// On the server thread (i.e. with a server level), all transfer functionality is always supported.
	/// On the client thread (i.e. with a client level), contents of queried Storages are unreliable and should not be modified.
	public static final BlockApiLookup<Storage<ItemVariant>, @Nullable Direction> SIDED =
			BlockApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "sided_item_storage"), Storage.asClass(), Direction.class);

	/// Item access to item variant storages.
	/// Querying should happen through [ContainerItemContext#find].
	///
	/// This may be queried both client-side and server-side.
	/// Returned APIs should behave the same regardless of the logical side.
	public static final ItemApiLookup<Storage<ItemVariant>, ContainerItemContext> ITEM =
			ItemApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "item_storage"), Storage.asClass(), ContainerItemContext.class);

	private ItemStorage() {
	}

	static {
		// Composter support.
		ItemStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, direction) -> ComposterWrapper.get(level, pos, direction), Blocks.COMPOSTER);

		// Support for SidedStorageBlockEntity.
		ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
			if (blockEntity instanceof SidedStorageBlockEntity sidedStorageBlockEntity) {
				return sidedStorageBlockEntity.getItemStorage(direction);
			}

			return null;
		});

		// Register container fallback.
		ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
			Container containerToWrap = null;

			if (state.getBlock() instanceof WorldlyContainerHolder provider) {
				WorldlyContainer first = provider.getContainer(state, level, pos);
				WorldlyContainer second = provider.getContainer(state, level, pos);

				// Hopefully we can trust the sided container not to change.
				if (first == second && first != null) {
					return ContainerStorage.of(first, direction);
				}
			}

			if (blockEntity instanceof Container container) {
				if (blockEntity instanceof ChestBlockEntity && state.getBlock() instanceof ChestBlock chestBlock) {
					containerToWrap = ChestBlock.getContainer(chestBlock, state, level, pos, true);

					// For double chests, we need to retrieve a wrapper for each part separately.
					if (containerToWrap instanceof CompoundContainerAccessor accessor) {
						SlottedStorage<ItemVariant> first = ContainerStorage.of(accessor.fabric_getContainer1(), direction);
						SlottedStorage<ItemVariant> second = ContainerStorage.of(accessor.fabric_getContainer2(), direction);

						return new CombinedSlottedStorage<>(List.of(first, second));
					}
				} else {
					containerToWrap = container;
				}
			}

			return containerToWrap != null ? ContainerStorage.of(containerToWrap, direction) : null;
		});

		ItemStorage.ITEM.registerForItems(
				(itemStack, context) -> new ItemContainerContentsStorage(context, 27),
				Items.SHULKER_BOX,
				Items.WHITE_SHULKER_BOX,
				Items.ORANGE_SHULKER_BOX,
				Items.MAGENTA_SHULKER_BOX,
				Items.LIGHT_BLUE_SHULKER_BOX,
				Items.YELLOW_SHULKER_BOX,
				Items.LIME_SHULKER_BOX,
				Items.PINK_SHULKER_BOX,
				Items.GRAY_SHULKER_BOX,
				Items.LIGHT_GRAY_SHULKER_BOX,
				Items.CYAN_SHULKER_BOX,
				Items.PURPLE_SHULKER_BOX,
				Items.BLUE_SHULKER_BOX,
				Items.BROWN_SHULKER_BOX,
				Items.GREEN_SHULKER_BOX,
				Items.RED_SHULKER_BOX,
				Items.BLACK_SHULKER_BOX
		);

		ItemStorage.ITEM.registerForItems(
				(itemStack, context) -> new BundleContentsStorage(context),
				Items.BUNDLE,
				Items.WHITE_BUNDLE,
				Items.ORANGE_BUNDLE,
				Items.MAGENTA_BUNDLE,
				Items.LIGHT_BLUE_BUNDLE,
				Items.YELLOW_BUNDLE,
				Items.LIME_BUNDLE,
				Items.PINK_BUNDLE,
				Items.GRAY_BUNDLE,
				Items.LIGHT_GRAY_BUNDLE,
				Items.CYAN_BUNDLE,
				Items.PURPLE_BUNDLE,
				Items.BLUE_BUNDLE,
				Items.BROWN_BUNDLE,
				Items.GREEN_BUNDLE,
				Items.RED_BUNDLE,
				Items.BLACK_BUNDLE
		);
	}
}
