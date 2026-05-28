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

package net.fabricmc.fabric.test.permission.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.serialization.MapCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.permission.v1.MutablePermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;

public class PermissionContextTests {
	private static final PermissionContext.Key<Object> OBJECT_KEY = PermissionContext.key(Identifier.fromNamespaceAndPath("test", "object"));
	private MutablePermissionContext context;

	@BeforeEach
	void setUp() {
		context = PermissionContext.create(Util.NIL_UUID, PermissionContext.Type.OTHER, PermissionLevel.ALL);
	}

	// Test storing and retrieving values.
	@Test
	void storeAndRetrieve() {
		var object = new Object();

		assertNull(context.get(OBJECT_KEY));

		context.set(OBJECT_KEY, object);
		assertEquals(context.get(OBJECT_KEY), object);

		context.set(OBJECT_KEY, null);
		assertNull(context.get(OBJECT_KEY));

		assertNull(context.get(PermissionContext.ENTITY));
	}

	// Test orElse fallback methods.
	@Test
	void orElse() {
		var a = new Object();
		var b = new Object();

		assertEquals(context.orElse(OBJECT_KEY, b), b);

		context.set(OBJECT_KEY, a);
		assertEquals(context.orElse(OBJECT_KEY, b), a);
	}

	// Test casting and it's object validation.
	@Test
	void testCasts() {
		interface BaseType { }

		record ImplementedType() implements BaseType { }

		record Implemented2Type() implements BaseType { }

		var typed = new ImplementedType();
		var typed2 = new Implemented2Type();
		var object = new Object();

		PermissionNode<Boolean> booleanPermission = PermissionNode.of("test", "boolean");
		assertDoesNotThrow(() -> assertNull(booleanPermission.cast(null)));
		assertDoesNotThrow(() -> assertNotNull(booleanPermission.cast(true)));
		assertDoesNotThrow(() -> assertNotNull(booleanPermission.cast(Boolean.FALSE)));
		assertThrows(IllegalArgumentException.class, () -> booleanPermission.cast(typed));

		PermissionNode<String> stringPermission = PermissionNode.ofString("test", "string");
		assertDoesNotThrow(() -> assertNull(stringPermission.cast(null)));
		assertDoesNotThrow(() -> assertNotNull(stringPermission.cast("Right type")));
		assertThrows(IllegalArgumentException.class, () -> stringPermission.cast(typed));

		PermissionNode<Integer> intPermission = PermissionNode.ofInteger("test", "int");
		assertDoesNotThrow(() -> assertNull(intPermission.cast(null)));
		assertDoesNotThrow(() -> assertNotNull(intPermission.cast(1234)));
		assertDoesNotThrow(() -> assertNotNull(intPermission.cast(Integer.valueOf(5))));
		assertThrows(IllegalArgumentException.class, () -> intPermission.cast(typed));

		PermissionNode<Object> customGenericPermission = PermissionNode.ofCustom("test", "custom_generic", MapCodec.unitCodec(object), Object.class);
		assertDoesNotThrow(() -> assertNull(customGenericPermission.cast(null)));
		assertDoesNotThrow(() -> assertNotNull(customGenericPermission.cast(1234)));
		assertDoesNotThrow(() -> assertNotNull(customGenericPermission.cast(object)));
		assertDoesNotThrow(() -> assertNotNull(customGenericPermission.cast(typed)));

		PermissionNode<BaseType> customSpecificPermission = PermissionNode.ofCustom("test", "custom_specific", MapCodec.unitCodec(typed), BaseType.class);
		assertDoesNotThrow(() -> assertNull(customSpecificPermission.cast(null)));
		assertDoesNotThrow(() -> assertNotNull(customSpecificPermission.cast(typed)));
		assertDoesNotThrow(() -> assertNotNull(customSpecificPermission.cast(typed2)));
		assertThrows(IllegalArgumentException.class, () -> customSpecificPermission.cast(object));
	}
}
