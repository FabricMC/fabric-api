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

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public class HorseFoodImpl implements ServerLivingEntityEvents.HorseFed.HorseFood {
	float health;
	int ageUp;
	int temper;
	boolean inLove;

	public HorseFoodImpl(float health, int ageUp, int temper, boolean inLove) {
		this.health = health;
		this.ageUp = ageUp;
		this.temper = temper;
		this.inLove = inLove;
	}

	@Override
	public float health() {
		return health;
	}

	@Override
	public int ageUp() {
		return ageUp;
	}

	@Override
	public int temper() {
		return temper;
	}

	@Override
	public boolean inLove() {
		return inLove;
	}

	@Override
	public void health(float health) {
		this.health = health;
	}

	@Override
	public void ageUp(int ageUp) {
		this.ageUp = ageUp;
	}

	@Override
	public void temper(int temper) {
		this.temper = temper;
	}

	@Override
	public void inLove(boolean inLove) {
		this.inLove = inLove;
	}
}
