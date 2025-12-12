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

package net.fabricmc.fabric.api.resource.v1.pack;

/**
 * Represents the resource pack activation type.
 */
public enum PackActivationType {
	/**
	 * Normal activation. The user has full control over the activation of the resource pack.
	 */
	NORMAL,
	/**
	 * Enabled by default. The user has still full control over the activation of the resource pack.
	 */
	DEFAULT_ENABLED,
	/**
	 * Always enabled. The user cannot disable the resource pack.
	 */
	ALWAYS_ENABLED;

	/**
	 * Returns whether this resource pack will be enabled by default or not.
	 *
	 * @return {@code true} if enabled by default, else {@code false}
	 */
	public boolean isEnabledByDefault() {
		return this == DEFAULT_ENABLED || this == ALWAYS_ENABLED;
	}
}
