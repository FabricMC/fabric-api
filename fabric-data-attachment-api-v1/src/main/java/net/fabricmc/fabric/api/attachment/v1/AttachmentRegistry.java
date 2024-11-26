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

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;

/**
 * Class used to create and register {@link AttachmentType}s. To quickly create {@link AttachmentType}s, use one of the various
 * {@code createXXX} methods:
 * <ul>
 * 	   <li>{@link #create(Identifier, Consumer)}: attachments can be further configured by the supplied consumer.</li>
 *     <li>{@link #create(Identifier)}: attachments will be neither persistent nor auto-initialized.</li>
 *     <li>{@link #createDefaulted(Identifier, Supplier)}: attachments will be auto-initialized, but not persistent.</li>
 *     <li>{@link #createPersistent(Identifier, Codec)}: attachments will be persistent, but not auto-initialized.</li>
 * </ul>
 *
 * <p>For finer control over the attachment type and its properties, use {@link #create(Identifier, Consumer)} to
 * get and configure a {@link Builder} instance.</p>
 */
@ApiStatus.Experimental
public final class AttachmentRegistry {
	private AttachmentRegistry() {
	}

	/**
	 * Creates <i>and registers</i> an attachment using a {@linkplain Builder builder}.
	 *
	 * @param id  the identifier of this attachment
	 * @param consumer a lambda that configures a {@link Builder} for this attachment type
	 * @param <A> the type of attached data
	 * @return the registered {@link AttachmentType} instance
	 */
	public static <A> AttachmentType<A> create(Identifier id, Consumer<Builder<A>> consumer) {
		AttachmentRegistry.Builder<A> builder = AttachmentRegistryImpl.builder();

		consumer.accept(builder);

		return builder.buildAndRegister(id);
	}

	/**
	 * Creates <i>and registers</i> an attachment. The data will not be persisted or synchronized.
	 *
	 * @param id  the identifier of this attachment
	 * @param <A> the type of attached data
	 * @return the registered {@link AttachmentType} instance
	 */
	public static <A> AttachmentType<A> create(Identifier id) {
		return create(id, builder -> { });
	}

	/**
	 * Creates <i>and registers</i> an attachment, that will be automatically initialized with a default value
	 * when an attachment does not exist on a given target, using {@link AttachmentTarget#getAttachedOrCreate(AttachmentType)}.
	 *
	 * @param id          the identifier of this attachment
	 * @param initializer the initializer used to provide a default value
	 * @param <A>         the type of attached data
	 * @return the registered {@link AttachmentType} instance
	 */
	public static <A> AttachmentType<A> createDefaulted(Identifier id, Supplier<A> initializer) {
		return create(id, builder -> builder.initializer(initializer));
	}

	/**
	 * Creates <i>and registers</i> an attachment, that will persist across server restarts.
	 *
	 * @param id    the identifier of this attachment
	 * @param codec the codec used for (de)serialization
	 * @param <A>   the type of attached data
	 * @return the registered {@link AttachmentType} instance
	 */
	public static <A> AttachmentType<A> createPersistent(Identifier id, Codec<A> codec) {
		return create(id, builder -> builder.persistent(codec));
	}

	/**
	 * Creates a {@link Builder}, that gives finer control over the attachment's properties. Calling this method
	 * directly is not recommended, as it requires explicit type parameters. {@link #create} should be used instead.
	 *
	 * @param <A> the type of the attached data
	 * @return a {@link Builder} instance
	 */
	@Deprecated
	public static <A> Builder<A> builder() {
		return AttachmentRegistryImpl.builder();
	}

	/**
	 * A builder for creating {@link AttachmentType}s with finer control over their properties.
	 *
	 * @param <A> the type of the attached data
	 */
	@ApiStatus.NonExtendable
	public interface Builder<A> {
		/**
		 * Declares that attachments should persist between server restarts, using the provided {@link Codec} for
		 * (de)serialization.
		 *
		 * @param codec the codec used for (de)serialization
		 * @return the builder
		 */
		Builder<A> persistent(Codec<A> codec);

		/**
		 * Declares that when a player dies and respawns, the attachments of this type should remain.
		 *
		 * @return the builder
		 */
		Builder<A> copyOnDeath();

		/**
		 * Sets the default initializer for this attachment type. The initializer will be called by
		 * {@link AttachmentTarget#getAttachedOrCreate(AttachmentType)} to automatically initialize attachments that
		 * don't yet exist. It must not return {@code null}.
		 *
		 * <p>It is <i>encouraged</i> for {@link A} to be an immutable data type, such as a primitive type
		 * or an immutable record.</p>
		 *
		 * <p>Otherwise, it is important to ensure that attachments <i>do not share any mutable state</i>.
		 * As an example, for a (mutable) list/array attachment type,
		 * the initializer should create a new independent instance each time it is called.</p>
		 *
		 * @param initializer the initializer
		 * @return the builder
		 */
		Builder<A> initializer(Supplier<A> initializer);

		/**
		 * Declares that this attachment type may be automatically synchronized with some clients, as determined by {@code syncPredicate}.
		 *
		 * @param packetCodec the codec used to serialize the attachment data over the network
		 * @param syncPredicate an {@link AttachmentSyncPredicate} determining with which clients to synchronize data
		 * @return the builder
		 */
		AttachmentRegistry.Builder<A> syncWith(PacketCodec<? super RegistryByteBuf, A> packetCodec, AttachmentSyncPredicate syncPredicate);

		/**
		 * Builds and registers the {@link AttachmentType}.
		 *
		 * @param id the attachment's identifier
		 * @return the built and registered {@link AttachmentType}
		 */
		AttachmentType<A> buildAndRegister(Identifier id);
	}
}
