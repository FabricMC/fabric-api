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

/**
 * Interface to obtain {@link GlobalAttachments} from {@link net.minecraft.world.level.Level Level}
 * and {@link net.minecraft.server.MinecraftServer MinecraftServer}.
 */
// Internally, also implemented on ClientPacketListener for use in ClientLevelMixin.
public interface GlobalAttachmentsProvider {
	default GlobalAttachments globalAttachments() {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}
}
