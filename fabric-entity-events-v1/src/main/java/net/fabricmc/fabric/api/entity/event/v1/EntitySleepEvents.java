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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/// Events about the sleep of [living entities][LivingEntity].
///
/// These events can be categorized into three groups:
///
/// 1. Simple listeners: [#START_SLEEPING] and [#STOP_SLEEPING]
/// 2. Predicates: [#ALLOW_BED], [#ALLOW_RESETTING_TIME],
///    [#ALLOW_NEARBY_MONSTERS], [#ALLOW_SETTING_SPAWN] and [#ALLOW_SLEEPING]
///
///    **Note:** Only the [#ALLOW_BED] event applies to non-player entities.
/// 3. Modifiers: [#MODIFY_SLEEPING_DIRECTION], [#SET_BED_OCCUPATION_STATE]
///    and [#MODIFY_WAKE_UP_POSITION]
///
/// Sleep events are useful for making custom bed blocks that do not extend [net.minecraft.world.level.block.BedBlock].
/// Custom beds generally only need a custom [#ALLOW_BED] checker and a [#MODIFY_SLEEPING_DIRECTION] callback,
/// but the other events might be useful as well.
public final class EntitySleepEvents {
	/// An event that checks whether a player can start to sleep in a bed-like block.
	/// This event only applies to sleeping using [Player#startSleepInBed(BlockPos)].
	///
	/// **Note:** Please use the more detailed event [#ALLOW_NEARBY_MONSTERS]
	/// if it matches your use case! This helps with mod compatibility.
	///
	/// If this event returns a [net.minecraft.world.entity.player.Player.BedSleepingProblem], it is used
	/// as the return value of [Player#startSleepInBed(BlockPos)] and sleeping fails. A `null` return value
	/// means that the player will start sleeping.
	///
	/// When this event is called, all vanilla sleeping checks have already succeeded, i.e. this event
	/// is used in addition to vanilla checks. The more detailed event [#ALLOW_NEARBY_MONSTERS]
	/// is also checked before this event.
	public static final Event<AllowSleeping> ALLOW_SLEEPING = EventFactory.createArrayBacked(AllowSleeping.class, callbacks -> (player, sleepingPos) -> {
		for (AllowSleeping callback : callbacks) {
			Player.BedSleepingProblem reason = callback.allowSleep(player, sleepingPos);

			if (reason != null) {
				return reason;
			}
		}

		return null;
	});

	/// An event that is called when an entity starts to sleep.
	public static final Event<StartSleeping> START_SLEEPING = EventFactory.createArrayBacked(StartSleeping.class, callbacks -> (entity, sleepingPos) -> {
		for (StartSleeping callback : callbacks) {
			callback.onStartSleeping(entity, sleepingPos);
		}
	});

	/// An event that is called when an entity stops sleeping and wakes up.
	public static final Event<StopSleeping> STOP_SLEEPING = EventFactory.createArrayBacked(StopSleeping.class, callbacks -> (entity, sleepingPos) -> {
		for (StopSleeping callback : callbacks) {
			callback.onStopSleeping(entity, sleepingPos);
		}
	});

