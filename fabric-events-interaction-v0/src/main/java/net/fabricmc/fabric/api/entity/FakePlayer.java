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

package net.fabricmc.fabric.api.entity;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.event.interaction.FakePlayerNetworkHandler;

/**
 * A "fake player" is a {@link ServerPlayerEntity} that is not a human player.
 * They are typically used to automatically perform player actions such as placing blocks.
 *
 * <p>The easiest way to obtain a fake player is with {@link FakePlayer#get(ServerWorld)} or {@link FakePlayer#get(ServerWorld, GameProfile)}.
 * It is also possible to create a subclass for more control over the fake player's behavior.
 *
 * <p>For good inter-mod compatibility, fake players should have the UUID of their owning (human) player.
 * They should still have a different name to ensure the {@link GameProfile} is different.
 * For example:
 * <pre>{@code
 * UUID humanPlayerUuid = ...;
 * String humanPlayerName = ...;
 * GameProfile fakeProfile = new GameProfile(humanPlayerUuid, "[Block Breaker of " + humanPlayerName + "]");
 * }</pre>
 * If a fake player does not belong to a specific player, the {@link #DEFAULT_UUID default UUID} should be used.
 *
 * <p>Fake players try to behave like regular {@link ServerPlayerEntity} objects to a reasonable extent.
 * In some edge cases, or for gameplay considerations, it might be necessary to check whether a {@link ServerPlayerEntity} is a fake player.
 * This can be done with an {@code instanceof} check: {@code player instanceof FakePlayer}.
 */
public class FakePlayer extends ServerPlayerEntity {
	/**
	 * Default UUID, for fake players not associated with a specific (human) player.
	 */
	public static final UUID DEFAULT_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
	private static final GameProfile DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[Minecraft]");

	/**
	 * Retrieves a fake player for the specified world, using the {@link #DEFAULT_UUID default UUID}.
	 * This is suitable when the fake player is not associated with a specific (human) player.
	 * Otherwise, the UUID of the owning (human) player should be used (see class javadoc).
	 *
	 * <p>Instances are reused for the same world parameter.
	 *
	 * <p>Caution should be exerted when storing the returned value,
	 * as strong references to the fake player will keep the world loaded.
	 */
	public static FakePlayer get(ServerWorld world) {
		return get(world, DEFAULT_PROFILE);
	}

	/**
	 * Retrieves a fake player for the specified world and game profile.
	 * See class javadoc for more information on fake player game profiles.
	 *
	 * <p>Instances are reused for the same parameters.
	 *
	 * <p>Caution should be exerted when storing the returned value,
	 * as strong references to the fake player will keep the world loaded.
	 */
	public static FakePlayer get(ServerWorld world, GameProfile profile) {
		Objects.requireNonNull(world, "World may not be null.");
		Objects.requireNonNull(profile, "Game profile may not be null.");

		return FAKE_PLAYER_MAP.computeIfAbsent(new FakePlayerKey(world, profile), key -> new FakePlayer(key.world, key.profile));
	}

	private record FakePlayerKey(ServerWorld world, GameProfile profile) { }
	private static final Map<FakePlayerKey, FakePlayer> FAKE_PLAYER_MAP = new MapMaker().weakValues().makeMap();

	protected FakePlayer(ServerWorld world, GameProfile profile) {
		super(world.getServer(), world, profile, SyncedClientOptions.createDefault());

		this.networkHandler = new FakePlayerNetworkHandler(this);
	}

	@Override
	public void tick() { }

	@Override
	public void setClientOptions(SyncedClientOptions settings) { }

	@Override
	public void increaseStat(Stat<?> stat, int amount) { }

	@Override
	public void resetStat(Stat<?> stat) { }

	@Override
	public boolean isInvulnerableTo(ServerWorld world, DamageSource damageSource) {
		return true;
	}

	@Nullable
	@Override
	public Team getScoreboardTeam() {
		// Scoreboard team is checked using the gameprofile name by default, which we don't want.
		return null;
	}

	@Override
	public void sleep(BlockPos pos) {
		// Don't lock bed forever.
	}

	@Override
	public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
		return false;
	}

	@Override
	public void openEditSignScreen(SignBlockEntity sign, boolean front) { }

	@Override
	public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
		return OptionalInt.empty();
	}

	@Override
	public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) { }
}
