package net.fabricmc.fabric.test.rendering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Mannequin;

import net.fabricmc.fabric.test.rendering.DebugSubscriptions;
import net.fabricmc.fabric.test.rendering.SusDebugInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(
			method = "registerDebugValues",
			at = @At("HEAD")
	)
	private void addDebugData(
			ServerLevel serverLevel,
			DebugValueSource.Registration registration,
			CallbackInfo ci
	) {
		if ((Entity) (Object) this instanceof Avatar avatar) {
			registration.register(DebugSubscriptions.SUS_AVATAR, () -> new SusDebugInfo(
					avatar.getPlainTextName(),
					avatar instanceof Mannequin
			));
		}
	}
}
