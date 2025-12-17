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

package net.fabricmc.fabric.test.screenhandler.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.test.screenhandler.ScreenHandlerTest;

public class ClientScreenHandlerTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ScreenHandlerTest.BAG_SCREEN_HANDLER, DispenserScreen::new);
		MenuScreens.register(ScreenHandlerTest.POSITIONED_BAG_SCREEN_HANDLER, PositionedScreen::new);
		MenuScreens.register(ScreenHandlerTest.BOX_SCREEN_HANDLER, PositionedScreen::new);
	}
}
