/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.entity.event.v1.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to {@linkplain MobEffect status effects} in mobs.
 *
 * <p>Status effect events are useful when implementing generic behavior for
 * many status effects and modifying the addition and removal behavior of
 * existing status effects.
 *
 * <p>When only one class of {@linkplain MobEffect status effects}
 * requires code to be run before/after the addition or removal of the effect,
 * consider using {@link MobEffect#onEffectAdded(MobEffectInstance, LivingEntity)},
 * {@link MobEffect#onEffectRemoved(MobEffectInstance, LivingEntity)}, or
 * {@link MobEffect#onEffectStarted(MobEffectInstance, LivingEntity)}.
 *
 * <p>Additionally, an {@link EffectEventContext} parameter is passed to all
 * listeners of these events.
 *
 * @see FabricMobEffect
 * @see #ALLOW_EARLY_REMOVE
 */
public final class ServerMobEffectEvents {
	/**
	 * An event that checks whether an effect may be added.
	 *
	 * <p>This event can be used to cancel effects given specific
	 * conditions such as a held item, a
	 * {@linkplain net.fabricmc.fabric.api.attachment.v1.AttachmentType data attachment},
	 * or another status effect.
	 */
	public static final Event<AllowAdd> ALLOW_ADD = EventFactory.createArrayBacked(AllowAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (AllowAdd callback : callbacks) {
			if (!callback.allowAdd(effectInstance, entity, ctx)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event that is called before an effect is added.
	 *
	 * <p>This event can be used to generalize behavior normally
	 * in the {@link MobEffect#onEffectAdded} methods to a subset
	 * of status effects or even to all status effects.
	 *
	 * @see MobEffect#onEffectAdded(MobEffectInstance, LivingEntity)
	 */
	public static final Event<BeforeAdd> BEFORE_ADD = EventFactory.createArrayBacked(BeforeAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (BeforeAdd callback : callbacks) {
			callback.beforeAdd(effectInstance, entity, ctx);
		}
	});

	/**
	 * An event that is called after an effect is added.
	 *
	 * <p>This event is useful for code that is required to run
	 * only after an effect is applied, such as a method that
	 * checks if an effect is present on an entity.
	 */
	public static final Event<AfterAdd> AFTER_ADD = EventFactory.createArrayBacked(AfterAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (AfterAdd callback : callbacks) {
			callback.afterAdd(effectInstance, entity, ctx);
		}
	});

	/**
	 * An event that checks whether an effect may be removed.
	 *
	 * <p><b>Note:</b> this event is not called when an
	 * effect expires. The behavior of effect expiry
	 * typically should not be modified.
	 *
	 * <p>This event is called when effects are removed before
	 * they expire. For example, drinking milk, drinking honey
	 * when poisoned, using a totem of undying, or using a command
	 * such as {@code /effect clear} triggers this event. If you
	 * don't want commands to be affected, an {@link EffectEventContext}
	 * parameter is passed to listeners.
	 *
	 * <p>This event can be used to cancel the removal of
	 * effects given specific conditions such as a held item, a
	 * {@linkplain net.fabricmc.fabric.api.attachment.v1.AttachmentType data attachment},
	 * or another status effect.
	 */
	public static final Event<AllowEarlyRemove> ALLOW_EARLY_REMOVE = EventFactory.createArrayBacked(AllowEarlyRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (AllowEarlyRemove callback : callbacks) {
			if (!callback.allowEarlyRemove(effectInstance, entity, ctx)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event that is called before an effect is removed.
	 *
	 * <p>This event can be used to generalize behavior normally
	 * in the {@link MobEffect#onEffectRemoved} method to a subset
	 * of status effects or even to all status effects.
	 *
	 * @see MobEffect#onEffectRemoved(MobEffectInstance, LivingEntity)
	 */
	public static final Event<BeforeRemove> BEFORE_REMOVE = EventFactory.createArrayBacked(BeforeRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (BeforeRemove callback : callbacks) {
			callback.beforeRemove(effectInstance, entity, ctx);
		}
	});

	/**
	 * An event that is called after an effect is removed.
	 *
	 * <p>This event is useful for code that is required to run
	 * only after an effect is removed, such as a method that
	 * checks if an effect is present on an entity.
	 */
	public static final Event<AfterRemove> AFTER_REMOVE = EventFactory.createArrayBacked(AfterRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
		for (AfterRemove callback : callbacks) {
			callback.afterRemove(effectInstance, entity, ctx);
		}
	});

	static {
		BEFORE_ADD.register((effectInstance, entity, ctx) -> {
			effectInstance.getEffect().value().onEffectAdded(effectInstance, entity);
		});
		AFTER_ADD.register((effectInstance, entity, ctx) -> {
			effectInstance.getEffect().value().onEffectStarted(effectInstance, entity);
		});
		BEFORE_REMOVE.register((effectInstance, entity, ctx) -> {
			effectInstance.getEffect().value().onEffectRemoved(effectInstance, entity);
		});
	}

	private ServerMobEffectEvents() {
	}

	@FunctionalInterface
	public interface AllowAdd {
		/**
		 * Checks whether an effect may be added.
		 *
		 * @param effectInstance the instance of the status effect being added
		 * @param entity the entity on which the effect is being added
		 * @param ctx context
		 * @return whether this effect may be added
		 */
		boolean allowAdd(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}

	@FunctionalInterface
	public interface BeforeAdd {
		/**
		 * Called before an effect is added.
		 *
		 * @param effectInstance the instance of the status effect being added
		 * @param entity the entity on which the effect is being added
		 * @param ctx context
		 */
		void beforeAdd(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}

	@FunctionalInterface
	public interface AfterAdd {
		/**
		 * Called after an effect is added.
		 *
		 * @param effectInstance the instance of the added status effect
		 * @param entity the entity on which the effect has been added
		 * @param ctx context
		 */
		void afterAdd(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}

	@FunctionalInterface
	public interface AllowEarlyRemove {
		/**
		 * Checks whether an effect may be removed early.
		 *
		 * <p><b>Note:</b> this event is not called when an
		 * effect expires. The behavior of effect expiry typically should not be modified.
		 *
		 * @param effectInstance the instance of the status effect being removed
		 * @param entity the entity on which the effect is being removed
		 * @param ctx context
		 * @return whether this effect may be removed
		 */
		boolean allowEarlyRemove(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}

	@FunctionalInterface
	public interface BeforeRemove {
		/**
		 * Called before an effect is removed.
		 *
		 * @param effectInstance the instance of the status effect being removed
		 * @param entity the entity on which the effect is being removed
		 * @param ctx context
		 */
		void beforeRemove(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}

	@FunctionalInterface
	public interface AfterRemove {
		/**
		 * Called after an effect is removed.
		 *
		 * @param effectInstance the instance of the removed status effect
		 * @param entity the entity from which the effect has been removed
		 * @param ctx context
		 */
		void afterRemove(MobEffectInstance effectInstance, LivingEntity entity, EffectEventContext ctx);
	}
}
