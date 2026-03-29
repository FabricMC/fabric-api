package net.fabricmc.fabric.impl.test;

import java.util.function.Function;

import net.fabricmc.fabric.impl.base.event.ArrayBackedEvent;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

public class TestableEventFactoryImpl extends EventFactoryImpl {
	@Override
	protected <T> ArrayBackedEvent<T> doCreateArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new TestableArrayBackedEvent<>(type, invokerFactory);
	}
}
