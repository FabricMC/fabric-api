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

package net.fabricmc.fabric.impl.debug.client.renderer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererFactory;

public final class DebugRendererRegistryImpl {
	public static final Set<Entry> RENDERERS = new HashSet<>();

	public static <T> void register(
			DebugSubscription<T> debugSubscription,
			DebugRendererFactory rendererFactory
	) {
		RENDERERS.add(new Entry(debugSubscription, rendererFactory));
	}

	public record Entry(
			DebugSubscription<?> debugSubscription,
			DebugRendererFactory rendererFactory
	) {
		// Ensure DebugSubscriptions with different values don't both
		// get into the Set, causing undesirable behavior

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			Entry entry = (Entry) o;
			return Objects.equals(
					debugSubscription,
					entry.debugSubscription
			);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(debugSubscription);
		}
	}
}
