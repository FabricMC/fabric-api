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

package net.fabricmc.fabric.mixin.resource;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.world.level.DataPackConfig;

import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;

/**
 * @see ModPackResourcesUtil#createTestServerSettings
 */
@Mixin(GameTestServer.class)
public class GameTestServerMixin {
	@Redirect(method = "create", at = @At(value = "NEW", target = "(Ljava/util/List;Ljava/util/List;)Lnet/minecraft/world/level/DataPackConfig;"))
	private static DataPackConfig replaceDefaultDataPackSettings(List<String> enabled, List<String> disabled) {
		return ModPackResourcesUtil.createTestServerSettings(enabled, disabled);
	}
}
