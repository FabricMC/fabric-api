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

package net.fabricmc.fabric.test.client.keymapping;

import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.CUSTOM_CONTEXT;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.always;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.custom;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.inGame;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.inScreen;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.legacy;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.screenSpecific;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.screenTransitionToggle;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.shared;
import static net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.toggle;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.ContextScreen;
import net.fabricmc.fabric.test.client.keymapping.KeyMappingContextTest.OtherContextScreen;

public class KeyMappingContextGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try {
			testContextConflicts(context);
			testKeyboardMatching(context);
			testMouseMatching(context);
			testScreenConflicts(context);
			testScreenContext(context);
			testScreenSubclass(context);
			testSharedKey(context);
			testInactiveContext(context);
			testInactiveRelease(context);
			testToggleRestore(context);
			testVanillaConflicts(context);
			openScreen(context);
			testDebugShortcut(context);
			testDebugKeyCapture(context);
			testKeyboardCapture(context);
			testMouseCapture(context);
			testScreenKeyboard(context);
			testScreenMouse(context);
			testScreenReplacement(context);
			testLegacyInput(context);
			testProgrammaticClick(context);
			context.setScreen(TitleScreen::new);

			try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
				singleplayer.getConnection().waitForChunksDownload();
				testScreenContextInGame(context);
				testScreenClosing(context);
				testKeyboardTransition(context);
				testMouseTransition(context);
			}
		} finally {
			context.getInput().releaseKey(always);
			context.getInput().releaseKey(inScreen);
			context.getInput().releaseKey(screenTransitionToggle);
			context.runOnClient(client -> {
				always.setKey(always.getDefaultKey());
				inScreen.setKey(inScreen.getDefaultKey());
				KeyMapping.resetMapping();
				KeyMapping.releaseAll();
				KeyMapping.resetToggleKeys();
				toggle.shouldRestoreStateOnScreenClosed();
				screenTransitionToggle.shouldRestoreStateOnScreenClosed();
				CUSTOM_CONTEXT.active = false;
			});
			context.setScreen(TitleScreen::new);
		}
	}

	private static void testContextConflicts(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (inGame.same(inScreen) || inScreen.same(inGame)) {
				throw new AssertionError("Game and screen contexts must not conflict");
			}

			if (!inScreen.same(always) || !always.same(inScreen) || !custom.same(shared)) {
				throw new AssertionError("Overlapping contexts must conflict in both directions");
			}
		});
	}

	private static void testKeyboardMatching(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			KeyEvent event = new KeyEvent(KeyMappingHelper.getBoundKeyOf(inScreen).getValue(), 0, 0);
			KeyEvent legacyEvent = new KeyEvent(KeyMappingHelper.getBoundKeyOf(legacy).getValue(), 0, 0);
			KeyEvent otherEvent = new KeyEvent(InputConstants.KEY_F1, 0, 0);

			if (!KeyMappingHelper.isActiveAndMatches(inScreen, event) || !KeyMappingHelper.isActiveAndMatches(legacy, legacyEvent)) {
				throw new AssertionError("Keyboard matching must accept active mappings and mappings without an explicit context");
			}

			if (KeyMappingHelper.isActiveAndMatches(inGame, event) || KeyMappingHelper.isActiveAndMatches(inScreen, otherEvent)) {
				throw new AssertionError("Keyboard matching must reject inactive contexts and unrelated keys");
			}
		});
	}

	private static void testMouseMatching(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			InputConstants.Key screenKey = KeyMappingHelper.getBoundKeyOf(inScreen);
			InputConstants.Key gameKey = KeyMappingHelper.getBoundKeyOf(inGame);
			InputConstants.Key legacyKey = KeyMappingHelper.getBoundKeyOf(legacy);
			InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4);
			MouseButtonEvent event = new MouseButtonEvent(0, 0, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_4, 0));
			MouseButtonEvent otherEvent = new MouseButtonEvent(0, 0, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_5, 0));

			try {
				inScreen.setKey(mouseKey);
				inGame.setKey(mouseKey);
				legacy.setKey(mouseKey);

				if (!KeyMappingHelper.isActiveAndMatches(inScreen, event) || !KeyMappingHelper.isActiveAndMatches(legacy, event)) {
					throw new AssertionError("Mouse matching must accept active mappings and mappings without an explicit context");
				}

				if (KeyMappingHelper.isActiveAndMatches(inGame, event) || KeyMappingHelper.isActiveAndMatches(inScreen, otherEvent)) {
					throw new AssertionError("Mouse matching must reject inactive contexts and unrelated buttons");
				}
			} finally {
				inScreen.setKey(screenKey);
				inGame.setKey(gameKey);
				legacy.setKey(legacyKey);
			}
		});
	}

	private static void testScreenConflicts(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyMappingContext screenContext = KeyMappingContext.of(screenSpecific);
			KeyMappingContext sameScreen = KeyMappingContext.forScreen(ContextScreen.class);
			KeyMappingContext parentScreen = KeyMappingContext.forScreen(Screen.class);
			KeyMappingContext overlappingScreens = KeyMappingContext.forScreen(TitleScreen.class, OtherContextScreen.class);
			KeyMappingContext unrelatedScreen = KeyMappingContext.forScreen(TitleScreen.class);

			if (!screenSpecific.same(inScreen) || !inScreen.same(screenSpecific) || !screenSpecific.same(always)) {
				throw new AssertionError("Screen-specific mappings must conflict with screen and global mappings");
			}

			if (!KeyMappingContext.conflicts(screenContext, sameScreen)
					|| !KeyMappingContext.conflicts(screenContext, parentScreen)
					|| !KeyMappingContext.conflicts(parentScreen, screenContext)
					|| !KeyMappingContext.conflicts(screenContext, overlappingScreens)
					|| !KeyMappingContext.conflicts(overlappingScreens, screenContext)) {
				throw new AssertionError("Screen contexts must conflict with the same class and overlapping subclasses");
			}

			if (screenSpecific.same(inGame) || inGame.same(screenSpecific)
					|| KeyMappingContext.conflicts(screenContext, unrelatedScreen)
					|| KeyMappingContext.conflicts(unrelatedScreen, screenContext)) {
				throw new AssertionError("Screen contexts must not conflict with gameplay or unrelated screens");
			}
		});
	}

	private static void testScreenContext(ClientGameTestContext context) {
		context.setScreen(ContextScreen::new);
		context.getInput().holdKey(screenSpecific);
		context.runOnClient(client -> {
			if (!screenSpecific.isDown() || !screenSpecific.consumeClick()) {
				throw new AssertionError("Screen-specific mappings must receive input in their screen");
			}
		});
		context.getInput().releaseKey(screenSpecific);
		context.setScreen(OtherContextScreen::new);
		context.getInput().pressKey(screenSpecific);
		context.runOnClient(client -> {
			if (!screenSpecific.consumeClick()) {
				throw new AssertionError("Screen-specific mappings must receive input in any of their screens");
			}
		});
		context.setScreen(TitleScreen::new);
		context.getInput().pressKey(screenSpecific);
		context.runOnClient(client -> {
			if (screenSpecific.isDown() || screenSpecific.consumeClick()) {
				throw new AssertionError("Screen-specific mappings must remain inactive in unrelated screens");
			}
		});
	}

	private static void testScreenSubclass(ClientGameTestContext context) {
		context.setScreen(() -> new ContextScreen() { });
		context.getInput().pressKey(screenSpecific);
		context.runOnClient(client -> {
			if (!screenSpecific.consumeClick()) {
				throw new AssertionError("Screen-specific mappings must also receive input in subclasses");
			}
		});
	}

	private static void testScreenContextInGame(ClientGameTestContext context) {
		context.setScreen(() -> null);
		context.getInput().holdKey(screenSpecific);
		context.runOnClient(client -> {
			if (screenSpecific.isDown() || screenSpecific.consumeClick()) {
				throw new AssertionError("Screen-specific mappings must remain inactive without a screen");
			}
		});
		context.getInput().releaseKey(screenSpecific);
	}

	private static void testSharedKey(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyMapping.releaseAll();
			CUSTOM_CONTEXT.active = true;
			InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(custom);
			KeyMapping.set(key, true);
			KeyMapping.click(key);

			if (!custom.isDown() || !custom.consumeClick() || !shared.isDown() || !shared.consumeClick()) {
				throw new AssertionError("All active mappings sharing a key must receive input");
			}

			KeyMapping.set(key, false);
		});
	}

	private static void testInactiveContext(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyMapping.releaseAll();
			CUSTOM_CONTEXT.active = false;
			InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(custom);
			KeyMapping.set(key, true);
			KeyMapping.click(key);
			CUSTOM_CONTEXT.active = true;

			if (custom.isDown() || custom.consumeClick()) {
				throw new AssertionError("Input received while inactive must not appear when the context becomes active");
			}
		});
	}

	private static void testInactiveRelease(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyMapping.releaseAll();
			CUSTOM_CONTEXT.active = true;
			InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(custom);
			KeyMapping.set(key, true);
			KeyMapping.click(key);
			CUSTOM_CONTEXT.active = false;

			if (custom.isDown() || custom.consumeClick()) {
				throw new AssertionError("Inactive contexts must hide held state and queued clicks");
			}

			KeyMapping.set(key, false);
			CUSTOM_CONTEXT.active = true;

			if (custom.isDown() || !custom.consumeClick()) {
				throw new AssertionError("Releasing an inactive mapping must clear held state without discarding its queued click");
			}
		});
	}

	private static void testToggleRestore(ClientGameTestContext context) {
		context.runOnClient(client -> {
			CUSTOM_CONTEXT.active = true;
			KeyMapping.set(KeyMappingHelper.getBoundKeyOf(toggle), true);
			CUSTOM_CONTEXT.active = false;
			KeyMapping.releaseAll();

			if (!toggle.shouldRestoreStateOnScreenClosed()) {
				throw new AssertionError("An inactive context must not prevent vanilla toggle restoration");
			}
		});
	}

	private static void testScreenKeyboard(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			ScreenKeyboardEvents.beforeKeyPress(client.gui.screen()).register((screen, event) -> {
				if (inScreen.matches(event) && (!inScreen.isDown() || !inScreen.consumeClick() || !always.isDown() || inGame.isDown() || inGame.consumeClick())) {
					throw new AssertionError("Screen mappings must receive input before screen listeners, without activating game mappings");
				}
			});
			ScreenKeyboardEvents.allowKeyRelease(client.gui.screen()).register((screen, event) -> false);
		});
		context.getInput().holdKey(inScreen);
		context.runOnClient(client -> {
			if (!always.consumeClick() || always.consumeClick() || inScreen.consumeClick()) {
				throw new AssertionError("Screen key listeners must see exactly one click per active mapping");
			}
		});
		context.getInput().releaseKey(inScreen);
		context.runOnClient(client -> {
			if (inScreen.isDown() || always.isDown()) {
				throw new AssertionError("Canceled screen key releases must clear held mappings");
			}
		});
	}

	private static void testScreenMouse(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			inScreen.setKey(InputConstants.Type.MOUSE.getOrCreate(4));
			always.setKey(KeyMappingHelper.getBoundKeyOf(inScreen));
			KeyMapping.resetMapping();
			ScreenMouseEvents.beforeMouseClick(client.gui.screen()).register((screen, event) -> {
				if (inScreen.matchesMouse(event) && (!inScreen.isDown() || !inScreen.consumeClick() || !always.isDown())) {
					throw new AssertionError("Rebound mouse mappings must receive input before screen listeners");
				}
			});
			ScreenMouseEvents.allowMouseRelease(client.gui.screen()).register((screen, event) -> false);
		});
		context.getInput().holdMouse(4);
		context.runOnClient(client -> {
			if (!always.consumeClick() || always.consumeClick() || inScreen.consumeClick()) {
				throw new AssertionError("Screen mouse listeners must see exactly one click per active mapping");
			}
		});
		context.getInput().releaseMouse(4);
		context.runOnClient(client -> {
			if (inScreen.isDown() || always.isDown()) {
				throw new AssertionError("Canceled screen mouse releases must clear held mappings");
			}

			inScreen.setKey(inScreen.getDefaultKey());
			always.setKey(always.getDefaultKey());
			KeyMapping.resetMapping();
		});
	}

	private static void testScreenReplacement(ClientGameTestContext context) {
		openScreen(context);
		context.getInput().holdKey(inScreen);
		openScreen(context);
		context.runOnClient(client -> {
			if (inScreen.isDown() || inScreen.consumeClick() || always.isDown() || always.consumeClick()) {
				throw new AssertionError("Replacing a screen must clear held mappings and queued clicks");
			}
		});
		context.getInput().releaseKey(inScreen);
	}

	private static void testLegacyInput(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			legacy.setDown(true);
			KeyMapping.click(KeyMappingHelper.getBoundKeyOf(legacy));

			if (!legacy.isDown() || !legacy.consumeClick()) {
				throw new AssertionError("Mappings registered without a context must preserve their legacy screen behavior");
			}

			legacy.setDown(false);
		});
	}

	private static void testProgrammaticClick(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> ScreenKeyboardEvents.beforeKeyPress(client.gui.screen()).register((screen, event) -> {
			if (always.matches(event)) {
				KeyMapping.click(KeyMappingHelper.getBoundKeyOf(always));
			}
		}));
		context.getInput().pressKey(always);
		context.runOnClient(client -> {
			if (!always.consumeClick() || !always.consumeClick() || always.consumeClick()) {
				throw new AssertionError("A programmatic click must be queued independently of the physical press");
			}
		});
	}

	private static void testScreenClosing(ClientGameTestContext context) {
		openScreen(context);
		context.getInput().holdKey(always);
		context.setScreen(() -> null);
		context.runOnClient(client -> {
			if (!always.isDown() || inScreen.isDown() || inScreen.consumeClick()) {
				throw new AssertionError("Closing a screen must preserve all-context held state and deactivate screen mappings");
			}
		});
		context.getInput().releaseKey(always);
		context.runOnClient(client -> {
			if (always.isDown() || !always.consumeClick() || always.consumeClick()) {
				throw new AssertionError("Release after closing a screen must clear held state and retain the queued click");
			}
		});
	}

	private static void testKeyboardTransition(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> ScreenKeyboardEvents.beforeKeyPress(client.gui.screen()).register((screen, event) -> {
			if (screenTransitionToggle.matches(event)) {
				client.gui.setScreen(null);
			}
		}));
		context.getInput().holdKey(screenTransitionToggle);
		context.runOnClient(client -> {
			if (client.gui.screen() != null || !screenTransitionToggle.isDown()) {
				throw new AssertionError("Closing a screen during a press must toggle the mapping exactly once");
			}

			if (!screenTransitionToggle.consumeClick() || screenTransitionToggle.consumeClick()) {
				throw new AssertionError("A press crossing from a screen to gameplay must queue exactly one click");
			}
		});
		context.getInput().releaseKey(screenTransitionToggle);
	}

	private static void testMouseTransition(ClientGameTestContext context) {
		openScreen(context);
		context.runOnClient(client -> {
			always.setKey(InputConstants.Type.MOUSE.getOrCreate(4));
			KeyMapping.resetMapping();
			ScreenMouseEvents.beforeMouseClick(client.gui.screen()).register((screen, event) -> {
				if (always.matchesMouse(event)) {
					if (!always.consumeClick() || always.consumeClick()) {
						throw new AssertionError("A mouse press must queue exactly one click before its screen listener");
					}

					client.gui.setScreen(null);
				}
			});
		});
		context.getInput().holdMouse(4);
		context.runOnClient(client -> {
			if (client.gui.screen() != null || always.consumeClick() || !always.isDown()) {
				throw new AssertionError("Closing a screen during a mouse press must preserve held state without queuing another click");
			}
		});
		context.getInput().releaseMouse(4);
		context.runOnClient(client -> {
			if (always.isDown()) {
				throw new AssertionError("Mouse release after closing a screen must clear held state");
			}
		});
	}

	private static void openScreen(ClientGameTestContext context) {
		context.setScreen(() -> new Screen(Component.literal("Key mapping context test")) { });
	}

	private static void testDebugShortcut(ClientGameTestContext context) {
		boolean advancedTooltips = context.computeOnClient(client -> client.options.advancedItemTooltips);

		try {
			context.getInput().holdKey(options -> options.keyDebugModifier);
			context.getInput().pressKey(options -> options.keyDebugShowAdvancedTooltips);
			context.runOnClient(client -> {
				if (client.options.advancedItemTooltips == advancedTooltips) {
					throw new AssertionError("The vanilla advanced tooltip shortcut must work while a screen is open");
				}
			});
		} finally {
			context.getInput().releaseKey(options -> options.keyDebugModifier);
			context.runOnClient(client -> {
				client.options.advancedItemTooltips = advancedTooltips;
				client.options.save();
			});
		}
	}

	private static void testVanillaConflicts(ClientGameTestContext context) {
		context.runOnClient(client -> {
			InputConstants.Key screenKey = KeyMappingHelper.getBoundKeyOf(inScreen);
			InputConstants.Key jumpKey = KeyMappingHelper.getBoundKeyOf(client.options.keyJump);
			KeyMapping[] screenOrGlobalMappings = {
				client.options.keyInventory,
				client.options.keyDrop,
				client.options.keyHotbarSlots[0],
				client.options.keyPickItem,
				client.options.keyFullscreen,
				client.options.keyFriends,
				client.options.keyAdvancements,
				client.options.keyDebugShowAdvancedTooltips
			};

			try {
				for (KeyMapping vanillaMapping : screenOrGlobalMappings) {
					inScreen.setKey(KeyMappingHelper.getBoundKeyOf(vanillaMapping));

					if (!inScreen.same(vanillaMapping) || !vanillaMapping.same(inScreen)) {
						throw new AssertionError("Screen mappings must conflict symmetrically with vanilla screen or global actions: " + vanillaMapping.getName());
					}
				}

				inScreen.setKey(jumpKey);

				if (inScreen.same(client.options.keyJump) || client.options.keyJump.same(inScreen)) {
					throw new AssertionError("Screen mappings must be able to share a key with vanilla gameplay-only actions");
				}

				client.options.keyJump.setKey(KeyMappingHelper.getBoundKeyOf(client.options.keyInventory));

				if (!client.options.keyJump.same(client.options.keyInventory) || !client.options.keyInventory.same(client.options.keyJump)) {
					throw new AssertionError("Vanilla mappings must still conflict when bound to the same key");
				}
			} finally {
				inScreen.setKey(screenKey);
				client.options.keyJump.setKey(jumpKey);
				KeyMapping.resetMapping();
			}
		});
	}

	private static void testKeyboardCapture(ClientGameTestContext context) {
		InputConstants.Key previousKey = context.computeOnClient(client -> KeyMappingHelper.getBoundKeyOf(legacy));

		try {
			openKeyCapture(context);
			context.getInput().holdKey(inScreen);
			context.runOnClient(client -> {
				KeyBindsScreen screen = (KeyBindsScreen) client.gui.screen();

				if (screen.selectedKey != null || !legacy.matches(KeyMappingHelper.getBoundKeyOf(inScreen))) {
					throw new AssertionError("The controls screen must capture the keyboard binding");
				}

				if (inScreen.isDown() || always.isDown() || inScreen.consumeClick() || always.consumeClick()) {
					throw new AssertionError("Capturing a keyboard binding must not activate contextual shortcuts");
				}
			});
			context.getInput().releaseKey(inScreen);
			context.getInput().pressKey(inScreen);
			context.runOnClient(client -> {
				if (!inScreen.consumeClick() || !always.consumeClick()) {
					throw new AssertionError("Keyboard shortcuts must resume after binding capture");
				}
			});
		} finally {
			context.getInput().releaseKey(inScreen);
			context.runOnClient(client -> {
				legacy.setKey(previousKey);
				KeyMapping.resetMapping();
			});
		}
	}

	private static void testMouseCapture(ClientGameTestContext context) {
		InputConstants.Key previousKey = context.computeOnClient(client -> KeyMappingHelper.getBoundKeyOf(legacy));

		try {
			openKeyCapture(context);
			context.runOnClient(client -> {
				inScreen.setKey(InputConstants.Type.MOUSE.getOrCreate(4));
				always.setKey(KeyMappingHelper.getBoundKeyOf(inScreen));
				KeyMapping.resetMapping();
			});
			context.getInput().holdMouse(4);
			context.runOnClient(client -> {
				KeyBindsScreen screen = (KeyBindsScreen) client.gui.screen();

				if (screen.selectedKey != null || !legacy.matches(KeyMappingHelper.getBoundKeyOf(inScreen))) {
					throw new AssertionError("The controls screen must capture the mouse binding");
				}

				if (inScreen.isDown() || always.isDown() || inScreen.consumeClick() || always.consumeClick()) {
					throw new AssertionError("Capturing a mouse binding must not activate contextual shortcuts");
				}
			});
			context.getInput().releaseMouse(4);
			context.getInput().pressMouse(4);
			context.runOnClient(client -> {
				if (!inScreen.consumeClick() || !always.consumeClick()) {
					throw new AssertionError("Mouse shortcuts must resume after binding capture");
				}
			});
		} finally {
			context.getInput().releaseMouse(4);
			context.runOnClient(client -> {
				legacy.setKey(previousKey);
				inScreen.setKey(inScreen.getDefaultKey());
				always.setKey(always.getDefaultKey());
				KeyMapping.resetMapping();
			});
		}
	}

	private static void openKeyCapture(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyBindsScreen screen = new KeyBindsScreen(client.gui.screen(), client.options);
			client.gui.setScreen(screen);
			screen.selectedKey = legacy;
		});
	}

	private static void testDebugKeyCapture(ClientGameTestContext context) {
		Screen previousScreen = context.computeOnClient(client -> client.gui.screen());
		InputConstants.Key jumpKey = context.computeOnClient(client -> KeyMappingHelper.getBoundKeyOf(client.options.keyJump));
		boolean advancedTooltips = context.computeOnClient(client -> client.options.advancedItemTooltips);
		boolean debugOverlayVisible = context.computeOnClient(client -> client.debugEntries.isOverlayVisible());

		try {
			context.runOnClient(client -> {
				KeyBindsScreen screen = new KeyBindsScreen(previousScreen, client.options);
				client.gui.setScreen(screen);
				screen.selectedKey = client.options.keyJump;
			});
			context.getInput().holdKey(options -> options.keyDebugModifier);
			context.runOnClient(client -> {
				KeyBindsScreen screen = (KeyBindsScreen) client.gui.screen();
				InputConstants.Key boundJumpKey = KeyMappingHelper.getBoundKeyOf(client.options.keyJump);
				InputConstants.Key debugModifierKey = KeyMappingHelper.getBoundKeyOf(client.options.keyDebugModifier);

				if (screen.selectedKey != null || !boundJumpKey.equals(debugModifierKey)) {
					throw new AssertionError("The controls screen must capture the debug modifier for rebinding");
				}
			});
			context.getInput().pressKey(options -> options.keyDebugShowAdvancedTooltips);
			context.runOnClient(client -> {
				if (client.options.advancedItemTooltips != advancedTooltips) {
					throw new AssertionError("A captured debug modifier must not activate vanilla debug shortcuts");
				}
			});
		} finally {
			context.getInput().releaseKey(options -> options.keyDebugShowAdvancedTooltips);
			context.getInput().releaseKey(options -> options.keyDebugModifier);
			context.runOnClient(client -> {
				client.options.keyJump.setKey(jumpKey);
				KeyMapping.resetMapping();
				client.options.advancedItemTooltips = advancedTooltips;
				client.options.save();
				client.debugEntries.setOverlayVisible(debugOverlayVisible);
				client.gui.setScreen(previousScreen);
			});
		}
	}
}
