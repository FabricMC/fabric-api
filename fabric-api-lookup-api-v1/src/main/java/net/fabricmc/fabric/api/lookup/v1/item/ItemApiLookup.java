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

package net.fabricmc.fabric.api.lookup.v1.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.impl.lookup.item.ItemApiLookupImpl;

/**
 * An object that allows retrieving APIs from item stacks.
 * Instances of this interface can be obtained through {@link #get}.
 *
 * <p>When trying to {@link #find} an API for an item stack, the provider registered for the item of the stack will be queried if it exists.
 * If it doesn't exist, or if it returns {@code null}, the fallback providers will be queried in order.
 *
 * <h3>Usage Example</h3>
 * Let us reuse {@code FluidContainer} from {@linkplain BlockApiLookup the BlockApiLookup example}.
 * We will query {@code FluidContainer} instances from the stack directly.
 * We need no context, so we will use {@code Void}.
 *
 * <pre>{@code
 * public interface FluidContainer {
 *     boolean containsFluids(); // return true if not empty
 * }}</pre>
 * We need to create the ItemApiLookup:
 *
 * <pre>{@code
 * public final class MyApi {
 *     public static final ItemApiLookup<FluidContainer, Void> FLUID_CONTAINER_ITEM = ItemApiLookup.get(Identifier.of("mymod", "fluid_container"), FluidContainer.class, Void.class);
 * }}</pre>
 * API instances are easy to access:
 *
 * <pre>{@code
 * FluidContainer container = MyApi.FLUID_CONTAINER_ITEM.find(itemStack, null); // Void is always null
 * if (container != null) {
 *     // Do something with the container
 *     if (container.containsFluids()) {
 *         System.out.println("It contains fluids!");
 *     }
 * }}</pre>
 * For the query to return a useful result, we must expose the API:
 *
 * <pre>{@code
 * // If the item directly implements the interface, registerSelf can be used.
 * public class InfiniteWaterItem implements FluidContainer {
 *     ＠Override
 *     public boolean containsFluids() {
 *         return true; // This item always contains fluids!
 *     }
 * }
 * MyApi.FLUID_CONTAINER_ITEM.registerSelf(INFINITE_WATER_ITEM);
 *
 * // Otherwise, registerForItems can be used.
 * MyApi.FLUID_CONTAINER_ITEM.registerForItems((itemStack, ignored) -> {
 *     // return a FluidContainer for your item, or null if there is none
 *     // the second parameter is Void in this case, so it's always null and can be ignored
 * }, ITEM_INSTANCE, ANOTHER_ITEM_INSTANCE); // register as many items as you want
 *
 * // General fallback, to interface with anything, for example another ItemApiLookup.
 * MyApi.FLUID_CONTAINER_ITEM.registerFallback((itemStack, ignored) -> {
 *     // return something if available, or null
 * });}</pre>
 *
 * <h3>Generic context types</h3>
 * Note that {@code FluidContainer} and {@code Void} were completely arbitrary in this example.
 * We can define any {@code ItemApiLookup&lt;A, C&gt;}, where {@code A} is the type of the queried API, and {@code C} is the type of the additional context
 * (the void parameter in the previous example).
 * If no context is necessary, {@code Void} should be used, and {@code null} instances should be passed, like we did in the example.
 *
 * @param <A> The type of the API.
 * @param <C> The type of the additional context object.
 */
@ApiStatus.NonExtendable
public interface ItemApiLookup<A, C> {
	/**
	 * Retrieve the {@link ItemApiLookup} associated with an identifier, or create it if it didn't exist yet.
	 *
	 * @param lookupId The unique identifier of the lookup.
	 * @param apiClass The class of the API.
	 * @param contextClass The class of the additional context.
	 * @return The unique lookup with the passed lookupId.
	 * @throws IllegalArgumentException If another {@code apiClass} or another {@code contextClass} was already registered with the same identifier.
	 */
	static <A, C> ItemApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
		return ItemApiLookupImpl.get(lookupId, apiClass, contextClass);
	}

	/**
	 * Attempt to retrieve an API from an item stack.
	 *
	 * <p>Note: An API may or may not allow the item stack to be modified by the returned instance.
	 * API authors are strongly encouraged to document this behavior so that implementors can refer
	 * to the API documentation.
	 * <br>While providers may capture a reference to the stack, it is expected that they do not modify it directly.
	 *
	 * @param itemStack The item stack.
	 * @param context Additional context for the query, defined by type parameter C.
	 * @return The retrieved API, or {@code null} if no API was found.
	 */
	@Nullable
	A find(ItemStack itemStack, C context);

	/**
	 * Expose the API for the passed items directly implementing it.
	 *
	 * @param items Items for which to expose the API.
	 * @throws IllegalArgumentException If the API class is not assignable from a class of one of the items.
	 */
	void registerSelf(ItemConvertible... items);

	/**
	 * Expose the API for the passed items.
	 * The mapping from the parameters of the query to the API is handled by the passed {@link ItemApiProvider}.
	 *
	 * @param provider The provider.
	 * @param items The items.
	 */
	void registerForItems(ItemApiProvider<A, C> provider, ItemConvertible... items);

	/**
	 * Expose the API for all queries: the fallbacks providers will be invoked if no object was found using the regular providers.
	 *
	 * @param fallbackProvider The fallback provider.
	 */
	void registerFallback(ItemApiProvider<A, C> fallbackProvider);

	/**
	 * Return the identifier of this lookup.
	 */
	Identifier getId();

	/**
	 * Return the API class of this lookup.
	 */
	Class<A> apiClass();

	/**
	 * Return the context class of this lookup.
	 */
	Class<C> contextClass();

	/**
	 * Return the provider for the passed item (registered with one of the {@code register} functions), or null if none was registered (yet).
	 * Queries should go through {@link #find}, only use this to inspect registered providers!
	 */
	@Nullable
	ItemApiProvider<A, C> getProvider(Item item);

	@FunctionalInterface
	interface ItemApiProvider<A, C> {
		/**
		 * Return an API of type {@code A} if available for the given item stack with the given context, or {@code null} otherwise.
		 *
		 * <p>Note: An API may or may not allow the item stack to be modified by the returned instance.
		 * API authors are strongly encouraged to document this behavior so that implementors can refer
		 * to the API documentation.
		 * <br>While providers may capture a reference to the stack, it is expected that they do not modify it directly.
		 *
		 * @param itemStack The item stack.
		 * @param context Additional context passed to the query.
		 * @return An API of type {@code A}, or {@code null} if no API is available.
		 */
		@Nullable
		A find(ItemStack itemStack, C context);
	}
}
