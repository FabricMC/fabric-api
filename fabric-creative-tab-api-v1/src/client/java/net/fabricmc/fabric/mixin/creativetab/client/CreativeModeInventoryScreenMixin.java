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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ItemTabButton;

import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;

import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements FabricCreativeModeInventoryScreen {
	CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory playerInventory, Component component) {
		super(menu, playerInventory, component);
	}

	@Shadow
	protected abstract void selectTab(CreativeModeTab creativeModeTab_1);

	@Shadow
	private static CreativeModeTab selectedTab;

	@Shadow
	private EditBox searchBox;
	@Shadow
	private TabNavigationBar tabBarTop;
	@Shadow
	private TabNavigationBar tabBarBottom;
	@Mutable
	@Shadow
	@Final
	private @Nullable TabManager tabManager;

	@Shadow
	protected abstract void refreshSearchResults();

	@Shadow
	private CreativeInventoryListener listener;
	@Shadow
	@Final
	private static Component TOP_TAB_BAR_NAME;
	@Shadow
	@Final
	private static Component BOTTOM_TAB_BAR_NAME;

	@Shadow
	protected abstract void showTab(CreativeModeTab tab);

	@Shadow
	private @Nullable List<Slot> originalSlots;
	// "static" matches selectedTab
	@Unique
	private static int currentPage = 0;

	TabManager newTabManager() {
		return new TabManager((var0) -> {
		}, (var0) -> {
		}, (tab) -> {
			if (tab instanceof CreativeModeInventoryScreen.CreativeModeTabAdapter tabAdapter) {
				this.showTab(tabAdapter.tab());
			}

		}, (tab) -> {
			if (tab instanceof CreativeModeInventoryScreen.CreativeModeTabAdapter tabAdapter) {
				if (tabAdapter.tab().getType() == CreativeModeTab.Type.INVENTORY) {
					((ItemPickerMenu)this.menu).slots.clear();
					((ItemPickerMenu)this.menu).slots.addAll(this.originalSlots);
					this.originalSlots = null;
				}
			}

		});
	}

	@Unique
	private void updateSelection() {
		apply(groups.get(getCurrentPage()));
		if (!isTabVisible(selectedTab)) {
			CreativeModeTabs.allTabs()
					.stream()
					.filter(this::isTabVisible)
					.min((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight()))
					.ifPresent(this::selectTab);
		}
	}

	void apply(group group) {
		if (tabBarTop != null) tabBarTop.children().forEach(a -> {
			if (a instanceof TabButton tabButton) tabButton.setVisible(false);
		});
		if (tabBarBottom != null) tabBarBottom.children().forEach(a -> {
			if (a instanceof TabButton tabButton) tabButton.setVisible(false);
		});
		this.tabBarTop = group.top;
		tabBarTop.children().forEach(a -> {
			if (a instanceof TabButton tabButton) tabButton.setVisible(true);
		});
		this.tabBarBottom = group.bottom;
		tabBarBottom.children().forEach(a -> {
			if (a instanceof TabButton tabButton) tabButton.setVisible(true);
		});
		this.tabManager = group.manager;
	}
	List<group> groups = List.of();
	record group(TabNavigationBar top, TabNavigationBar bottom, TabManager manager) {}
	/**
	 * @author go away mcdev
	 * @reason
	 */
	@Overwrite
	protected void init() {
		if (!this.minecraft.player.hasInfiniteMaterials()) {
			this.minecraft.gui.setScreen(new InventoryScreen(this.minecraft.player));
		} else {
			super.init();
			TabNavigationBar.Builder tabBarTopBuilder = TabNavigationBar.builder(this.tabManager, this.leftPos, this.topPos - 32 + 4, this.imageWidth, 32, LinearLayout.Orientation.HORIZONTAL).spacing(1).name(TOP_TAB_BAR_NAME);
			TabNavigationBar.Builder tabBarBottomBuilder = TabNavigationBar.builder(this.tabManager, this.leftPos, this.topPos + this.imageHeight - 4, this.imageWidth, 32, LinearLayout.Orientation.HORIZONTAL).spacing(1).name(BOTTOM_TAB_BAR_NAME);
			List<group> groups = new ArrayList<>();
			int limit = 0;
			TabManager tabManager1 = tabManager;
			for(CreativeModeTab tab : CreativeModeTabs.tabs()) {
				if (tab.getType() != CreativeModeTab.Type.CATEGORY) continue;
				boolean isTop = limit < 5;// tab.row() == CreativeModeTab.Row.TOP;
				int index = Math.clamp((long)tab.column(), 0, 6) + 1;
				String row = isTop ? "top" : "bottom";
				WidgetSprites tabSprite = new WidgetSprites(Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_unselected_" + index), Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_selected_" + index));
				CreativeModeInventoryScreen.CreativeModeTabAdapter tabAdapter = CreativeModeInventoryScree2nMixin.CreativeModeTabAdapter(tab);
				ItemTabButton tabButton = ItemTabButton.builder(this.tabManager, tabAdapter, tabSprite).size(26, 32).icon(tab.getIconItem()).iconYOffset(isTop ? 1 : -1).message(tab.getDisplayName()).build();
				TabNavigationBar.Builder tabBarBuilder = isTop ? tabBarTopBuilder : tabBarBottomBuilder;
				TabNavigationBar.Align align = tab.isAlignedRight() ? TabNavigationBar.Align.RIGHT_OR_BOTTOM : TabNavigationBar.Align.LEFT_OR_TOP;
				tabBarBuilder.addTabToGroup(align, tabButton, tabAdapter);
				limit++;
				if (limit >= FabricCreativeModeTabImpl.TABS_PER_PAGE) {
					for(CreativeModeTab tab2 : CreativeModeTabs.tabs()) {
						if (tab2.getType() == CreativeModeTab.Type.CATEGORY) continue;
						isTop = tab2.row() == CreativeModeTab.Row.TOP;
						index = Math.clamp((long) tab2.column(), 0, 6) + 1;
						row = isTop ? "top" : "bottom";
						tabSprite = new WidgetSprites(Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_unselected_" + index), Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_selected_" + index));
						tabAdapter = CreativeModeInventoryScree2nMixin.CreativeModeTabAdapter(tab2);
						tabButton = ItemTabButton.builder(this.tabManager, tabAdapter, tabSprite).size(26, 32).icon(tab2.getIconItem()).iconYOffset(isTop ? 1 : -1).message(tab2.getDisplayName()).build();
						tabBarBuilder = isTop ? tabBarTopBuilder : tabBarBottomBuilder;
						align = tab2.isAlignedRight() ? TabNavigationBar.Align.RIGHT_OR_BOTTOM : TabNavigationBar.Align.LEFT_OR_TOP;
						tabBarBuilder.addTabToGroup(align, tabButton, tabAdapter);
					}
					limit = 0;
					groups.add(new group((TabNavigationBar)this.addWidget(tabBarTopBuilder.build()), (TabNavigationBar)this.addWidget(tabBarBottomBuilder.build()), tabManager));
//					this.tabBarBottom = ;
					tabBarTopBuilder = TabNavigationBar.builder(this.tabManager, this.leftPos, this.topPos - 32 + 4, this.imageWidth, 32, LinearLayout.Orientation.HORIZONTAL).spacing(1).name(TOP_TAB_BAR_NAME);
					tabBarBottomBuilder = TabNavigationBar.builder(this.tabManager, this.leftPos, this.topPos + this.imageHeight - 4, this.imageWidth, 32, LinearLayout.Orientation.HORIZONTAL).spacing(1).name(BOTTOM_TAB_BAR_NAME);
					this.tabManager = newTabManager();
				}
			}
			if (limit > 0) {
				for(CreativeModeTab tab2 : CreativeModeTabs.tabs()) {
					if (tab2.getType() == CreativeModeTab.Type.CATEGORY) continue;
					var isTop = tab2.row() == CreativeModeTab.Row.TOP;
					var index = Math.clamp((long) tab2.column(), 0, 6) + 1;
					var row = isTop ? "top" : "bottom";
					var tabSprite = new WidgetSprites(Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_unselected_" + index), Identifier.withDefaultNamespace("container/creative_inventory/tab_" + row + "_selected_" + index));
					var tabAdapter = CreativeModeInventoryScree2nMixin.CreativeModeTabAdapter(tab2);
					var tabButton = ItemTabButton.builder(this.tabManager, tabAdapter, tabSprite).size(26, 32).icon(tab2.getIconItem()).iconYOffset(isTop ? 1 : -1).message(tab2.getDisplayName()).build();
					var tabBarBuilder = isTop ? tabBarTopBuilder : tabBarBottomBuilder;
					var align = tab2.isAlignedRight() ? TabNavigationBar.Align.RIGHT_OR_BOTTOM : TabNavigationBar.Align.LEFT_OR_TOP;
					tabBarBuilder.addTabToGroup(align, tabButton, tabAdapter);
				}
				groups.add(new group((TabNavigationBar)this.addWidget(tabBarTopBuilder.build()), (TabNavigationBar)this.addWidget(tabBarBottomBuilder.build()), tabManager));
			}
			tabManager = tabManager1;
			this.groups = groups;
			apply(groups.getFirst());
//			this.tabBarTop = (TabNavigationBar)this.addWidget(tabBarTopBuilder.build());
//			this.tabBarBottom = (TabNavigationBar)this.addWidget(tabBarBottomBuilder.build());
			Font var10003 = this.font;
			int var10004 = this.leftPos + 82;
			int var10005 = this.topPos + 6;
			Objects.requireNonNull(this.font);
			this.searchBox = new EditBox(var10003, var10004, var10005, 80, 9, Component.translatable("itemGroup.search"));
			this.searchBox.setMaxLength(50);
			this.searchBox.setBordered(false);
			this.searchBox.setVisible(false);
			this.searchBox.setTextColor(-1);

			currentPage = getPage(selectedTab);

			int xpos = leftPos + 171;
			int ypos = topPos + 4;

			CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
			addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos + 10, ypos, FabricCreativeGuiComponents.Type.NEXT, self));
			addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos, ypos, FabricCreativeGuiComponents.Type.PREVIOUS, self));


			this.searchBox.setInvertHighlightedTextColor(false);
			this.searchBox.setResponder((value) -> {
				if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
					this.refreshSearchResults();
				}

			});
			this.addRenderableWidget(this.searchBox);
			this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
			this.listener = new CreativeInventoryListener(this.minecraft);
			this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
			if (!selectedTab.shouldDisplay()) {
				selectedTab = CreativeModeTabs.getDefaultTab();
			}

			this.selectTab(selectedTab);
		}
	}
