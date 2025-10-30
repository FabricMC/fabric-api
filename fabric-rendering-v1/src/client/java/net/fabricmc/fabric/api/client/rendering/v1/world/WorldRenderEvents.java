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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.util.hit.HitResult;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Mods should use these events to introduce custom rendering during {@link WorldRenderer#render}
 * without adding complicated and conflict-prone injections there.  Using these events also enables 3rd-party renderers
 * that make large-scale rendering changes to maintain compatibility by calling any broken event invokers directly.
 *
 * <p>These events can be separated into two categories, the "extraction" events and the "drawing" events,
 * reflecting the respective vanilla phases. All data needed for rendering should be prepared in the "extraction" phase
 * and drawn to the frame buffer during the "drawing" phase. All "extraction" events have the suffix "Extraction".
 * All events without the "Extraction" suffix are "drawing" events. All "drawing" events support OpenGL calls.
 *
 * <p>To attach modded data to vanilla render states, see {@link net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState FabricRenderState}.
 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.world.ClientWorld}.
 */
public final class WorldRenderEvents {
	private WorldRenderEvents() { }

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
	 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.world.ClientWorld}.
	 *
	 * <p>Setting the outline render state to null by any event subscriber
	 * will cancel the default block outline render and suppress the {@link #BEFORE_BLOCK_OUTLINE} event.
	 * This has no effect on other subscribers to this event - all subscribers will always be called.
	 * Setting outline render state to null here is appropriate
	 * when there is still a valid block hit (with a fluid, for example)
	 * and you don't want the block outline render to appear.
	 *
	 * <p>This event should NOT be used for general-purpose replacement of
	 * the default block outline rendering because it will interfere with mod-specific
	 * renders.  Mods that replace the default block outline for specific blocks
	 * should instead subscribe to {@link #BEFORE_BLOCK_OUTLINE}.
	 */
	public static final Event<AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = EventFactory.createArrayBacked(AfterBlockOutlineExtraction.class, callbacks -> (context, hit) -> {
		for (final AfterBlockOutlineExtraction callback : callbacks) {
			callback.afterBlockOutlineExtraction(context, hit);
		}
	});

	/**
	 * Called after all render states are extracted, before any is drawn.
	 * Use this to extract general custom data needed for rendering.
	 *
	 * <p>To attach modded data to vanilla render states, see {@link net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState FabricRenderState}.
	 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.world.ClientWorld}.
	 */
	public static final Event<EndExtraction> END_EXTRACTION = EventFactory.createArrayBacked(EndExtraction.class, callbacks -> context -> {
		for (final EndExtraction callback : callbacks) {
			callback.endExtraction(context);
		}
	});

	/**
	 * Called after all chunks to be rendered are uploaded to GPU,
	 * before any chunks are drawn to the framebuffer.
	 */
	public static final Event<StartMain> START_MAIN = EventFactory.createArrayBacked(StartMain.class, callbacks -> context -> {
		for (final StartMain callback : callbacks) {
			callback.startMain(context);
		}
	});

	/**
	 * Called after the {@link net.minecraft.client.render.BlockRenderLayer#SOLID SOLID}, {@link net.minecraft.client.render.BlockRenderLayer#CUTOUT CUTOUT},
	 * and {@link net.minecraft.client.render.BlockRenderLayer#CUTOUT_MIPPED CUTOUT_MIPPED} terrain layers are drawn to the framebuffer,
	 * before entity and block entities are submitted and drawn to the framebuffer.
	 *
	 * <p>Use to render non-translucent terrain to the framebuffer.
	 *
	 * <p>Note that 3rd-party renderers may combine these passes or otherwise alter the
	 * rendering pipeline for sake of performance or features. This can break direct writes to the
	 * framebuffer.  Use this event for cases that cannot be satisfied by FabricBakedModel,
	 * BlockEntityRenderer or other existing abstraction. If at all possible, use an existing terrain
	 * RenderLayer instead of outputting to the framebuffer directly with GL calls.
	 *
	 * <p>The consumer is responsible for setup and tear down of GL state appropriate for the intended output.
	 *
	 * <p>Because solid and cutout quads are depth-tested, order of output does not matter except to improve
	 * culling performance, which should not be significant after primary terrain rendering. This means
	 * mods that currently hook calls to individual render layers can simply execute them all at once when
	 * the event is called.
	 * However, you should not access any data outside the provided render states. If more data is needed,
	 * extract them during {@link #END_EXTRACTION}.
	 */
	public static final Event<BeforeEntities> BEFORE_ENTITIES = EventFactory.createArrayBacked(BeforeEntities.class, callbacks -> context -> {
		for (final BeforeEntities callback : callbacks) {
			callback.beforeEntities(context);
		}
	});

	/**
	 * Called after entities and block entities are drawn to the framebuffer.
	 */
	public static final Event<AfterEntities> AFTER_ENTITIES = EventFactory.createArrayBacked(AfterEntities.class, callbacks -> context -> {
		for (final AfterEntities callback : callbacks) {
			callback.afterEntities(context);
		}
	});

	/**
	 * Called after entities, block breaking, and most non-translucent objects are drawn to the framebuffer,
	 * before vanilla debug renderers and translucency are drawn to the framebuffer.
	 *
	 * <p>Use to drawn lines, overlays and other content similar to vanilla debug renders.
	 */
	public static final Event<DebugRender> BEFORE_DEBUG_RENDER = EventFactory.createArrayBacked(DebugRender.class, callbacks -> context -> {
		for (final DebugRender callback : callbacks) {
			callback.beforeDebugRender(context);
		}
	});

	/**
	 * Called after entities and block entities are drawn to the framebuffer,
	 * before translucent terrain is drawn to the framebuffer,
	 * and before translucency combine has happened in fabulous mode.
	 *
	 * <p>Use to draw on top of the main and entity framebuffer targets
	 * before clouds and weather are drawn.
	 */
	public static final Event<BeforeTranslucent> BEFORE_TRANSLUCENT = EventFactory.createArrayBacked(BeforeTranslucent.class, callbacks -> context -> {
		for (final BeforeTranslucent callback : callbacks) {
			callback.beforeTranslucent(context);
		}
	});

	/**
	 * Called after block outline render checks are made
	 * and before the default block outline is drawn to the framebuffer.
	 * This will NOT be called if the default outline render state
	 * was set to null in {@link #AFTER_BLOCK_OUTLINE_EXTRACTION}.
	 *
	 * <p>Use this to replace the default block outline rendering for specific blocks that
	 * need special outline rendering or to add information that doesn't replace the block outline.
	 * Subscribers cannot affect each other or detect if another subscriber is also
	 * handling a specific block.  If two subscribers render for the same block, both
	 * renders will appear.
	 *
	 * <p>Returning false from any event subscriber will cancel the default block
	 * outline render.  This has no effect on other subscribers to this event -
	 * all subscribers will always be called.  Canceling is appropriate when the
	 * subscriber replacing the default block outline render for a specific block.
	 */
	public static final Event<BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = EventFactory.createArrayBacked(BeforeBlockOutline.class, callbacks -> (context, outlineRenderState) -> {
		boolean shouldRender = true;

		for (final BeforeBlockOutline callback : callbacks) {
			if (!callback.beforeBlockOutline(context, outlineRenderState)) {
				shouldRender = false;
			}
		}

		return shouldRender;
	});

	/**
	 * Called at the end of the main render pass, after entities, block entities,
	 * terrain, and translucent terrain are drawn to the framebuffer,
	 * before particles, clouds, weather, and late debug are drawn to the framebuffer.
	 *
	 * <p>Use to draw on top of the world before hand and GUI are drawn.
	 */
	public static final Event<EndMain> END_MAIN = EventFactory.createArrayBacked(EndMain.class, callbacks -> context -> {
		for (final EndMain callback : callbacks) {
			callback.endMain(context);
		}
	});

	@FunctionalInterface
	public interface AfterBlockOutlineExtraction {
		void afterBlockOutlineExtraction(WorldExtractionContext context, @Nullable HitResult result);
	}

	@FunctionalInterface
	public interface EndExtraction {
		void endExtraction(WorldExtractionContext context);
	}

	@FunctionalInterface
	public interface StartMain {
		void startMain(WorldTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeEntities {
		void beforeEntities(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface AfterEntities {
		void afterEntities(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface DebugRender {
		void beforeDebugRender(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeTranslucent {
		void beforeTranslucent(WorldRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeBlockOutline {
		boolean beforeBlockOutline(WorldRenderContext context, OutlineRenderState outlineRenderState);
	}

	@FunctionalInterface
	public interface EndMain {
		void endMain(WorldRenderContext context);
	}
}
