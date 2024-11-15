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

package net.fabricmc.fabric.api.dev.v1;

import java.util.function.Supplier;

import com.mojang.brigadier.ParseResults;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.Bootstrap;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.collection.Weight;
import net.minecraft.world.Heightmap;

/** Mods should not directly use these fields; they only exist here as a reference of what Dev Properties exist */
@ApiStatus.Internal
@SuppressWarnings("JavadocReference")
public class FabricDevProperties {
	/**
	 * Logs an error when a weight is set to zero</br>
	 * Property: <code>fabric.dev.zeroWeightWarning</code></br>
	 * {@link Weight#validate(int)}
	 */
	public static final boolean ZERO_WEIGHT_WARNING = getProperty("zeroWeightWarning");

	/**
	 * Logs an error when a translation is missing</br>
	 * Property: <code>fabric.dev.logMissingTranslations</code></br>
	 * {@link Bootstrap#logMissing()}
	 */
	public static final boolean LOG_MISSING_TRANSLATIONS = getProperty("logMissingTranslations");

	/**
	 * Logs an error if Block classes don't end with "Block" and if Item classes don't end with "Item"</br>
	 * Property: <code>fabric.dev.logConventionIssues</code></br>
	 * {@link net.minecraft.block.Block#Block} and {@link net.minecraft.item.Item#Item}
	 */
	public static final boolean LOG_BLOCK_AND_ITEM_CONVENTION_ISSUES = getProperty("logBlockAndItemConventionIssues");

	/**
	 * Registers Minecraft's debug commands
	 * (TestCommand, RaidCommand, DebugPathCommand, DebugMobSpawningCommand,
	 * WardenSpawnTrackerCommand, SpawnArmorTrimsCommand, ServerPackCommand),
	 * and if on the server DebugConfigCommand</br>
	 * Property: <code>fabric.dev.registerDebugCommands</code></br>
	 * {@link CommandManager#CommandManager}
	 */
	public static final boolean REGISTER_DEBUG_COMMANDS = getProperty("registerDebugCommands");

	/**
	 * Logs an error if a command threw an exception</br>
	 * Property: <code>fabric.dev.enableCommandExceptionLogging</code></br>
	 * {@link CommandManager#execute(ParseResults, String)}
	 */
	public static final boolean ENABLE_COMMAND_EXCEPTION_LOGGING = getProperty("enableCommandExceptionLogging");

	/**
	 * Logs an error regarding argument ambiguity and throws an exception if an argument type is not registered</br>
	 * Property: <code>fabric.dev.enableCommandArgumentLogging</code></br>
	 * {@link CommandManager#checkMissing()}
	 */
	public static final boolean ENABLE_COMMAND_ARGUMENT_LOGGING = getProperty("enableCommandArgumentLogging");

	/**
	 * Throw's an exception if a bounding box is invalid</br>
	 * Property: <code>fabric.dev.throwOnInvalidBlockBoxes</code></br>
	 * {@link net.minecraft.util.math.BlockBox#BlockBox(int, int, int, int, int, int)}
	 */
	public static final boolean THROW_ON_INVALID_BLOCK_BOXES = getProperty("throwOnInvalidBlockBoxes");

	/**
	 * Logs an error if the heightmap is null</br>
	 * Property: <code>fabric.dev.enableUnprimedHeightmapLogging</code></br>
	 * {@link net.minecraft.world.chunk.Chunk#sampleHeightmap(Heightmap.Type, int, int)}
	 */
	public static final boolean ENABLE_UNPRIMED_HEIGHTMAP_LOGGING = getProperty("enableUnprimedHeightmapLogging");

	/**
	 * Set's the current thread's name to the activeThreadName if debugRunnable or debugSupplier is called</br>
	 * Property: <code>fabric.dev.enableSupplierAndRunnableDebugging</code></br>
	 * {@link net.minecraft.util.Util#debugRunnable(String, Runnable)} and {@link net.minecraft.util.Util#debugSupplier(String, Supplier)}
	 */
	public static final boolean ENABLE_SUPPLIER_AND_RUNNABLE_DEBUGGING = getProperty("enableSupplierAndRunnableDebugging");

	/**
	 * Invokes a method in which you should have a breakpoint to debug errors
	 * thrown with Util#error and exceptions thrown with Util#throwOrPause</br>
	 * Property: <code>fabric.dev.enableExceptionIdePausing</code></br>
	 * {@link net.minecraft.util.Util#error(String)}, {@link net.minecraft.util.Util#error(String, Throwable)}
	 * and {@link net.minecraft.util.Util#throwOrPause(Throwable)}
	 */
	public static final boolean ENABLE_EXCEPTION_IDE_PAUSING = getProperty("enableExceptionIdePausing");

	private static boolean getProperty(String name) {
		return Boolean.getBoolean("fabric.dev." + name);
	}
}
