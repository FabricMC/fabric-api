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

package net.fabricmc.fabric.impl.entity.event.effect;

import net.fabricmc.fabric.api.entity.event.v1.EntityFedEvents;

public class HorseFoodImpl implements EntityFedEvents.HorseFed.HorseFood {
	float heal;
	int ageUpSeconds;
	int temperIncrease;
	boolean allowsBreeding;

	public HorseFoodImpl(float heal, int ageUpSeconds, int temper, boolean inLove) {
		this.heal = heal;
		this.ageUpSeconds = ageUpSeconds;
		this.temperIncrease = temper;
		this.allowsBreeding = inLove;
	}

	@Override
	public float getHealAmount() {
		return heal;
	}

	@Override
	public int getAgeUpSeconds() {
		return ageUpSeconds;
	}

	@Override
	public int getTemperIncrease() {
		return temperIncrease;
	}

	@Override
	public boolean getAllowsBreeding() {
		return allowsBreeding;
	}

	@Override
	public void setHealAmount(float heal) {
		this.heal = heal;
	}

	@Override
	public void setAgeUpSeconds(int ageUpSeconds) {
		this.ageUpSeconds = ageUpSeconds;
	}

	@Override
	public void setTemperIncrease(int temperIncrease) {
		this.temperIncrease = temperIncrease;
	}

	@Override
	public void setAllowsBreeding(boolean allowsBreeding) {
		this.allowsBreeding = allowsBreeding;
	}
}
