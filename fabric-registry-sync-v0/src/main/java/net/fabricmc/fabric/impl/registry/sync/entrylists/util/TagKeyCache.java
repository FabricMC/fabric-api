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

package net.fabricmc.fabric.impl.registry.sync.entrylists.util;

import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.registry.tag.TagKey;

public class TagKeyCache {
	// a weak immutable set containing all the TagKey instances in the game
	private static Set<TagKey<?>> CACHE;

	public static void acceptCache(Set<TagKey<?>> cache) {
		CACHE = cache;
	}

	static Stream<TagKey<?>> stream() {
		return CACHE.stream();
	}
}
