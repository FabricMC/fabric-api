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
 * Provides a way of conditionally loading JSON-based resources. By default, this can
 * be used with recipes, advancements, loot tables, predicates, and item modifiers.
 * Conditions are identified by an identifier and registered at {@link
 * net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions}.
 *
 * <h2>JSON format</h2>
 *
 * <p>Add an array with the {@code fabric:load_conditions} key to the JSON file:
 * <pre>{@code
 * {
 *   "type": "minecraft:crafting_shapeless",
 *   "ingredients": [
 *     {
 *       "item": "minecraft:dirt"
 *     }
 *   ],
 *   "result": {
 *     "item": "minecraft:diamond"
 *   },
 *   "fabric:load_conditions": [
 *     {
 *       "condition": "<insert condition ID here>",
 *       // values of the condition
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>Unknown/invalid conditions will be skipped and considered successful.
 *
 * <h2>Data generation integration</h2>
 *
 * <p>Fabric Data Generation API supports adding a {@link
 * net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition} to a generated file.
 * Please check the documentation of the Data Generation API.
 */
@NullMarked
package net.fabricmc.fabric.api.resource.conditions.v1;

import org.jspecify.annotations.NullMarked;
