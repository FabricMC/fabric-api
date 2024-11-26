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

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.attachment.sync.SupportedAttachmentsClientConnection;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements SupportedAttachmentsClientConnection {
	private Set<Identifier> fabric_supportedAttachments = new HashSet<>();

	@Override
	public void fabric_setSupportedAttachments(Set<Identifier> supportedAttachments) {
		fabric_supportedAttachments = supportedAttachments;
	}

	@Override
	public Set<Identifier> fabric_getSupportedAttachments() {
		return fabric_supportedAttachments;
	}
}
