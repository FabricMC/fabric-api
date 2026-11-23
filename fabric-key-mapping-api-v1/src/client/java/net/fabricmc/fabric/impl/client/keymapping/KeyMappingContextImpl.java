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

package net.fabricmc.fabric.impl.client.keymapping;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;

public class KeyMappingContextImpl {
	public static final KeyMappingContext IN_GAME = new KeyMappingContext() {
		@Override
		public boolean isActive(Minecraft client) {
			return client.gui.screen() == null;
		}

		@Override
		public boolean conflicts(KeyMappingContext other) {
			return this == other;
		}
	};

	public static final KeyMappingContext IN_SCREEN = new KeyMappingContext() {
		@Override
		public boolean isActive(Minecraft client) {
			return client.gui.screen() != null;
		}

		@Override
		public boolean conflicts(KeyMappingContext other) {
			return this == other;
		}
	};

	public static final KeyMappingContext ALL = new KeyMappingContext() {
		@Override
		public boolean isActive(Minecraft client) {
			return true;
		}

		@Override
		public boolean conflicts(KeyMappingContext other) {
			return true;
		}
	};

	public record ScreenContext(List<Class<? extends Screen>> screenClasses) implements KeyMappingContext {
		@Override
		public boolean isActive(Minecraft client) {
			Screen screen = client.gui.screen();

			for (Class<? extends Screen> screenClass : screenClasses) {
				if (screenClass.isInstance(screen)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean conflicts(KeyMappingContext other) {
			if (other == IN_SCREEN) {
				return true;
			}

			if (other instanceof ScreenContext(List<Class<? extends Screen>> classes)) {
				for (Class<? extends Screen> screenClass : screenClasses) {
					for (Class<? extends Screen> otherClass : classes) {
						if (screenClass.isAssignableFrom(otherClass) || otherClass.isAssignableFrom(screenClass)) {
							return true;
						}
					}
				}
			}

			return false;
		}
	}
}
