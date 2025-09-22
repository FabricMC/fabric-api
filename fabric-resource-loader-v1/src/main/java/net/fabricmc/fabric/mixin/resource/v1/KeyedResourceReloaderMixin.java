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

package net.fabricmc.fabric.mixin.resource.v1;

import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.v1.FabricResourceReloader;

@Mixin({
		/* public */
		ServerRecipeManager.class, ServerAdvancementLoader.class, FunctionLoader.class
		/* private */
})
public abstract class KeyedResourceReloaderMixin implements FabricResourceReloader {
	@Unique
	private Identifier id;

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public Identifier fabric$getId() {
		if (this.id == null) {
			Object self = this;

			if (self instanceof ServerRecipeManager) {
				this.id = ResourceReloaderKeys.Server.RECIPES;
			} else if (self instanceof ServerAdvancementLoader) {
				this.id = ResourceReloaderKeys.Server.ADVANCEMENTS;
			} else if (self instanceof FunctionLoader) {
				this.id = ResourceReloaderKeys.Server.FUNCTIONS;
			} else {
				this.id = Identifier.ofVanilla("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.id;
	}
}
