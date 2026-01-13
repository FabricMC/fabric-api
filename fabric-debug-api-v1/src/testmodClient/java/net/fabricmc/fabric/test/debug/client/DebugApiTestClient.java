package net.fabricmc.fabric.test.debug.client;

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
import net.fabricmc.fabric.api.debug.v1.client.ClientDebugSubscriptionRegistry;
import net.fabricmc.fabric.api.debug.v1.client.renderer.DebugRendererRegistry;
import net.fabricmc.fabric.test.debug.DebugApiTest;
import net.fabricmc.loader.api.FabricLoader;

public class DebugApiTestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ClientDebugSubscriptionRegistry.register(
					DebugApiTest.SUS_AVATAR,
					DebugApiTest.DEBUG_SUS_AVATAR
			);
			DebugRendererRegistry.register(
					DebugApiTest.SUS_AVATAR,
					SuspiciousDebugRenderer::new,
					DebugApiTest.DEBUG_SUS_AVATAR
			);
		}
	}

	public static class SuspiciousDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
		public SuspiciousDebugRenderer(Minecraft ignoredMinecraft) {
		}

		@Override
		public void emitGizmos(
				double d,
				double e,
				double f,
				DebugValueAccess debugValueAccess,
				Frustum frustum,
				float g
		) {
			debugValueAccess.forEachEntity(
					DebugApiTest.SUS_AVATAR,
					(entity, susDebugInfo) -> {
						Gizmos.billboardText(
								susDebugInfo.playerName(),
								new Vec3(entity.getX(), entity.getY() + 3.25, entity.getZ()),
								TextGizmo.Style.whiteAndCentered()
						);

						if (susDebugInfo.isSuspicious()) {
							Vec3 arrowPos = new Vec3(
									entity.getX() + 1.0,
									entity.getY() + 2.7,
									entity.getZ() + 1.0
							);
							Gizmos.arrow(
									arrowPos,
									arrowPos.add(new Vec3(-0.5, -1.0, -0.5)),
									ARGB.color(255, 255, 0, 0)
							);
							Gizmos.billboardText(
									"Sussy",
									arrowPos.add(0.0, 0.125, 0.0),
									TextGizmo.Style.forColorAndCentered(ARGB.color(
											255,
											191,
											0,
											0
									))
							);
							Gizmos.circle(
									new SwappedVec3(entity.getEyePosition()),
									0.5f,
									GizmoStyle.stroke(ARGB.color(
											255,
											255,
											0,
											0
									), 8.0f)
							);
						}
					}
			);
		}
	}

	private static class SwappedVec3 extends Vec3 {
		SwappedVec3(double x, double y, double z) {
			super(x, y, z);
		}

		SwappedVec3(Vec3 vec3) {
			this(vec3.x(), vec3.y(), vec3.z());
		}

		@Override
		public Vec3 add(double d, double e, double f) {
			return super.add(
					f,
					d,
					e
			);
		}
	}
}
