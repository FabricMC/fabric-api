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

package net.fabricmc.fabric.impl.resource.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.base.toposort.SortableNode;

class ResourceReloaderPhaseData extends SortableNode<ResourceReloaderPhaseData> {
	final Identifier id;
	ResourceReloader resourceReloader;
	/**
	 * This is used to keep track of the source and ordering expectation of this resource reloader.
	 * <ul>
	 *   <li>A resource reloader that is Vanilla must abide by Vanilla's ordering.</li>
	 *   <li>
	 *     A resource reloader that is before one of the Vanilla reloaders
	 *     will be considered as before Vanilla,
	 *     any other resource reloaders that must happen before
	 *     will also be considered as before Vanilla.
	 *   </li>
	 *   <li>
	 *     A resource reloader that is after Vanilla must happen after Vanilla, reloaders depending on it must
	 *     also happen after Vanilla.
	 *   </li>
	 *   <li>
	 *     A resource reloader that doesn't have anything specified will be automatically ordered after Vanilla.
	 *   </li>
	 * </ul>
	 */
	VanillaStatus vanillaStatus = VanillaStatus.NONE;

	ResourceReloaderPhaseData(Identifier id, @Nullable ResourceReloader resourceReloader) {
		super();
		this.id = id;
		this.resourceReloader = resourceReloader;
	}

	/**
	 * Marks this phase and all preceding phases as running before Vanilla.
	 */
	void markBefore() {
		boolean isAfter = this.vanillaStatus == VanillaStatus.AFTER;

		if (this.vanillaStatus != VanillaStatus.NONE && !isAfter) return;

		this.vanillaStatus = VanillaStatus.BEFORE;

		for (ResourceReloaderPhaseData prev : this.previousNodes) {
			prev.markBefore();
		}
	}

	/**
	 * Marks this phase and all succeeding phases as running after Vanilla.
	 */
	void markAfter() {
		if (this.vanillaStatus != VanillaStatus.NONE) return;

		this.vanillaStatus = VanillaStatus.AFTER;

		for (ResourceReloaderPhaseData next : this.subsequentNodes) {
			next.markAfter();
		}
	}

	void setVanillaStatus(VanillaStatus status) {
		if (this.vanillaStatus == VanillaStatus.NONE) {
			this.vanillaStatus = status;
		}
	}

	@Override
	protected String getDescription() {
		return this.id.toString();
	}

	@Override
	protected void addSubsequentNode(ResourceReloaderPhaseData phase) {
		super.addSubsequentNode(phase);

		if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.AFTER) {
			phase.markAfter();
		}
	}

	protected void addPreviousNode(ResourceReloaderPhaseData phase) {
		super.addPreviousNode(phase);

		if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.BEFORE) {
			// We also mark the phase before
			phase.markBefore();
		}
	}

	enum VanillaStatus {
		NONE,
		AFTER,
		BEFORE,
		VANILLA
	}

	static class AfterVanilla extends ResourceReloaderPhaseData {
		AfterVanilla(Identifier id) {
			super(id, null);
			this.setVanillaStatus(VanillaStatus.VANILLA);
		}

		@Override
		public void markBefore() {
		}

		@Override
		public void markAfter() {
		}
	}
}
