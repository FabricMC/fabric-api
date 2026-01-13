package net.fabricmc.fabric.mixin.debug.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

import net.fabricmc.fabric.impl.debug.client.renderer.DebugRendererRegistryImpl;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> renderers;

	private DebugRendererMixin() {
	}

	@Inject(method = "refreshRendererList", at = @At("RETURN"))
	private void registerRenderers(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		for (DebugRendererRegistryImpl.Entry entry : DebugRendererRegistryImpl.RENDERERS) {
			// a Stream#map would make the most sense here, but they're banned
			// so you have to suffer with me now
			renderers.add(entry.rendererFactory().create(minecraft));
		}
	}
}
