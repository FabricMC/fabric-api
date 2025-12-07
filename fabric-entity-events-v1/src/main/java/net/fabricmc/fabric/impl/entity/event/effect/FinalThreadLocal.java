package net.fabricmc.fabric.impl.entity.event.effect;

import java.util.function.Supplier;

/**
 * {@link ThreadLocal} but you cannot modify it.
 */
public final class FinalThreadLocal<T> extends ThreadLocal<T> {
	private final Supplier<T> valueConstructor;

	public FinalThreadLocal(Supplier<T> value) {
		this.valueConstructor = value;
	}

	@Override
	protected T initialValue() {
		return this.valueConstructor.get();
	}

	/**
	 * @deprecated This isn't deprecated, but <b>don't use it.</b>
	 * It will throw an {@link UnsupportedOperationException}.
	 */
	@Deprecated
	@Override
	public void set(T value) {
		throw new UnsupportedOperationException("Setting a value in a FinalThreadLocal is illegal");
	}

	/**
	 * @deprecated This isn't deprecated, but <b>don't use it.</b>
	 * It will throw an {@link UnsupportedOperationException}.
	 */
	@Deprecated
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Removing a value from a FinalThreadLocal is illegal");
	}
}
