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

/**
 * This class allows easy creation of {@link BlockSetType}s.
 *
 * <p>A {@link BlockSetType} is used to tell the game various properties of related blocks, such as what sounds they should use.
 *
 * @see WoodTypeBuilder
 */
public final class BlockSetTypeBuilder {
	private boolean openableByHand = true;
	private boolean openableByWindCharge = true;
	private boolean buttonActivatedByArrows = true;
	private BlockSetType.PressurePlateSensitivity pressurePlateActivationRule = BlockSetType.PressurePlateSensitivity.EVERYTHING;
	private SoundType soundGroup = SoundType.WOOD;
	private SoundEvent doorCloseSound = SoundEvents.WOODEN_DOOR_CLOSE;
	private SoundEvent doorOpenSound = SoundEvents.WOODEN_DOOR_OPEN;
	private SoundEvent trapdoorCloseSound = SoundEvents.WOODEN_TRAPDOOR_CLOSE;
	private SoundEvent trapdoorOpenSound = SoundEvents.WOODEN_TRAPDOOR_OPEN;
	private SoundEvent pressurePlateClickOffSound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF;
	private SoundEvent pressurePlateClickOnSound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON;
	private SoundEvent buttonClickOffSound = SoundEvents.WOODEN_BUTTON_CLICK_OFF;
	private SoundEvent buttonClickOnSound = SoundEvents.WOODEN_BUTTON_CLICK_ON;

	/**
	 * Sets whether this block set type's door and trapdoor can be opened by hand.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder openableByHand(boolean openableByHand) {
		this.openableByHand = openableByHand;
		return this;
	}

	/**
	 * Sets whether this block set type's door and trapdoor can be opened by wind charge.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder openableByWindCharge(boolean openableByWindCharge) {
		this.openableByWindCharge = openableByWindCharge;
		return this;
	}

	/**
	 * Sets whether this block set type's button can be activated by an arrow.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder buttonActivatedByArrows(boolean buttonActivatedByArrows) {
		this.buttonActivatedByArrows = buttonActivatedByArrows;
		return this;
	}

	/**
	 * Sets this block set type's pressure plate {@link BlockSetType.PressurePlateSensitivity}.
	 *
	 * <p>Defaults to {@link BlockSetType.PressurePlateSensitivity#EVERYTHING}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder pressurePlateActivationRule(BlockSetType.PressurePlateSensitivity activationRule) {
		this.pressurePlateActivationRule = activationRule;
		return this;
	}

	/**
	 * Sets this block set type's sound group.
	 *
	 * <p>Defaults to {@link SoundType#WOOD}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder soundGroup(SoundType soundGroup) {
		this.soundGroup = soundGroup;
		return this;
	}

	/**
	 * Sets this block set type's door close sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_DOOR_CLOSE}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder doorCloseSound(SoundEvent doorCloseSound) {
		this.doorCloseSound = doorCloseSound;
		return this;
	}

	/**
	 * Sets this block set type's door open sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_DOOR_OPEN}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder doorOpenSound(SoundEvent doorOpenSound) {
		this.doorOpenSound = doorOpenSound;
		return this;
	}

	/**
	 * Sets this block set type's trapdoor close sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_TRAPDOOR_CLOSE}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder trapdoorCloseSound(SoundEvent trapdoorCloseSound) {
		this.trapdoorCloseSound = trapdoorCloseSound;
		return this;
	}

	/**
	 * Sets this block set type's trapdoor open sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_TRAPDOOR_OPEN}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder trapdoorOpenSound(SoundEvent trapdoorOpenSound) {
		this.trapdoorOpenSound = trapdoorOpenSound;
		return this;
	}

	/**
	 * Sets this block set type's pressure plate click off sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_PRESSURE_PLATE_CLICK_OFF}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder pressurePlateClickOffSound(SoundEvent pressurePlateClickOffSound) {
		this.pressurePlateClickOffSound = pressurePlateClickOffSound;
		return this;
	}

	/**
	 * Sets this block set type's pressure plate click on sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_PRESSURE_PLATE_CLICK_ON}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder pressurePlateClickOnSound(SoundEvent pressurePlateClickOnSound) {
		this.pressurePlateClickOnSound = pressurePlateClickOnSound;
		return this;
	}

	/**
	 * Sets this block set type's button click off sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_BUTTON_CLICK_OFF}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder buttonClickOffSound(SoundEvent buttonClickOffSound) {
		this.buttonClickOffSound = buttonClickOffSound;
		return this;
	}

	/**
	 * Sets this block set type's button click on sound.
	 *
	 * <p>Defaults to {@link SoundEvents#WOODEN_BUTTON_CLICK_ON}.
	 *
	 * @return this builder for chaining
	 */
	public BlockSetTypeBuilder buttonClickOnSound(SoundEvent buttonClickOnSound) {
		this.buttonClickOnSound = buttonClickOnSound;
		return this;
	}

