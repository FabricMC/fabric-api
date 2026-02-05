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

package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;

@Mixin(ScreenManager.class)
public class ScreenManagerMixin {
	@ModifyExpressionValue(method = "findBestMonitor(Lcom/mojang/blaze3d/platform/Window;)Lcom/mojang/blaze3d/platform/Monitor;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getScreenWidth()I"))
	private int getRealWidth(int original, Window window) {
		return ((WindowHooks) (Object) window).fabric_getRealWidth();
	}

	@ModifyExpressionValue(method = "findBestMonitor(Lcom/mojang/blaze3d/platform/Window;)Lcom/mojang/blaze3d/platform/Monitor;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getScreenHeight()I"))
	private int getRealHeight(int original, Window window) {
		return ((WindowHooks) (Object) window).fabric_getRealHeight();
	}
}
