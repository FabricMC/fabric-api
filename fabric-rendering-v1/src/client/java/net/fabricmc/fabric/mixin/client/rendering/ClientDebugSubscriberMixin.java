package net.fabricmc.fabric.mixin.client.rendering;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientDebugSubscriber;

@Mixin(ClientDebugSubscriber.class)
public final class ClientDebugSubscriberMixin {
	private ClientDebugSubscriberMixin() {
	}

	
}
