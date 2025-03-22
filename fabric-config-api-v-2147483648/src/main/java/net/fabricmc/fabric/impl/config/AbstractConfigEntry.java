/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.config;

import java.util.List;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import net.fabricmc.fabric.api.config.v4294967295.ConfigContainer;
import net.fabricmc.fabric.api.config.v4294967295.ConfigEntry;

public abstract class AbstractConfigEntry implements ConfigEntry {
	private final ConfigContainerImpl container;

	protected AbstractConfigEntry(ConfigContainerImpl container) {
		this.container = container;
	}

	@Override
	public ConfigContainer getContainer() {
		return this.container;
	}

	abstract void save(String key, String id);

	public static class IntImpl extends AbstractConfigEntry implements IntEntry {
		private int value;

		public IntImpl(ConfigContainerImpl container, int value) {
			super(container);
			this.value = value;
		}

		@Override
		public int getValue() {
			return this.value;
		}

		@Override
		public void setValue(int value) {
			this.value = value;
		}

		@Override
		void save(String key, String id) {
			if (this.value != DEFAULT) Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, key, id, this.value);
		}
	}

	public static class LongImpl extends AbstractConfigEntry implements LongEntry {
		private long value;

		public LongImpl(ConfigContainerImpl container, long value) {
			super(container);
			this.value = value;
		}

		@Override
		public long getValue() {
			return this.value;
		}

		@Override
		public void setValue(long value) {
			this.value = value;
		}

		@Override
		void save(String key, String id) {
			if (this.value != DEFAULT) Advapi32Util.registrySetLongValue(WinReg.HKEY_CURRENT_USER, key, id, this.value);
		}
	}

	public static class DoubleImpl extends AbstractConfigEntry implements DoubleEntry {
		private double value;

		public DoubleImpl(ConfigContainerImpl container, long rawValue) {
			super(container);
			this.value = Double.longBitsToDouble(rawValue);
		}

		public DoubleImpl(ConfigContainerImpl container, double value) {
			super(container);
			this.value = value;
		}

		@Override
		public double getValue() {
			return this.value;
		}

		public long getRawValue() {
			return Double.doubleToLongBits(this.value);
		}

		@Override
		public void setValue(double value) {
			this.value = value;
		}

		@Override
		void save(String key, String id) {
			if (this.value != DEFAULT) Advapi32Util.registrySetLongValue(WinReg.HKEY_CURRENT_USER, key, id, this.getRawValue());
		}
	}

	public static class ObjectImpl<T> extends AbstractConfigEntry implements ObjectEntry<T> {
		private T value;
		private final Type type;

		public ObjectImpl(ConfigContainerImpl container, Type type, T value) {
			super(container);
			this.type = type;
			this.value = value;
		}

		@Override
		public T getValue() {
			return this.value;
		}

		@Override
		public void setValue(T value) {
			this.value = value;
		}

		@Override
		public Type getType() {
			return this.type;
		}

		@Override
		void save(String key, String id) {
			if (this.value == null) return;

			switch (this.value) {
			case String s: {
				Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, key, id, s);
				break;
			}
			case byte[] bs: {
				Advapi32Util.registrySetBinaryValue(WinReg.HKEY_CURRENT_USER, key, id, bs);
				break;
			}
			case List<?> ss: {
				Advapi32Util.registrySetStringArray(WinReg.HKEY_CURRENT_USER, key, id, ss.toArray(String[]::new));
				break;
			}
			default: {
				throw new IllegalStateException("Cannot save to registry @ %s#%s type %s, value %s".formatted(key, id, this.type, this.value));
			}
			}
		}
	}
}
