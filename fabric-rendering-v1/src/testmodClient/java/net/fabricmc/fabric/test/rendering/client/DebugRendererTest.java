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

package net.fabricmc.fabric.test.rendering.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.debug.DebugRendererRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.test.rendering.DebugSubscriptions;
import net.fabricmc.loader.api.FabricLoader;

public class DebugRendererTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DebugSubscriptions.init();
		ServerPlayerEvents.JOIN.register(player -> {
			player.requestDebugSubscriptions(Set.of(DebugSubscriptions.SUS_AVATAR));
		});
		DebugRendererRegistry.registerConditional(SuspiciousDebugRenderer::new, DebugSubscriptions.SUS_AVATAR, minecraft -> FabricLoader.getInstance().isDevelopmentEnvironment());
	}

	public static class SuspiciousDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
		private final Minecraft minecraft;

		public SuspiciousDebugRenderer(Minecraft minecraft) {
			this.minecraft = minecraft;
		}

		@Override
		public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
			debugValueAccess.forEachEntity(DebugSubscriptions.SUS_AVATAR, (entity, susDebugInfo) -> {
				Gizmos.billboardText(susDebugInfo.sussyPlayerName(), new Vec3(entity.getX(), entity.getY() + 3.25, entity.getZ()), TextGizmo.Style.whiteAndCentered());

				if (susDebugInfo.isSuspicious()) {
					Vec3 arrowPos = new Vec3(entity.getX() + 1.0, entity.getY() + 2.7, entity.getZ() + 1.0);
					Gizmos.arrow(arrowPos, arrowPos.add(new Vec3(-0.5, -1.0, -0.5)), ARGB.color(255, 255, 0, 0));
					Gizmos.billboardText("Sussy", arrowPos.add(0.0, 0.125, 0.0), TextGizmo.Style.forColorAndCentered(ARGB.color(255, 191, 0, 0)));
					Gizmos.circle(entity.getEyePosition(), 0.5f,
							GizmoStyle.stroke(ARGB.color(255, 255, 0, 0), 8.0f));
				}
			});
		}
	}
}
