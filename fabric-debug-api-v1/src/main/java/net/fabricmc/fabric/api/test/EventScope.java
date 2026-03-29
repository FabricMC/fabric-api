package net.fabricmc.fabric.api.test;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventScope extends AutoCloseable {
	@Override
	void close();
}
