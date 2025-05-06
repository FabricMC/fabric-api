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

package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;

@Mixin({
		/* public */
		ServerRecipeManager.class, ServerAdvancementLoader.class, FunctionLoader.class
		/* private */
})
public abstract class KeyedResourceReloadListenerMixin implements IdentifiableResourceReloadListener {
	@Unique
	private Identifier id;
	@Unique
	private Collection<Identifier> dependencies;

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Identifier getFabricId() {
		if (this.id == null) {
			Object self = this;

			if (self instanceof ServerRecipeManager) {
				this.id = ResourceReloadListenerKeys.RECIPES;
			} else if (self instanceof ServerAdvancementLoader) {
				this.id = ResourceReloadListenerKeys.ADVANCEMENTS;
			} else if (self instanceof FunctionLoader) {
				this.id = ResourceReloadListenerKeys.FUNCTIONS;
			} else {
				this.id = Identifier.ofVanilla("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions", "RedundantCast"})
	public Collection<Identifier> getFabricDependencies() {
		if (this.dependencies == null) {
			this.dependencies = Collections.emptyList();
		}

		return this.dependencies;
	}
}
