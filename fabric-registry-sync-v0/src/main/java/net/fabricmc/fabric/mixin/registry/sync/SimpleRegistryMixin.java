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

package net.fabricmc.fabric.mixin.registry.sync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemapStateImpl;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements MutableRegistry<T>, RemappableRegistry, ListenableRegistry<T>, FabricRegistry {
	// Namespaces used by the vanilla game. "brigadier" is used by command argument type registry.
	// While Realms use "realms" namespace, it is irrelevant for Registry Sync.
	@Unique
	private static final Set<String> VANILLA_NAMESPACES = Set.of("minecraft", "brigadier");

	@Shadow
	@Final
	private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
	@Shadow
	@Final
	private Reference2IntMap<T> entryToRawId;
	@Shadow
	@Final
	private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
	@Shadow
	@Final
	private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

	@Shadow
	public abstract Optional<RegistryKey<T>> getKey(T entry);

	@Shadow
	public abstract @Nullable T get(@Nullable Identifier id);

	@Shadow
	public abstract RegistryKey<? extends Registry<T>> getKey();

	@Unique
	private static final Logger FABRIC_LOGGER = LoggerFactory.getLogger(SimpleRegistryMixin.class);

	@Unique
	private Event<RegistryEntryAddedCallback<T>> fabric_addObjectEvent;

	@Unique
	private Event<RegistryIdRemapCallback<T>> fabric_postRemapEvent;

	@Unique
	private Object2IntMap<Identifier> fabric_prevIndexedEntries;
	@Unique
	private BiMap<Identifier, RegistryEntry.Reference<T>> fabric_prevEntries;
	@Unique
	// invariant: the sets of keys and values are disjoint (every alias points to a 'deepest' non-alias ID)
	private Map<Identifier, Identifier> aliases = new HashMap<>();

	@Shadow
	public abstract boolean containsId(Identifier id);

	@Shadow
	public abstract String toString();

	@Shadow
	@Final
	private RegistryKey<? extends Registry<T>> key;

	@Shadow
	protected abstract void assertNotFrozen();

	@Override
	public Event<RegistryEntryAddedCallback<T>> fabric_getAddObjectEvent() {
		return fabric_addObjectEvent;
	}

	@Override
	public Event<RegistryIdRemapCallback<T>> fabric_getRemapEvent() {
		return fabric_postRemapEvent;
	}

	@Inject(method = "<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("RETURN"))
	private void init(RegistryKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
		fabric_addObjectEvent = EventFactory.createArrayBacked(RegistryEntryAddedCallback.class,
			(callbacks) -> (rawId, id, object) -> {
				for (RegistryEntryAddedCallback<T> callback : callbacks) {
					callback.onEntryAdded(rawId, id, object);
				}
			}
		);
		// aliasing: check that no new entries use the id of an alias
		fabric_addObjectEvent.register((rawId, id, object) -> {
			if (aliases.containsKey(id)) {
				throw new IllegalArgumentException(
						"Tried registering %s to registry %s, but it is already an alias (for %s)".formatted(
								id,
								this.key,
								aliases.get(id)
						)
				);
			}
		});
		fabric_postRemapEvent = EventFactory.createArrayBacked(RegistryIdRemapCallback.class,
			(callbacks) -> (a) -> {
				for (RegistryIdRemapCallback<T> callback : callbacks) {
					callback.onRemap(a);
				}
			}
		);
	}

	@Unique
	private void onChange(RegistryKey<T> registryKey) {
		if (RegistrySyncManager.postBootstrap || !VANILLA_NAMESPACES.contains(registryKey.getValue().getNamespace())) {
			RegistryAttributeHolder holder = RegistryAttributeHolder.get(getKey());

			if (!holder.hasAttribute(RegistryAttribute.MODDED)) {
				Identifier id = getKey().getValue();
				FABRIC_LOGGER.debug("Registry {} has been marked as modded, registry entry {} was changed", id, registryKey.getValue());
				RegistryAttributeHolder.get(getKey()).addAttribute(RegistryAttribute.MODDED);
			}
		}
	}

	@Inject(method = "add", at = @At("RETURN"))
	private void set(RegistryKey<T> key, T entry, RegistryEntryInfo arg, CallbackInfoReturnable<RegistryEntry.Reference<T>> info) {
		// We need to restore the 1.19 behavior of binding the value to references immediately.
		// Unfrozen registries cannot be interacted with otherwise, because the references would throw when
		// trying to access their values.
		info.getReturnValue().setValue(entry);

		fabric_addObjectEvent.invoker().onEntryAdded(entryToRawId.getInt(entry), key.getValue(), entry);
		onChange(key);
	}

	@Override
	public void remap(Object2IntMap<Identifier> remoteIndexedEntries, RemapMode mode) throws RemapException {
		// Throw on invalid conditions.
		switch (mode) {
		case AUTHORITATIVE:
			break;
		case REMOTE: {
			List<String> strings = null;

			for (Identifier remoteId : remoteIndexedEntries.keySet()) {
				if (!this.containsId(remoteId)) {
					if (strings == null) {
						strings = new ArrayList<>();
					}

					strings.add(" - " + remoteId);
				}
			}

			if (strings != null) {
				StringBuilder builder = new StringBuilder("Received ID map for " + getKey() + " contains IDs unknown to the receiver!");

				for (String s : strings) {
					builder.append('\n').append(s);
				}

				throw new RemapException(builder.toString());
			}

			break;
		}
		}

		// Make a copy of the previous maps.
		// For now, only one is necessary - on an integrated server scenario,
		// AUTHORITATIVE == CLIENT, which is fine.
		// The reason we preserve the first one is because it contains the
		// vanilla order of IDs before mods, which is crucial for vanilla server
		// compatibility.
		if (fabric_prevIndexedEntries == null) {
			fabric_prevIndexedEntries = new Object2IntOpenHashMap<>();
			fabric_prevEntries = HashBiMap.create(idToEntry);

			for (T o : this) {
				fabric_prevIndexedEntries.put(getId(o), getRawId(o));
			}
		}

		Int2ObjectMap<Identifier> oldIdMap = new Int2ObjectOpenHashMap<>();

		for (T o : this) {
			oldIdMap.put(getRawId(o), getId(o));
		}

		// If we're AUTHORITATIVE, we append entries which only exist on the
		// local side to the new entry list. For REMOTE, we instead drop them.
		switch (mode) {
		case AUTHORITATIVE: {
			int maxValue = 0;

			Object2IntMap<Identifier> oldRemoteIndexedEntries = remoteIndexedEntries;
			remoteIndexedEntries = new Object2IntOpenHashMap<>();

			for (Identifier id : oldRemoteIndexedEntries.keySet()) {
				int v = oldRemoteIndexedEntries.getInt(id);
				remoteIndexedEntries.put(id, v);
				if (v > maxValue) maxValue = v;
			}

			for (Identifier id : getIds()) {
				if (!remoteIndexedEntries.containsKey(id)) {
					FABRIC_LOGGER.warn("Adding " + id + " to saved/remote registry.");
					remoteIndexedEntries.put(id, ++maxValue);
				}
			}

			break;
		}
		case REMOTE: {
			int maxId = -1;

			for (Identifier id : getIds()) {
				if (!remoteIndexedEntries.containsKey(id)) {
					if (maxId < 0) {
						for (int value : remoteIndexedEntries.values()) {
							if (value > maxId) {
								maxId = value;
							}
						}
					}

					if (maxId < 0) {
						throw new RemapException("Failed to assign new id to client only registry entry");
					}

					maxId++;

					FABRIC_LOGGER.debug("An ID for {} was not sent by the server, assuming client only registry entry and assigning a new id ({}) in {}", id.toString(), maxId, getKey().getValue().toString());
					remoteIndexedEntries.put(id, maxId);
				}
			}

			break;
		}
		}

		Int2IntMap idMap = new Int2IntOpenHashMap();

		for (int i = 0; i < rawIdToEntry.size(); i++) {
			RegistryEntry.Reference<T> reference = rawIdToEntry.get(i);

			// Unused id, can happen if there are holes in the registry.
			if (reference == null) {
				throw new RemapException("Unused id " + i + " in registry " + getKey().getValue());
			}

			Identifier id = reference.registryKey().getValue();

			// see above note
			if (remoteIndexedEntries.containsKey(id)) {
				idMap.put(i, remoteIndexedEntries.getInt(id));
			}
		}

		// entries was handled above, if it was necessary.
		rawIdToEntry.clear();
		entryToRawId.clear();

		List<Identifier> orderedRemoteEntries = new ArrayList<>(remoteIndexedEntries.keySet());
		orderedRemoteEntries.sort(Comparator.comparingInt(remoteIndexedEntries::getInt));

		for (Identifier identifier : orderedRemoteEntries) {
			int id = remoteIndexedEntries.getInt(identifier);
			RegistryEntry.Reference<T> object = idToEntry.get(identifier);

			// Warn if an object is missing from the local registry.
			// This should only happen in AUTHORITATIVE mode, and as such we
			// throw an exception otherwise.
			if (object == null) {
				if (mode != RemapMode.AUTHORITATIVE) {
					throw new RemapException(identifier + " missing from registry, but requested!");
				} else {
					FABRIC_LOGGER.warn(identifier + " missing from registry, but requested!");
				}

				continue;
			}

			// Add the new object
			rawIdToEntry.size(Math.max(this.rawIdToEntry.size(), id + 1));
			assert rawIdToEntry.get(id) == null;
			rawIdToEntry.set(id, object);
			entryToRawId.put(object.value(), id);
		}

		fabric_getRemapEvent().invoker().onRemap(new RemapStateImpl<>(this, oldIdMap, idMap));
	}

	@Override
	public void unmap() throws RemapException {
		if (fabric_prevIndexedEntries != null) {
			List<Identifier> addedIds = new ArrayList<>();

			// Emit AddObject events for previously culled objects.
			for (Identifier id : fabric_prevEntries.keySet()) {
				if (!idToEntry.containsKey(id)) {
					assert fabric_prevIndexedEntries.containsKey(id);
					addedIds.add(id);
				}
			}

			idToEntry.clear();
			keyToEntry.clear();

			idToEntry.putAll(fabric_prevEntries);

			for (Map.Entry<Identifier, RegistryEntry.Reference<T>> entry : fabric_prevEntries.entrySet()) {
				RegistryKey<T> entryKey = RegistryKey.of(getKey(), entry.getKey());
				keyToEntry.put(entryKey, entry.getValue());
			}

			remap(fabric_prevIndexedEntries, RemapMode.AUTHORITATIVE);

			for (Identifier id : addedIds) {
				fabric_getAddObjectEvent().invoker().onEntryAdded(entryToRawId.getInt(idToEntry.get(id)), id, get(id));
			}

			fabric_prevIndexedEntries = null;
			fabric_prevEntries = null;
		}
	}

	@Override
	public void addAlias(Identifier old, Identifier newId) {
		Objects.requireNonNull(old, "alias cannot be null");
		Objects.requireNonNull(newId, "aliased id cannot be null");

		if (aliases.containsKey(old)) {
			throw new IllegalArgumentException(
					"Tried adding %s as an alias for %s, but it is already an alias (for %s) in registry %s".formatted(
							old,
							newId,
							aliases.get(old),
							this.key
					)
			);
		}

		if (this.idToEntry.containsKey(old)) {
			throw new IllegalArgumentException(
					"Tried adding %s as an alias, but it is already present in registry %s".formatted(
							old,
							this.key
					)
			);
		}

		if (old.equals(aliases.get(newId))) {
			// since an alias corresponds to at most one identifier, this is the only way to create a cycle
			// that doesn't already fall under the first condition
			throw new IllegalArgumentException(
					"Making %1$s an alias of %2$s would create a cycle, as %2$s is already an alias of %1$s (registry %3$s)".formatted(
							old,
							newId,
							this.key
					)
			);
		}

		if (!this.idToEntry.containsKey(newId)) {
			FABRIC_LOGGER.warn(
					"Adding {} as an alias for {}, but the latter doesn't exist in registry {}",
					old,
					newId,
					this.key
			);
		}

		assertNotFrozen();

		// recompute alias map to preserve invariant, i.e. make sure all keys point to a non-alias ID
		Identifier deepest = aliases.getOrDefault(newId, newId);

		for (Map.Entry<Identifier, Identifier> entry : aliases.entrySet()) {
			if (old.equals(entry.getValue())) {
				entry.setValue(deepest);
			}
		}

		aliases.put(old, deepest);
		FABRIC_LOGGER.debug("Adding alias {} for {} in registry {}", old, newId, this.key);
	}

	@ModifyVariable(
			method = {
					"getEntry(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;",
					"get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;",
					"containsId"
			},
			at = @At("HEAD"),
			argsOnly = true
	)
	private Identifier aliasIdentifierParameter(Identifier original) {
		return aliases.getOrDefault(original, original);
	}

	@ModifyVariable(
			method = {
					"get(Lnet/minecraft/registry/RegistryKey;)Ljava/lang/Object;",
					"getOptional(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;",
					"getOrCreateEntry",
					"contains",
					"getEntryInfo"
			},
			at = @At("HEAD"),
			argsOnly = true
	)
	private RegistryKey<T> aliasRegistryKeyParameter(RegistryKey<T> original) {
		Identifier aliased = aliases.get(original.getValue());
		return aliased == null ? original : RegistryKey.of(original.getRegistryRef(), aliased);
	}
}
