package net.fabricmc.fabric.api.loot.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import net.fabricmc.fabric.impl.loot.LootModifierImpl;

/**
 * A data-driven modifier for loot tables.
 * Loot modifiers can add new {@linkplain LootPool pools} and {@linkplain LootItemFunction functions}
 * to loot tables without overriding loot table data files.
 *
 * <p>Loot modifiers can be defined in the {@link #DATA_DIRECTORY data/[namespace]/fabric/loot_modifier}
 * directory as JSON files with the following format: TODO add format
 * {@snippet lang=json :
 * "something"
 * }
 * You can also generate loot modifier files using the {@linkplain #CODEC codec}.
 * New modifiers can be created with a {@linkplain #builder() builder}.
 *
 * <p>Loot modifiers determine which loot tables they modify by using a {@link LootModifierTarget}.
 * You can use them to target specific loot tables or only builtin loot tables, just like with {@link LootTableEvents#MODIFY}.
 */
@ApiStatus.NonExtendable
public interface LootModifier {
	/**
	 * The data directory for loot modifiers.
	 */
	String DATA_DIRECTORY = "fabric/loot_modifier";

	/**
	 * The loot modifier codec.
	 */
	Codec<LootModifier> CODEC = LootModifierImpl.CODEC;

	/**
	 * {@return the target of this loot modifier}
	 */
	LootModifierTarget target();

	/**
	 * {@return the pools added by this loot modifier}
	 */
	List<LootPool> pools();

	/**
	 * {@return the functions added by this loot modifier}
	 */
	List<LootItemFunction> functions();

	/**
	 * {@return a new loot modifier builder}
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder for {@link LootModifier}.
	 */
	final class Builder {
		private @Nullable LootModifierTarget target;
		private final List<LootPool> pools = new ArrayList<>();
		private final List<LootItemFunction> functions = new ArrayList<>();

		/**
		 * Sets the loot modifier target.
		 *
		 * @param target the target
		 * @return this builder
		 */
		public Builder target(LootModifierTarget target) {
			Objects.requireNonNull(target, "Loot modifier target cannot be null");
			this.target = target;
			return this;
		}

		// TODO: methods to add pools and functions
		// TODO: test datagenning

		/**
		 * Builds a loot modifier from this builder.
		 *
		 * @return the created modifier
		 * @throws NullPointerException if the target hasn't been set
		 */
		public LootModifier build() {
			Objects.requireNonNull(target, "Loot modifier target not set");
			return new LootModifierImpl(target, pools, functions);
		}
	}
}
