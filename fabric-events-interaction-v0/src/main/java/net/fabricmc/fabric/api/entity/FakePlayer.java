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
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.PlayerTeam;

import net.fabricmc.fabric.impl.event.interaction.FakePlayerNetworkHandler;

/**
 * A "fake player" is a {@link ServerPlayer} that is not a human player.
 * They are typically used to automatically perform player actions such as placing blocks.
 *
 * <p>The easiest way to obtain a fake player is with {@link FakePlayer#get(ServerLevel)} or {@link FakePlayer#get(ServerLevel, GameProfile)}.
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
 * <p>Fake players try to behave like regular {@link ServerPlayer} objects to a reasonable extent.
 * In some edge cases, or for gameplay considerations, it might be necessary to check whether a {@link ServerPlayer} is a fake player.
 * This can be done with an {@code instanceof} check: {@code player instanceof FakePlayer}.
 */
public class FakePlayer extends ServerPlayer {
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
	public static FakePlayer get(ServerLevel world) {
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
	public static FakePlayer get(ServerLevel world, GameProfile profile) {
		Objects.requireNonNull(world, "World may not be null.");
		Objects.requireNonNull(profile, "Game profile may not be null.");

		return FAKE_PLAYER_MAP.computeIfAbsent(new FakePlayerKey(world, profile), key -> new FakePlayer(key.world, key.profile));
	}

	private record FakePlayerKey(ServerLevel world, GameProfile profile) { }
	private static final Map<FakePlayerKey, FakePlayer> FAKE_PLAYER_MAP = new MapMaker().weakValues().makeMap();

	protected FakePlayer(ServerLevel world, GameProfile profile) {
		super(world.getServer(), world, profile, ClientInformation.createDefault());

		this.connection = new FakePlayerNetworkHandler(this);
	}

	@Override
	public void tick() { }

	@Override
	public void updateOptions(ClientInformation settings) { }

	@Override
	public void awardStat(Stat<?> stat, int amount) { }

	@Override
	public void resetStat(Stat<?> stat) { }

	@Override
	public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
		return true;
	}

	@Nullable
	@Override
	public PlayerTeam getTeam() {
		// Scoreboard team is checked using the gameprofile name by default, which we don't want.
		return null;
	}

	@Override
	public void startSleeping(BlockPos pos) {
		// Don't lock bed forever.
	}

	@Override
	public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
		return false;
	}

	@Override
	public void openTextEdit(SignBlockEntity sign, boolean front) { }

	@Override
	public OptionalInt openMenu(@Nullable MenuProvider factory) {
		return OptionalInt.empty();
	}

	@Override
	public void openHorseInventory(AbstractHorse horse, Container inventory) { }
}
