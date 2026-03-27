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

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.world.phys.HitResult;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Mods should use these events to introduce custom rendering during {@link LevelRenderer#renderLevel}
 * without adding complicated and conflict-prone injections there.  Using these events also enables 3rd-party renderers
 * that make large-scale rendering changes to maintain compatibility by calling any broken event invokers directly.
 *
 * <p>These events can be separated into two categories, the "extraction" events and the "drawing" events,
 * reflecting the respective vanilla phases. All data needed for rendering should be prepared in the "extraction" phase
 * and drawn to the frame buffer during the "drawing" phase. All "extraction" events have the suffix "Extraction".
 * All events without the "Extraction" suffix are "drawing" events. All "drawing" events support OpenGL calls.
 *
 * <p>To attach modded data to vanilla render states, see {@link net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState FabricRenderState}.
 * Only attach the minimum data needed for rendering. Do not attach objects that are not thread-safe such as {@link net.minecraft.client.multiplayer.ClientLevel}.
 */
public final class LevelRenderEvents {
	private LevelRenderEvents() { }

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

	/**
	 * Called at the start of the main pass, after the sky is drawn to the appropriate framebuffers and all chunks to be
	 * rendered are uploaded to GPU, and before any chunks are drawn to the appropriate framebuffers.
	 */
	public static final Event<StartMain> START_MAIN = EventFactory.createArrayBacked(StartMain.class, callbacks -> context -> {
		for (final StartMain callback : callbacks) {
			callback.startMain(context);
		}
	});

	/**
	 * Called after {@linkplain ChunkSectionLayerGroup#OPAQUE opaque} terrain is drawn to the appropriate framebuffers,
	 * and before any submit nodes are added to the submit node storage.
	 *
	 * <p>Use this event to render additional opaque terrain-like geometry.
	 */
	public static final Event<AfterOpaqueTerrain> AFTER_OPAQUE_TERRAIN = EventFactory.createArrayBacked(AfterOpaqueTerrain.class, callbacks -> context -> {
		for (final AfterOpaqueTerrain callback : callbacks) {
			callback.afterOpaqueTerrain(context);
		}
	});

	/**
	 * Called after {@linkplain ChunkSectionLayerGroup#OPAQUE opaque} terrain is drawn to the appropriate framebuffers
	 * and all submit nodes from entities, block entities, and particles are added to the submit node storage, and
	 * before any submit geometry is drawn to the appropriate framebuffers.
	 *
	 * <p>Use this event to add additional submits to {@link LevelRenderContext#submitNodeCollector()}.
	 */
	public static final Event<CollectSubmits> COLLECT_SUBMITS = EventFactory.createArrayBacked(CollectSubmits.class, callbacks -> context -> {
		for (final CollectSubmits callback : callbacks) {
			callback.collectSubmits(context);
		}
	});

	/**
	 * Called after the solid geometry of submits collected from entities, block entities, and particles are drawn to
	 * the appropriate framebuffers.
	 */
	public static final Event<AfterSolidFeatures> AFTER_SOLID_FEATURES = EventFactory.createArrayBacked(AfterSolidFeatures.class, callbacks -> context -> {
		for (final AfterSolidFeatures callback : callbacks) {
			callback.afterSolidFeatures(context);
		}
	});

	/**
	 * Called after the translucent geometry of submits collected from entities and block entities are drawn to the
	 * appropriate framebuffers. Note that this excludes translucent particle geometry, which is rendered much later.
	 */
	public static final Event<AfterTranslucentFeatures> AFTER_TRANSLUCENT_FEATURES = EventFactory.createArrayBacked(AfterTranslucentFeatures.class, callbacks -> context -> {
		for (final AfterTranslucentFeatures callback : callbacks) {
			callback.afterTranslucentFeatures(context);
		}
	});

	/**
	 * Called after block outline render checks are made
	 * and before the default block outline is drawn to the appropriate framebuffers.
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
	 * Called after all geometry of submits collected from entities, block entities, and particles (except translucent
	 * particle geometry), the block breaking overlay, and the block outline for solid blocks are drawn to the
	 * appropriate framebuffers, and before gizmos are collected.
	 */
	public static final Event<BeforeGizmos> BEFORE_GIZMOS = EventFactory.createArrayBacked(BeforeGizmos.class, callbacks -> context -> {
		for (final BeforeGizmos callback : callbacks) {
			callback.beforeGizmos(context);
		}
	});

	/**
	 * Called after opaque terrain, entities, block entities, solid particles, overlays, and gizmos are drawn to the
	 * appropriate framebuffers, and before {@linkplain ChunkSectionLayerGroup#TRANSLUCENT translucent} terrain and
	 * translucent particles are drawn to the appropriate framebuffers.
	 */
	public static final Event<BeforeTranslucentTerrain> BEFORE_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(BeforeTranslucentTerrain.class, callbacks -> context -> {
		for (final BeforeTranslucentTerrain callback : callbacks) {
			callback.beforeTranslucentTerrain(context);
		}
	});

	/**
	 * Called after terrain, entities, block entities, solid particles, overlays, and gizmos are drawn to the
	 * appropriate framebuffers, and before translucent particles are drawn to the appropriate framebuffers.
	 *
	 * <p>Use this event to render additional translucent terrain-like geometry.
	 */
	public static final Event<AfterTranslucentTerrain> AFTER_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(AfterTranslucentTerrain.class, callbacks -> context -> {
		for (final AfterTranslucentTerrain callback : callbacks) {
			callback.afterTranslucentTerrain(context);
		}
	});

	/**
	 * Called at the end of the main render pass, after terrain, entities, block entities, and particles are drawn to
	 * the appropriate framebuffers, and before clouds, weather, and late debug are drawn to the appropriate
	 * framebuffers and before fabulous translucent framebuffers are combined.
	 *
	 * <p><strong>Warning:</strong> after rendering things in this event, consumers should call
	 * {@link MultiBufferSource.BufferSource#endBatch() context.bufferSource().endBatch()}, otherwise
	 * you may get strange rendering bugs!
	 */
	public static final Event<EndMain> END_MAIN = EventFactory.createArrayBacked(EndMain.class, callbacks -> context -> {
		for (final EndMain callback : callbacks) {
			callback.endMain(context);
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

	@FunctionalInterface
	public interface StartMain {
		void startMain(LevelTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface AfterOpaqueTerrain {
		void afterOpaqueTerrain(LevelTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface CollectSubmits {
		void collectSubmits(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface AfterSolidFeatures {
		void afterSolidFeatures(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTranslucentFeatures {
		void afterTranslucentFeatures(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeBlockOutline {
		boolean beforeBlockOutline(LevelRenderContext context, BlockOutlineRenderState outlineRenderState);
	}

	@FunctionalInterface
	public interface BeforeGizmos {
		void beforeGizmos(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeTranslucentTerrain {
		void beforeTranslucentTerrain(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTranslucentTerrain {
		void afterTranslucentTerrain(LevelRenderContext context);
	}

	@FunctionalInterface
	public interface EndMain {
		void endMain(LevelRenderContext context);
	}
}
