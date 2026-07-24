package net.fabricmc.fabric.mixin.debug.client;

import net.fabricmc.fabric.impl.debug.client.DebugKeyBindingRegistryImpl;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {

	@Inject(
			method = "handleDebugKeys",
			at = @At("RETURN"),
			cancellable = true
	)
	private void onHandleDebugKeys(
			KeyEvent event,
			CallbackInfoReturnable<Boolean> cir
	) {
		boolean debugAction = cir.getReturnValue();
		cir.setReturnValue(DebugKeyBindingRegistryImpl.invoke(event, debugAction));
	}

}
