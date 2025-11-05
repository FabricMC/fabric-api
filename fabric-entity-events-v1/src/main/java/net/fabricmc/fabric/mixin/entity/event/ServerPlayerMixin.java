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
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends LivingEntityMixin {
	@Shadow
	public abstract ServerLevel level();

	/**
	 * Minecraft by default does not call Entity#onKilledOther for a ServerPlayerEntity being killed.
	 * This is a Mojang bug.
	 * This is implements the method call on the server player entity and then calls the corresponding event.
	 */
	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getKillCredit()Lnet/minecraft/world/entity/LivingEntity;"))
	private void callOnKillForPlayer(DamageSource source, CallbackInfo ci) {
		final Entity attacker = source.getEntity();

		// If the damage source that killed the player was an entity, then fire the event.
		if (attacker != null) {
			attacker.killedEntity(this.level(), (ServerPlayer) (Object) this, source);
			ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(this.level(), attacker, (ServerPlayer) (Object) this, source);
		}
	}

	@Inject(method = "die", at = @At("TAIL"))
	private void notifyDeath(DamageSource source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((ServerPlayer) (Object) this, source);
	}

	/**
	 * This is called by {@code teleportTo}.
	 */
	@Inject(method = "triggerDimensionChangeTriggers(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
	private void afterWorldChanged(ServerLevel origin, CallbackInfo ci) {
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld((ServerPlayer) (Object) this, origin, this.level());
	}

	@Inject(method = "restoreFrom", at = @At("TAIL"))
	private void onCopyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
		ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer(oldPlayer, (ServerPlayer) (Object) this, alive);
	}

	@WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
	private Comparable<?> redirectSleepDirection(BlockState instance, Property<Direction> property, Operation<Comparable<Direction>> original, BlockPos pos, @Cancellable CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
		Direction initial = (Direction) (instance.hasProperty(property) ? original.call(instance, property) : null);
		Direction dir = EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity) (Object) this, pos, initial);

		if (dir == null) {
			cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
		}

		return dir;
	}

	@WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"))
	private void onSetSpawnPoint(ServerPlayer player, ServerPlayer.RespawnConfig spawnPoint, boolean sendMessage, Operation<Void> original) {
		if (EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, spawnPoint.respawnData().pos())) {
			original.call(player, spawnPoint, sendMessage);
		}
	}

	@Redirect(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
	private boolean hasNoMonstersNearby(List<Monster> monsters, BlockPos pos) {
		boolean vanillaResult = monsters.isEmpty();
		InteractionResult result = EntitySleepEvents.ALLOW_NEARBY_MONSTERS.invoker().allowNearbyMonsters((Player) (Object) this, pos, vanillaResult);
		return result != InteractionResult.PASS ? result.consumesAction() : vanillaResult;
	}
}
