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
 * The Fabric Loot API for manipulating and creating loot tables.
 *
 * <h2>Events</h2>
 * {@link net.fabricmc.fabric.api.loot.v2.LootTableEvents} has events to modify existing loot tables,
 * or outright replace them with a new loot table.
 *
 * <p>You can also check where loot tables are coming from in those events with
 * {@link net.fabricmc.fabric.api.loot.v2.LootTableSource}. This is useful when you only want to modify
 * loot tables from mods or vanilla, but not user-created data packs.
 *
 * <h2>Extended loot table and pool builders</h2>
 * This API has injected interfaces to add useful methods to
 * {@linkplain net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder loot table} and
 * {@linkplain net.fabricmc.fabric.api.loot.v2.FabricLootPoolBuilder loot pool} builders.
 * They let you add pre-built objects instead of builders, and collections of objects to the builder
 * with one method call.
 */
package net.fabricmc.fabric.api.loot.v2;
