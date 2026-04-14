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

import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;

/**
 * Interface-injected to {@link net.minecraft.data.tags.TagAppender}.
 */
@SuppressWarnings("unchecked")
public interface FabricTagAppender<T> {
	/**
	 * Sets the value of the {@code replace} flag. When set to {@code true}
	 * this tag will replace contents of any other tag.
	 * @param replace whether to replace the contents of the tag
	 * @return this, for chaining
	 */
	default TagAppender<T> setReplace(boolean replace) {
		return (TagAppender<T>) this;
	}

	default TagAppender<T> forceAddTag(TagKey<T> tag) {
		return (TagAppender<T>) this;
	}
}
