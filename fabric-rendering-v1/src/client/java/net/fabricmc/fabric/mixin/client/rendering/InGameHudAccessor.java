package net.fabricmc.fabric.mixin.client.rendering;

import net.minecraft.client.gui.hud.InGameHud;

import net.minecraft.entity.LivingEntity;

import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
	@Accessor("renderHealthValue")
	int getRenderHealthValue();

	@Invoker("getRiddenEntity")
	LivingEntity callGetRiddenEntity();

	@Invoker("getHeartCount")
	int callGetHeartCount(LivingEntity entity);

	@Invoker("getHeartRows")
	int callGetHeartRows(int health);

	@Invoker("getCameraPlayer")
	PlayerEntity callGetCameraPlayer();
}
