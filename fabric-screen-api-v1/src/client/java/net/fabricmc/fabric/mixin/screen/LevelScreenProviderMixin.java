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

package net.fabricmc.fabric.mixin.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;

@Mixin(LevelScreenProvider.class)
public interface LevelScreenProviderMixin {
	@WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;"))
	private static Map<Optional<RegistryKey<WorldPreset>>, LevelScreenProvider> makeMutable(Object k1, Object v1, Object k2, Object v2, Operation<Map<Optional<RegistryKey<WorldPreset>>, LevelScreenProvider>> operation) {
		return new HashMap<>(operation.call(k1, v1, k2, v2));
	}
}
