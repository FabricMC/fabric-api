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

package net.fabricmc.fabric.test.registry.sync;

import com.mojang.serialization.Lifecycle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

import net.fabricmc.fabric.api.event.registry.BootstrapHelper;

public class FabricRegistrySetBuilderTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testSetBuilderLifecycle() {
		HolderLookup.Provider registries = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.add(Registries.CHICKEN_VARIANT, Lifecycle.experimental(), _ -> {
			});
			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), _ -> {
			}));
		});

		Assertions.assertEquals(Lifecycle.experimental(), registries.allRegistriesLifecycle());

		HolderLookup.Provider registries2 = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), _ -> {
			});
			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), _ -> {
			}));
		});

		Assertions.assertEquals(Lifecycle.stable(), registries2.allRegistriesLifecycle());

		HolderLookup.Provider registries3 = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.add(Registries.CHICKEN_VARIANT, Lifecycle.deprecated(400), _ -> {
			});
			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), _ -> {
			}));
			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.deprecated(300), _ -> {
			}));
		});
		Assertions.assertInstanceOf(Lifecycle.Deprecated.class, registries3.allRegistriesLifecycle());
		Assertions.assertEquals(300, ((Lifecycle.Deprecated) registries3.allRegistriesLifecycle()).since());
	}

	@Test
	void testBuilderMerging() {
		HolderLookup.Provider registries = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), registry -> {
				registry.register(ChickenVariants.COLD, new ChickenVariant(new ModelAndTexture<>(ChickenVariant.ModelType.COLD, Identifier.withDefaultNamespace("idk")), new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("idk")), SpawnPrioritySelectors.EMPTY));
			}));

			pendingRegistries.withBootstrapsFrom(new RegistrySetBuilder().add(Registries.CHICKEN_VARIANT, Lifecycle.stable(), registry -> {
				registry.register(ChickenVariants.TEMPERATE, new ChickenVariant(new ModelAndTexture<>(ChickenVariant.ModelType.COLD, Identifier.withDefaultNamespace("idk")), new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("idk")), SpawnPrioritySelectors.EMPTY));
			}));
		});

		Assertions.assertTrue(registries.lookup(Registries.CHICKEN_VARIANT).isPresent());
		Assertions.assertTrue(registries.lookup(Registries.CHICKEN_VARIANT).flatMap(r -> r.get(ChickenVariants.TEMPERATE)).isPresent());
		Assertions.assertTrue(registries.lookup(Registries.CHICKEN_VARIANT).flatMap(r -> r.get(ChickenVariants.COLD)).isPresent());
		Assertions.assertTrue(registries.lookup(Registries.CHICKEN_VARIANT).flatMap(r -> r.get(ChickenVariants.WARM)).isEmpty());
	}
}
