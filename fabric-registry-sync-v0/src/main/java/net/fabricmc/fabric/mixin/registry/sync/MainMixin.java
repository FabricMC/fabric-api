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

package net.fabricmc.fabric.mixin.registry.sync;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Main;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.registry.sync.trackers.vanilla.BlockInitTracker;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(Main.class)
public class MainMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;startTimerHackThread()V"), method = "main")
	private static void afterModInit(CallbackInfo info) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			// Freeze the registries on the server
			LOGGER.debug("Freezing registries");

			BuiltInRegistries.bootStrap();
			BlockInitTracker.postFreeze();
			CreativeModeTabs.validate();
		}
	}
}
