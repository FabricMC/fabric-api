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

package net.fabricmc.fabric.mixin.content.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.HoneycombItem;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
	@Dynamic("method_34723: Synthetic lambda body for Suppliers.memoize in initialization of UNWAXED_TO_WAXED_BLOCKS")
	@Inject(method = "method_34723", at = @At("RETURN"), cancellable = true)
	private static void createUnwaxedToWaxedMap(CallbackInfoReturnable<BiMap> cir) {
		cir.setReturnValue(HashBiMap.create(cir.getReturnValue()));
	}
}
