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
	private ButtonList fabric$buttonList;
	@Unique
	private Event<ScreenEvents.Remove> fabric$removeEvent;
	@Unique
	private Event<ScreenEvents.BeforeTick> fabric$beforeTickEvent;
	@Unique
	private Event<ScreenEvents.AfterTick> fabric$afterTickEvent;
	@Unique
	private Event<ScreenEvents.BeforeRender> fabric$beforeRenderEvent;
	@Unique
	private Event<ScreenEvents.AfterRender> fabric$afterRenderEvent;

	// Keyboard
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyPress> fabric$allowKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyPress> fabric$beforeKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyPress> fabric$afterKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyRelease> fabric$allowKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric$beforeKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyRelease> fabric$afterKeyReleaseEvent;

	// Mouse
	@Unique
	private Event<ScreenMouseEvents.AllowMouseClick> fabric$allowMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseClick> fabric$beforeMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseClick> fabric$afterMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseRelease> fabric$allowMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseRelease> fabric$beforeMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseRelease> fabric$afterMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseDrag> fabric$allowMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseDrag> fabric$beforeMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseDrag> fabric$afterMouseDragEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseScroll> fabric$allowMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseScroll> fabric$beforeMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseScroll> fabric$afterMouseScrollEvent;

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
		this.fabric$buttonList = null;
		this.fabric$removeEvent = ScreenEventFactory.createRemoveEvent();
		this.fabric$beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
		this.fabric$afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
		this.fabric$beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
		this.fabric$afterTickEvent = ScreenEventFactory.createAfterTickEvent();

		// Keyboard
		this.fabric$allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
		this.fabric$beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
		this.fabric$afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
		this.fabric$allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
		this.fabric$beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
		this.fabric$afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();

		// Mouse
		this.fabric$allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
		this.fabric$beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
		this.fabric$afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
		this.fabric$allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
		this.fabric$beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
		this.fabric$afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
		this.fabric$allowMouseDragEvent = ScreenEventFactory.createAllowMouseDragEvent();
		this.fabric$beforeMouseDragEvent = ScreenEventFactory.createBeforeMouseDragEvent();
		this.fabric$afterMouseDragEvent = ScreenEventFactory.createAfterMouseDragEvent();
		this.fabric$allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
		this.fabric$beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
		this.fabric$afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();

		ScreenEvents.BEFORE_INIT.invoker().beforeInit(client, (Screen) (Object) this, width, height);
	}

	@Unique
	private void afterInit(MinecraftClient client, int width, int height) {
		ScreenEvents.AFTER_INIT.invoker().afterInit(client, (Screen) (Object) this, width, height);
	}

	@Override
	public List<ClickableWidget> fabric_getButtons() {
		// Lazy init to make the list access safe after Screen#init
		if (this.fabric$buttonList == null) {
			this.fabric$buttonList = new ButtonList(this.drawables, this.selectables, this.children);
		}

		return this.fabric$buttonList;
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
		return ensureEventsAreInitialized(this.fabric$removeEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeTickEvent);
	}

	@Override
	public Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent() {
		return ensureEventsAreInitialized(this.fabric$afterTickEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeRender> fabric_getBeforeRenderEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeRenderEvent);
	}

	@Override
	public Event<ScreenEvents.AfterRender> fabric_getAfterRenderEvent() {
		return ensureEventsAreInitialized(this.fabric$afterRenderEvent);
	}

	// Keyboard

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent() {
		return ensureEventsAreInitialized(this.fabric$allowKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent() {
		return ensureEventsAreInitialized(this.fabric$afterKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$allowKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$afterKeyReleaseEvent);
	}

	// Mouse

	@Override
	public Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent() {
		return ensureEventsAreInitialized(this.fabric$allowMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent() {
		return ensureEventsAreInitialized(this.fabric$afterMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$allowMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.fabric$afterMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent() {
		return ensureEventsAreInitialized(this.fabric$allowMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent() {
		return ensureEventsAreInitialized(this.fabric$afterMouseDragEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent() {
		return ensureEventsAreInitialized(this.fabric$allowMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent() {
		return ensureEventsAreInitialized(this.fabric$beforeMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent() {
		return ensureEventsAreInitialized(this.fabric$afterMouseScrollEvent);
	}
}
