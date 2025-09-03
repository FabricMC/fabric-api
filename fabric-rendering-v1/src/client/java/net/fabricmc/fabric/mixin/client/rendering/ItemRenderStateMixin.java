package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;

import net.minecraft.client.render.item.ItemRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderState.class)
public abstract class ItemRenderStateMixin {

	@Inject(method = "clear", at = @At("TAIL"))
	private void clearExtraRenderData(CallbackInfo ci) {
		((FabricRenderState) this).clearData();
	}

}
