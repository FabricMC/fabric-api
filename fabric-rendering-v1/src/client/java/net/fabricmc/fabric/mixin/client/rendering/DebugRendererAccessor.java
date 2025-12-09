package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.debug.DebugRenderer;

@Mixin(DebugRenderer.class)
public interface DebugRendererAccessor {
	@Accessor
	List<DebugRenderer.SimpleDebugRenderer> getRenderers();
}
