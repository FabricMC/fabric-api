package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * An extension for {@link MobEffect} subclasses adding basic events.
 */
public interface FabricMobEffectEventHandlers {
	default void onEffectAdded(MobEffectInstance effectInstance, LivingEntity entity) {
	}

	default void onEffectRemoved(MobEffectInstance effectInstance, LivingEntity entity) {
	}
}
