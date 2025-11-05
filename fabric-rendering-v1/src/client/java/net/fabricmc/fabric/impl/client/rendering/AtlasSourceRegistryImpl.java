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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.Objects;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.mixin.client.rendering.SpriteSourcesAccessor;

public final class AtlasSourceRegistryImpl {
	private AtlasSourceRegistryImpl() {
	}

	public static void register(Identifier id, MapCodec<? extends SpriteSource> codec) {
		Objects.requireNonNull(id, "id must not be null!");
		Objects.requireNonNull(codec, "codec must not be null!");
		SpriteSourcesAccessor.getAtlasSourceCodecs().put(id, codec);
	}
}
