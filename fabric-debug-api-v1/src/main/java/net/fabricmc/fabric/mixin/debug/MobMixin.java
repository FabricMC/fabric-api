package net.fabricmc.fabric.mixin.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Mob;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;

/// The Mob class does not super-call
/// [net.minecraft.world.entity.Entity#registerDebugValues], so we have to
/// duplicate some code.
@Mixin(Mob.class)
public abstract class MobMixin {
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
