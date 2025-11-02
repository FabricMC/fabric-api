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

package net.fabricmc.fabric.mixin.recipe.sync;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.ClientConnection;
import net.minecraft.recipe.RecipeSerializer;

import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwareClientConnection;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements SyncedSerializerAwareClientConnection {
	@Unique
	private Set<RecipeSerializer<?>> syncedRecipeSerializers = Set.of();

	@Override
	public void fabric_setSyncedRecipeSerializers(Set<RecipeSerializer<?>> syncedRecipeSerializers) {
		this.syncedRecipeSerializers = syncedRecipeSerializers;
	}

	@Override
	public Set<RecipeSerializer<?>> fabric_getSyncedRecipeSerializers() {
		return syncedRecipeSerializers;
	}
}
