package net.fabricmc.fabric.api.gamerule.v1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.impl.gamerule.sync.VanillaGameRuleSynchronizationImpl;

/// A utility class for syncing vanilla gamerules. Invoke these methods from the main entrypoint.
public final class VanillaGameRuleSynchronization {
	private VanillaGameRuleSynchronization() {
	}

	public static void synchronizeGameRule(GameRule<?> gameRule) {
		Objects.requireNonNull(gameRule, "game rule can't be null!");
		VanillaGameRuleSynchronizationImpl.addSynchronizedGameRule(gameRule);
	}

	public static void synchronizeGameRules(GameRule<?>... gameRules) {
		synchronizeGameRules(List.of(gameRules));
	}

	public static void synchronizeGameRules(Collection<GameRule<?>> gameRules) {
		Objects.requireNonNull(gameRules, "game rule collection can't be null!");

		for (GameRule<?> gameRule : gameRules) {
			synchronizeGameRule(gameRule);
		}
	}
}
