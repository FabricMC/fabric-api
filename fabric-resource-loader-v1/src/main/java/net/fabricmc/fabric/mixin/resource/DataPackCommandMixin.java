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

package net.fabricmc.fabric.mixin.resource;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import net.fabricmc.fabric.impl.resource.pack.FabricPack;

/**
 * Disables enabling/disabling internal data packs.
 * Listing them is still allowed, but they do not appear in suggestions.
 */
@Mixin(DataPackCommand.class)
public class DataPackCommandMixin {
	@Unique
	private static final DynamicCommandExceptionType INTERNAL_PACK_EXCEPTION = new DynamicCommandExceptionType(
			packName -> Component.translatableEscape("commands.datapack.fabric.internal", packName));

	@Redirect(method = "method_13136", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getSelectedIds()Ljava/util/Collection;"))
	private static Collection<String> filterEnabledPackSuggestions(PackRepository dataPackManager) {
		return dataPackManager.getSelectedPacks().stream().filter(profile -> !((FabricPack) profile).fabric$isHidden()).map(Pack::getId).toList();
	}

	@WrapOperation(method = "method_13120", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0))
	private static Stream<Pack> filterDisabledPackSuggestions(Stream<Pack> instance, Predicate<? super Pack> predicate, Operation<Stream<Pack>> original) {
		return original.call(instance, predicate).filter(profile -> !((FabricPack) profile).fabric$isHidden());
	}

	@Inject(method = "getPack", at = @At(value = "INVOKE", target = "Ljava/util/Collection;contains(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE))
	private static void errorOnInternalPack(CommandContext<CommandSourceStack> context, String name, boolean enable, CallbackInfoReturnable<Pack> cir, @Local Pack profile) throws CommandSyntaxException {
		if (((FabricPack) profile).fabric$isHidden()) throw INTERNAL_PACK_EXCEPTION.create(profile.getId());
	}
}
