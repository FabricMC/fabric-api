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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class WorldRenderEvents {
	private WorldRenderEvents() { }

	public static final RenderStateDataKey<BlockState> BLOCK_OUTLINE_BLOCK_STATE = RenderStateDataKey.create(() -> "block outline block state");

	public static final Event<StartExtraction> START_EXTRACTION = EventFactory.createArrayBacked(StartExtraction.class, callbacks -> (state, world) -> {
		for (StartExtraction callback : callbacks) {
			callback.startExtraction(state, world);
		}
	});

	public static final Event<EndExtraction> END_EXTRACTION = EventFactory.createArrayBacked(EndExtraction.class, callbacks -> (state, world) -> {
		for (EndExtraction callback : callbacks) {
			callback.endExtraction(state, world);
		}
	});

	public static final Event<BeforeSubmitEntityCommands> BEFORE_SUBMIT_ENTITY_COMMANDS = EventFactory.createArrayBacked(BeforeSubmitEntityCommands.class, callbacks -> (state, matrices, commandQueue, consumers) -> {
		for (BeforeSubmitEntityCommands callback : callbacks) {
			callback.beforeSubmitEntityCommands(state, matrices, commandQueue, consumers);
		}
	});

	public static final Event<AfterSubmitEntityCommands> AFTER_SUBMIT_ENTITY_COMMANDS = EventFactory.createArrayBacked(AfterSubmitEntityCommands.class, callbacks -> (state, matrices, commandQueue, consumers) -> {
		for (AfterSubmitEntityCommands callback : callbacks) {
			callback.afterSubmitEntityCommands(state, matrices, commandQueue, consumers);
		}
	});

	public static final Event<StartRender> START_RENDER = EventFactory.createArrayBacked(StartRender.class, callbacks -> (state, sectionState) -> {
		for (StartRender callback : callbacks) {
			callback.startRender(state, sectionState);
		}
	});

	public static final Event<AfterTerrainRender> AFTER_TERRAIN_RENDER = EventFactory.createArrayBacked(AfterTerrainRender.class, callbacks -> (state, sectionState) -> {
		for (AfterTerrainRender callback : callbacks) {
			callback.afterTerrainRender(state, sectionState);
		}
	});

	// This might be merged into after terrain render in the future, but right now, these two events are not in the same place.
	public static final Event<BeforeEntityRender> BEFORE_ENTITY_RENDER = EventFactory.createArrayBacked(BeforeEntityRender.class, callbacks -> (state, sectionState, matrices, consumers) -> {
		for (BeforeEntityRender callback : callbacks) {
			callback.beforeEntityRender(state, sectionState, matrices, consumers);
		}
	});

	public static final Event<AfterEntityRender> AFTER_ENTITY_RENDER = EventFactory.createArrayBacked(AfterEntityRender.class, callbacks -> (state, sectionState, matrices, consumers) -> {
		for (AfterEntityRender callback : callbacks) {
			callback.afterEntityRender(state, sectionState, matrices, consumers);
		}
	});

	public static final Event<AfterDebugRender> AFTER_DEBUG_RENDER = EventFactory.createArrayBacked(AfterDebugRender.class, callbacks -> (state, sectionState, matrices, consumers) -> {
		for (AfterDebugRender callback : callbacks) {
			callback.afterDebugRender(state, sectionState, matrices, consumers);
		}
	});

	public static final Event<AfterTranslucentRender> AFTER_TRANSLUCENT_RENDER = EventFactory.createArrayBacked(AfterTranslucentRender.class, callbacks -> (state, sectionState, matrices, consumers) -> {
		for (AfterTranslucentRender callback : callbacks) {
			callback.afterTranslucentRender(state, sectionState, matrices, consumers);
		}
	});

	public static final Event<BeforeBlockOutlineRender> BEFORE_BLOCK_OUTLINE_RENDER = EventFactory.createArrayBacked(BeforeBlockOutlineRender.class, callbacks -> (state, matrices, consumers) -> {
		boolean shouldRender = true;

		for (final BeforeBlockOutlineRender callback : callbacks) {
			if (!callback.beforeBlockOutlineRender(state, matrices, consumers)) {
				shouldRender = false;
			}
		}

		return shouldRender;
	});

	public static final Event<EndRender> END_RENDER = EventFactory.createArrayBacked(EndRender.class, callbacks -> (state, sectionState, matrices, consumers) -> {
		for (EndRender callback : callbacks) {
			callback.endRender(state, sectionState, matrices, consumers);
		}
	});

	@FunctionalInterface
	public interface StartExtraction {
		void startExtraction(WorldRenderState state, ClientWorld world);
	}

	@FunctionalInterface
	public interface EndExtraction {
		void endExtraction(WorldRenderState state, ClientWorld world);
	}

	@FunctionalInterface
	public interface BeforeSubmitEntityCommands {
		void beforeSubmitEntityCommands(WorldRenderState state, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface AfterSubmitEntityCommands {
		void afterSubmitEntityCommands(WorldRenderState state, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface StartRender {
		void startRender(WorldRenderState state, SectionRenderState sectionState);
	}

	@FunctionalInterface
	public interface AfterTerrainRender {
		void afterTerrainRender(WorldRenderState state, SectionRenderState sectionState);
	}

	@FunctionalInterface
	public interface BeforeEntityRender {
		void beforeEntityRender(WorldRenderState state, SectionRenderState sectionState, MatrixStack matrices, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface AfterEntityRender {
		void afterEntityRender(WorldRenderState state, SectionRenderState sectionState, MatrixStack matrices, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface AfterDebugRender {
		void afterDebugRender(WorldRenderState state, SectionRenderState sectionState, MatrixStack matrices, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface AfterTranslucentRender {
		void afterTranslucentRender(WorldRenderState state, SectionRenderState sectionState, MatrixStack matrices, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface BeforeBlockOutlineRender {
		boolean beforeBlockOutlineRender(WorldRenderState state, MatrixStack matrices, VertexConsumerProvider consumers);
	}

	@FunctionalInterface
	public interface EndRender {
		void endRender(WorldRenderState state, SectionRenderState sectionState, MatrixStack matrices, VertexConsumerProvider consumers);
	}
}
