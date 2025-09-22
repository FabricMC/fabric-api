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

package net.fabricmc.fabric.test.resource.reloader.v1;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.client.resource.PeriodicNotificationManager;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.v1.ResourceLoaderImpl;

public class ResourceLoaderImplTest {
	@Test
	public void testReloaderSorting() {
		ResourceLoader resourceLoader = ResourceLoader.get(ResourceType.CLIENT_RESOURCES);

		record Dummy(Identifier id, ResourceReloader reloader) {
			Dummy(Identifier id) {
				this(id, new DummyResourceReloader());
			}
		}

		var dummyReloader1 = new Dummy(Identifier.of("fabric", "dummy_reloader_1"));
		var dummyReloader2 = new Dummy(Identifier.of("fabric", "dummy_reloader_2"));
		var dummyReloader3 = new Dummy(Identifier.of("fabric", "dummy_reloader_3"));
		var dummyReloader4 = new Dummy(Identifier.of("fabric", "dummy_reloader_4"));

		resourceLoader.registerReloader(dummyReloader1.id, dummyReloader1.reloader);
		resourceLoader.registerReloader(dummyReloader2.id, dummyReloader2.reloader);
		resourceLoader.registerReloader(dummyReloader3.id, dummyReloader3.reloader);
		resourceLoader.registerReloader(dummyReloader4.id, dummyReloader4.reloader);

		resourceLoader.addReloaderOrdering(dummyReloader3.id, dummyReloader1.id);
		resourceLoader.addReloaderOrdering(dummyReloader1.id, dummyReloader2.id);

		resourceLoader.addReloaderOrdering(dummyReloader4.id, ResourceReloaderKeys.BEFORE_VANILLA);

		var languageReloader = new LanguageManager(
				"en_us", translationStorage -> {
		});
		var splashTextsReloader = new SplashTextResourceSupplier(null);
		var periodicNotificationManager = new PeriodicNotificationManager(Identifier.of("a"), o -> true);

		List<ResourceReloader> sorted = ResourceLoaderImpl.sort(ResourceType.CLIENT_RESOURCES, List.of(
				languageReloader,
				splashTextsReloader,
				periodicNotificationManager
		));

		assertSame(sorted.getFirst(), dummyReloader4.reloader);
		assertSame(sorted.get(1), languageReloader);
		assertSame(sorted.get(2), splashTextsReloader);
		assertSame(sorted.get(3), periodicNotificationManager);
		assertSame(sorted.get(4), dummyReloader3.reloader);
		assertSame(sorted.get(5), dummyReloader1.reloader);
		assertSame(sorted.get(6), dummyReloader2.reloader);
	}
}
