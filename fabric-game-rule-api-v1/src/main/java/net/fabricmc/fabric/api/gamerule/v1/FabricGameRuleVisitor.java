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

package net.fabricmc.fabric.api.gamerule.v1;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

/**
 * An extended game rule visitor which supports Fabric's own rule types.
 *
 * <p>Game rule visitors are typically used when iterating all game rules.
 * In vanilla, the visitor is used to register game rule commands and populate the {@code Edit Game Rules} screen.
 *
 * <p>Rule types specified by this interface are not exhaustive.
 * New entries may be added in the future.
 */
public interface FabricGameRuleVisitor extends GameRuleTypeVisitor {
	/**
	 * Visit a double rule.
	 */
	default void visitDouble(GameRule<Double> doubleRule) {
	}

	/**
	 * Visit an enum rule.
	 */
	default <E extends Enum<E>> void visitEnum(GameRule<E> enumRule) {
	}
}
