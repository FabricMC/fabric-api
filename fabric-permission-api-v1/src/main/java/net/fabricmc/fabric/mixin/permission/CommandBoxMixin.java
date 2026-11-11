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

package net.fabricmc.fabric.mixin.permission;

import java.util.function.Predicate;

import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandBox;
import net.minecraft.server.permissions.PermissionSetSupplier;

import net.fabricmc.fabric.impl.permission.FabricPermissionCommandCheck;

@Mixin(CommandBox.class)
class CommandBoxMixin {
	@Inject(method = "canNodeRun", at = @At(value = "INVOKE", target = "Ljava/lang/UnsupportedOperationException;<init>(Ljava/lang/String;)V"), cancellable = true)
	private static void handleFabricPermissionCheck(CommandNode<? extends PermissionSetSupplier> node, PermissionSetSupplier context, CallbackInfoReturnable<Boolean> cir) {
		if (node.getRequirement() instanceof FabricPermissionCommandCheck<?> check) {
			cir.setReturnValue(check.cast().test(context));
		}
	}

	@Inject(method = "convertNodePermissionCheck", at = @At(value = "INVOKE", target = "Ljava/lang/UnsupportedOperationException;<init>(Ljava/lang/String;)V"), cancellable = true)
	private static void convertFabricPermissionCheck(Predicate<? extends PermissionSetSupplier> original, CallbackInfoReturnable<Predicate<PermissionSetSupplier>> cir) {
		if (original instanceof FabricPermissionCommandCheck<?> owner) {
			cir.setReturnValue(owner.cast());
		}
	}
}
