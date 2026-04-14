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

package net.fabricmc.fabric.api.client.rendering.v1.level;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.phys.HitResult;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events fired from within {@link LevelExtractor} to be used by mods to add or modify extracted render state.
 */
public class LevelExtractionEvents {
	/**
	 * Called after the block outline render state is extracted, before it is drawn.
	 * Can optionally cancel the default rendering by setting the outline render state to null
	 * but all handlers for this event will always be called.
	 *
	 * <p>Use this to extract custom data needed when decorating or replacing
	 * the default block outline rendering for specific modded blocks
	 * or when normally, the block outline would not be extracted to be rendered.
	 * Normally, outline rendering will not happen for entities, fluids,
	 * or other game objects that do not register a block-type hit.
	 *
	 * <p>To attach modded data to vanilla render states, see {@link net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState FabricRenderState}.
	 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.multiplayer.ClientLevel}.
	 *
	 * <p>Setting the outline render state to null by any event subscriber
	 * will cancel the default block outline render and suppress the {@link LevelRenderEvents#BEFORE_BLOCK_OUTLINE} event.
	 * This has no effect on other subscribers to this event - all subscribers will always be called.
	 * Setting outline render state to null here is appropriate
	 * when there is still a valid block hit (with a fluid, for example)
	 * and you don't want the block outline render to appear.
	 *
	 * <p>This event should NOT be used for general-purpose replacement of
	 * the default block outline rendering because it will interfere with mod-specific
	 * renders.  Mods that replace the default block outline for specific blocks
	 * should instead subscribe to {@link LevelRenderEvents#BEFORE_BLOCK_OUTLINE}.
	 */
	public static final Event<AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = EventFactory.createArrayBacked(AfterBlockOutlineExtraction.class, callbacks -> (context, hit) -> {
		for (final AfterBlockOutlineExtraction callback : callbacks) {
			callback.afterBlockOutlineExtraction(context, hit);
		}
	});

	/**
	 * Called after all render states are extracted, before any are drawn.
	 * Use this to extract general custom data needed for rendering.
	 *
	 * <p>To attach modded data to vanilla render states, see {@link net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState FabricRenderState}.
	 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.multiplayer.ClientLevel}.
	 */
	public static final Event<EndExtraction> END_EXTRACTION = EventFactory.createArrayBacked(EndExtraction.class, callbacks -> context -> {
		for (final EndExtraction callback : callbacks) {
			callback.endExtraction(context);
		}
	});

	@FunctionalInterface
	public interface AfterBlockOutlineExtraction {
		void afterBlockOutlineExtraction(LevelExtractionContext context, @Nullable HitResult result);
	}

	@FunctionalInterface
	public interface EndExtraction {
		void endExtraction(LevelExtractionContext context);
	}
}
