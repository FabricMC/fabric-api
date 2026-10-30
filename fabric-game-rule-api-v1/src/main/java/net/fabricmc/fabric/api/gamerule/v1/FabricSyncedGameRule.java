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

package net.fabricmc.fabric.api.gamerule.v1;

/**
 * A game rule that can be queried from the client. It will be synced to the client when joining a {@link net.minecraft.server.level.ServerLevel} or when the value is changed.
 * @see FabricSyncedGameRulesList
 * @see GameRuleBuilder#synced()
 */
public interface FabricSyncedGameRule {
	/**
	 * @return whether this game rule is synced.
	 */
	default boolean isSynced() {
		throw new AssertionError("Implemented via Mixin");
	}
}
