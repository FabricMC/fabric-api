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

package net.fabricmc.fabric.mixin.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.DataAccessorHandler;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;

@Mixin({BlockEntity.class, Entity.class, Level.class, ChunkAccess.class, GlobalAttachmentsImpl.class})
abstract class AttachmentTargetsMixin implements AttachmentTargetImpl {
	@Unique
	@Nullable
	private IdentityHashMap<AttachmentType<?>, Object> dataAttachments = null;
	@Unique
	@Nullable
	private IdentityHashMap<AttachmentType<?>, AttachmentChange> syncedAttachments = null;
	@Unique
	@Nullable
	private Set<AttachmentType<?>> deferredSyncedAttachments = null;
	@Unique
	@Nullable
	private IdentityHashMap<AttachmentType<?>, Event<OnAttachedSet<?>>> attachedChangedListeners = null;

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getAttached(AttachmentType<T> type) {
		return dataAttachments == null ? null : (T) dataAttachments.get(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T setAttached(AttachmentType<T> type, @Nullable T value) {
		T oldValue;

		if (value == null) {
			oldValue = dataAttachments == null ? null : (T) dataAttachments.remove(type);
		} else {
			if (dataAttachments == null) {
				dataAttachments = new IdentityHashMap<>();
			}

			oldValue = (T) dataAttachments.put(type, value);
		}

		if (attachedChangedListeners != null) {
			Event<OnAttachedSet<T>> event = (Event<OnAttachedSet<T>>) (Event<?>) attachedChangedListeners.get(type);

			if (event != null) {
				event.invoker().onAttachedSet(oldValue, value);
			}
		}

		if (!Objects.equals(oldValue, value)) {
			this.fabric_markChanged(type);

			if (this.fabric_shouldTryToSync() && type.isSynced()) {
				AttachmentChange change = AttachmentChange.create(fabric_getSyncTargetInfo(), type, value, fabric_getRegistryAccess());
				acknowledgeSyncedEntry(type, change);
				this.fabric_syncChange(type, change);
			}
		}

		return oldValue;
	}

	@Override
	public boolean hasAttached(AttachmentType<?> type) {
		return dataAttachments != null && dataAttachments.containsKey(type);
	}

	@Override
	public <A> Event<OnAttachedSet<A>> onAttachedSet(AttachmentType<A> type) {
		if (attachedChangedListeners == null) {
			attachedChangedListeners = new IdentityHashMap<>();
		}

		return (Event<OnAttachedSet<A>>) (Event<?>) attachedChangedListeners.computeIfAbsent(type, t -> {
			return (Event<OnAttachedSet<?>>) (Event<?>) EventFactory.createArrayBacked(OnAttachedSet.class, (Function<OnAttachedSet<A>[], OnAttachedSet<A>>) listeners -> (oldValue, newValue) -> {
				for (OnAttachedSet<A> listener : listeners) {
					listener.onAttachedSet(oldValue, newValue);
				}
			});
		});
	}

	@Override
	public void fabric_writeAttachmentsToNbt(ValueOutput output) {
		AttachmentSerializingImpl.serializeAttachmentData(output, dataAttachments);
	}

	@Override
	public void fabric_readAttachmentsFromNbt(ValueInput input) {
		if (DataAccessorHandler.APPLYING_DATA_CHANGE.isBound()) {
			// DataAccessorHandler handles applying data changes separately.
			return;
		}

		// Note on player targets: no syncing can happen here as the networkHandler is still null
		// Instead it is done on player join (see AttachmentSync)
		IdentityHashMap<AttachmentType<?>, Object> fromNbt = AttachmentSerializingImpl.deserializeAttachmentData(input);

		// If the NBT is devoid of data attachments, treat it as a no-op, rather than wiping them out.
		// Any changes to data attachments (including removals) post-load are done independently of this
		// code path, so we don't need to blindly overwrite it every time if Vanilla MC sends updates
		// (i.e. block entity updates) sans data attachments. See https://github.com/FabricMC/fabric/issues/4638
		if (fromNbt == null) {
			return;
		}

		this.dataAttachments = fromNbt;

		if (this.fabric_shouldTryToSync() && this.dataAttachments != null) {
			this.dataAttachments.forEach((type, value) -> {
				if (type.isSynced()) {
					acknowledgeSynced(type, value, input.lookup());
				}
			});

			// Avoid unnecessary extra syncing after initial sync
			fabric_clearDeferredSyncChanges();
		}
	}

	@Override
	public boolean fabric_hasPersistentAttachments() {
		return AttachmentSerializingImpl.hasPersistentAttachments(dataAttachments);
	}

	@Override
	public Map<AttachmentType<?>, ?> fabric_getAttachments() {
		return dataAttachments;
	}

	@Unique
	private void acknowledgeSynced(AttachmentType<?> type, Object value, HolderLookup.Provider registries) {
		RegistryAccess registryAccess = (registries instanceof RegistryAccess ra) ? ra : fabric_getRegistryAccess();
		acknowledgeSyncedEntry(type, AttachmentChange.create(fabric_getSyncTargetInfo(), type, value, registryAccess));
	}

	@Unique
	private void acknowledgeSyncedEntry(AttachmentType<?> type, @Nullable AttachmentChange change) {
		if (change == null) {
			if (syncedAttachments == null) {
				return;
			}

			syncedAttachments.remove(type);

			if (fabric_shouldDeferSync()) {
				deferredSyncedAttachments.add(type);
			}
		} else {
			if (syncedAttachments == null) {
				syncedAttachments = new IdentityHashMap<>();
			}

			syncedAttachments.put(type, change);

			if (fabric_shouldDeferSync()) {
				if (deferredSyncedAttachments == null) {
					deferredSyncedAttachments = Collections.newSetFromMap(new IdentityHashMap<>());
				}

				deferredSyncedAttachments.add(type);
			}
		}
	}

	@Override
	public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
		if (syncedAttachments == null) {
			return;
		}

		for (Map.Entry<AttachmentType<?>, AttachmentChange> entry : syncedAttachments.entrySet()) {
			if (((AttachmentTypeImpl<?>) entry.getKey()).syncPredicate().test(this, player)) {
				changeOutput.accept(entry.getValue());
			}
		}
	}

	@Override
	public void fabric_sendAndClearDeferredSyncChanges(List<ServerPlayer> players) {
		if (syncedAttachments == null || deferredSyncedAttachments == null || deferredSyncedAttachments.isEmpty()) {
			return;
		}

		List<AttachmentChange> deferredChanges = deferredSyncedAttachments.stream().map(type -> {
			AttachmentChange change = syncedAttachments.get(type);

			if (change == null) { // attachment was removed
				change = AttachmentChange.create(fabric_getSyncTargetInfo(), type, null, fabric_getRegistryAccess());
			}

			return change;
		}).toList();

		for (ServerPlayer player : players) {
			List<AttachmentChange> syncableChanges = new ArrayList<>();

			for (AttachmentChange change : deferredChanges) {
				if (((AttachmentTypeImpl<?>) change.type()).syncPredicate().test(this, player)) {
					syncableChanges.add(change);
				}
			}

			if (!syncableChanges.isEmpty()) {
				AttachmentSync.trySync(syncableChanges, player);
			}
		}

		deferredSyncedAttachments.clear();
	}

	@Override
	public void fabric_clearDeferredSyncChanges() {
		if (deferredSyncedAttachments != null) {
			deferredSyncedAttachments.clear();
		}
	}

	@Override
	public <T> void fabric_updateSyncTarget(AttachmentTargetInfo<T> oldTargetInfo, AttachmentTargetInfo<T> newTargetInfo) {
		if (syncedAttachments == null) {
			return;
		}

		syncedAttachments.replaceAll((_, attachmentChange) -> {
			if (attachmentChange.targetInfo().equals(oldTargetInfo)) {
				return attachmentChange.withNewTarget(newTargetInfo);
			}

			return attachmentChange;
		});
	}
}
