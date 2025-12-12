package net.fabricmc.fabric.test.rendering.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.debug.ServerDebugSubscribers;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(ServerDebugSubscribers.class)
public abstract class ServerDebugSubscribersMixin {
	@Definition(
			id = "IS_RUNNING_IN_IDE",
			field = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"
	)
	@Expression("IS_RUNNING_IN_IDE")
	@WrapOperation(
			method = "hasRequiredPermissions",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean requireInIde(Operation<Boolean> original) {
		return original.call() || FabricLoader.getInstance()
				.isDevelopmentEnvironment();
	}
}
