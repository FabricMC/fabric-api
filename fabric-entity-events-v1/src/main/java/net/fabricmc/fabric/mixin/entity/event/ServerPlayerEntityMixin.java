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

package net.fabricmc.fabric.mixin.entity.event;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
	@Shadow
	public abstract ServerWorld getServerWorld();

	/**
	 * Minecraft by default does not call Entity#onKilledOther for a ServerPlayerEntity being killed.
	 * This is a Mojang bug.
	 * This is implements the method call on the server player entity and then calls the corresponding event.
	 */
	@Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getPrimeAdversary()Lnet/minecraft/entity/LivingEntity;"))
	private void callOnKillForPlayer(DamageSource source, CallbackInfo ci) {
		final Entity attacker = source.getAttacker();

		// If the damage source that killed the player was an entity, then fire the event.
		if (attacker != null) {
			attacker.onKilledOther(this.getServerWorld(), (ServerPlayerEntity) (Object) this);
			ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(this.getServerWorld(), attacker, (ServerPlayerEntity) (Object) this);
		}
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	private void notifyDeath(DamageSource source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((ServerPlayerEntity) (Object) this, source);
	}

	/**
	 * This is called by {@code teleportTo}.
	 */
	@Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
	private void afterWorldChanged(ServerWorld origin, CallbackInfo ci) {
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld((ServerPlayerEntity) (Object) this, origin, this.getServerWorld());
	}

	@Inject(method = "copyFrom", at = @At("TAIL"))
	private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer(oldPlayer, (ServerPlayerEntity) (Object) this, alive);
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
	private Comparable<?> redirectSleepDirection(BlockState state, Property<?> property, BlockPos pos) {
		Direction initial = state.contains(property) ? (Direction) state.get(property) : null;
		return EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity) (Object) this, pos, initial);
	}

	@Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;", shift = At.Shift.BY, by = 3), cancellable = true)
	private void onTrySleepDirectionCheck(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info, @Local @Nullable Direction sleepingDirection) {
		// This checks the result from the event call above.
		if (sleepingDirection == null) {
			info.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE));
		}
	}

	@WrapOperation(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/server/network/ServerPlayerEntity$Respawn;Z)V"))
	private void onSetSpawnPoint(ServerPlayerEntity player, ServerPlayerEntity.Respawn spawnPoint, boolean sendMessage, Operation<Void> original) {
		if (EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, spawnPoint.pos())) {
			original.call(player, spawnPoint, sendMessage);
		}
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
	private boolean hasNoMonstersNearby(List<HostileEntity> monsters, BlockPos pos) {
		boolean vanillaResult = monsters.isEmpty();
		ActionResult result = EntitySleepEvents.ALLOW_NEARBY_MONSTERS.invoker().allowNearbyMonsters((PlayerEntity) (Object) this, pos, vanillaResult);
		return result != ActionResult.PASS ? result.isAccepted() : vanillaResult;
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
	private boolean redirectDaySleepCheck(World world, BlockPos pos) {
		boolean day = world.isDay();
		ActionResult result = EntitySleepEvents.ALLOW_SLEEP_TIME.invoker().allowSleepTime((PlayerEntity) (Object) this, pos, !day);

		if (result != ActionResult.PASS) {
			return !result.isAccepted(); // true from the event = night-like conditions, so we have to invert
		}

		return day;
	}
}
