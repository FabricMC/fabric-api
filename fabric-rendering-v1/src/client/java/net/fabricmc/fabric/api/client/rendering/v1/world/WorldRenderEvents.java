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

import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.util.hit.HitResult;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class WorldRenderEvents {
	private WorldRenderEvents() { }

	public static final Event<AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = EventFactory.createArrayBacked(AfterBlockOutlineExtraction.class, callbacks -> (context, hit) -> {
		for (final AfterBlockOutlineExtraction callback : callbacks) {
			callback.afterBlockOutlineExtraction(context, hit);
		}
	});

	public static final Event<EndExtraction> END_EXTRACTION = EventFactory.createArrayBacked(EndExtraction.class, callbacks -> context -> {
		for (final EndExtraction callback : callbacks) {
			callback.endExtraction(context);
		}
	});

	@ApiStatus.Experimental
	public static final Event<BeforeSubmitEntityCommands> BEFORE_SUBMIT_ENTITY_COMMANDS = EventFactory.createArrayBacked(BeforeSubmitEntityCommands.class, callbacks -> context -> {
		for (final BeforeSubmitEntityCommands callback : callbacks) {
			callback.beforeSubmitEntityCommands(context);
		}
	});

	@ApiStatus.Experimental
	public static final Event<AfterSubmitEntityCommands> AFTER_SUBMIT_ENTITY_COMMANDS = EventFactory.createArrayBacked(AfterSubmitEntityCommands.class, callbacks -> context -> {
		for (final AfterSubmitEntityCommands callback : callbacks) {
			callback.afterSubmitEntityCommands(context);
		}
	});

	public static final Event<StartRender> START_RENDER = EventFactory.createArrayBacked(StartRender.class, callbacks -> context -> {
		for (final StartRender callback : callbacks) {
			callback.startRender(context);
		}
	});

	public static final Event<AfterTerrainRender> AFTER_TERRAIN_RENDER = EventFactory.createArrayBacked(AfterTerrainRender.class, callbacks -> context -> {
		for (final AfterTerrainRender callback : callbacks) {
			callback.afterTerrainRender(context);
		}
	});

	// This might be merged into after terrain render in the future, but right now, these two events are not in the same place.
	public static final Event<BeforeEntityRender> BEFORE_ENTITY_RENDER = EventFactory.createArrayBacked(BeforeEntityRender.class, callbacks -> context -> {
		for (final BeforeEntityRender callback : callbacks) {
			callback.beforeEntityRender(context);
		}
	});

	public static final Event<AfterEntityRender> AFTER_ENTITY_RENDER = EventFactory.createArrayBacked(AfterEntityRender.class, callbacks -> context -> {
		for (final AfterEntityRender callback : callbacks) {
			callback.afterEntityRender(context);
		}
	});

	public static final Event<DebugRender> BEFORE_DEBUG_RENDER = EventFactory.createArrayBacked(DebugRender.class, callbacks -> context -> {
		for (final DebugRender callback : callbacks) {
			callback.beforeDebugRender(context);
		}
	});

	public static final Event<AfterTranslucent> AFTER_TRANSLUCENT = EventFactory.createArrayBacked(AfterTranslucent.class, callbacks -> context -> {
		for (final AfterTranslucent callback : callbacks) {
			callback.afterTranslucent(context);
		}
	});

	public static final Event<BlockOutline> BLOCK_OUTLINE = EventFactory.createArrayBacked(BlockOutline.class, callbacks -> (context, outlineRenderState) -> {
		boolean shouldRender = true;

		for (final BlockOutline callback : callbacks) {
			if (!callback.onBlockOutline(context, outlineRenderState)) {
				shouldRender = false;
			}
		}

		return shouldRender;
	});

	public static final Event<Last> LAST = EventFactory.createArrayBacked(Last.class, callbacks -> context -> {
		for (final Last callback : callbacks) {
			callback.onLast(context);
		}
	});

	@FunctionalInterface
	public interface AfterBlockOutlineExtraction {
		void afterBlockOutlineExtraction(WorldExtractionContext context, HitResult result);
	}

	@FunctionalInterface
	public interface EndExtraction {
		void endExtraction(WorldExtractionContext context);
	}

	@FunctionalInterface
	public interface BeforeSubmitEntityCommands {
		void beforeSubmitEntityCommands(WorldEntitySubmitContext context);
	}

	@FunctionalInterface
	public interface AfterSubmitEntityCommands {
		void afterSubmitEntityCommands(WorldEntitySubmitContext context);
	}

	@FunctionalInterface
	public interface StartRender {
		void startRender(WorldTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTerrainRender {
		void afterTerrainRender(WorldTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeEntityRender {
		void beforeEntityRender(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface AfterEntityRender {
		void afterEntityRender(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface DebugRender {
		void beforeDebugRender(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTranslucent {
		void afterTranslucent(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface BlockOutline {
		boolean onBlockOutline(WorldRenderContext context, OutlineRenderState outlineRenderState);
	}

	@FunctionalInterface
	public interface Last {
		void onLast(WorldRenderContext context);
	}
}
