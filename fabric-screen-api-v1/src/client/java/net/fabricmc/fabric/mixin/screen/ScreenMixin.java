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

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ButtonList;
import net.fabricmc.fabric.impl.client.screen.ScreenEventFactory;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;

@Mixin(Screen.class)
abstract class ScreenMixin implements ScreenExtensions {
	@Shadow
	@Final
	protected List<Selectable> selectables;
	@Shadow
	@Final
	protected List<Element> children;
	@Shadow
	@Final
	protected List<Drawable> drawables;

	@Unique
	private ButtonList fabricButtons;
	@Unique
	private Event<ScreenEvents.Remove> removeEvent;
	@Unique
	private Event<ScreenEvents.BeforeTick> beforeTickEvent;
	@Unique
	private Event<ScreenEvents.AfterTick> afterTickEvent;
	@Unique
	private Event<ScreenEvents.BeforeRender> beforeRenderEvent;
	@Unique
	private Event<ScreenEvents.AfterRender> afterRenderEvent;

	// Keyboard
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyPress> allowKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyPress> beforeKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyPress> afterKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyRelease> allowKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyRelease> beforeKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyRelease> afterKeyReleaseEvent;

	// Mouse
	@Unique
	private Event<ScreenMouseEvents.AllowMouseClick> allowMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseClick> beforeMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseClick> afterMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseRelease> allowMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseRelease> beforeMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseRelease> afterMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseDrag> allowMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseDrag> beforeMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseDrag> afterMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseScroll> allowMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseScroll> beforeMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseScroll> afterMouseScrollEvent;

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("HEAD"))
	private void beforeInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		beforeInit(client, width, height);
	}

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
	private void afterInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		afterInit(client, width, height);
	}

	@Inject(method = "resize", at = @At("HEAD"))
	private void beforeResizeScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		beforeInit(client, width, height);
	}

	@Inject(method = "resize", at = @At("TAIL"))
	private void afterResizeScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		afterInit(client, width, height);
	}

	@Unique
	private void beforeInit(MinecraftClient client, int width, int height) {
		// All elements are repopulated on the screen, so we need to reinitialize all events
		this.fabricButtons = null;
		this.removeEvent = ScreenEventFactory.createRemoveEvent();
		this.beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
		this.afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
		this.beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
		this.afterTickEvent = ScreenEventFactory.createAfterTickEvent();

		// Keyboard
		this.allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
		this.beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
		this.afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
		this.allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
		this.beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
		this.afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();

		// Mouse
		this.allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
		this.beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
		this.afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
		this.allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
		this.beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
		this.afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
		this.allowMouseDragEvent = ScreenEventFactory.createAllowMouseDragEvent();
		this.beforeMouseDragEvent = ScreenEventFactory.createBeforeMouseDragEvent();
		this.afterMouseDragEvent = ScreenEventFactory.createAfterMouseDragEvent();
		this.allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
		this.beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
		this.afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();

		ScreenEvents.BEFORE_INIT.invoker().beforeInit(client, (Screen) (Object) this, width, height);
	}

	@Unique
	private void afterInit(MinecraftClient client, int width, int height) {
		ScreenEvents.AFTER_INIT.invoker().afterInit(client, (Screen) (Object) this, width, height);
	}

	@Override
	public List<ClickableWidget> fabric_getButtons() {
		// Lazy init to make the list access safe after Screen#init
		if (this.fabricButtons == null) {
			this.fabricButtons = new ButtonList(this.drawables, this.selectables, this.children);
		}

		return this.fabricButtons;
	}

	@Unique
	private <T> Event<T> ensureEventsAreInitialized(Event<T> event) {
		if (event == null) {
			throw new IllegalStateException(String.format("[fabric-screen-api-v1] The current screen (%s) has not been correctly initialised, please send this crash log to the mod author. This is usually caused by calling setScreen on the wrong thread.", this.getClass().getName()));
		}

		return event;
	}

	@Override
	public Event<ScreenEvents.Remove> fabric_getRemoveEvent() {
		return ensureEventsAreInitialized(this.removeEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent() {
		return ensureEventsAreInitialized(this.beforeTickEvent);
	}

	@Override
	public Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent() {
		return ensureEventsAreInitialized(this.afterTickEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeRender> fabric_getBeforeRenderEvent() {
		return ensureEventsAreInitialized(this.beforeRenderEvent);
	}

	@Override
	public Event<ScreenEvents.AfterRender> fabric_getAfterRenderEvent() {
		return ensureEventsAreInitialized(this.afterRenderEvent);
	}

	// Keyboard

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent() {
		return ensureEventsAreInitialized(this.allowKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent() {
		return ensureEventsAreInitialized(this.beforeKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent() {
		return ensureEventsAreInitialized(this.afterKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.allowKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.beforeKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.afterKeyReleaseEvent);
	}

	// Mouse

	@Override
	public Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent() {
		return ensureEventsAreInitialized(this.allowMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent() {
		return ensureEventsAreInitialized(this.beforeMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent() {
		return ensureEventsAreInitialized(this.afterMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.allowMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.beforeMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.afterMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent() {
		return ensureEventsAreInitialized(this.allowMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent() {
		return ensureEventsAreInitialized(this.beforeMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent() {
		return ensureEventsAreInitialized(this.afterMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent() {
		return ensureEventsAreInitialized(this.allowMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent() {
		return ensureEventsAreInitialized(this.beforeMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent() {
		return ensureEventsAreInitialized(this.afterMouseScrollEvent);
	}
}
