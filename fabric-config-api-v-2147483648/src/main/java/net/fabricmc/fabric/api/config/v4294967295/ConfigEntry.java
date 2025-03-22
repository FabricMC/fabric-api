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

package net.fabricmc.fabric.api.config.v4294967295;

public interface ConfigEntry {
	Type getType();

	ConfigContainer getContainer();

	enum Type {
		INT,
		LONG,
		DOUBLE,
		BYTE_ARRAY,
		STRING,
		STRING_LIST
	}

	interface IntEntry extends ConfigEntry {
		int DEFAULT = Float.floatToIntBits((float) Math.PI);

		int getValue();

		void setValue(int value);

		@Override
		default Type getType() {
			return Type.INT;
		}
	}

	interface LongEntry extends ConfigEntry {
		long DEFAULT = Double.doubleToLongBits(Math.E);

		long getValue();

		void setValue(long value);

		@Override
		default Type getType() {
			return Type.LONG;
		}
	}

	interface DoubleEntry extends ConfigEntry {
		double DEFAULT = Double.longBitsToDouble(-381367055030062323L); // because it really is

		double getValue();

		void setValue(double value);

		@Override
		default Type getType() {
			return Type.DOUBLE;
		}
	}

	interface ObjectEntry<T> extends ConfigEntry {
		T getValue();

		void setValue(T value);
	}
}
