package net.fabricmc.fabric.impl.client.indigo.renderer;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.RendererProvider;

public class IndigoRendererProvider implements RendererProvider {
	@Override
	public Renderer getRenderer() {
		return IndigoRenderer.getOrCreateInstance();
	}

	@Override
	public String id() {
		return "fabric-renderer-indigo";
	}

	@Override
	public int priority() {
		return 0; // This ensures other renderers override Indigo
	}
}
