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

package net.fabricmc.fabric.api.client.rendering.v1.world;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.render.SectionRenderState;

@ApiStatus.NonExtendable
public interface WorldTerrainRenderContext extends AbstractWorldRenderContext {
	/**
	 * The render state for all chunk section to be rendered.
	 *
	 * <p>Render states contain information about the current frame used for rendering,
	 * and should be used instead of accessing the world or other objects directly from rendering events.
	 */
	SectionRenderState sectionState();
}
