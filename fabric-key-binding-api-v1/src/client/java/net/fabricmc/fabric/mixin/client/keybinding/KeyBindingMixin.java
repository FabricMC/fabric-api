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

package net.fabricmc.fabric.mixin.client.keybinding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingExtensions;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements KeyBindingExtensions {
	@Shadow
	private int timesPressed;

	@Shadow
	@Final
	private static Map<String, KeyBinding> KEYS_BY_ID;

	@Shadow
	private InputUtil.Key boundKey;

	@Unique
	private KeyBindingContext fabric_context;

	@Unique
	private Set<KeyBinding> fabric_conflictingKeyBinds;

	@Override
	public KeyBindingContext fabric_getContext() {
		return fabric_context;
	}

	@Override
	public void fabric_setContext(KeyBindingContext context) {
		this.fabric_context = context;
	}

	@Unique
	private static boolean fabric_isActiveAndHasNoConflict(@Nullable Set<KeyBinding> conflictingKeyBinds, KeyBinding binding) {
		KeyBindingMixin mixed = (KeyBindingMixin) (Object) binding;
		if (!mixed.fabric_context.isActive(MinecraftClient.getInstance())) return false;

		if (conflictingKeyBinds == null) {
			return true;
		} else {
			boolean hasNoConflict = true;

			for (KeyBinding conflictingKeyBind : mixed.fabric_conflictingKeyBinds) {
				// single ampersand so it always added to the set
				hasNoConflict = hasNoConflict & conflictingKeyBinds.add(conflictingKeyBind);
			}

			return hasNoConflict;
		}
	}

	@Inject(method = "onKeyPressed", at = @At("HEAD"))
	private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
		List<KeyBinding> list = KeyBindingRegistryImpl.KEY_TO_BINDINGS.get(key);
		if (list == null) return;

		Set<KeyBinding> conflictingKeyBinds = list.size() <= 1 ? null : Collections.newSetFromMap(new IdentityHashMap<>());

		for (KeyBinding binding : list) {
			if (fabric_isActiveAndHasNoConflict(conflictingKeyBinds, binding)) {
				((KeyBindingMixin) (Object) binding).timesPressed++;
			}
		}
	}

	@Inject(method = "setKeyPressed", at = @At("HEAD"))
	private static void setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
		List<KeyBinding> list = KeyBindingRegistryImpl.KEY_TO_BINDINGS.get(key);
		if (list == null) return;

		Set<KeyBinding> conflictingKeyBinds = list.size() <= 1 ? null : Collections.newSetFromMap(new IdentityHashMap<>());

		for (KeyBinding binding : list) {
			if (fabric_isActiveAndHasNoConflict(conflictingKeyBinds, binding)) {
				binding.setPressed(pressed);
			}
		}
	}

	@Inject(method = "updateKeysByCode", at = @At("HEAD"))
	private static void updateKeysByCode(CallbackInfo ci) {
		KeyBindingRegistryImpl.KEY_TO_BINDINGS.clear();

		for (KeyBinding binding : KEYS_BY_ID.values()) {
			KeyBindingRegistryImpl.putToMap(KeyBindingHelper.getBoundKeyOf(binding), binding);
		}

		for (List<KeyBinding> bindings : KeyBindingRegistryImpl.KEY_TO_BINDINGS.values()) {
			for (KeyBinding binding : bindings) {
				KeyBindingMixin mixed = (KeyBindingMixin) (Object) binding;
				mixed.fabric_conflictingKeyBinds.clear();
				mixed.fabric_conflictingKeyBinds.add(binding);
			}

			for (KeyBinding binding : bindings) {
				KeyBindingMixin mixed = (KeyBindingMixin) (Object) binding;

				for (KeyBinding otherBinding : bindings) {
					if (binding == otherBinding) continue;
					KeyBindingMixin otherMixed = (KeyBindingMixin) (Object) otherBinding;

					if (KeyBindingContext.conflicts(mixed.fabric_context, otherMixed.fabric_context)) {
						otherMixed.fabric_conflictingKeyBinds.add(binding);
						otherMixed.fabric_conflictingKeyBinds.addAll(mixed.fabric_conflictingKeyBinds);
						mixed.fabric_conflictingKeyBinds.add(otherBinding);
						mixed.fabric_conflictingKeyBinds.addAll(otherMixed.fabric_conflictingKeyBinds);
					}
				}
			}
		}
	}

	@Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At("TAIL"))
	private void init(String translationKey, InputUtil.Type type, int code, String category, CallbackInfo ci) {
		fabric_context = KeyBindingContext.IN_GAME;
		fabric_conflictingKeyBinds = Collections.newSetFromMap(new IdentityHashMap<>());
		KeyBindingRegistryImpl.putToMap(boundKey, (KeyBinding) (Object) this);
	}

	@Inject(method = "equals", at = @At("RETURN"), cancellable = true)
	private void equals(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
		if (!KeyBindingContext.conflicts(fabric_context, KeyBindingContext.of(other))) {
			cir.setReturnValue(false);
		}
	}

	// Make KEYS_BY_ID deterministic
	@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", ordinal = 0))
	private static HashMap<?, ?> makeMapOrdered() {
		return new LinkedHashMap<>();
	}

	// Return empty set, skipping the loop
	@Redirect(method = "updateKeysByCode", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private static Collection<?> skipVanillaLoop(Map<?, ?> instance) {
		return Collections.emptySet();
	}

	// Skip putting this to KEY_TO_BINDINGS, this also skips vanilla onKeyPressed and setKeyPressed loops
	@Redirect(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
	private Object skipVanillaMapping(Map<?, ?> instance, Object k, Object v) {
		return null;
	}
}
