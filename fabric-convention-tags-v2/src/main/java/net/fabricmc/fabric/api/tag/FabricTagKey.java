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

package net.fabricmc.fabric.api.tag;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/**
 * General-purpose Fabric-provided extensions for {@link TagKey} subclasses.
 *
 * <p>These extensions were designed primarily for giving extra utility methods for TagKeys usages.
 * Getting a TagKey's translation key for example.
 *
 * <p>Note: This interface is automatically implemented on all {@link TagKey} instances via Mixin and interface injection.
 */
public interface FabricTagKey {
	/**
	 * Use this to get a TagKey's translation key safely on any side.
	 *
	 * <p>Format for vanilla registry TagKeys is:
	 * tag.(registry_path).(tag_namespace).(tag_path)
	 *
	 * <p>Format for modded registry TagKeys is:
	 * tag.(registry_namespace).(registry_path).(tag_namespace).(tag_path)
	 *
	 * <p>The registry's path and tag path's slashes will be converted to periods.
	 *
	 * @return the translation key for a TagKey
	 */
	default String getTranslationKey() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("tag.");

		TagKey<?> tagKey = (TagKey<?>) this;
		Identifier registryIdentifier = tagKey.registry().identifier();
		Identifier tagIdentifier = tagKey.location();

		if (!registryIdentifier.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			stringBuilder.append(registryIdentifier.getNamespace())
					.append(".");
		}

		stringBuilder.append(registryIdentifier.getPath().replace("/", "."))
				.append(".")
				.append(tagIdentifier.getNamespace())
				.append(".")
				.append(tagIdentifier.getPath().replace("/", ".").replace(":", "."));

		return stringBuilder.toString();
	}

	/**
	 * Use this to get a TagKey's translatable text for display purposes.
	 *
	 * <p>The text uses the result of {@link TagKey#getTranslationKey} for the translation key
	 * and will fall back to displaying #tag_namespace:tag_path if no translation exists.
	 *
	 * @return the translatable text for a TagKey
	 */
	default Component getName() {
		return Component.translatableWithFallback(getTranslationKey(), "#" + ((TagKey<?>) this).location().toString());
	}
}
