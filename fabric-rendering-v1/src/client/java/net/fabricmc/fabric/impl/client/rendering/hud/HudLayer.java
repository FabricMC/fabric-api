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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.function.Function;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

public interface HudLayer {
	Identifier id();

	HudElement element(HudElement vanillaElement);

	boolean isRemoved();

	static HudLayer ofVanilla(Identifier id) {
		return of(id, Function.identity(), false);
	}

	static HudLayer ofElement(Identifier id, HudElement element) {
		return of(id, $ -> element, false);
	}

	static HudLayer of(Identifier id, Function<HudElement, HudElement> operator, boolean isRemoved) {
		return new HudLayer() {
			@Override
			public Identifier id() {
				return id;
			}

			@Override
			public HudElement element(HudElement vanillaElement) {
				return operator.apply(vanillaElement);
			}

			@Override
			public boolean isRemoved() {
				return isRemoved;
			}
		};
	}
}