	/// An event that is called to check whether a block is valid for sleeping.
	///
	/// Used for checking whether the block at the current sleeping position is a valid bed block.
	/// If `false`, the player wakes up.
	///
	/// This event is only checked _during_ sleeping, so an entity can
	/// [start sleeping][LivingEntity#startSleeping(BlockPos)] on any block, but will immediately
	/// wake up if this check fails.
	///
	/// @see LivingEntity#checkBedExists()
	public static final Event<AllowBed> ALLOW_BED = EventFactory.createArrayBacked(AllowBed.class, callbacks -> (entity, sleepingPos, state, vanillaResult) -> {
		for (AllowBed callback : callbacks) {
			InteractionResult result = callback.allowBed(entity, sleepingPos, state, vanillaResult);

			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		return InteractionResult.PASS;
	});

	/// An event that checks whether players can sleep when monsters are nearby.
	///
	/// This event can also be used to force a failing result, meaning it can do custom monster checks.
	public static final Event<AllowNearbyMonsters> ALLOW_NEARBY_MONSTERS = EventFactory.createArrayBacked(AllowNearbyMonsters.class, callbacks -> (player, sleepingPos, vanillaResult) -> {
		for (AllowNearbyMonsters callback : callbacks) {
			InteractionResult result = callback.allowNearbyMonsters(player, sleepingPos, vanillaResult);

			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		return InteractionResult.PASS;
	});

	/// An event that checks whether a sleeping player counts into skipping the current day and resetting the time to 0.
	///
	/// When this event is called, all vanilla time resetting checks have already succeeded, i.e. this event
	/// is used in addition to vanilla checks.
	public static final Event<AllowResettingTime> ALLOW_RESETTING_TIME = EventFactory.createArrayBacked(AllowResettingTime.class, callbacks -> player -> {
		for (AllowResettingTime callback : callbacks) {
			if (!callback.allowResettingTime(player)) {
				return false;
			}
		}

		return true;
	});

	/// An event that can be used to provide the entity's sleep direction if missing.
	///
	/// This is useful for custom bed blocks that need to determine the sleeping direction themselves.
	/// If the block is not a [net.minecraft.world.level.block.BedBlock], you need to provide the sleeping direction manually
	/// with this event.
	public static final Event<ModifySleepingDirection> MODIFY_SLEEPING_DIRECTION = EventFactory.createArrayBacked(ModifySleepingDirection.class, callbacks -> (entity, sleepingPos, sleepingDirection) -> {
		for (ModifySleepingDirection callback : callbacks) {
			sleepingDirection = callback.modifySleepDirection(entity, sleepingPos, sleepingDirection);
		}

		return sleepingDirection;
	});

	/// An event that checks whether a player's spawn can be set when sleeping.
	///
	/// Vanilla always allows this operation.
	public static final Event<AllowSettingSpawn> ALLOW_SETTING_SPAWN = EventFactory.createArrayBacked(AllowSettingSpawn.class, callbacks -> (player, sleepingPos) -> {
		for (AllowSettingSpawn callback : callbacks) {
			if (!callback.allowSettingSpawn(player, sleepingPos)) {
				return false;
			}
		}

		return true;
	});

	/// An event that sets the occupation state of a bed.
	///
	/// Note that this is **not** needed for blocks using [net.minecraft.world.level.block.BedBlock],
	/// which are handled automatically.
	public static final Event<SetBedOccupationState> SET_BED_OCCUPATION_STATE = EventFactory.createArrayBacked(SetBedOccupationState.class, callbacks -> (entity, sleepingPos, bedState, occupied) -> {
		for (SetBedOccupationState callback : callbacks) {
			if (callback.setBedOccupationState(entity, sleepingPos, bedState, occupied)) {
				return true;
			}
		}

		return false;
	});

	/// An event that can be used to provide the entity's wake-up position if missing.
	///
	/// This is useful for custom bed blocks that need to determine the wake-up position themselves.
	/// If the block is not a [net.minecraft.world.level.block.BedBlock], you need to provide the wake-up position manually
	/// with this event.
	public static final Event<ModifyWakeUpPosition> MODIFY_WAKE_UP_POSITION = EventFactory.createArrayBacked(ModifyWakeUpPosition.class, callbacks -> (entity, sleepingPos, bedState, wakeUpPos) -> {
		for (ModifyWakeUpPosition callback : callbacks) {
			wakeUpPos = callback.modifyWakeUpPosition(entity, sleepingPos, bedState, wakeUpPos);
		}

		return wakeUpPos;
	});

	@FunctionalInterface
	public interface AllowSleeping {
		/// Checks whether a player can start sleeping in a bed-like block.
		///
		/// @param player      the sleeping player
		/// @param sleepingPos the future [sleeping position][LivingEntity#getSleepingPos()] of the entity
		/// @return `null` if the player can sleep, or a failure reason if they cannot
		/// @see Player#startSleepInBed(BlockPos)
		Player.@Nullable BedSleepingProblem allowSleep(Player player, BlockPos sleepingPos);
	}

	@FunctionalInterface
	public interface StartSleeping {
		/// Called when an entity starts to sleep.
		///
		/// @param entity      the sleeping entity
		/// @param sleepingPos the [sleeping position][LivingEntity#getSleepingPos()] of the entity
		void onStartSleeping(LivingEntity entity, BlockPos sleepingPos);
	}

	@FunctionalInterface
	public interface StopSleeping {
		/// Called when an entity stops sleeping and wakes up.
		///
		/// @param entity      the sleeping entity
		/// @param sleepingPos the [sleeping position][LivingEntity#getSleepingPos()] of the entity
		void onStopSleeping(LivingEntity entity, BlockPos sleepingPos);
	}

	@FunctionalInterface
	public interface AllowBed {
		/// Checks whether a block is a valid bed for the entity.
		///
		/// Non-[passing][InteractionResult#PASS] return values cancel further callbacks.
		///
		/// @param entity        the sleeping entity
		/// @param sleepingPos   the position of the block
		/// @param state         the block state to check
		/// @param vanillaResult `true` if vanilla allows the block, `false` otherwise
		/// @return [InteractionResult#SUCCESS] if the bed is valid, [InteractionResult#FAIL] if it's not,
		///         [InteractionResult#PASS] to fall back to other callbacks
		InteractionResult allowBed(LivingEntity entity, BlockPos sleepingPos, BlockState state, boolean vanillaResult);
	}

	@FunctionalInterface
	public interface AllowSleepTime {
		/// Checks whether the current time of day is valid for sleeping.
		///
		/// Non-[passing][InteractionResult#PASS] return values cancel further callbacks.
		///
		/// @param player        the sleeping player
		/// @param sleepingPos   the (possibly still unset) [sleeping position][LivingEntity#getSleepingPos()] of the player
		/// @param vanillaResult `true` if vanilla allows the time, `false` otherwise
		/// @return [InteractionResult#SUCCESS] if the time is valid, [InteractionResult#FAIL] if it's not,
		///         [InteractionResult#PASS] to fall back to other callbacks
		InteractionResult allowSleepTime(Player player, BlockPos sleepingPos, boolean vanillaResult);
	}

	@FunctionalInterface
	public interface AllowNearbyMonsters {
		/// Checks whether a player can sleep when monsters are nearby.
		///
		/// Non-[passing][InteractionResult#PASS] return values cancel further callbacks.
		///
		/// @param player        the sleeping player
		/// @param sleepingPos   the (possibly still unset) [sleeping position][LivingEntity#getSleepingPos()] of the player
		/// @param vanillaResult `true` if vanilla's monster check succeeded (there were no monsters), `false` otherwise
		/// @return [InteractionResult#SUCCESS] to allow sleeping, [InteractionResult#FAIL] to prevent sleeping,
		///         [InteractionResult#PASS] to fall back to other callbacks
		InteractionResult allowNearbyMonsters(Player player, BlockPos sleepingPos, boolean vanillaResult);
	}

	@FunctionalInterface
	public interface AllowResettingTime {
		/// Checks whether a sleeping player counts into skipping the current day and resetting the time to 0.
		///
		/// @param player        the sleeping player
		/// @return `true` if allowed, `false` otherwise
		boolean allowResettingTime(Player player);
	}

	@FunctionalInterface
	public interface ModifySleepingDirection {
		/// Modifies or provides a sleeping direction for a block.
		/// The sleeping direction is where a player's head is pointing when they're sleeping.
		///
		/// @param entity            the sleeping entity
		/// @param sleepingPos       the position of the block slept on
		/// @param sleepingDirection the old sleeping direction, or `null` if not determined by vanilla or previous callbacks
		/// @return the new sleeping direction
		@Nullable
		Direction modifySleepDirection(LivingEntity entity, BlockPos sleepingPos, @Nullable Direction sleepingDirection);
	}

	@FunctionalInterface
	public interface AllowSettingSpawn {
		/// Checks whether a player's spawn can be set when sleeping.
		///
		/// @param player      the sleeping player
		/// @param sleepingPos the sleeping position
		/// @return `true` if allowed, `false` otherwise
		boolean allowSettingSpawn(Player player, BlockPos sleepingPos);
	}

	@FunctionalInterface
	public interface SetBedOccupationState {
		/// Sets the occupation state of a bed block.
		///
		/// @param entity      the sleeping entity
		/// @param sleepingPos the sleeping position
		/// @param bedState    the block state of the bed
		/// @param occupied    `true` if occupied, `false` if free
		/// @return `true` if the occupation state was successfully modified, `false` to fall back to other callbacks
		boolean setBedOccupationState(LivingEntity entity, BlockPos sleepingPos, BlockState bedState, boolean occupied);
	}

	@FunctionalInterface
	public interface ModifyWakeUpPosition {
		/// Modifies or provides a wake-up position for an entity waking up.
		///
		/// @param entity      the sleeping entity
		/// @param sleepingPos the position of the block slept on
		/// @param bedState    the block slept on
		/// @param wakeUpPos   the old wake-up position, or `null` if not determined by vanilla or previous callbacks
		/// @return the new wake-up position
		@Nullable
		Vec3 modifyWakeUpPosition(LivingEntity entity, BlockPos sleepingPos, BlockState bedState, @Nullable Vec3 wakeUpPos);
	}

	private EntitySleepEvents() {
	}
}
