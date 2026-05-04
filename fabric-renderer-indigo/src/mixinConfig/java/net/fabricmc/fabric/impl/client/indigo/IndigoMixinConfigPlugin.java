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

package net.fabricmc.fabric.impl.client.indigo;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class IndigoMixinConfigPlugin implements IMixinConfigPlugin {
	/** Set by other renderers to disable loading of Indigo. */
	private static final String JSON_KEY_DISABLE_INDIGO = "fabric-renderer-api-v1:contains_renderer";

	private static boolean needsLoad = true;

	private static boolean indigoApplicable = true;

	private static void loadIfNeeded() {
		if (needsLoad) {
			for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
				final ModMetadata meta = container.getMetadata();

				if (meta.containsCustomValue(JSON_KEY_DISABLE_INDIGO)) {
					indigoApplicable = false;
				}
			}

			needsLoad = false;
		}
	}

	static boolean shouldApplyIndigo() {
		loadIfNeeded();
		return indigoApplicable;
	}

	@Override
	public void onLoad(String mixinPackage) { }

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return shouldApplyIndigo();
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
