package net.fabricmc.fabric.test.rendering.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.RenderEntityCallback;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.text.Text;


public class EntityRenderEventTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		RenderEntityCallback.EVENT.register(((entity) -> {

			if (entity instanceof CreeperEntity creeperEntity) {
				creeperEntity.setCustomName(Text.literal("creeper, aw man."));
			}

		}));
	}
}
