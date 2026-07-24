package net.fabricmc.fabric.api.client.debug.v1;

import net.fabricmc.fabric.impl.debug.client.DebugKeyBindingRegistryImpl;

import net.minecraft.client.KeyMapping;

/// A registry for debug keybindings on the client, allowing the
/// creation keybindings only accessed when the debug modifier key is
/// pressed (f3 is the default debug modifier key).
public final class DebugKeyBindingRegistry {

	/// @param keyMapping the [KeyMapping] that when pressed along with the debug modifier key runs the handler's function.
	/// @param handler the logic to run when the [KeyMapping] is pressed along with the debug modifier key.
	public static void register(KeyMapping keyMapping, DebugKeyHandler handler) {
		DebugKeyBindingRegistryImpl.register(keyMapping, handler);
	}

	/// Returning `true` indicates that the key press was handled and performed
	/// an action. When `true`, the game will consume the debug key combination,
	/// but the key will retain its normal behavior when pressed without the
	/// debug modifier.
	@FunctionalInterface
	public interface DebugKeyHandler {
		boolean onDebugKey();
	}
}
