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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.Collection;
import java.util.stream.Stream;

import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

/**
 * Interface-injected to {@link TagProvider.ProvidedTagBuilder}.
 */
@SuppressWarnings("unchecked")
public interface FabricProvidedTagBuilder {
	/**
	 * Sets the value of the {@code replace} flag. When set to {@code true}
	 * this tag will replace contents of any other tag.
	 * @param replace whether to replace the contents of the tag
	 * @return this, for chaining
	 */
	default FabricProvidedTagBuilder setReplace(boolean replace) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Forces a tag key into the tag, bypassing any errors resulting from the
	 * tag not existing at runtime.
	 * @param tag The tag to force into the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder forceAddTag(TagKey<T> tag) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Removes an entry from the tag.
	 * @param element The entry to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder remove(RegistryKey<T> element) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder remove(final RegistryKey<T>... elements) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder removeAll(final Collection<T> elements) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Removes multiple entries from the tag.
	 * @param elements The entries to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder removeAll(final Stream<T> elements) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Removes all entries of the specified tag from the tag.
	 * @param tag The tag to remove from the contents of the tag
	 * @return this, for chaining
	 */
	default <T> FabricProvidedTagBuilder removeTag(TagKey<T> tag) {
		throw new AssertionError("Implemented via mixin");
	}
}
