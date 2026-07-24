package net.fabricmc.fabric.impl.debug.client;

import net.fabricmc.fabric.api.client.debug.v1.DebugKeyBindingRegistry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugKeyBindingRegistryImpl {
	private static final Map<KeyMapping, DebugKeyBindingRegistry.DebugKeyHandler> BINDINGS = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-debug-api-v1");

	public static synchronized void register(KeyMapping keyMapping, DebugKeyBindingRegistry.DebugKeyHandler handler) {
		if (BINDINGS.containsKey(keyMapping)) {
			throw new IllegalStateException(
					"KeyMapping " + keyMapping.getName() + " is already registered for a fabric debug action"
			);
		}
		BINDINGS.put(keyMapping, handler);
	}

	public static boolean invoke(KeyEvent event, boolean didAction) {
		for (Map.Entry<KeyMapping, DebugKeyBindingRegistry.DebugKeyHandler> entry : BINDINGS.entrySet()) {
			KeyMapping keyMapping = entry.getKey();

			if (keyMapping.matches(event)) {
				try {
					didAction = entry.getValue().onDebugKey() || didAction;
				} catch (Throwable t) {
					LOGGER.error("Exception running debug key handler for {}", keyMapping.getName(), t);
				}
			}
		}

		return didAction;
	}
}
