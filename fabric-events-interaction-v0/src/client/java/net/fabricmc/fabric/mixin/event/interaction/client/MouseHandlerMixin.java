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

package net.fabricmc.fabric.mixin.event.interaction.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@WrapOperation(
			method = "onScroll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V")
	)
	private void wrapSelectedSlot(
			Inventory instance,
			int selected,
			Operation<Void> original,
			// we must use scaled offsets so that the scroll sensitivity applies
			@Local(name = "scaledXOffset") double scaledXOffset,
			@Local(name = "scaledYOffset") double scaledYOffset
	) {
		int currentSlot = instance.getSelectedSlot();
		boolean allow = ClientHotbarScrollEvents.ALLOW.invoker().allowScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);

		if (allow) {
			ClientHotbarScrollEvents.BEFORE.invoker().beforeScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);
			original.call(instance, selected);
			ClientHotbarScrollEvents.AFTER.invoker().afterScroll(instance, currentSlot, selected, scaledXOffset, scaledYOffset);
		}
	}
}
