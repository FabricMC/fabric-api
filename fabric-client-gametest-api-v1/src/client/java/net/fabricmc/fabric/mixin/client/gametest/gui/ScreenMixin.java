package net.fabricmc.fabric.mixin.client.gametest.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public class ScreenMixin {
	@ModifyReturnValue(method = "allowRotatingPanorama", at = @At("RETURN"))
	private boolean disableRotatingPanoramaForClientGameTests(boolean original) {
		return false;
	}
}
