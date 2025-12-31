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

/// # The API Lookup, version 1.
///
/// This module allows API instances to be associated with game objects without specifying how the association is implemented.
/// This is useful when the same API could be implemented more than once or implemented in different ways.
/// ## Definitions and purpose
///
///   - What we call an _API_ is any object that can be offered or queried, possibly by different mods, to be used in an agreed-upon manner.
///   - This module allows flexible retrieving of such APIs, represented by the generic type `A`, from blocks in the level or from item stacks.
///   - It also provides building blocks for defining custom ways of retrieving APIs from other game objects.
///
/// ## Retrieving APIs from blocks in the level
///
///   - A block query for an API is an operation that takes a level, a block position, and additional context of type `C`, and uses that
///     to find an object of type `A`, or `null` if there was no such object.
///   - An instance of [BlockApiLookup&lt;A, C&gt;][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup]
///     provides a [find()][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#find] function that does exactly that.
///   - It also allows registering APIs for blocks, because for the query to work the API must be registered first.
///     Registration primarily happens through [registerSelf()][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerSelf],
///     [registerForBlocks()][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerForBlocks]
///     and [registerForBlockEntities()][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerForBlockEntities].
/// 	     - `BlockApiLookup` instances can be accessed through [BlockApiLookup#get()][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#get].
///     For optimal performance, it is better to store them in a `public static final` field instead of querying them multiple times.
///   - See [BlockApiLookup][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup] for example code.
///
/// ## Retrieving APIs from item stacks
///
///   - Item API queries work similarly to block queries.
///   - An item query for an API is an operation that takes an item stack, and additional context of type `C`, and uses that
///     to find an object of type `A`, `null` if there was no such object.
///   - [ItemApiLookup&lt;A, C&gt;][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup] instances
///     provide a [find()][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#find] function that does exactly that,
///     and registration happens primarily through [registerSelf()][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#registerSelf] and
///     [registerForItems()][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#registerForItems].
///   - These instances can be accessed through [ItemApiLookup#get()][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#get]
///     and should be stored in a `public static final` field.
///   - See [ItemApiLookup][net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup] for example code.
///
/// ## Retrieving APIs from entities
///
///   - A query for an entity API takes an entity and additional context of type `C`,
///     and uses that to find an object of type `A`, or  `null` if there's no such object.
///   - [EntityApiLookup&lt;A, C&gt;][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup] instances provide a
///     [find()][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#find] function that does the query, and registration happens
///     primarily through [registerSelf()][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#registerSelf] and
///     [registerForTypes()][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#registerForTypes].
///   - These instances can be accessed through [EntityApiLookup#get()][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#get]
///     and should be stored in a `public static final` field.
///   - See [EntityApiLookup][net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup] for example code.
///
/// ## Retrieving APIs from custom game objects
///
///   - The subpackage `custom` provides helper classes to accelerate implementations of `ApiLookup`s for custom objects,
///     similar to the existing [BlockApiLookup][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup], but with different query parameters.
///   - [ApiLookupMap][net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap] is a map meant to be used as the backing storage for custom `ApiLookup` instances,
///     to implement a custom equivalent of [BlockApiLookup#get][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#get].
///   - [ApiProviderMap][net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap] is a fast thread-safe copy-on-write map meant to be used as the backing storage for registered providers.
///   - See [ApiLookupMap][net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap] for example code.
///
@NullMarked
package net.fabricmc.fabric.api.lookup.v1;

import org.jspecify.annotations.NullMarked;
