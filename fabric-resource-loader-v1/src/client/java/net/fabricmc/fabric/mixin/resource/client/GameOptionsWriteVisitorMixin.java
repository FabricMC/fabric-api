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

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import net.fabricmc.fabric.impl.resource.pack.FabricPack;

/**
 * Mixins to the anonymous class in #write method.
 */
@Mixin(targets = "net.minecraft.client.Options$3")
public class GameOptionsWriteVisitorMixin {
	@Unique
	private static List<String> toPackListString(List<String> packs) {
		var copy = new ArrayList<String>(packs.size());
		PackRepository manager = Minecraft.getInstance().getResourcePackRepository();

		for (String pack : packs) {
			Pack profile = manager.getPack(pack);

			// Nonexistent pack profiles should be handled in the same way as vanilla
			if (profile == null || !((FabricPack) profile).fabric$isHidden()) copy.add(pack);
		}

		return copy;
	}

	@SuppressWarnings("unchecked")
	@ModifyArg(method = "process(Ljava/lang/String;Ljava/lang/Object;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
	private <T> T skipHiddenPacks(T value, @Local String key) {
		if ("resourcePacks".equals(key) && value instanceof List) {
			return (T) toPackListString((List<String>) value);
		}

		return value;
	}
}
