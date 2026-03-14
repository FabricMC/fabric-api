package net.fabricmc.fabric.mixin.client.rendering.renderstate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.state.level.ParticlesRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;

@Mixin(ParticlesRenderState.class)
abstract class ParticlesRenderStateMixin {
	@Inject(method = "reset", at = @At("TAIL"))
	private void clearExtraRenderData(CallbackInfo ci) {
		((FabricRenderState) this).clearExtraData();
	}
}
