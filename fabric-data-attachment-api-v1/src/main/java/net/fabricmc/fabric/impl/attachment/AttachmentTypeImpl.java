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

package net.fabricmc.fabric.impl.attachment;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public record AttachmentTypeImpl<A>(
		Identifier identifier,
		@Nullable Supplier<A> initializer,
		@Nullable Codec<A> persistenceCodec,
		@Nullable PacketCodec<? super RegistryByteBuf, A> packetCodec,
		@Nullable AttachmentSyncPredicate syncPredicate,
		boolean copyOnDeath
) implements AttachmentType<A> {
	@Override
	public boolean isSynced() {
		return syncPredicate != null;
	}
}
