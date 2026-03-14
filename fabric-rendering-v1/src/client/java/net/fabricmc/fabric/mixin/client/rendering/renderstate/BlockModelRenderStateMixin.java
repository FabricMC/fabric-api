package net.fabricmc.fabric.mixin.client.rendering.renderstate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.BlockModelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;

@Mixin(BlockModelRenderState.class)
abstract class BlockModelRenderStateMixin {
	@Inject(method = "clear", at = @At("TAIL"))
	private void clearExtraRenderData(CallbackInfo ci) {
		((FabricRenderState) this).clearExtraData();
	}
}
