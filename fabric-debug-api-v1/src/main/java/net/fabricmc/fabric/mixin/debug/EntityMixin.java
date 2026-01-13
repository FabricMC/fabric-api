package net.fabricmc.fabric.mixin.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(
			method = "registerDebugValues",
			at = @At("HEAD")
	)
	private void addDebugValues(
			ServerLevel level,
			DebugValueSource.Registration registration,
			CallbackInfo ci
	) {
		EntityDebugSubscriptionRegistryImpl.addDebugValues(this, registration);
	}
}
