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

package net.fabricmc.fabric.mixin.menu;

import java.util.Objects;
import java.util.OptionalInt;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.impl.menu.Networking;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	@Shadow
	private int containerCounter;

	private ServerPlayerMixin(Level level, GameProfile gameProfile) {
		super(level, gameProfile);
	}

	@Shadow
	public abstract void closeContainer();

	@Redirect(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
	private void fabric_closeContainerScreenIfAllowed(ServerPlayer player, MenuProvider factory) {
		if (factory.shouldCloseCurrentScreen()) {
			this.closeContainer();
		} else {
			// Called by closeContainer in vanilla
			this.doCloseContainer();
		}
	}

	@Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private void fabric_storeOpenedMenu(MenuProvider factory, CallbackInfoReturnable<OptionalInt> info, @Local(name = "menu") AbstractContainerMenu menu) {
		if (factory instanceof ExtendedMenuProvider || (factory instanceof SimpleMenuProvider simpleFactory && simpleFactory.menuConstructor instanceof ExtendedMenuProvider)) {
			// Set the menu, so the factory method can access it through the player.
			containerMenu = menu;
		} else if (menu.getType() instanceof ExtendedMenuType<?, ?>) {
			Identifier id = BuiltInRegistries.MENU.getKey(menu.getType());
			throw new IllegalArgumentException("[Fabric] Extended menu " + id + " must be opened with an ExtendedMenuProvider!");
		}
	}

	@Redirect(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private void fabric_replaceVanillaScreenPacket(ServerGamePacketListenerImpl networkHandler, Packet<?> packet, MenuProvider factory) {
		if (factory instanceof SimpleMenuProvider simpleProvider && simpleProvider.menuConstructor instanceof ExtendedMenuProvider<?> extendedProvider) {
			factory = extendedProvider;
		}

		if (factory instanceof ExtendedMenuProvider<?> extendedFactory) {
			AbstractContainerMenu handler = Objects.requireNonNull(containerMenu);

			if (handler.getType() instanceof ExtendedMenuType<?, ?>) {
				Networking.sendOpenPacket((ServerPlayer) (Object) this, extendedFactory, handler, containerCounter);
			} else {
				Identifier id = BuiltInRegistries.MENU.getKey(handler.getType());
				throw new IllegalArgumentException("[Fabric] Non-extended menu " + id + " must not be opened with an ExtendedMenuProvider!");
			}
		} else {
			// Use vanilla logic for non-extended menus
			networkHandler.send(packet);
		}
	}
}
