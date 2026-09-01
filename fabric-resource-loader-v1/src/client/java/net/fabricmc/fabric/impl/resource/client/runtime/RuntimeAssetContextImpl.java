package net.fabricmc.fabric.impl.resource.client.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.client.resource.v1.runtime.RuntimeAssetContext;
import net.fabricmc.fabric.api.client.resource.v1.runtime.provider.assets.RuntimeAssetProvider;

public final class RuntimeAssetContextImpl implements RuntimeAssetContext {
	public static final RuntimeAssetContextImpl INSTANCE = new RuntimeAssetContextImpl();
	public final Set<RuntimeAssetProvider.Factory<?>> factories = new HashSet<>();
	public final Map<Class<?>, ProviderHandler<?>> handlerPseudoRegistry = new HashMap<>();
	public boolean instantiated = false;

	@Override
	public <T extends RuntimeAssetProvider> void addProvider(RuntimeAssetProvider.Factory<T> factory) {
		this.checkInstantiated();
		this.factories.add(factory);
	}

	@Override
	public <T extends RuntimeAssetProvider> void registerHandler(
			Class<T> clazz,
			ProviderHandler<T> handler
	) {
		this.checkInstantiated();
		this.handlerPseudoRegistry.put(clazz, handler);
	}

	private void checkInstantiated() {
		if (instantiated) {
			throw new IllegalStateException("RuntimeAssetContext is no longer mutable after resource loading has concluded.");
		}
	}
}
