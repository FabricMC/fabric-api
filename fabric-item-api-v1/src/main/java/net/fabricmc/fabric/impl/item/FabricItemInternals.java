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

package net.fabricmc.fabric.impl.item;

import java.util.WeakHashMap;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;

public final class FabricItemInternals {
	private static final WeakHashMap<Item.Properties, ExtraData> extraData = new WeakHashMap<>();

	private FabricItemInternals() {
	}

	public static ExtraData computeExtraData(Item.Properties settings) {
		return extraData.computeIfAbsent(settings, s -> new ExtraData());
	}

	public static void onBuild(Item.Properties settings, Item item) {
		ExtraData data = extraData.get(settings);

		if (data != null) {
			((ItemExtensions) item).fabric_setEquipmentSlotProvider(data.equipmentSlotProvider);
			((ItemExtensions) item).fabric_setCustomDamageHandler(data.customDamageHandler);
		}
	}

	public static final class ExtraData {
		private @Nullable EquipmentSlotProvider equipmentSlotProvider;
		private @Nullable CustomDamageHandler customDamageHandler;

		public void equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
			this.equipmentSlotProvider = equipmentSlotProvider;
		}

		public void customDamage(CustomDamageHandler handler) {
			this.customDamageHandler = handler;
		}
	}
}
