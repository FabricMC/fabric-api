package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * An extension for {@link MobEffect} subclasses adding basic events.
 */
public interface FabricMobEffectEventHandlers {
	/**
	 * Called after an {@linkplain MobEffectInstance instance of this effect} has been added to a {@linkplain LivingEntity living entity}.
	 *
	 * @param effectInstance an instance of this effect
	 * @param entity the entity the effect instance is being applied to
	 */
	default void onEffectAdded(MobEffectInstance effectInstance, LivingEntity entity) {
	}

	/**
	 * Called after an {@linkplain MobEffectInstance instance of this effect} has been removed from a {@linkplain LivingEntity living entity}.
	 *
	 * @param effectInstance an instance of this effect
	 * @param entity the entity the effect instance is being removed from
	 */
	default void onEffectRemoved(MobEffectInstance effectInstance, LivingEntity entity) {
	}
}