//
//	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setTextColor(I)V", shift = At.Shift.AFTER))
//	private void init(CallbackInfo info) {
//
//	}

	@Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
	private void setSelectedTab(CreativeModeTab creativeModeTab, CallbackInfo info) {
		if (!isTabVisible(creativeModeTab)) {
			info.cancel();
		}
	}

//	@Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
//	private void renderTabTooltipIfHovered(GuiGraphicsExtractor graphics, CreativeModeTab creativeModeTab, int mx, int my, CallbackInfoReturnable<Boolean> info) {
//		if (!isTabVisible(creativeModeTab)) {
//			info.setReturnValue(false);
//		}
//	}
//
//	@Inject(method = "checkTabClicked", at = @At("HEAD"), cancellable = true)
//	private void isClickInTab(CreativeModeTab creativeModeTab, double mx, double my, CallbackInfoReturnable<Boolean> info) {
//		if (!isTabVisible(creativeModeTab)) {
//			info.setReturnValue(false);
//		}
//	}

//	@WrapOperation(method = "extractUnselectedTabButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/TabButton;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
//	private void extractTabButton(TabButton button, GuiGraphicsExtractor guiGraphicsExtractor, Operation<Void> original) {
//		if (button.tab() instanceof CreativeModeInventoryScreen.CreativeModeTabAdapter tabAdapter) {
//			if (isTabVisible(tabAdapter.tab())) {
//				original.call(button, guiGraphicsExtractor);
//			}
//		}
//	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void keyPressed(KeyEvent context, CallbackInfoReturnable<Boolean> cir) {
		if (context.key() == InputConstants.KEY_PAGEUP) {
			if (switchToPreviousPage()) {
				cir.setReturnValue(true);
			}
		} else if (context.key() == InputConstants.KEY_PAGEDOWN) {
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
		CreativeModeTab.ItemDisplayParameters parameters = CreativeModeTabs.CACHED_PARAMETERS;
		return parameters != null && CreativeModeTabs.tabs().size() > (parameters.hasPermissions() ? 14 : 13);
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
