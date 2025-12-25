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

/**
 * The Fabric menu API for creating menu and menu types.
 *
 * <p>Menu types are used to synchronize {@linkplain net.minecraft.world.inventory.AbstractContainerMenu menus}
 * between the server and the client. Their main job is to create menu instances on the client.
 * Menus manage the items and integer properties that are
 * needed to show on screens, such as the items in a chest or the progress of a furnace.
 *
 * <h2>Simple and extended menus</h2>
 * "Simple" menus are the type of menus used in vanilla.
 * They can automatically synchronize items and integer properties between the server and the client,
 * but they don't support having custom data sent in the opening packet.
 * You can create simple menus using vanilla's {@link net.minecraft.world.inventory.MenuType}.
 *
 * <p>This module adds <i>extended menus</i> that can synchronize their own custom data
 * when they are opened, which can be useful for defining additional properties of a screen on the server.
 * For example, a mod can synchronize text that will show up as a label.
 * You can create extended menus using
 * {@link net.fabricmc.fabric.api.menu.v1.ExtendedMenuType}.
 *
 * <h2>Opening menus</h2>
 * Menus can be opened using
 * {@link net.minecraft.world.entity.player.Player#openMenu(net.minecraft.world.MenuProvider)}.
 * Note that calling it on the logical client does nothing. To open an extended menu, the factory passed in
 * should be an {@link net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider}, or a
 * {@link net.minecraft.world.SimpleMenuProvider} that wraps such factory.
 *
 * <h2>Overwriting menus</h2>
 * You might have noticed that calling {@link net.minecraft.world.entity.player.Player#openMenu(net.minecraft.world.MenuProvider)  openMenu} while on another screen will move
 * the cursor to the center of the screen. This is because the current screen gets closed before
 * opening the screen, resetting the cursor position. Since this behavior can be problematic,
 * this API provides a way to disable this. By overriding {@link
 * net.fabricmc.fabric.api.menu.v1.FabricMenuProvider#shouldCloseCurrentScreen()}
 * on the menu factory to return {@code false} and passing that to the {@code
 * openMenu} method, it will stop closing the screen and instead "overwrites" it.
 */
@NullMarked
package net.fabricmc.fabric.api.menu.v1;

import org.jspecify.annotations.NullMarked;
