/**
 * Provides support for client gametests. To register a client gametest, add an entry to the
 * {@code fabric-client-gametest} entrypoint in your {@code fabric.mod.json}. Your gametest class should implement
 * {@link net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest FabricClientGameTest}.
 *
 * <h1>Lifecycle</h1>
 * Client gametests are run sequentially. When a gametest ends, the game will be
 * returned to the title screen. When all gametests have been run, the game will be closed.
 *
 * <h1>Threading</h1>
 *
 * <p>Client gametests run on the client gametest thread. Use the functions inside
 * {@link net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext ClientGameTestContext} and other test helper
 * classes to run code on the correct thread. Exceptions are transparently rethrown on the test thread, and their stack
 * traces are mutated to include the async stack trace, to make them easy to track. You can disable this behavior by
 * setting the {@code fabric.client.gametest.disableJoinAsyncStackTraces} system property.
 *
 * <p>The game remains paused unless you explicitly unpause it using various waiting functions such as
 * {@link net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext#waitTick() ClientGameTestContext.waitTick()}.
 *
 * <p>A few changes have been made to how the vanilla game threads run, to make tests more reproducible. Notably, there
 * is exactly one server tick per client tick while a server is running (singleplayer or multiplayer). There is also a
 * limit of one client tick per frame.
 *
 * <h1>Network synchronization</h1>
 *
 * <p>Network packets are internally tracked and managed so that they are always handled at a consistent time, always
 * before the next tick. Calling {@code waitTick()} is always enough for a server packet to be handled on the client or
 * vice versa.
 *
 * <p>If your mod interacts with the network code at a low level, such as by directly hooking into the Netty pipeline to
 * send or handle packets, you may need to disable network synchronization. You can do this by setting the
 * {@code fabric.client.gametest.disableNetworkSynchronizer} system property.
 *
 * <h1>Default settings</h1>
 * The client gametest API adjusts some default settings, usually for consistency of tests. These settings can always be
 * changed back to the default value or a different value inside a gametest.
 *
 * <h2>Game options</h2>
 * <table>
 *     <tr>
 *         <th>Setting name</th>
 *         <th>Gametest default</th>
 *         <th>Vanilla default</th>
 *         <th>Reason</th>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.option.GameOptions#tutorialStep Tutorial step}</td>
 *         <td>{@link net.minecraft.client.tutorial.TutorialStep#NONE NONE}</td>
 *         <td>{@link net.minecraft.client.tutorial.TutorialStep#MOVEMENT MOVEMENT}</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.option.GameOptions#getCloudRenderMode() Cloud render mode}</td>
 *         <td>{@link net.minecraft.client.option.CloudRenderMode#OFF OFF}</td>
 *         <td>{@link net.minecraft.client.option.CloudRenderMode#FANCY FANCY}</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.option.GameOptions#onboardAccessibility Onboard accessibility}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Would cause the game test runner to have to click through the onboard accessibility prompt</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.option.GameOptions#getViewDistance() View distance}</td>
 *         <td>{@code 5}</td>
 *         <td>{@code 10}</td>
 *         <td>Speeds up loading of chunks, especially for functions such as
 *         {@link net.fabricmc.fabric.api.client.gametest.v1.TestClientWorldContext#waitForChunksRender() TestClientWorldContext.waitForChunksRender()}</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.option.GameOptions#getSoundVolumeOption(net.minecraft.sound.SoundCategory) Music volume}</td>
 *         <td>{@code 0.0}</td>
 *         <td>{@code 1.0}</td>
 *         <td>The game music is annoying while running gametests</td>
 *     </tr>
 * </table>
 *
 * <h2>World creation options</h2>
 * These adjusted defaults only apply if the world builder's
 * {@linkplain net.fabricmc.fabric.api.client.gametest.v1.TestWorldBuilder#setUseConsistentSettings(boolean) consistent settings}
 * have not been set to {@code false}.
 *
 * <table>
 *     <tr>
 *         <th>Setting name</th>
 *         <th>Gametest default</th>
 *         <th>Vanilla default</th>
 *         <th>Reason</th>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.gui.screen.world.WorldCreator#setWorldType(net.minecraft.client.gui.screen.world.WorldCreator.WorldType) World type}</td>
 *         <td>{@link net.minecraft.world.gen.WorldPresets#FLAT FLAT}</td>
 *         <td>{@link net.minecraft.world.gen.WorldPresets#DEFAULT DEFAULT}</td>
 *         <td>Creates cleaner test cases</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.gui.screen.world.WorldCreator#setSeed(String) Seed}</td>
 *         <td>{@code 1}</td>
 *         <td>Random value</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.client.gui.screen.world.WorldCreator#setGenerateStructures(boolean) Generate structures}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Consistency of tests and creates cleaner tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.world.GameRules#DO_DAYLIGHT_CYCLE Do daylight cycle}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.world.GameRules#DO_WEATHER_CYCLE Do weather cycle}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 *     <tr>
 *         <td>{@linkplain net.minecraft.world.GameRules#DO_MOB_SPAWNING Do mob spawning}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Consistency of tests</td>
 *     </tr>
 * </table>
 *
 * <h2>Dedicated server properties</h2>
 * <table>
 *     <tr>
 *         <th>Setting name</th>
 *         <th>Gametest default</th>
 *         <th>Vanilla default</th>
 *         <th>Reason</th>
 *     </tr>
 *     <tr>
 *         <td>{@code online-mode}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>Allows the gametest client to connect to the dedicated server without being logged in to a Minecraft
 *         account</td>
 *     </tr>
 *     <tr>
 *         <td>{@code sync-chunk-writes}</td>
 *         <td>{@code true} on Windows, {@code false} on other operating systems</td>
 *         <td>{@code true}</td>
 *         <td>Causes world saving and closing to be extremely slow (on the order of many seconds to minutes) on Unix
 *         systems. The vanilla default is set correctly in singleplayer but not on dedicated servers.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code spawn-protection}</td>
 *         <td>{@code 0}</td>
 *         <td>{@code 16}</td>
 *         <td>Spawn protection prevents non-opped players from modifying the world within a certain radius of the world
 *         spawn point, a likely source of confusion when writing gametests</td>
 *     </tr>
 *     <tr>
 *         <td>{@code max-players}</td>
 *         <td>{@code 1}</td>
 *         <td>{@code 20}</td>
 *         <td>Stops other players from joining the server and interfering with the test</td>
 *     </tr>
 * </table>
 */
@ApiStatus.Experimental
package net.fabricmc.fabric.api.client.gametest.v1;

import org.jetbrains.annotations.ApiStatus;
