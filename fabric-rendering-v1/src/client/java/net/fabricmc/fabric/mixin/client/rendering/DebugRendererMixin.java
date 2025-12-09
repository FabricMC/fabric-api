package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

import net.fabricmc.fabric.impl.client.rendering.DebugRendererRegistryImpl;

@Mixin(DebugRenderer.class)
public final class DebugRendererMixin {
	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> renderers;

	private DebugRendererMixin() {
	}

	@Inject(method = "refreshRendererList", at = @At("RETURN"))
	private void registerRenderers(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		for (DebugRendererRegistryImpl.RendererEntry entry : DebugRendererRegistryImpl.RENDERERS) {
			// a Stream#map would make the most sense here, but they're banned
			// so you have to suffer with me now
			if (entry.isEnabled() == null || entry.isEnabled().test(minecraft)) {
				renderers.add(entry.debugRenderer().create(minecraft));
			}
		}
	}
}
