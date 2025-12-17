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
 * The Fabric screen handler API for creating screen handlers and screen handler types.
 *
 * <p>Screen handlers types are used to synchronize {@linkplain net.minecraft.world.inventory.AbstractContainerMenu screen handlers}
 * between the server and the client. Their main job is to create screen handler instances on the client.
 * Screen handlers manage the items and integer properties that are
 * needed to show on screens, such as the items in a chest or the progress of a furnace.
 *
 * <h2>Simple and extended screen handlers</h2>
 * "Simple" screen handlers are the type of screen handlers used in vanilla.
 * They can automatically synchronize items and integer properties between the server and the client,
 * but they don't support having custom data sent in the opening packet.
 * You can create simple screen handlers using vanilla's {@link net.minecraft.world.inventory.MenuType}.
 *
 * <p>This module adds <i>extended screen handlers</i> that can synchronize their own custom data
 * when they are opened, which can be useful for defining additional properties of a screen on the server.
 * For example, a mod can synchronize text that will show up as a label.
 * You can create extended screen handlers using
 * {@link net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType}.
 *
 * <h2>Opening screen handlers</h2>
 * Screen handlers can be opened using
 * {@link net.minecraft.world.entity.player.Player#openMenu(net.minecraft.world.MenuProvider)}.
 * Note that calling it on the logical client does nothing. To open an extended screen handler, the factory passed in
 * should be an {@link net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory}, or a
 * {@link net.minecraft.world.SimpleMenuProvider} that wraps such factory.
 *
 * <h2>Overwriting screen handlers</h2>
 * You might have noticed that calling {@link net.minecraft.world.entity.player.Player#openMenu(net.minecraft.world.MenuProvider)}  openHandledScreen} while on another screen will move
 * the cursor to the center of the screen. This is because the current screen gets closed before
 * opening the screen, resetting the cursor position. Since this behavior can be problematic,
 * this API provides a way to disable this. By overriding {@link
 * net.fabricmc.fabric.api.screenhandler.v1.FabricScreenHandlerFactory#shouldCloseCurrentScreen()}
 * on the screen handler factory to return {@code false} and passing that to the {@code
 * openHandledScreen} method, it will stop closing the screen and instead "overwrites" it.
 */
@NullMarked
package net.fabricmc.fabric.api.screenhandler.v1;

import org.jspecify.annotations.NullMarked;
