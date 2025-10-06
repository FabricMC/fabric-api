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

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class WorldRenderEvents {
	private WorldRenderEvents() { }

	public static final Event<Start> START = EventFactory.createArrayBacked(Start.class, callbacks -> context -> {
		for (final Start callback : callbacks) {
			callback.onStart(context);
		}
	});

	public static final Event<UpdateState> UPDATE_STATE = EventFactory.createArrayBacked(UpdateState.class, callbacks -> (context, world) -> {
		for (final UpdateState callback : callbacks) {
			callback.afterStateUpdate(context, world);
		}
	});

	public static final Event<BeforeEntities> BEFORE_ENTITIES = EventFactory.createArrayBacked(BeforeEntities.class, callbacks -> context -> {
		for (final BeforeEntities callback : callbacks) {
			callback.beforeEntities(context);
		}
	});

	public static final Event<AfterEntities> AFTER_ENTITIES = EventFactory.createArrayBacked(AfterEntities.class, callbacks -> context -> {
		for (final AfterEntities callback : callbacks) {
			callback.afterEntities(context);
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

	public static final Event<Last> LAST = EventFactory.createArrayBacked(Last.class, callbacks -> context -> {
		for (final Last callback : callbacks) {
			callback.onLast(context);
		}
	});


	@FunctionalInterface
	public interface Start {
		void onStart(WorldRenderState worldRenderState);
	}

	@FunctionalInterface
	public interface UpdateState {
		void afterStateUpdate(WorldRenderState worldRenderState, ClientWorld world);
	}

	@FunctionalInterface
	public interface BeforeEntities {
		void beforeEntities(WorldRenderState worldRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue renderCommandQueue);
	}

	@FunctionalInterface
	public interface AfterEntities {
		void afterEntities(WorldRenderState worldRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue renderCommandQueue);
	}

	@FunctionalInterface
	public interface DebugRender {
		void beforeDebugRender(WorldRenderState worldRenderState);
	}

	@FunctionalInterface
	public interface AfterTranslucent {
		void afterTranslucent(WorldRenderState worldRenderState);
	}

	@FunctionalInterface
	public interface Last {
		void onLast(WorldRenderState worldRenderState);
	}
}
