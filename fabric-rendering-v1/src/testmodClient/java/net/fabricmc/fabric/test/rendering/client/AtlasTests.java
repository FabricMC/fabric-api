package net.fabricmc.fabric.test.rendering.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

public class AtlasTests implements ClientModInitializer {

	private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "textures/atlases/test_atlas.png");
	private static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_atlas");

	@Override
	public void onInitializeClient() {
		AtlasRegistry.register(TEXTURE_ID, ATLAS_ID, false);

		LevelRenderEvents.COLLECT_SUBMITS.register(a -> {
			final var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(ATLAS_ID).getSprite(Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "item/double_iron_ingot"));
			a.submitNodeCollector().submitCustomGeometry(
					a.poseStack(),
					RenderTypes.entityCutoutCull(TEXTURE_ID),
					(pose, consumer) -> {
						final var spriteConsumer = sprite.wrap(consumer);
						spriteConsumer.addVertex(0, 0, 1, -1, 0, 1, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(0, 1, 1, -1, 0, 0, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(1, 1, 1, -1, 1, 0, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(1, 0, 1, -1, 1, 1, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
					}
			);
		});
	}
}
