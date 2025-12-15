package net.fabricmc.fabric.impl.test;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.test.EventScope;
import net.fabricmc.fabric.impl.base.event.ArrayBackedEvent;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class TestableEventFactoryImpl extends EventFactoryImpl {
	@Override
	protected <T> ArrayBackedEvent<T> doCreateArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new TestableArrayBackedEvent<>(type, invokerFactory);
	}
}