	/**
	 * Creates a new {@link BlockSetTypeBuilder} that copies all of another builder's values.
	 *
	 * @param builder the {@link BlockSetTypeBuilder} whose values are to be copied
	 *
	 * @return the created copy
	 */
	public static BlockSetTypeBuilder copyOf(BlockSetTypeBuilder builder) {
		BlockSetTypeBuilder copy = new BlockSetTypeBuilder();
		copy.openableByHand(builder.openableByHand);
		copy.openableByWindCharge(builder.openableByWindCharge);
		copy.buttonActivatedByArrows(builder.buttonActivatedByArrows);
		copy.pressurePlateActivationRule(builder.pressurePlateActivationRule);
		copy.soundGroup(builder.soundGroup);
		copy.doorCloseSound(builder.doorCloseSound);
		copy.doorOpenSound(builder.doorOpenSound);
		copy.trapdoorCloseSound(builder.trapdoorCloseSound);
		copy.trapdoorOpenSound(builder.trapdoorOpenSound);
		copy.pressurePlateClickOffSound(builder.pressurePlateClickOffSound);
		copy.pressurePlateClickOnSound(builder.pressurePlateClickOnSound);
		copy.buttonClickOffSound(builder.buttonClickOffSound);
		copy.buttonClickOnSound(builder.buttonClickOnSound);
		return copy;
	}

	/**
	 * Creates a new {@link BlockSetTypeBuilder} that copies all of another block set type's values.
	 *
	 * @param setType the {@link BlockSetType} whose values are to be copied
	 *
	 * @return the created copy
	 */
	public static BlockSetTypeBuilder copyOf(BlockSetType setType) {
		BlockSetTypeBuilder copy = new BlockSetTypeBuilder();
		copy.openableByHand(setType.canOpenByHand());
		copy.openableByWindCharge(setType.canOpenByWindCharge());
		copy.buttonActivatedByArrows(setType.canButtonBeActivatedByArrows());
		copy.pressurePlateActivationRule(setType.pressurePlateSensitivity());
		copy.soundGroup(setType.soundType());
		copy.doorCloseSound(setType.doorClose());
		copy.doorOpenSound(setType.doorOpen());
		copy.trapdoorCloseSound(setType.trapdoorClose());
		copy.trapdoorOpenSound(setType.trapdoorOpen());
		copy.pressurePlateClickOffSound(setType.pressurePlateClickOff());
		copy.pressurePlateClickOnSound(setType.pressurePlateClickOn());
		copy.buttonClickOffSound(setType.buttonClickOff());
		copy.buttonClickOnSound(setType.buttonClickOn());
		return copy;
	}

	/**
	 * Builds and registers a {@link BlockSetType} from this builder's values.
	 *
	 * <p>Alternatively, you can use {@link #build(Identifier)} to build without registering.
	 * <br>Then {@link BlockSetType#register(BlockSetType)} can be used to register it later.
	 *
	 * @param id the id for the built {@link BlockSetType}
	 *
	 * @return the built and registered {@link BlockSetType}
	 */
	public BlockSetType register(Identifier id) {
		return BlockSetType.register(this.build(id));
	}

	/**
	 * Builds a {@link BlockSetType} from this builder's values without registering it.
	 *
	 * <p>Use {@link BlockSetType#register(BlockSetType)} to register it later.
	 * <br>Alternatively, you can use {@link #register(Identifier)} to build and register it now.
	 *
	 * @param id the id for the built {@link BlockSetType}
	 *
	 * @return the built {@link BlockSetType}
	 */
	public BlockSetType build(Identifier id) {
		return new BlockSetType(id.toString(), openableByHand, openableByWindCharge, buttonActivatedByArrows, pressurePlateActivationRule, soundGroup, doorCloseSound, doorOpenSound, trapdoorCloseSound, trapdoorOpenSound, pressurePlateClickOffSound, pressurePlateClickOnSound, buttonClickOffSound, buttonClickOnSound);
	}
}
