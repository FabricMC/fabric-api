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

package net.fabricmc.fabric.api.client.rendering.v1.advancement;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface AdvancementIconRenderContext extends AbstractAdvancementRenderContext {
	/**
	 * @return The x coordinate of the icon's top-left corner.
	 */
	int x();

	/**
	 * @return The y coordinate of the icon's top-left corner.
	 */
	int y();

	/**
	 * @return {@code true} if the mouse is hovered over the icon.
	 */
	boolean isHovered();

	/**
	 * @return {@code true} if the icon is rendered as a selected tab.
	 */
	boolean isSelected();
}
