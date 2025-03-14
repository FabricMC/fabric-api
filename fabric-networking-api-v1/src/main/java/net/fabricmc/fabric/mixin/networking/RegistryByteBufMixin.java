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

package net.fabricmc.fabric.mixin.networking;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.networking.FabricRegistryByteBuf;

@Mixin(RegistryByteBuf.class)
public class RegistryByteBufMixin implements FabricRegistryByteBuf {
	@Unique
	private Set<Identifier> sendableConfigurationChannels = null;

	@Override
	public void fabric_setSendableConfigurationChannels(Set<Identifier> globalChannels) {
		this.sendableConfigurationChannels = Objects.requireNonNull(globalChannels);
	}

	@Override
	public @Nullable Set<Identifier> fabric_getSendableConfigurationChannels() {
		return this.sendableConfigurationChannels;
	}
}
