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

/// # The Transfer API, version 1.
///
/// This module provides common facilities for the transfer of fluids and other game resources.
/// ## Transactions
/// The [Transaction][net.fabricmc.fabric.api.transfer.v1.transaction.Transaction] system provides a
/// scope that can be used to simulate any number of transfer operations, and then cancel or validate all of them at once.
/// One can think of transactions as video game checkpoints. A more detailed explanation can be found in the class javadoc of `Transaction`.
/// Every transfer operation requires a `Transaction` parameter.
/// [SnapshotParticipant][net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant]
/// is the reference implementation of a "participant", that is an object participating in a transaction.
/// ## Storages
/// A [Storage&lt;T&gt;][net.fabricmc.fabric.api.transfer.v1.storage.Storage] is any object that can store resources of type `T`.
/// Its contents can be read, and resources can be inserted into it or extracted from it.
/// [StorageUtil][net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil] provides a few helpful function to work with `Storage`s,
/// for example to move resources between two `Storage`s.
/// The [storage/base package][net.fabricmc.fabric.api.transfer.v1.storage.base] provides a few helpers to accelerate
/// implementation of `Storage&lt;T&gt;`.
/// Usage of [StoragePreconditions][net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions] is recommended to detect
/// wrong usage of `Storage` and `StorageView` methods.
///
/// Implementors of transfer variant storages with a fixed number of "slots" or "tanks" can use
/// [SingleVariantStorage][net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage],
/// and combine them with [CombinedStorage][net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage].
/// ## Fluid transfer
/// A `Storage<FluidVariant>` is any object that can store fluids. It is just a `Storage<T>`, where `T` is
/// [FluidVariant][net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant], the immutable combination of a `Fluid` and additional components.
/// Instances can be accessed through the API lookups defined in [FluidStorage][net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage].
///
/// The amount for fluid transfer is droplets, that is 1/81000ths of a bucket.
/// [FluidConstants][net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants] contains a few helpful constants to work with droplets.
///
/// Client-side [fluid variant rendering][net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering] will use regular fluid rendering by default,
/// ignoring the additional components.
/// `Fluid`s that wish to render differently depending on the stored components can register a
/// [FluidVariantRenderHandler][net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler].
/// ## Item transfer
/// A `Storage<ItemVariant>` is any object that can store items.
/// Instances can be accessed through the API lookup defined in [ItemStorage][net.fabricmc.fabric.api.transfer.v1.item.ItemStorage].
///
/// The lookup already provides compatibility with vanilla inventories, however it may sometimes be interesting to use
/// [ContainerStorage][net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage] or
/// [PlayerInventoryStorage][net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage] when interaction with
/// [Container][net.minecraft.world.Container]-based APIs is required.
/// ## `ContainerItemContext`
/// [ContainerItemContext][net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext] is a context designed for `ItemApiLookup` queries
/// that allows the returned APIs to interact with the container.
/// Notably, it is used by the `FluidStorage.ITEM` lookup for fluid-containing items.
@NullMarked
package net.fabricmc.fabric.api.transfer.v1;

import org.jspecify.annotations.NullMarked;
