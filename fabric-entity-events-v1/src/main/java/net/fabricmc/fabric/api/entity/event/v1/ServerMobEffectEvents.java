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

package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to {@linkplain MobEffect status effects} in mobs.
 *
 * <p>TODO: improve this, look at {@link EntitySleepEvents} for inspiration
 */
public final class ServerMobEffectEvents {
	public static final Event<AllowAdd> ALLOW_ADD = EventFactory.createArrayBacked(AllowAdd.class, callbacks -> (effectInstance, entity) -> {
		for (AllowAdd callback : callbacks) {
			if (!callback.allowAdd(effectInstance, entity)) {
				return false;
			}
		}

		return true;
	});

	public static final Event<BeforeAdd> BEFORE_ADD = EventFactory.createArrayBacked(BeforeAdd.class, callbacks -> (effectInstance, entity) -> {
		for (BeforeAdd callback : callbacks) {
			callback.beforeAdd(effectInstance, entity);
		}
	});

	public static final Event<AfterAdd> AFTER_ADD = EventFactory.createArrayBacked(AfterAdd.class, callbacks -> (effectInstance, entity) -> {
		for (AfterAdd callback : callbacks) {
			callback.afterAdd(effectInstance, entity);
		}
	});

	public static final Event<AllowEarlyRemove> ALLOW_EARLY_REMOVE = EventFactory.createArrayBacked(AllowEarlyRemove.class, callbacks -> (effectInstance, entity) -> {
		for (AllowEarlyRemove callback : callbacks) {
			if (!callback.allowEarlyRemove(effectInstance, entity)) {
				return false;
			}
		}

		return true;
	});

	public static final Event<BeforeRemove> BEFORE_REMOVE = EventFactory.createArrayBacked(BeforeRemove.class, callbacks -> (effectInstance, entity) -> {
		for (BeforeRemove callback : callbacks) {
			callback.beforeRemove(effectInstance, entity);
		}
	});

	public static final Event<AfterRemove> AFTER_REMOVE = EventFactory.createArrayBacked(AfterRemove.class, callbacks -> (effectInstance, entity) -> {
		for (AfterRemove callback : callbacks) {
			callback.afterRemove(effectInstance, entity);
		}
	});

	static {
		AFTER_ADD.register(((effectInstance, entity) -> {
			effectInstance.getEffect().value().onEffectAdded(effectInstance, entity);
		}));
		AFTER_REMOVE.register((effectInstance, entity) -> {
			effectInstance.getEffect().value().onEffectRemoved(effectInstance, entity);
		});
	}

	private ServerMobEffectEvents() {
	}

	@FunctionalInterface
	public interface AllowAdd {
		boolean allowAdd(MobEffectInstance effectInstance, LivingEntity entity);
	}

	@FunctionalInterface
	public interface BeforeAdd {
		void beforeAdd(MobEffectInstance effectInstance, LivingEntity entity);
	}

	@FunctionalInterface
	public interface AfterAdd {
		void afterAdd(MobEffectInstance effectInstance, LivingEntity entity);
	}

	@FunctionalInterface
	public interface AllowEarlyRemove {
		boolean allowEarlyRemove(MobEffectInstance effectInstance, LivingEntity entity);
	}

	@FunctionalInterface
	public interface BeforeRemove {
		void beforeRemove(MobEffectInstance effectInstance, LivingEntity entity);
	}

	@FunctionalInterface
	public interface AfterRemove {
		void afterRemove(MobEffectInstance effectInstance, LivingEntity entity);
	}
}
