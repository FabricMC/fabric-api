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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingContext;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public abstract class KeyBindingEntryMixin {
	@Shadow
	@Final
	private KeyBinding binding;

	@ModifyConstant(method = "update", constant = @Constant(stringValue = ", "))
	private String makeConflictTextMultiline(String constant) {
		return "\n";
	}

	@ModifyConstant(method = "update", constant = @Constant(stringValue = "controls.keybinds.duplicateKeybinds"))
	private String replaceConflictText(String constant) {
		for (KeyBinding otherBinding : MinecraftClient.getInstance().options.allKeys) {
			if (otherBinding == binding || !binding.equals(otherBinding)) continue;

			if (KeyBindingContext.of(binding) != KeyBindingContext.of(otherBinding)) {
				return "fabric.keybinding.conflicts";
			}
		}

		return constant;
	}
}
