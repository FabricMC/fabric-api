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

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;

@Mixin(Window.class)
public abstract class WindowMixin implements WindowHooks {
	@Shadow
	private int x;
	@Shadow
	private int y;
	@Shadow
	private int windowedX;
	@Shadow
	private int windowedY;
	@Shadow
	private int width;
	@Shadow
	private int height;
	@Shadow
	private int windowedWidth;
	@Shadow
	private int windowedHeight;
	@Shadow
	private int framebufferWidth;
	@Shadow
	private int framebufferHeight;
	@Shadow
	private boolean fullscreen;
	@Shadow
	@Final
	private WindowEventHandler eventHandler;
	@Shadow
	@Final
	private ScreenManager screenManager;
	@Shadow
	private Optional<VideoMode> preferredFullscreenVideoMode;

	@Shadow
	protected abstract void setMode();

	@Unique
	private int defaultWidth;
	@Unique
	private int defaultHeight;
	@Unique
	private int realWidth;
	@Unique
	private int realHeight;
	@Unique
	private int realFramebufferWidth;
	@Unique
	private int realFramebufferHeight;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(WindowEventHandler eventHandler, ScreenManager monitorTracker, DisplayData settings, @Nullable String fullscreenVideoMode, String title, CallbackInfo ci) {
		this.defaultWidth = settings.width();
		this.defaultHeight = settings.height();
		this.realWidth = this.width;
		this.realHeight = this.height;
		this.realFramebufferWidth = this.framebufferWidth;
		this.realFramebufferHeight = this.framebufferHeight;

		this.width = this.windowedWidth = this.framebufferWidth = defaultWidth;
		this.height = this.windowedHeight = this.framebufferHeight = defaultHeight;
	}

	@Inject(method = {"onFocus", "onEnter", "onIconify"}, at = @At("HEAD"), cancellable = true)
	private void cancelEvents(CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "onResize", at = @At("HEAD"), cancellable = true)
	private void cancelWindowSizeChanged(long window, int width, int height, CallbackInfo ci) {
		realWidth = width;
		realHeight = height;
		ci.cancel();
	}

	@Inject(method = "onFramebufferResize", at = @At("HEAD"), cancellable = true)
	private void cancelFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci) {
		realFramebufferWidth = width;
		realFramebufferHeight = height;
		ci.cancel();
	}

	@WrapMethod(method = "setMode")
	private void wrapUpdateWindowRegion(Operation<Void> original) {
		int prevWidth = this.width;
		int prevHeight = this.height;
		int prevWindowedWidth = this.windowedWidth;
		int prevWindowedHeight = this.windowedHeight;

		original.call();

		this.realWidth = this.width;
		this.realHeight = this.height;

		this.width = prevWidth;
		this.height = prevHeight;
		this.windowedWidth = prevWindowedWidth;
		this.windowedHeight = prevWindowedHeight;
	}

	@Inject(method = "setWindowed", at = @At("HEAD"), cancellable = true)
	private void setWindowedSize(int width, int height, CallbackInfo ci) {
		this.fullscreen = false;
		fabric_resize(width, height);
		ci.cancel();
	}

	@Override
	public int fabric_getRealWidth() {
		return realWidth;
	}

	@Override
	public int fabric_getRealHeight() {
		return realHeight;
	}

	@Override
	public int fabric_getRealFramebufferWidth() {
		return realFramebufferWidth;
	}

	@Override
	public int fabric_getRealFramebufferHeight() {
		return realFramebufferHeight;
	}

	@Override
	public void fabric_resetSize() {
		fabric_resize(defaultWidth, defaultHeight);
	}

	@Override
	public void fabric_resize(int width, int height) {
		if (width == this.width && width == this.windowedWidth && width == this.framebufferWidth && height == this.height && height == this.windowedHeight && height == this.framebufferHeight) {
			return;
		}

		// Move the top left corner of the window so that the window expands/contracts from its center, while also
		// trying to keep the window within the monitor's bounds
		Monitor monitor = this.screenManager.findBestMonitor((Window) (Object) this);

		if (monitor != null) {
			VideoMode videoMode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);

			this.x += (this.windowedWidth - width) / 2;
			this.y += (this.windowedHeight - height) / 2;

			if (this.x + width > monitor.getX() + videoMode.getWidth()) {
				this.x = monitor.getX() + videoMode.getWidth() - width;
			}

			if (this.x < monitor.getX()) {
				this.x = monitor.getX();
			}

			if (this.y + height > monitor.getY() + videoMode.getHeight()) {
				this.y = monitor.getY() + videoMode.getHeight() - height;
			}

			if (this.y < monitor.getY()) {
				this.y = monitor.getY();
			}

			this.windowedX = this.x;
			this.windowedY = this.y;
		}

		this.width = this.windowedWidth = this.framebufferWidth = width;
		this.height = this.windowedHeight = this.framebufferHeight = height;

		setMode();
		this.eventHandler.resizeDisplay();
	}
}
