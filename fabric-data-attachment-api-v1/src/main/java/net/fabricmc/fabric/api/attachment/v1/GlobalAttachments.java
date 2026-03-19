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

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

/**
 * An {@link AttachmentTarget} representing global (server-wide) data attachments that are not tied
 * to any specific {@link Level Level}.
 *
 * <p>This target can be obtained via {@link Level#globalAttachments()}, which returns the appropriate instance
 * on either side. Additionally, {@link MinecraftServer#globalAttachments()} can be used on the server-side
 * for convenience.
 *
 * <p>On the server, the lifecycle of this target is bound to the lifecycle of the {@link MinecraftServer}
 * while on the client it is bound to {@code ClientPacketListener} and should only be accessed when in a world
 * (when {@code Minecraft.getInstance().level} is not null).
 */
public interface GlobalAttachments extends AttachmentTarget {
}
