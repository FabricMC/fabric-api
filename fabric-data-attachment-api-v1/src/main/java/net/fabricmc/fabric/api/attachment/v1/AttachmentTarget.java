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

package net.fabricmc.fabric.api.attachment.v1;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import net.fabricmc.fabric.api.event.Event;

/**
 * Marks all objects on which data can be attached using {@link AttachmentType}s.
 *
 * <p>Fabric implements this on {@link Entity}, {@link BlockEntity}, {@link ServerWorld} and {@link Chunk} via mixin.</p>
 *
 * <p>Note about {@link BlockEntity} and {@link Chunk} targets: these objects need to be notified of changes to their
 * state (using {@link BlockEntity#markDirty()} and {@link Chunk#markNeedsSaving()} respectively), otherwise the modifications will not take effect properly.
 * The {@link #setAttached(AttachmentType, Object)} method handles this automatically, but this needs to be done manually
 * when attached data is mutable, for example:
 * <pre>{@code
 * AttachmentType<MutableType> MUTABLE_ATTACHMENT_TYPE = ...;
 * BlockEntity be = ...;
 * MutableType data = be.getAttachedOrCreate(MUTABLE_ATTACHMENT_TYPE);
 * data.mutate();
 * be.markDirty(); // Required because we are not using setAttached
 * }</pre>
 * </p>
 *
 * <p>
 * Note about {@link BlockEntity} targets: by default, many block entities use their NBT to synchronize with the client.
 * That would mean persistent attachments are automatically synced with the client for those block entities. As this is
 * undesirable behavior, the API completely removes attachments from the result of {@link BlockEntity#toInitialChunkDataNbt},
 * which takes care of all vanilla types. However, modded block entities may be coded differently, so be wary of this
 * when attaching data to modded block entities.
 * </p>
 *
 * <p>
 * Note about {@link Chunk} targets with {@link ChunkStatus#EMPTY}: These chunks are not saved unless the generation
 * progresses to at least {@link ChunkStatus#STRUCTURE_STARTS}. Therefore, persistent attachments to those chunks may not
 * be saved. The {@link #setAttached(AttachmentType, Object)} method will log a warning when this is attempted.
 * </p>
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface AttachmentTarget {
	String NBT_ATTACHMENT_KEY = "fabric:attachments";

	/**
	 * Gets the data associated with the given {@link AttachmentType}, or {@code null} if it doesn't yet exist.
	 *
	 * @param type the attachment type
	 * @param <A>  the type of the data
	 * @return the attached data
	 */
	@Nullable
	default <A> A getAttached(AttachmentType<A> type) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Gets the data associated with the given {@link AttachmentType}, throwing a {@link NullPointerException} if it doesn't yet exist.
	 *
	 * @param type the attachment type
	 * @param <A>  the type of the data
	 * @return the attached data
	 */
	default <A> A getAttachedOrThrow(AttachmentType<A> type) {
		return Objects.requireNonNull(getAttached(type), "No value was attached");
	}

	/**
	 * Gets the data associated with the given {@link AttachmentType}, or initializes it using the provided non-{@code null}
	 * default value.
	 *
	 * @param type         the attachment type
	 * @param defaultValue the fallback default value
	 * @param <A>          the type of the data
	 * @return the attached data, initialized if originally absent
	 */
	default <A> A getAttachedOrSet(AttachmentType<A> type, A defaultValue) {
		Objects.requireNonNull(defaultValue, "default value cannot be null");
		A attached = getAttached(type);

		if (attached != null) {
			return attached;
		} else {
			setAttached(type, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Gets the data associated with the given {@link AttachmentType}, or initializes it using the non-{@code null} result
	 * of the provided {@link Supplier}.
	 *
	 * @param type        the attachment type
	 * @param initializer the fallback initializer
	 * @param <A>         the type of the data
	 * @return the attached data, initialized if originally absent
	 */
	default <A> A getAttachedOrCreate(AttachmentType<A> type, Supplier<A> initializer) {
		A attached = getAttached(type);

		if (attached != null) {
			return attached;
		} else {
			A initialized = Objects.requireNonNull(initializer.get(), "initializer result cannot be null");
			setAttached(type, initialized);
			return initialized;
		}
	}

	/**
	 * Specialization of {@link #getAttachedOrCreate(AttachmentType, Supplier)}, but <i>only for attachment types with
	 * {@link AttachmentType#initializer() initializers}.</i> It will throw an exception if one is not present.
	 *
	 * @param type the attachment type
	 * @param <A>  the type of the data
	 * @return the attached data, initialized if originally absent
	 */
	default <A> A getAttachedOrCreate(AttachmentType<A> type) {
		Supplier<A> init = type.initializer();

		if (init == null) {
			throw new IllegalArgumentException("Single-argument getAttachedOrCreate is reserved for attachment types with default initializers");
		}

		return getAttachedOrCreate(type, init);
	}

	/**
	 * Gets the data associated with the given {@link AttachmentType}, or returns the provided default value if it doesn't exist.
	 * Unlike {@link #getAttachedOrCreate(AttachmentType, Supplier)}, this doesn't initialize the attachment with the default value.
	 *
	 * @param type         the attachment type
	 * @param defaultValue the default value to use as fallback
	 * @param <A>          the type of the attached data
	 * @return the attached data, or the default value
	 */
	@Contract("_, !null -> !null")
	default <A> A getAttachedOrElse(AttachmentType<A> type, @Nullable A defaultValue) {
		A attached = getAttached(type);
		return attached == null ? defaultValue : attached;
	}

	/**
	 * Gets the data associated with the given {@link AttachmentType}, or gets the provided default value from the
	 * provided non-{@code null} supplier if it doesn't exist. The supplier may return {@code null}.
	 * Unlike {@link #getAttachedOrCreate(AttachmentType, Supplier)}, this doesn't initialize the attachment with the default value.
	 *
	 * @param type         the attachment type
	 * @param defaultValue the default value supplier to use as fallback
	 * @param <A>          the type of the attached data
	 * @return the attached data, or the default value
	 */
	default <A> A getAttachedOrGet(AttachmentType<A> type, Supplier<A> defaultValue) {
		Objects.requireNonNull(defaultValue, "default value supplier cannot be null");

		A attached = getAttached(type);
		return attached == null ? defaultValue.get() : attached;
	}

	/**
	 * Sets the data associated with the given {@link AttachmentType}. Passing {@code null} removes the data.
	 *
	 * @param type  the attachment type
	 * @param value the new value
	 * @param <A>   the type of the data
	 * @return the previous data
	 */
	@Nullable
	default <A> A setAttached(AttachmentType<A> type, @Nullable A value) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Tests whether the given {@link AttachmentType} has any associated data. This doesn't create any data, and may return
	 * {@code false} even for attachment types with an automatic initializer.
	 *
	 * @param type the attachment type
	 * @return whether there is associated data
	 */
	default boolean hasAttached(AttachmentType<?> type) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Removes any data associated with the given {@link AttachmentType}. Equivalent to calling {@link #setAttached(AttachmentType, Object)}
	 * with {@code null}.
	 *
	 * @param type the attachment type
	 * @param <A>  the type of the data
	 * @return the previous data
	 */
	@Nullable
	default <A> A removeAttached(AttachmentType<A> type) {
		return setAttached(type, null);
	}

	/**
	 * Provides an {@link Event} that lets the listener know when the target has its {@link AttachmentType} attachment changed.
	 *
	 * <p>The event is called after the attachment was changed, be aware of the potential of recursion if you intend on calling {@link #setAttached} on the target.
	 *
	 * @param type the attachment type
	 * @return event associated with this target and attachment type
	 */
	default <A> Event<OnAttachedSet<A>> onAttachedSet(AttachmentType<A> type) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Modifies the data associated with the given {@link AttachmentType}. Functionally the same as calling {@link #getAttached(AttachmentType)},
	 * applying the modifier, then calling {@link #setAttached(AttachmentType, Object)} with the result. The modifier
	 * takes in the currently attached value, or {@code null} if no attachment is present.
	 *
	 * @param type     the attachment type
	 * @param modifier the operation to apply to the current data, or to {@code null} if it doesn't exist yet
	 * @param <A>      the type of the data
	 * @return the previous data
	 */
	@Nullable
	default <A> A modifyAttached(AttachmentType<A> type, UnaryOperator<A> modifier) {
		return setAttached(type, modifier.apply(getAttached(type)));
	}

	@FunctionalInterface
	interface OnAttachedSet<A> {
		/**
		 * Called after the attachment is set on this target.
		 *
		 * @see AttachmentTarget#onAttachedSet(AttachmentType)
		 * @param oldValue attachment value on the target prior to it being set
		 * @param newValue attachment value on the target after it was set
		 */
		void onAttachedSet(@Nullable A oldValue, @Nullable A newValue);
	}
}
