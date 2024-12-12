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

/**
 * <h1>The Transfer API, version 1.</h1>
 *
 * <p>This module provides common facilities for the transfer of fluids and other game resources.
 *
 * <h2>Transactions</h2>
 * The {@link net.fabricmc.fabric.api.transfer.v1.transaction.Transaction Transaction} system provides a
 * scope that can be used to simulate any number of transfer operations, and then cancel or validate all of them at once.
 * One can think of transactions as video game checkpoints. A more detailed explanation can be found in the class javadoc of {@code Transaction}.
 * Every transfer operation requires a {@code Transaction} parameter.
 * {@link net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant SnapshotParticipant}
 * is the reference implementation of a "participant", that is an object participating in a transaction.
 *
 * <h2>Storages</h2>
 * A {@link net.fabricmc.fabric.api.transfer.v1.storage.Storage Storage&lt;T&gt;} is any object that can store resources of type {@code T}.
 * Its contents can be read, and resources can be inserted into it or extracted from it.
 * {@link net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil StorageUtil} provides a few helpful function to work with {@code Storage}s,
 * for example to move resources between two {@code Storage}s.
 * The {@link net.fabricmc.fabric.api.transfer.v1.storage.base storage/base package} provides a few helpers to accelerate
 * implementation of {@code Storage&lt;T&gt;}.
 * Usage of {@link net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions StoragePreconditions} is recommended to detect
 * wrong usage of {@code Storage} and {@code StorageView} methods.
 *
 * <p>Implementors of transfer variant storages with a fixed number of "slots" or "tanks" can use
 * {@link net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage SingleVariantStorage},
 * and combine them with {@link net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage CombinedStorage}.
 *
 * <h2>Fluid transfer</h2>
 * A {@code Storage<FluidVariant>} is any object that can store fluids. It is just a {@code Storage<T>}, where {@code T} is
 * {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant FluidVariant}, the immutable combination of a {@code Fluid} and additional components.
 * Instances can be accessed through the API lookups defined in {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage FluidStorage}.
 *
 * <p>The amount for fluid transfer is droplets, that is 1/81000ths of a bucket.
 * {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants FluidConstants} contains a few helpful constants to work with droplets.
 *
 * <p>Client-side {@linkplain net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering fluid variant rendering} will use regular fluid rendering by default,
 * ignoring the additional components.
 * {@code Fluid}s that wish to render differently depending on the stored components can register a
 * {@link net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler FluidVariantRenderHandler}.
 *
 * <h2>Item transfer</h2>
 * A {@code Storage<ItemVariant>} is any object that can store items.
 * Instances can be accessed through the API lookup defined in {@link net.fabricmc.fabric.api.transfer.v1.item.ItemStorage ItemStorage}.
 *
 * <p>The lookup already provides compatibility with vanilla inventories, however it may sometimes be interesting to use
 * {@link net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage InventoryStorage} or
 * {@link net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage PlayerInventoryStorage} when interaction with
 * {@code Inventory}-based APIs is required.
 *
 * <h2>{@code ContainerItemContext}</h2>
 * {@link net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext ContainerItemContext} is a context designed for {@code ItemApiLookup} queries
 * that allows the returned APIs to interact with the containing inventory.
 * Notably, it is used by the {@code FluidStorage.ITEM} lookup for fluid-containing items.
 */
package net.fabricmc.fabric.api.transfer.v1;
