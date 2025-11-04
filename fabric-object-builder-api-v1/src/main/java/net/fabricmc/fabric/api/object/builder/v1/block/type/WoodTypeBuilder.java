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

package net.fabricmc.fabric.api.object.builder.v1.block.type;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * This class allows easy creation of {@link WoodType}s.
 *
 * <p>A {@link WoodType} is used to tell the game what textures signs should use, as well as sounds for both signs and fence gates.
 *
 * <p>Regular sign textures are stored at {@code [namespace]/textures/entity/signs/[path].png}.
 * <br>Hanging sign textures are stored at {@code [namespace]/textures/entity/signs/hanging/[path].png}.
 *
 * @see BlockSetTypeBuilder
 */
public final class WoodTypeBuilder {
	private SoundType soundGroup = SoundType.WOOD;
	private SoundType hangingSignSoundGroup = SoundType.HANGING_SIGN;
	private SoundEvent fenceGateCloseSound = SoundEvents.FENCE_GATE_CLOSE;
	private SoundEvent fenceGateOpenSound = SoundEvents.FENCE_GATE_OPEN;

	/**
	 * Sets this wood type's sound group.
	 *
	 * <p>Defaults to {@link SoundType#WOOD}.
	 *
	 * @return this builder for chaining
	 */
	public WoodTypeBuilder soundGroup(SoundType soundGroup) {
		this.soundGroup = soundGroup;
		return this;
	}

	/**
	 * Sets this wood type's hanging sign sound group.
	 *
	 * <p>Defaults to {@link SoundType#HANGING_SIGN}.
	 *
	 * @return this builder for chaining
	 */
	public WoodTypeBuilder hangingSignSoundGroup(SoundType hangingSignSoundGroup) {
		this.hangingSignSoundGroup = hangingSignSoundGroup;
		return this;
	}

	/**
	 * Sets this wood type's fence gate close sound.
	 *
	 * <p>Defaults to {@link SoundEvents#FENCE_GATE_CLOSE}.
	 *
	 * @return this builder for chaining
	 */
	public WoodTypeBuilder fenceGateCloseSound(SoundEvent fenceGateCloseSound) {
		this.fenceGateCloseSound = fenceGateCloseSound;
		return this;
	}

	/**
	 * Sets this wood type's fence gate open sound.
	 *
	 * <p>Defaults to {@link SoundEvents#FENCE_GATE_OPEN}.
	 *
	 * @return this builder for chaining
	 */
	public WoodTypeBuilder fenceGateOpenSound(SoundEvent fenceGateOpenSound) {
		this.fenceGateOpenSound = fenceGateOpenSound;
		return this;
	}

	/**
	 * Creates a new {@link WoodTypeBuilder} that copies all of another builder's values.
	 *
	 * @param builder the {@link WoodTypeBuilder} whose values are to be copied
	 *
	 * @return the created copy
	 */
	public static WoodTypeBuilder copyOf(WoodTypeBuilder builder) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundGroup(builder.soundGroup);
		copy.hangingSignSoundGroup(builder.hangingSignSoundGroup);
		copy.fenceGateCloseSound(builder.fenceGateCloseSound);
		copy.fenceGateOpenSound(builder.fenceGateOpenSound);
		return copy;
	}

	/**
	 * Creates a new {@link WoodTypeBuilder} that copies all of another wood type's values.
	 *
	 * @param woodType the {@link WoodType} whose values are to be copied
	 *
	 * @return the created copy
	 */
	public static WoodTypeBuilder copyOf(WoodType woodType) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundGroup(woodType.soundType());
		copy.hangingSignSoundGroup(woodType.hangingSignSoundType());
		copy.fenceGateCloseSound(woodType.fenceGateClose());
		copy.fenceGateOpenSound(woodType.fenceGateOpen());
		return copy;
	}

	/**
	 * Builds and registers a {@link WoodType} from this builder's values.
	 *
	 * <p>Alternatively, you can use {@link #build(Identifier, BlockSetType)} to build without registering.
	 * <br>Then {@link WoodType#register(WoodType)} can be used to register it later.
	 *
	 * @param id the id for the built {@link WoodType}
	 * @param setType the {@link BlockSetType} for the built {@link WoodType}
	 *
	 * @return the built and registered {@link WoodType}
	 */
	public WoodType register(Identifier id, BlockSetType setType) {
		return WoodType.register(this.build(id, setType));
	}

	/**
	 * Builds a {@link WoodType} from this builder's values without registering it.
	 *
	 * <p>Use {@link WoodType#register(WoodType)} to register it later.
	 * <br>Alternatively, you can use {@link #register(Identifier, BlockSetType)} to build and register it now.
	 *
	 * @param id the id for the built {@link WoodType}
	 * @param setType the {@link BlockSetType} for the built {@link WoodType}
	 *
	 * @return the built {@link WoodType}
	 */
	public WoodType build(Identifier id, BlockSetType setType) {
		return new WoodType(id.toString(), setType, soundGroup, hangingSignSoundGroup, fenceGateCloseSound, fenceGateOpenSound);
	}
}
