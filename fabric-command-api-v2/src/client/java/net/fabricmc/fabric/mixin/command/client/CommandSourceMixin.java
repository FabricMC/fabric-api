package net.fabricmc.fabric.mixin.command.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.command.CommandSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSource.class)
public class CommandSourceMixin {

	// Minecraft is hardcoded to only autofill identifiers with the "minecraft" namespace. This cancels the check
	@WrapOperation(
			method = "forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V",
			at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0)
	)
	private static boolean cancelNamespaceCheck(String instance, Object o, Operation<Boolean> original) {
		return true;
	}

}
