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

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Various server-side only events related to living entities.
 */
public final class ServerLivingEntityEvents {
	/**
	 * An event that is called when a living entity is going to take damage.
	 * This is fired from {@link LivingEntity#hurtServer}, before armor or any other mitigation are applied.
	 * Mods can cancel this to prevent the damage entirely.
	 */
	public static final Event<AllowDamage> ALLOW_DAMAGE = EventFactory.createArrayBacked(AllowDamage.class, callbacks -> (entity, source, amount) -> {
		for (AllowDamage callback : callbacks) {
			if (!callback.allowDamage(entity, source, amount)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event that is called after an entity is damaged. This is fired from {@link LivingEntity#hurtServer} after damage
	 * is applied, or after that damage was blocked by a shield.
	 *
	 * <p>The base damage taken is the damage initially applied to the entity. Damage taken is the amount of damage the
	 * entity actually took, after effects such as shields and extra freezing damage are applied. Damage taken does NOT
	 * include damage reduction from armor and enchantments.
	 *
	 * <p>This event is not fired if the entity was killed by the damage.
	 */
	public static final Event<AfterDamage> AFTER_DAMAGE = EventFactory.createArrayBacked(AfterDamage.class, callbacks -> (entity, source, baseDamageTaken, damageTaken, blocked) -> {
		for (AfterDamage callback : callbacks) {
			callback.afterDamage(entity, source, baseDamageTaken, damageTaken, blocked);
		}
	});

	/**
	 * An event that is called when an entity takes fatal damage.
	 *
	 * <p>Mods can cancel this to keep the entity alive.
	 *
	 * <p>Vanilla checks for entity health {@code <= 0} each tick (with {@link LivingEntity#isDeadOrDying()}), and kills if true -
	 * so the entity will still die next tick if this event is cancelled.
	 * It's assumed that the listener will do something to prevent this, for example, if the entity is a player:
	 * <ul>
	 *     <li>a minigame mod teleporting the player into a 'respawn room' and setting their health to 20.0</li>
	 *     <li>a mod that changes death mechanics switching the player over to the mod's play-mode, where death doesn't apply</li>
	 * </ul>
	 */
	public static final Event<AllowDeath> ALLOW_DEATH = EventFactory.createArrayBacked(AllowDeath.class, callbacks -> (entity, damageSource, damageAmount) -> {
		for (AllowDeath callback : callbacks) {
			if (!callback.allowDeath(entity, damageSource, damageAmount)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * An event that is called when a living entity dies.
	 */
	public static final Event<AfterDeath> AFTER_DEATH = EventFactory.createArrayBacked(AfterDeath.class, callbacks -> (entity, damageSource) -> {
		for (AfterDeath callback : callbacks) {
			callback.afterDeath(entity, damageSource);
		}
	});

	/**
	 * An event that is called when a mob has been converted to another type.
	 *
	 * <p>When this event is called, the old instance has not yet been discarded, and the new one has not yet been spawned.
	 * Mods may use this event to copy some of the old entity's data to the converted one.</p>
	 *
	 * <p>This event only handles cases where the entity type changes, requiring a new instance. Notably it does not
	 * cover mooshrooms changing color from lightning, creepers getting charged, or wolves being tamed.</p>
	 */
	public static final Event<MobConversion> MOB_CONVERSION = EventFactory.createArrayBacked(MobConversion.class, callbacks -> (previous, converted, keepEquipment) -> {
		for (MobConversion callback : callbacks) {
			callback.onConversion(previous, converted, keepEquipment);
		}
	});

	@FunctionalInterface
	public interface AllowDamage {
		/**
		 * Called when a living entity is going to take damage. Can be used to cancel the damage entirely.
		 *
		 * <p>The amount corresponds to the "incoming" damage amount, before armor and other mitigations have been applied.
		 *
		 * @param entity the entity
		 * @param source the source of the damage
		 * @param amount the amount of damage that the entity will take (before mitigations)
		 * @return true if the damage should go ahead, false to cancel the damage.
		 */
		boolean allowDamage(LivingEntity entity, DamageSource source, float amount);
	}

	@FunctionalInterface
	public interface AfterDamage {
		/**
		 * Called after a living entity took damage, unless they were killed. The base damage taken is given as damage
		 * taken before armor or enchantments are applied, but after other effects like shields are applied.
		 *
		 * @param entity the entity that was damaged
		 * @param source the source of the damage
		 * @param baseDamageTaken the amount of damage initially dealt
		 * @param damageTaken the amount of damage actually taken by the entity, before armor and enchantment effects
		 * @param blocked whether the damage was blocked by a shield
		 */
		void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked);
	}

	@FunctionalInterface
	public interface AllowDeath {
		/**
		 * Called when a living entity takes fatal damage (before totems of undying can take effect).
		 *
		 * @param entity the entity
		 * @param damageSource the source of the fatal damage
		 * @param damageAmount the amount of damage that has killed the entity
		 * @return true if the death should go ahead, false to cancel the death.
		 */
		boolean allowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount);
	}

	@FunctionalInterface
	public interface AfterDeath {
		/**
		 * Called when a living entity dies. The death cannot be canceled at this point.
		 *
		 * @param entity the entity
		 * @param damageSource the source of the fatal damage
		 */
		void afterDeath(LivingEntity entity, DamageSource damageSource);
	}

	@FunctionalInterface
	public interface MobConversion {
		/**
		 * Called when a mob is converted to another type.
		 *
		 * @param previous the previous entity instance
		 * @param converted the new instance for the converted entity
		 * @param conversionContext the context used for the conversion
		 */
		void onConversion(Mob previous, Mob converted, ConversionParams conversionContext);
	}

	private ServerLivingEntityEvents() {
	}
}
