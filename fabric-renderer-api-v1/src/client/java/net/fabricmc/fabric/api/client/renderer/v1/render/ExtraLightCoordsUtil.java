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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.util.LightCoordsUtil;

// TODO FRAPI 26.1: docs
public final class ExtraLightCoordsUtil {
	private ExtraLightCoordsUtil() {
	}

	public static int smoothMax(final int coords1, final int coords2) {
		int block1 = LightCoordsUtil.smoothBlock(coords1);
		int block2 = LightCoordsUtil.smoothBlock(coords2);
		int sky1 = LightCoordsUtil.smoothSky(coords1);
		int sky2 = LightCoordsUtil.smoothSky(coords2);
		return LightCoordsUtil.smoothPack(Math.max(block1, block2), Math.max(sky1, sky2));
	}
}
