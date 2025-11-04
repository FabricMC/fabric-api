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

package net.fabricmc.fabric.impl.object.builder.client;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class SignTypeTextureHelper {
	/**
	 * Set to true after {@link Sheets} has been classloaded. If any new {@link WoodType}s are registered
	 * after this point, they need to be added to the texture maps manually. Always adding textures manually classloads
	 * {@link Sheets} too early, which causes issues such as decorated pot pattern textures not being
	 * initialized correctly.
	 */
	public static boolean shouldAddTextures = false;
}
