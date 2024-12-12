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
 * <h1>The API Lookup, version 1.</h1>
 *
 * <p>This module allows API instances to be associated with game objects without specifying how the association is implemented.
 * This is useful when the same API could be implemented more than once or implemented in different ways.</p>
 *
 * <h2>Definitions and purpose</h2>
 * <ul>
 *     <li>What we call an <i>API</i> is any object that can be offered or queried, possibly by different mods, to be used in an agreed-upon manner.</li>
 *     <li>This module allows flexible retrieving of such APIs, represented by the generic type {@code A}, from blocks in the world or from item stacks.</li>
 *     <li>It also provides building blocks for defining custom ways of retrieving APIs from other game objects.</li>
 * </ul>
 *
 * <h2>Retrieving APIs from blocks in the world</h2>
 * <ul>
 *     <li>A block query for an API is an operation that takes a world, a block position, and additional context of type {@code C}, and uses that
 *     to find an object of type {@code A}, or {@code null} if there was no such object.</li>
 *     <li>An instance of {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup BlockApiLookup&lt;A, C&gt;}
 *     provides a {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#find find()} function that does exactly that.</li>
 *     <li>It also allows registering APIs for blocks, because for the query to work the API must be registered first.
 *     Registration primarily happens through {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerSelf registerSelf()},
 *     {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerForBlocks registerForBlocks()}
 *     and {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#registerForBlockEntities registerForBlockEntities()}.</li>
 * 	   <li>{@code BlockApiLookup} instances can be accessed through {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#get BlockApiLookup#get()}.
 *     For optimal performance, it is better to store them in a {@code public static final} field instead of querying them multiple times.</li>
 *     <li>See {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup BlockApiLookup} for example code.</li>
 * </ul>
 *
 * <h2>Retrieving APIs from item stacks</h2>
 * <ul>
 *     <li>Item API queries work similarly to block queries.</li>
 *     <li>An item query for an API is an operation that takes an item stack, and additional context of type {@code C}, and uses that
 *     to find an object of type {@code A}, {@code null} if there was no such object.</li>
 *     <li>{@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup ItemApiLookup&lt;A, C&gt;} instances
 *     provide a {@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#find find()} function that does exactly that,
 *     and registration happens primarily through {@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#registerSelf registerSelf()} and
 *     {@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#registerForItems registerForItems()}.</li>
 *     <li>These instances can be accessed through {@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup#get ItemApiLookup#get()}
 *     and should be stored in a {@code public static final} field.</li>
 *     <li>See {@link net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup ItemApiLookup} for example code.</li>
 * </ul>
 *
 * <h2>Retrieving APIs from entities</h2>
 * <ul>
 *     <li>A query for an entity API takes an entity and additional context of type {@code C},
 *     and uses that to find an object of type {@code A}, or  {@code null} if there's no such object.</li>
 *     <li>{@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup EntityApiLookup&lt;A, C&gt;} instances provide a
 *     {@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#find find()} function that does the query, and registration happens
 *     primarily through {@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#registerSelf registerSelf()} and
 *     {@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#registerForTypes registerForTypes()}.</li>
 *     <li>These instances can be accessed through {@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup#get EntityApiLookup#get()}
 *     and should be stored in a {@code public static final} field.</li>
 *     <li>See {@link net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup EntityApiLookup} for example code.</li>
 * </ul>
 *
 * <h2>Retrieving APIs from custom game objects</h2>
 * <ul>
 *     <li>The subpackage {@code custom} provides helper classes to accelerate implementations of {@code ApiLookup}s for custom objects,
 * similar to the existing {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup BlockApiLookup}, but with different query parameters.</li>
 *     <li>{@link net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap ApiLookupMap} is a map meant to be used as the backing storage for custom {@code ApiLookup} instances,
 *     to implement a custom equivalent of {@link net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#get BlockApiLookup#get}.</li>
 *     <li>{@link net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap ApiProviderMap} is a fast thread-safe copy-on-write map meant to be used as the backing storage for registered providers.</li>
 *     <li>See {@link net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap ApiLookupMap} for example code.</li>
 * </ul>
 */
package net.fabricmc.fabric.api.lookup.v1;
