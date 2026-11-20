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

import com.mojang.renderpearl.api.commands.RenderPass;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Mods should use these events to introduce custom rendering during {@link LevelRenderer#render}
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
	 * Called after block outline render checks are made
	 * and before the default block outline is submitted for rendering.
	 * This will NOT be called if the default outline render state
	 * was set to null in {@link LevelExtractionEvents#AFTER_BLOCK_OUTLINE_EXTRACTION}.
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
	 * Called after submit nodes from entities, block entities, particles, the block breaking overlay, and the block
	 * outline are added to the submit node storage, and before gizmos are collected.
	 */
	public static final Event<BeforeGizmos> BEFORE_GIZMOS = EventFactory.createArrayBacked(BeforeGizmos.class, callbacks -> context -> {
		for (final BeforeGizmos callback : callbacks) {
			callback.beforeGizmos(context);
		}
	});

	/**
	 * Called after all submit nodes from entities, block entities, particles, and gizmos are added to the submit node
	 * storage, and before any submit geometry is drawn to the appropriate framebuffers.
	 *
	 * <p>Use this event to add additional submits to {@link LevelSubmitContext#submitNodeCollector()}.
	 */
	public static final Event<CollectSubmits> COLLECT_SUBMITS = EventFactory.createArrayBacked(CollectSubmits.class, callbacks -> context -> {
		for (final CollectSubmits callback : callbacks) {
			callback.collectSubmits(context);
		}
	});

	/**
	 * Called before the main {@link RenderPass} is opened, after the sky is drawn to the appropriate framebuffers when
	 * improved fog is enabled, and before {@link #START_MAIN}.
	 *
	 * <p>The main render pass draws opaque terrain and solid features, and also translucent terrain and features when
	 * improved transparency is disabled. {@link #START_MAIN}, {@link #AFTER_OPAQUE_TERRAIN} and
	 * {@link #AFTER_SOLID_FEATURES} are always called while it is open, so they are not a place to open a render pass
	 * of your own. Use this event instead when you need your own {@link RenderPass} that runs before the main one, for
	 * example to draw into a texture that is sampled during the main pass. Any render pass opened in this event must be
	 * closed before the callback returns.
	 *
	 * <p>To draw as part of the main render pass, use {@link #START_MAIN} or one of the later events instead.
	 */
	public static final Event<BeforeRenderPass> BEFORE_RENDER_PASS = EventFactory.createArrayBacked(BeforeRenderPass.class, callbacks -> context -> {
		for (final BeforeRenderPass callback : callbacks) {
			callback.beforeRenderPass(context);
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
	 * and before solid feature geometry is drawn to the appropriate framebuffers.
	 *
	 * <p>Use this event to render additional opaque terrain-like geometry.
	 */
	public static final Event<AfterOpaqueTerrain> AFTER_OPAQUE_TERRAIN = EventFactory.createArrayBacked(AfterOpaqueTerrain.class, callbacks -> context -> {
		for (final AfterOpaqueTerrain callback : callbacks) {
			callback.afterOpaqueTerrain(context);
		}
	});

	/**
	 * Called after the solid geometry of submits collected from entities, block entities, and particles is drawn to
	 * the appropriate framebuffers.
	 */
	public static final Event<AfterSolidFeatures> AFTER_SOLID_FEATURES = EventFactory.createArrayBacked(AfterSolidFeatures.class, callbacks -> context -> {
		for (final AfterSolidFeatures callback : callbacks) {
			callback.afterSolidFeatures(context);
		}
	});

	/// Called after the translucent geometry of submits collected from entities and block entities is drawn to the
	/// appropriate framebuffers. Note that this excludes translucent particle geometry, which is rendered much later.
	///
	/// When improved transparency is enabled, this event is called once for each OIT stage, after the stage's
	/// translucent terrain is drawn, and [LevelTranslucentRenderContext#oitStage()] returns the current stage.
	/// Otherwise, this event is called once before translucent terrain and `oitStage()` returns `null`.
	public static final Event<AfterTranslucentFeatures> AFTER_TRANSLUCENT_FEATURES = EventFactory.createArrayBacked(AfterTranslucentFeatures.class, callbacks -> context -> {
		for (final AfterTranslucentFeatures callback : callbacks) {
			callback.afterTranslucentFeatures(context);
		}
	});

	/// Called after opaque terrain, entities, block entities, solid particles, overlays, and gizmos are drawn to the
	/// appropriate framebuffers, and before [translucent][ChunkSectionLayerGroup#TRANSLUCENT] terrain and
	/// translucent particles are drawn to the appropriate framebuffers.
	///
	/// When improved transparency is enabled, this event is called once for each OIT stage, and
	/// [LevelTranslucentRenderContext#oitStage()] returns the current stage. Otherwise, this event is called once
	/// and `oitStage()` returns `null`.
	public static final Event<BeforeTranslucentTerrain> BEFORE_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(BeforeTranslucentTerrain.class, callbacks -> context -> {
		for (final BeforeTranslucentTerrain callback : callbacks) {
			callback.beforeTranslucentTerrain(context);
		}
	});

	/// Called after [translucent][ChunkSectionLayerGroup#TRANSLUCENT] terrain is drawn to the appropriate
	/// framebuffers, and before translucent particles are drawn to the appropriate framebuffers.
	///
	/// Use this event to render additional translucent terrain-like geometry.
	///
	/// When improved transparency is enabled, this event is called once for each OIT stage, and
	/// [LevelTranslucentRenderContext#oitStage()] returns the current stage. Otherwise, this event is called once
	/// and `oitStage()` returns `null`.
	public static final Event<AfterTranslucentTerrain> AFTER_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(AfterTranslucentTerrain.class, callbacks -> context -> {
		for (final AfterTranslucentTerrain callback : callbacks) {
			callback.afterTranslucentTerrain(context);
		}
	});

	/// Called at the end of the main [RenderPass], before it is closed, after opaque terrain and solid features are
	/// drawn to the appropriate framebuffers.
	///
	/// When improved transparency is disabled, this event is called
	/// after translucent terrain and features, clouds, weather, and the world border are drawn.
	/// When it is enabled, those are drawn after this event in OIT.
	///
	/// Use [#AFTER_RENDER_PASS] to run after the main render pass has been closed.
	public static final Event<EndMain> END_MAIN = EventFactory.createArrayBacked(EndMain.class, callbacks -> context -> {
		for (final EndMain callback : callbacks) {
			callback.endMain(context);
		}
	});

	/**
	 * Called after {@link #END_MAIN} and after the main {@link RenderPass} is closed.
	 *
	 * <p>When improved transparency is disabled, the main render pass has drawn terrain, solid and translucent features,
	 * clouds, weather and the world border at this point. When it is enabled, only opaque terrain and solid features have
	 * been drawn, and translucent geometry is drawn afterwards in separate render passes. In both cases, entity outlines,
	 * see-through features and always-on-top gizmos are drawn after this event.
	 *
	 * <p>Use this event when you need your own {@link RenderPass} that runs right after the main one, for example to
	 * read from or post-process what it drew. Any render pass opened in this event must be closed before the callback
	 * returns.
	 */
	public static final Event<AfterRenderPass> AFTER_RENDER_PASS = EventFactory.createArrayBacked(AfterRenderPass.class, callbacks -> context -> {
		for (final AfterRenderPass callback : callbacks) {
			callback.afterRenderPass(context);
		}
	});

	/**
	 * @see LevelExtractionEvents#AFTER_BLOCK_OUTLINE_EXTRACTION
	 */
	@Deprecated
	public static final Event<LevelExtractionEvents.AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION;

	/**
	 * @see LevelExtractionEvents#END_EXTRACTION
	 */
	@Deprecated
	public static final Event<LevelExtractionEvents.EndExtraction> END_EXTRACTION = LevelExtractionEvents.END_EXTRACTION;

	@FunctionalInterface
	public interface BeforeBlockOutline {
		boolean beforeBlockOutline(LevelSubmitContext context, BlockOutlineRenderState outlineRenderState);
	}

	@FunctionalInterface
	public interface BeforeGizmos {
		void beforeGizmos(LevelSubmitContext context);
	}

	@FunctionalInterface
	public interface CollectSubmits {
		void collectSubmits(LevelSubmitContext context);
	}

	@FunctionalInterface
	public interface BeforeRenderPass {
		void beforeRenderPass(LevelTerrainRenderContext context);
	}

	@FunctionalInterface
	public interface StartMain {
		void startMain(LevelRenderPassRenderContext context);
	}

	@FunctionalInterface
	public interface AfterOpaqueTerrain {
		void afterOpaqueTerrain(LevelRenderPassRenderContext context);
	}

	@FunctionalInterface
	public interface AfterSolidFeatures {
		void afterSolidFeatures(LevelRenderPassRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTranslucentFeatures {
		void afterTranslucentFeatures(LevelTranslucentRenderContext context);
	}

	@FunctionalInterface
	public interface BeforeTranslucentTerrain {
		void beforeTranslucentTerrain(LevelTranslucentRenderContext context);
	}

	@FunctionalInterface
	public interface AfterTranslucentTerrain {
		void afterTranslucentTerrain(LevelTranslucentRenderContext context);
	}

	@FunctionalInterface
	public interface EndMain {
		void endMain(LevelRenderPassRenderContext context);
	}

	@FunctionalInterface
	public interface AfterRenderPass {
		void afterRenderPass(LevelTerrainRenderContext context);
	}
}
