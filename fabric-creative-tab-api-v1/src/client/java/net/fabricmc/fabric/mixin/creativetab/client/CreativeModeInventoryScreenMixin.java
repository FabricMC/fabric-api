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

package net.fabricmc.fabric.mixin.creativetab.client;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.impl.client.creativetab.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabImpl;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements FabricCreativeModeInventoryScreen {
	public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory playerInventory, Component component) {
		super(menu, playerInventory, component);
	}

	@Shadow
	protected abstract void selectTab(CreativeModeTab creativeModeTab_1);

	@Shadow
	private static CreativeModeTab selectedTab;

	// "static" matches selectedTab
	@Unique
	private static int currentPage = 0;

	@Unique
	private void updateSelection() {
		if (!isTabVisible(selectedTab)) {
			CreativeModeTabs.allTabs()
					.stream()
					.filter(this::isTabVisible)
					.min((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight()))
					.ifPresent(this::selectTab);
		}
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setTextColor(I)V", shift = At.Shift.AFTER))
	private void init(CallbackInfo info) {
		currentPage = getPage(selectedTab);

		int xpos = leftPos + 171;
		int ypos = topPos + 4;

		CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
		addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos + 10, ypos, FabricCreativeGuiComponents.Type.NEXT, self));
		addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos, ypos, FabricCreativeGuiComponents.Type.PREVIOUS, self));
	}

	@Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
	private void setSelectedTab(CreativeModeTab creativeModeTab, CallbackInfo info) {
		if (!isTabVisible(creativeModeTab)) {
			info.cancel();
		}
	}

	@Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
	private void renderTabTooltipIfHovered(GuiGraphicsExtractor graphics, CreativeModeTab creativeModeTab, int mx, int my, CallbackInfoReturnable<Boolean> info) {
		if (!isTabVisible(creativeModeTab)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "checkTabClicked", at = @At("HEAD"), cancellable = true)
	private void isClickInTab(CreativeModeTab creativeModeTab, double mx, double my, CallbackInfoReturnable<Boolean> info) {
		if (!isTabVisible(creativeModeTab)) {
			info.setReturnValue(false);
		}
	}

	@Inject(method = "extractTabButton", at = @At("HEAD"), cancellable = true)
	private void extractTabButton(GuiGraphicsExtractor guiGraphics, int i, int j, CreativeModeTab creativeModeTab, CallbackInfo info) {
		if (!isTabVisible(creativeModeTab)) {
			info.cancel();
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void keyPressed(KeyEvent context, CallbackInfoReturnable<Boolean> cir) {
		if (context.key() == GLFW.GLFW_KEY_PAGE_UP) {
			if (switchToPreviousPage()) {
				cir.setReturnValue(true);
			}
		} else if (context.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
			if (switchToNextPage()) {
				cir.setReturnValue(true);
			}
		}
	}

	@Unique
	private boolean isTabVisible(CreativeModeTab creativeModeTab) {
		return creativeModeTab.shouldDisplay() && currentPage == getPage(creativeModeTab);
	}

	@Override
	public int getPage(CreativeModeTab creativeModeTab) {
		if (FabricCreativeGuiComponents.COMMON_TABS.contains(creativeModeTab)) {
			return currentPage;
		}

		final FabricCreativeModeTabImpl fabriccreativeModeTab = (FabricCreativeModeTabImpl) creativeModeTab;
		return fabriccreativeModeTab.fabric_getPage();
	}

	@Unique
	private boolean hasGroupForPage(int page) {
		return CreativeModeTabs.tabs()
				.stream()
				.anyMatch(creativeModeTab -> getPage(creativeModeTab) == page);
	}

	@Override
	public boolean switchToPage(int page) {
		if (!hasGroupForPage(page)) {
			return false;
		}

		if (currentPage == page) {
			return false;
		}

		currentPage = page;
		updateSelection();
		return true;
	}

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	@Override
	public int getPageCount() {
		return FabricCreativeGuiComponents.getPageCount();
	}

	@Override
	public List<CreativeModeTab> getTabsOnPage(int page) {
		return CreativeModeTabs.tabs()
				.stream()
				.filter(creativeModeTab -> getPage(creativeModeTab) == page)
				// Thanks to isXander for the sorting
				.sorted(Comparator.comparing(CreativeModeTab::row).thenComparingInt(CreativeModeTab::column))
				.sorted((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight()))
				.toList();
	}

	@Override
	public boolean hasAdditionalPages() {
		return CreativeModeTabs.tabs().size() > (Objects.requireNonNull(CreativeModeTabs.CACHED_PARAMETERS).hasPermissions() ? 14 : 13);
	}

	@Override
	public CreativeModeTab getSelectedTab() {
		return selectedTab;
	}

	@Override
	public boolean setSelectedTab(CreativeModeTab creativeModeTab) {
		Objects.requireNonNull(creativeModeTab, "creativeModeTab");

		if (selectedTab == creativeModeTab) {
			return false;
		}

		if (currentPage != getPage(creativeModeTab)) {
			if (!switchToPage(getPage(creativeModeTab))) {
				return false;
			}
		}

		selectTab(creativeModeTab);
		return true;
	}
}
