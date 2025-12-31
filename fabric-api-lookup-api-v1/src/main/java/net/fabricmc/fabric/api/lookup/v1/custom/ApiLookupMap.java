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

package net.fabricmc.fabric.api.lookup.v1.custom;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.lookup.custom.ApiLookupMapImpl;

//CHECKSTYLE.OFF: JavadocStyle - Checkstyle didn't like <A, C>, even though {@code ... } already escapes it.
/// A map meant to be used as the backing storage for custom `ApiLookup` instances,
/// to implement a custom equivalent of [BlockApiLookup#get][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup#get].
/// ### Usage Example
/// We will be implementing the following simplified version of an API lookup interface for item stacks
/// to illustrate how to use [ApiLookupMap] and [ApiProviderMap].
/// <pre>
/// `public interface ItemStackApiLookup<A, C>{static <A, C> ItemStackApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass){return ItemStackApiLookupImpl.get(lookupId, apiClass, contextClass);}// Find an API instance.＠NullableA find(ItemStack stack, C context);// Expose the API for some item.void register(ItemStackApiProvider<A, C> provider, Item item);interface ItemStackApiProvider<A, C>{// Return an API instance if available, or null otherwise.＠NullableA find(ItemStack stack, C context);}}`</pre>
/// All the implementation can fit in a single class:
/// <pre>
/// `public class ItemStackApiLookupImpl<A, C> implements ItemStackApiLookup<A, C>{// Management of lookup instances is handled by ApiLookupMap.private static final ApiLookupMap<ItemStackApiLookup<?, ?>> LOOKUPS = ApiLookupMap.create(ItemStackApiLookupImpl::new);// We have to perform an unchecked cast to convert <?, ?> back to <A, C>.＠SuppressWarnings("unchecked")public static <A, C> ItemStackApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass){// Null checks are already handled by ApiLookupMap#get.return (ItemStackApiLookup<A, C>) LOOKUPS.getLookup(lookupId, apiClass, contextClass);}private ItemStackApiLookupImpl(Identifier id, Class<?> apiClass, Class<?> contextClass){// We don't use these parameters, so nothing to do here.// In practice, these parameters should be stored and exposed with identifier(), apiClass() and contextClass() getter functions.}// We will use an ApiProviderMap to store the providers.private final ApiProviderMap<Item, ItemStackApiProvider<A, C>> providerMap = ApiProviderMap.create();＠Nullablepublic A find(ItemStack stack, C context){ItemStackApiProvider<A, C> provider = providerMap.get(stack.getItem());if (provider == null){return null;}else{return provider.find(stack, context);}}public void register(ItemStackApiProvider provider, Item item){// Let's add a few null checks just in case.Objects.requireNonNull(provider, "ItemStackApiProvider may not be null.");Objects.requireNonNull(item, "Item may not be null.");// Register the provider, or warn if it is already registeredif (providerMap.putIfAbsent(item, provider) != null){// Emit a warning printing the item ID to help users debug more easily.LoggerFactory.getLogger("The name of your mod").warn("Encountered duplicate API provider registration for item " + Registry.ITEM.getId(item) + ".");}}}`</pre>
///
/// @param <L> The type of the lookup implementation, similar to the existing [BlockApiLookup][net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup].
//CHECKSTYLE.ON: JavadocStyle
@ApiStatus.NonExtendable
public interface ApiLookupMap<L> extends Iterable<L> {
	/// Create a new instance.
	///
	/// @param lookupConstructor The constructor that is used to create API lookup instances.
	static <L> ApiLookupMap<L> create(LookupConstructor<L> lookupConstructor) {
		Objects.requireNonNull(lookupConstructor, "Lookup factory may not be null.");

		return new ApiLookupMapImpl<>(lookupConstructor);
	}

	/// Retrieve the API lookup associated with an identifier.
	///
	/// @param lookupId The unique identifier of the lookup.
	/// @param apiClass The class of the queried API.
	/// @param contextClass The class of the queried additional context.
	/// @return The unique lookup with the passed lookupId.
	/// @throws IllegalArgumentException If another `apiClass` or another `contextClass` was already registered with the same identifier.
	/// @throws NullPointerException If one of the arguments is null.
	L getLookup(Identifier lookupId, Class<?> apiClass, Class<?> contextClass);

	@FunctionalInterface
	interface LookupConstructor<L> {
		/// Create a new API lookup implementation.
		///
		/// @param identifier The identifier of this lookup.
		/// @param apiClass The API class passed to [#getLookup].
		/// @param contextClass The context class passed to [#getLookup].
		L get(Identifier identifier, Class<?> apiClass, Class<?> contextClass);
	}

	/// Create a new instance.
	///
	/// @param lookupFactory The factory that is used to create API lookup instances.
	/// @deprecated [LookupConstructor] should be used instead of lookup factory, to expose the identifier.
	@Deprecated(forRemoval = true)
	static <L> ApiLookupMap<L> create(LookupFactory<L> lookupFactory) {
		return create((id, apiClass, contextClass) -> lookupFactory.get(apiClass, contextClass));
	}

	/// @deprecated [LookupConstructor] should be used instead as it also passes the identifier.
	@Deprecated(forRemoval = true)
	interface LookupFactory<L> {
		/// Create a new API lookup implementation.
		///
		/// @param apiClass The API class passed to [#getLookup].
		/// @param contextClass The context class passed to [#getLookup].
		L get(Class<?> apiClass, Class<?> contextClass);
	}
}
