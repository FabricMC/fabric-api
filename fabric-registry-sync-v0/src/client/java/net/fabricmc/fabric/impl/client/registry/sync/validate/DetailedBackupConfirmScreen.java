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

package net.fabricmc.fabric.impl.client.registry.sync.validate;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.impl.registry.sync.validate.Details;
import net.fabricmc.fabric.mixin.registry.sync.client.BackupConfirmScreenAccessor;

public class DetailedBackupConfirmScreen extends BackupConfirmScreen {
	private final Supplier<Details> detailsSupplier;

	public DetailedBackupConfirmScreen(Runnable onCancel, Listener onProceed, Component title, Component description, Supplier<Details> detailsSupplier) {
		super(onCancel, onProceed, title, description, false);
		this.detailsSupplier = Suppliers.memoize(detailsSupplier);
	}

	@Override
	protected void init() {
		super.init();

		int textSize = (((BackupConfirmScreenAccessor) this).fabric_getMessage().getLineCount() + 1) * 9;
		this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.experimental.details"),
						(button) -> this.minecraft.setScreenAndShow(new DetailsScreen(Component.translatable("fabric-registry-sync-v0.missing-entries.details.title"), this.detailsSupplier, this)))
				.bounds(this.width / 2 - 155 + 80, 76 + textSize, 150, 20).build());
	}
}
