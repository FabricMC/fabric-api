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

package net.fabricmc.fabric.test.object.builder;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class FabricDefaultAttributeRegistryGameTest {
	@GameTest
	public void pigHasTestAttribute(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, 0, 0, 0);

		helper.assertTrue(pig.getAttributes().hasAttribute(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE), "Pig does not have test attribute");

		double testAttributeBaseValue = pig.getAttributeBaseValue(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE);
		helper.assertValueEqual(testAttributeBaseValue, FabricDefaultAttributeRegistryTest.PIG_TEST_ATTRIBUTE_BASE_VALUE, "Pig test attribute base value");

		double testAttributeValue = pig.getAttributeValue(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE);
		helper.assertValueEqual(testAttributeValue, FabricDefaultAttributeRegistryTest.PIG_TEST_ATTRIBUTE_BASE_VALUE, "Pig test attribute final value");

		helper.succeed();
	}

	@GameTest
	public void cowHasTestAttribute(GameTestHelper helper) {
		Cow cow = helper.spawn(EntityTypes.COW, 0, 0, 0);

		helper.assertTrue(cow.getAttributes().hasAttribute(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE), "Cow does not have test attribute");

		double testAttributeBaseValue = cow.getAttributeBaseValue(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE);
		helper.assertValueEqual(testAttributeBaseValue, 0.0, "Cow test attribute base value");

		double testAttributeValue = cow.getAttributeValue(FabricDefaultAttributeRegistryTest.TEST_ATTRIBUTE);
		helper.assertValueEqual(testAttributeValue, 0.0, "Cow test attribute final value");

		helper.succeed();
	}

	@GameTest
	public void pigDoesNotHavePlayerTestAttribute(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, 0, 0, 0);

		helper.assertFalse(pig.getAttributes().hasAttribute(FabricDefaultAttributeRegistryTest.TEST_CHICKEN_ONLY_ATTRIBUTE), "Pig has the chicken-only test attribute");

		helper.succeed();
	}

	@GameTest
	public void chickenHasChickenOnlyTestAttribute(GameTestHelper helper) {
		Chicken chicken = helper.spawn(EntityTypes.CHICKEN, 0, 0, 0);

		helper.assertTrue(chicken.getAttributes().hasAttribute(FabricDefaultAttributeRegistryTest.TEST_CHICKEN_ONLY_ATTRIBUTE), "Chicken does not have the chicken-only test attribute");

		double testAttributeBaseValue = chicken.getAttributeBaseValue(FabricDefaultAttributeRegistryTest.TEST_CHICKEN_ONLY_ATTRIBUTE);
		helper.assertValueEqual(testAttributeBaseValue, 0.0, "Chicken-only test attribute base value");

		double testAttributeValue = chicken.getAttributeValue(FabricDefaultAttributeRegistryTest.TEST_CHICKEN_ONLY_ATTRIBUTE);
		helper.assertValueEqual(testAttributeValue, 0.0, "Chicken-only test attribute final value");

		helper.succeed();
	}

	@GameTest
	public void pigStillHasItsVanillaAttributes(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, 0, 0, 0);

		helper.assertTrue(pig.getAttributes().hasAttribute(Attributes.MAX_HEALTH), "Pig does not have max health attribute");
		helper.assertTrue(pig.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE), "Pig does not have follow range attribute");
		helper.assertTrue(pig.getAttributes().hasAttribute(Attributes.TEMPT_RANGE), "Pig does not have tempt range attribute");
		helper.assertTrue(pig.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED), "Pig does not have movement speed attribute");
		helper.assertValueEqual(pig.getAttributeBaseValue(Attributes.MAX_HEALTH), 10.0, "Pig max health attribute base value");

		helper.succeed();
	}
}
