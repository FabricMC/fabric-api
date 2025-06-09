package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.hud.DebugHud;

import net.fabricmc.fabric.api.client.rendering.v1.GatherDebugTextEvents;

@Mixin(DebugHud.class)
abstract class DebugHudMixin {
	@Inject(method = "getLeftText", at = @At("RETURN"))
	protected void getLeftText(CallbackInfoReturnable<List<String>> ci) {
		GatherDebugTextEvents.LEFT.invoker().onGatherLeftDebugText(ci.getReturnValue());
	}

	@Inject(method = "getRightText", at = @At("RETURN"))
	protected void getRightText(CallbackInfoReturnable<List<String>> ci) {
		GatherDebugTextEvents.RIGHT.invoker().onGatherRightDebugText(ci.getReturnValue());
	}
}
