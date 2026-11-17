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

package net.fabricmc.fabric.test.creativemodetab.client;

import java.util.List;

import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class CreativeModeTabClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitForScreen(CreativeModeInventoryScreen.class);
			int lastPage = context.computeOnClient(client -> ((CreativeModeInventoryScreen) client.gui.screen()).getPageCount() - 1);

			if (lastPage < 1) {
				throw new AssertionError("Expected the test mod to add multiple creative tab pages");
			}

			for (int page : List.of(0, 1, lastPage)) {
				context.runOnClient(client -> {
					CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) client.gui.screen();
					screen.switchToPage(page);
					List<CreativeModeTab> tabs = screen.getTabsOnPage(page);
					List<TabButton> buttons = screen.children().stream()
							.filter(TabNavigationBar.class::isInstance)
							.map(TabNavigationBar.class::cast)
							.flatMap(bar -> bar.children().stream())
							.map(TabButton.class::cast)
							.toList();

					if (buttons.size() != tabs.size() || buttons.size() > 14) {
						throw new AssertionError("Unexpected creative tab buttons on page " + page + ": " + buttons.size());
					}

					for (CreativeModeTab tab : tabs) {
						screen.setSelectedTab(tab);

						if (screen.getSelectedTab() != tab) {
							throw new AssertionError("Could not select " + tab.getDisplayName().getString());
						}

						if (tab.getType() == CreativeModeTab.Type.CATEGORY && !screen.getMenu().items.equals(List.copyOf(tab.getDisplayItems()))) {
							throw new AssertionError("Incorrect contents for " + tab.getDisplayName().getString());
						}
					}

					screen.setSelectedTab(tabs.getFirst());
				});
				context.takeScreenshot("creative_tabs_page_" + page);
			}

			context.runOnClient(client -> ((CreativeModeInventoryScreen) client.gui.screen()).setSelectedTab(CreativeModeTabs.getDefaultTab()));
		}
	}
}
