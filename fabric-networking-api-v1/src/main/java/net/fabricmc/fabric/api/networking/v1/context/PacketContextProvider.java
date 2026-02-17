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

package net.fabricmc.fabric.api.networking.v1.context;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.Connection;

/**
 * This interface is used as a way to retrieve {@link PacketContext} from Connection and PacketListeners.
 *
 * <p>Note: This interface is automatically implemented on {@link Connection} and vanilla PacketListeners via Mixin and interface injection.
 */
@ApiStatus.NonExtendable
public interface PacketContextProvider {
	default PacketContext getPacketContext() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
