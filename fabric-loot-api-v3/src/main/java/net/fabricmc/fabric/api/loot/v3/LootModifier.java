package net.fabricmc.fabric.api.loot.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * You can also generate loot modifier files using {@link net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
 * FabricCodecDataProvider}.
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

		/**
		 * Adds pools to this builder.
		 *
		 * @param pools the pools to add
		 * @return this builder
		 */
		public Builder pools(LootPool.Builder... pools) {
			return pools(Arrays.stream(pools).map(LootPool.Builder::build).toList());
		}

		/**
		 * Adds pools to this builder.
		 *
		 * @param pools the pools to add
		 * @return this builder
		 */
		public Builder pools(LootPool... pools) {
			return pools(Arrays.asList(pools));
		}

		/**
		 * Adds pools to this builder.
		 *
		 * @param pools the pools to add
		 * @return this builder
		 */
		public Builder pools(Collection<? extends LootPool> pools) {
			this.pools.addAll(pools);
			return this;
		}

		/**
		 * Adds functions to this builder.
		 *
		 * @param functions the functions to add
		 * @return this builder
		 */
		public Builder functions(LootItemFunction.Builder... functions) {
			return functions(Arrays.stream(functions).map(LootItemFunction.Builder::build).toList());
		}

		/**
		 * Adds functions to this builder.
		 *
		 * @param functions the functions to add
		 * @return this builder
		 */
		public Builder functions(LootItemFunction... functions) {
			return functions(Arrays.asList(functions));
		}

		/**
		 * Adds functions to this builder.
		 *
		 * @param functions the functions to add
		 * @return this builder
		 */
		public Builder functions(Collection<? extends LootItemFunction> functions) {
			this.functions.addAll(functions);
			return this;
		}

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
