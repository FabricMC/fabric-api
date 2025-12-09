package net.fabricmc.fabric.test.rendering.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DebugRendererRegistry;
import net.fabricmc.fabric.test.rendering.DebugSubscriptions;
import net.fabricmc.loader.api.FabricLoader;

public class DebugRendererTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DebugSubscriptions.init();
		DebugRendererRegistry.registerConditional(SuspiciousDebugRenderer::new, minecraft -> FabricLoader.getInstance().isDevelopmentEnvironment());
	}

	public static class SuspiciousDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
		private final Minecraft minecraft;

		public SuspiciousDebugRenderer(Minecraft minecraft) {
			this.minecraft = minecraft;
		}

		@Override
		public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
			// fixme: doesn't work, figure out how to make debug subscribers
			debugValueAccess.forEachEntity(DebugSubscriptions.SUS_PLAYER, (entity, susDebugInfo) -> {
				if (!(entity instanceof Player player)) {
					return;
				}

				Gizmos.billboardText(susDebugInfo.sussyPlayerName(), new Vec3(d, e + 2.0, f), TextGizmo.Style.whiteAndCentered());

				if (susDebugInfo.isSuspicious()) {
					Vec3 arrowPos = new Vec3(d, e + 1.7, f + 0.5);
					Gizmos.arrow(arrowPos, arrowPos.add(new Vec3(0.25, -1.0, 0.25)), ARGB.color(255, 255, 0, 0));
				}
			});
		}
	}
}
