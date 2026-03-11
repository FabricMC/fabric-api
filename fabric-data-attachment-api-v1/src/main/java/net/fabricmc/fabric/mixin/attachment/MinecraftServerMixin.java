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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachmentsProvider;
import net.fabricmc.fabric.impl.attachment.AttachmentSavedData;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements GlobalAttachmentsProvider {
	@Shadow
	@Final
	private SavedDataStorage savedDataStorage;
	@Unique
	private GlobalAttachmentsImpl globalAttachments;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initGlobalAttachments(CallbackInfo ci) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		globalAttachments = new GlobalAttachmentsImpl(server);

		var type = new SavedDataType<>(
				AttachmentSavedData.ID,
				() -> new AttachmentSavedData(globalAttachments),
				AttachmentSavedData.codec(server),
				null
		);
		savedDataStorage.computeIfAbsent(type);
	}

	@Override
	public GlobalAttachments globalAttachments() {
		return globalAttachments;
	}
}
