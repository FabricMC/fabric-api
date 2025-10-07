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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.DebugOptionsComparator;

@Mixin(targets = "net/minecraft/client/gui/screen/DebugOptionsScreen$OptionsListWidget")
public class DebugOptionsScreenListWidgetMixin {
	@Redirect(method = "method_72822", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;compareTo(Lnet/minecraft/util/Identifier;)I"))
	private static int sort(Identifier o1, Identifier o2) {
		return DebugOptionsComparator.INSTANCE.compare(o1, o2);
	}

	@WrapOperation(method = "fillEntries", at = @At(value = "INVOKE", target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"))
	private boolean searchPath(String instance, CharSequence searchStrings, Operation<Boolean> original, @Local Map.Entry<Identifier, DebugHudEntry> entry) {
		final String namespace = entry.getKey().getNamespace();
		return original.call(instance, searchStrings)
				|| (!Identifier.DEFAULT_NAMESPACE.equals(namespace) && namespace.contains(searchStrings));
	}
}
