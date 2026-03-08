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

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * The wrapper class for a {@link RenderStateDataExtractorCallback}. This class links the callback
 * to a {@link EntityType} as well as a {@link RenderStateDataKey} to automically apply the
 * extracted data to.
 */
public final class RenderStateDataExtractor<S extends Entity, T> {
	private final @Nullable EntityType<S> entityType; //null signifies avatar entity extractor
	private final RenderStateDataKey<T> key;
	private final RenderStateDataExtractorCallback<S, T> callback;

	private RenderStateDataExtractor(@Nullable EntityType<S> entityType, RenderStateDataKey<T> key, RenderStateDataExtractorCallback<S, T> callback) {
		this.entityType = entityType;
		this.key = key;
		this.callback = callback;
	}

	/**
	 * Creates a new extractor for a given entity type.
	 * @param entityType The type of entity that the extractor will operate on.
	 * @param key The render state data key that the extracted data will be assigned to.
	 * @param callback The callback in charge of extracting the data from an instance of the
	 *                 entity.
	 * @param <T> The type of data that the callback will extract from the entity and apply to
	 *            the key.
	 * @param <S> The entity class.
	 * @return The newly created data key.
	 */
	public static <S extends Entity, T> RenderStateDataExtractor<S, T> create(EntityType<S> entityType, RenderStateDataKey<T> key, RenderStateDataExtractorCallback<S, T> callback) {
		Objects.requireNonNull(entityType, "entityType");
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(callback, "callback");
		return new RenderStateDataExtractor<>(entityType, key, callback);
	}

	/**
	 * Creates a new extractor for {@link Avatar} entities.
	 * @param key The render state data key that the extracted data will be assigned to.
	 * @param callback The callback in charge of extracting the data from the avatar entity.
	 * @param <T> The type of data that the callback will extract from the entity and apply to
	 *            the key.
	 * @return The newly created data key for avatar entities.
	 */
	public static <T> RenderStateDataExtractor<Avatar, T> createAvatar(RenderStateDataKey<T> key, RenderStateDataExtractorCallback<Avatar, T> callback) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(callback, "callback");
		return new RenderStateDataExtractor<>(null, key, callback);
	}

	public void extract(Entity subject, FabricRenderState state, float partialTicks) {
		// cancels if this is an avatar extractor, but the entity is not an avatar entity.
		if (entityType == null) {
			if (!(subject instanceof Avatar)) return;
		} else { // cancels if the entity type does not match the extractor's entity type.
			if (subject.getType() != entityType) return;
		}

		// for non-avatars, safe cast since object pointer equality implies generic type equality.
		// for avatars, S must be Avatar while Entity must be a derived class of Avatar
		S castedSubject = (S) subject;
		T extractedValue = callback.onExtractRenderState(castedSubject, partialTicks);
		state.setData(key, extractedValue);
	}

	public @Nullable EntityType<S> getEntityType() {
		return entityType;
	}
}
