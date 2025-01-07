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

package net.fabricmc.fabric.mixin.client.gametest;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;

import net.fabricmc.fabric.impl.client.gametest.WindowHooks;

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
	private MonitorTracker monitorTracker;
	@Shadow
	private Optional<VideoMode> fullscreenVideoMode;

	@Shadow
	protected abstract void updateWindowRegion();

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
	private void onInit(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, @Nullable String fullscreenVideoMode, String title, CallbackInfo ci) {
		this.defaultWidth = settings.width;
		this.defaultHeight = settings.height;
		this.realWidth = this.width;
		this.realHeight = this.height;
		this.realFramebufferWidth = this.framebufferWidth;
		this.realFramebufferHeight = this.framebufferHeight;

		this.width = this.windowedWidth = this.framebufferWidth = defaultWidth;
		this.height = this.windowedHeight = this.framebufferHeight = defaultHeight;
	}

	@Inject(method = {"onWindowFocusChanged", "onCursorEnterChanged", "onMinimizeChanged"}, at = @At("HEAD"), cancellable = true)
	private void cancelEvents(CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "onWindowSizeChanged", at = @At("HEAD"), cancellable = true)
	private void cancelWindowSizeChanged(long window, int width, int height, CallbackInfo ci) {
		realWidth = width;
		realHeight = height;
		ci.cancel();
	}

	@Inject(method = "onFramebufferSizeChanged", at = @At("HEAD"), cancellable = true)
	private void cancelFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci) {
		realFramebufferWidth = width;
		realFramebufferHeight = height;
		ci.cancel();
	}

	@WrapMethod(method = "updateWindowRegion")
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

	@Inject(method = "setWindowedSize", at = @At("HEAD"), cancellable = true)
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
		Monitor monitor = this.monitorTracker.getMonitor((Window) (Object) this);

		if (monitor != null) {
			VideoMode videoMode = monitor.findClosestVideoMode(this.fullscreenVideoMode);

			this.x += (this.windowedWidth - width) / 2;
			this.y += (this.windowedHeight - height) / 2;

			if (this.x + width > monitor.getViewportX() + videoMode.getWidth()) {
				this.x = monitor.getViewportX() + videoMode.getWidth() - width;
			}

			if (this.x < monitor.getViewportX()) {
				this.x = monitor.getViewportX();
			}

			if (this.y + height > monitor.getViewportY() + videoMode.getHeight()) {
				this.y = monitor.getViewportY() + videoMode.getHeight() - height;
			}

			if (this.y < monitor.getViewportY()) {
				this.y = monitor.getViewportY();
			}

			this.windowedX = this.x;
			this.windowedY = this.y;
		}

		this.width = this.windowedWidth = this.framebufferWidth = width;
		this.height = this.windowedHeight = this.framebufferHeight = height;

		updateWindowRegion();
		this.eventHandler.onResolutionChanged();
	}
}
