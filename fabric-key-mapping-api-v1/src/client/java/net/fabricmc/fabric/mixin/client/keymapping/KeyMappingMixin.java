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

package net.fabricmc.fabric.mixin.client.keymapping;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;

@Mixin(KeyMapping.class)
abstract class KeyMappingMixin implements KeyMappingExtensions {
	@Shadow
	private int clickCount;

	@Unique
	private @Nullable KeyMappingContext context;

	/// Used to set the default/fallback context of keys only for when they are being checked for conflicts.
	/// If [#context] is not null, this is ignored.
	/// We assume most keys are in the IN_GAME context, but in [OptionsMixin] some keys are set to [KeyMappingContext#ALL]
	@Unique
	private KeyMappingContext conflictContext = KeyMappingContext.IN_GAME;

	@Override
	public @Nullable KeyMappingContext fabric_getContext() {
		return context;
	}

	@Override
	public KeyMappingContext fabric_getConflictContext() {
		return context != null ? context : conflictContext;
	}

	@Override
	public void fabric_setContext(KeyMappingContext context) {
		this.context = context;
	}

	@Override
	public void fabric_setConflictContext(KeyMappingContext context) {
		conflictContext = context;
	}

	@WrapOperation(method = "click", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;forAllKeyMappings(Lcom/mojang/blaze3d/platform/InputConstants$Key;Ljava/util/function/Consumer;)V"))
	private static void clickActiveMappings(InputConstants.Key key, Consumer<KeyMapping> operation, Operation<Void> original) {
		original.call(key, (Consumer<KeyMapping>) mapping -> {
			KeyMappingContext context = ((KeyMappingExtensions) mapping).fabric_getContext();

			if (context == null || context.isActive(Minecraft.getInstance())) {
				operation.accept(mapping);
			}
		});
	}

	@WrapOperation(method = "set", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;forAllKeyMappings(Lcom/mojang/blaze3d/platform/InputConstants$Key;Ljava/util/function/Consumer;)V"))
	private static void setActiveMappings(InputConstants.Key key, Consumer<KeyMapping> operation, Operation<Void> original, InputConstants.Key inputKey, boolean state) {
		// Releases must reach inactive mappings so they cannot remain held when their context goes inactive.
		if (!state) {
			original.call(key, operation);
			return;
		}

		original.call(key, (Consumer<KeyMapping>) mapping -> {
			KeyMappingContext context = ((KeyMappingExtensions) mapping).fabric_getContext();

			if (context == null || context.isActive(Minecraft.getInstance())) {
				operation.accept(mapping);
			}
		});
	}

	@ModifyExpressionValue(method = "isDown", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyMapping;isDown:Z"))
	private boolean isDownInContext(boolean down) {
		return down && (context == null || context.isActive(Minecraft.getInstance()));
	}

	@Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
	private void consumeClickInContext(CallbackInfoReturnable<Boolean> cir) {
		if (clickCount != 0 && context != null && !context.isActive(Minecraft.getInstance())) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "same", at = @At("RETURN"))
	private boolean conflictsInContext(boolean same, KeyMapping other) {
		return same && KeyMappingContext.conflicts(fabric_getConflictContext(), KeyMappingContext.of(other));
	}
}
