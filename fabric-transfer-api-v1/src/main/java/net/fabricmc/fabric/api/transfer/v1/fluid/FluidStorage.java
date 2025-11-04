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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.impl.transfer.fluid.CombinedProvidersImpl;
import net.fabricmc.fabric.impl.transfer.fluid.EmptyBucketStorage;
import net.fabricmc.fabric.impl.transfer.fluid.WaterPotionStorage;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;

/**
 * Access to {@link Storage Storage&lt;FluidVariant&gt;} instances.
 */
public final class FluidStorage {
	/**
	 * Sided block access to fluid variant storages.
	 * Fluid amounts are always expressed in {@linkplain FluidConstants droplets}.
	 * The {@code Direction} parameter may be null, meaning that the full inventory (ignoring side restrictions) should be queried.
	 * Refer to {@link BlockApiLookup} for documentation on how to use this field.
	 *
	 * <p>A simple way to expose fluid variant storages for a block entity hierarchy is to extend {@link SidedStorageBlockEntity}.
	 *
	 * <p>When the operations supported by a storage change,
	 * that is if the return value of {@link Storage#supportsInsertion} or {@link Storage#supportsExtraction} changes,
	 * the storage should notify its neighbors with a block update so that they can refresh their connections if necessary.
	 *
	 * <p>This may be queried safely both on the logical server and on the logical client threads.
	 * On the server thread (i.e. with a server world), all transfer functionality is always supported.
	 * On the client thread (i.e. with a client world), contents of queried Storages are unreliable and should not be modified.
	 */
	public static final BlockApiLookup<Storage<FluidVariant>, @Nullable Direction> SIDED =
			BlockApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "sided_fluid_storage"), Storage.asClass(), Direction.class);

	/**
	 * Item access to fluid variant storages.
	 * Querying should happen through {@link ContainerItemContext#find}.
	 *
	 * <p>Fluid amounts are always expressed in {@linkplain FluidConstants droplets}.
	 * By default, Fabric API only registers storage support for buckets that have a 1:1 mapping to their fluid, and for water potions.
	 *
	 * <p>{@link #combinedItemApiProvider} and {@link #GENERAL_COMBINED_PROVIDER} should be used for API provider registration
	 * when multiple mods may want to offer a storage for the same item.
	 *
	 * <p>Base implementations are provided: {@link EmptyItemFluidStorage} and {@link FullItemFluidStorage}.
	 *
	 * <p>This may be queried both client-side and server-side.
	 * Returned APIs should behave the same regardless of the logical side.
	 */
	public static final ItemApiLookup<Storage<FluidVariant>, ContainerItemContext> ITEM =
			ItemApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "fluid_storage"), Storage.asClass(), ContainerItemContext.class);

	/**
	 * Get or create and register a {@link CombinedItemApiProvider} event for the passed item.
	 * Allows multiple API providers to provide a {@code Storage<FluidVariant>} implementation for the same item.
	 *
	 * <p>When the item is queried for an API through {@link #ITEM}, all the providers registered through the event will be invoked.
	 * All non-null {@code Storage<FluidVariant>} instances returned by the providers will be combined in a single storage,
	 * that will be the final result of the query, or {@code null} if no storage is offered by the event handlers.
	 *
	 * <p>This is appropriate to use when multiple mods could wish to expose the Fluid API for some items,
	 * for example when dealing with items added by the base Minecraft game such as buckets or empty bottles.
	 * A typical usage example is a mod adding support for filling empty bottles with a honey fluid:
	 * Fabric API already registers a storage for empty bottles to allow filling them with water through the event,
	 * and a mod can register an event handler that will attach a second storage allowing empty bottles to be filled with its honey fluid.
	 *
	 * @throws IllegalStateException If an incompatible provider is already registered for the item.
	 */
	public static Event<CombinedItemApiProvider> combinedItemApiProvider(Item item) {
		return CombinedProvidersImpl.getOrCreateItemEvent(item);
	}

	/**
	 * Allows multiple API providers to return {@code Storage<FluidVariant>} implementations for some items.
	 * {@link #combinedItemApiProvider} is per-item while this one is queried for all items, hence the "general" name.
	 *
	 * <p>Implementation note: This event is invoked both through an API Lookup fallback, and by the {@code combinedItemApiProvider} events.
	 * This means that per-item combined providers registered through {@code combinedItemApiProvider} DO NOT prevent these general providers from running,
	 * however regular providers registered through {@code ItemApiLookup#register...} that return a non-null API instance DO prevent it.
	 */
	public static final Event<CombinedItemApiProvider> GENERAL_COMBINED_PROVIDER = CombinedProvidersImpl.createEvent(false);

	@FunctionalInterface
	public interface CombinedItemApiProvider {
		/**
		 * Return a {@code Storage<FluidVariant>} if available in the given context, or {@code null} otherwise.
		 * The current item variant can be {@linkplain ContainerItemContext#getItemVariant() retrieved from the context}.
		 */
		@Nullable
		Storage<FluidVariant> find(ContainerItemContext context);
	}

	private FluidStorage() {
	}

	static {
		// Initialize vanilla cauldron wrappers
		CauldronFluidContent.getForFluid(Fluids.WATER);

		// Support for SidedStorageBlockEntity.
		FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, direction) -> {
			if (blockEntity instanceof SidedStorageBlockEntity sidedStorageBlockEntity) {
				return sidedStorageBlockEntity.getFluidStorage(direction);
			}

			return null;
		});

		// Register combined fallback
		FluidStorage.ITEM.registerFallback((stack, context) -> GENERAL_COMBINED_PROVIDER.invoker().find(context));
		// Register empty bucket storage
		combinedItemApiProvider(Items.BUCKET).register(EmptyBucketStorage::new);
		// Register full bucket storage
		GENERAL_COMBINED_PROVIDER.register(context -> {
			if (context.getItemVariant().getItem() instanceof BucketItem bucketItem) {
				Fluid bucketFluid = ((BucketItemAccessor) bucketItem).fabric_getFluid();

				// Make sure the mapping is bidirectional.
				if (bucketFluid != null && bucketFluid.getBucket() == bucketItem) {
					return new FullItemFluidStorage(context, Items.BUCKET, FluidVariant.of(bucketFluid), FluidConstants.BUCKET);
				}
			}

			return null;
		});
		// Register empty bottle storage, only water potion is supported!
		combinedItemApiProvider(Items.GLASS_BOTTLE).register(context -> {
			return new EmptyItemFluidStorage(context, emptyBottle -> {
				ItemStack newStack = emptyBottle.toStack();
				newStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
				return ItemVariant.of(Items.POTION, newStack.getComponentsPatch());
			}, Fluids.WATER, FluidConstants.BOTTLE);
		});
		// Register water potion storage
		combinedItemApiProvider(Items.POTION).register(WaterPotionStorage::find);
	}
}
