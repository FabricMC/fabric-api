package net.fabricmc.fabric.api.debug.v1;

/// Constructs a debug value of type [T] using data of type [D]
///
/// @param <D> the data passed for construction
/// (e.g. [net.minecraft.world.entity.Entity])
/// @param <T> the debug value being constructed
@FunctionalInterface
public interface DebugValueFactory<D, T> {
	T create(D data);
}
