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

package net.fabricmc.fabric.test.rendering;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugSubscription;

public final class DebugSubscriptions {
	public static final DebugSubscription<SusDebugInfo> SUS_AVATAR = register(
			"sus",
			SusDebugInfo.STREAM_CODEC
	);

	public static void init() {
	}

	private static <T> DebugSubscription<T> register(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return Registry.register(BuiltInRegistries.DEBUG_SUBSCRIPTION, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", name), new DebugSubscription<>(streamCodec));
	}
}
