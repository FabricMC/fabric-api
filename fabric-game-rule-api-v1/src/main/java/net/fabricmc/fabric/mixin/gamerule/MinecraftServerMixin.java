package net.fabricmc.fabric.mixin.gamerule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.impl.gamerule.GameRuleEventsImpl;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "onGameRuleUpdated", at = @At("RETURN"))
	private <T> void handleGameRuleUpdate(GameRule<T> rule, T value, CallbackInfo ci) {
		Event<GameRuleEvents.ValueUpdate<T>> event = GameRuleEventsImpl.getValueUpdate(rule);

		if (event != null) {
			event.invoker().onGameRuleUpdated(value, (MinecraftServer) (Object) this);
		}
	}
}
