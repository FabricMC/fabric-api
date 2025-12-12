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

package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;

import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

@Mixin(KnownPacksManager.class)
public class KnownPacksManagerMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("KnownPacksManagerMixin");

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/ServerPacksSource;createVanillaTrustedRepository()Lnet/minecraft/server/packs/repository/PackRepository;"))
	public PackRepository createClientManager() {
		return ModPackResourcesUtil.createClientManager();
	}

	@ModifyReturnValue(method = "trySelectingPacks", at = @At("RETURN"))
	List<KnownPack> getCommonKnownPacksReturn(List<KnownPack> original) {
		if (original.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
			LOGGER.warn("Too many knownPacks: Found {}; max {}", original.size(), ModResourcePackCreator.MAX_KNOWN_PACKS);
			return original.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS);
		}

		return original;
	}
}
