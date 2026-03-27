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

import net.minecraft.client.renderer.MultiBufferSource;

// Forces javac to generate a bridge method in LevelRenderContext returning MultiBufferSource,
// allowing code compiled against the old LevelRenderContext, where this method returned
// MultiBufferSource, to still run. Should be removed as soon as we're allowed to make breaking
// changes.
@Deprecated(forRemoval = true)
public interface LevelRenderContextBackwardsCompatHack {
	MultiBufferSource bufferSource();
}
