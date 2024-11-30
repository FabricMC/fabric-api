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

package net.fabricmc.fabric.impl.registry.sync;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.registry.RegistryKey;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;

public final class RegistryAttributeImpl implements RegistryAttributeHolder {
	private static final Map<RegistryKey<?>, RegistryAttributeHolder> HOLDER_MAP = new ConcurrentHashMap<>();

	public static RegistryAttributeHolder getHolder(RegistryKey<?> registryKey) {
		return HOLDER_MAP.computeIfAbsent(registryKey, key -> new RegistryAttributeImpl());
	}

	private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

	private RegistryAttributeImpl() {
	}

	@Override
	public RegistryAttributeHolder addAttribute(RegistryAttribute attribute) {
		attributes.add(attribute);
		return this;
	}

	@VisibleForTesting
	public void removeAttribute(RegistryAttribute attribute) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new AssertionError();
		}

		attributes.remove(attribute);
	}

	@Override
	public boolean hasAttribute(RegistryAttribute attribute) {
		return attributes.contains(attribute);
	}
}
