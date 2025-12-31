/// Provides support for client gametests. To register a client gametest, add an entry to the
/// `fabric-client-gametest` entrypoint in your `fabric.mod.json`. Your gametest class should implement
/// [FabricClientGameTest][net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest].
///
/// Loom provides an API to configure client gametests in your `build.gradle`. It is recommended to run
/// gametests from a separate source set and test mod:
/// <pre>
///
/// `fabricApi.configureTests{createSourceSet = truemodId = 'your-gametest-mod-id'}`
/// </pre>
/// # Lifecycle
/// Client gametests are run sequentially. When a gametest ends, the game will be
/// returned to the title screen. When all gametests have been run, the game will be closed.
/// # Threading
///
/// Client gametests run on the client gametest thread. Use the functions inside
/// [ClientGameTestContext][net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext] and other test
/// helper classes to run code on the correct thread. Exceptions are transparently rethrown on the test thread, and their
/// stack traces are mutated to include the async stack trace, to make them easy to track. You can disable this behavior
/// by setting the `fabric.client.gametest.disableJoinAsyncStackTraces` system property.
///
/// The game remains paused unless you explicitly unpause it using various waiting functions such as
/// [ClientGameTestContext.waitTick()][net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext#waitTick()].
/// A side effect of this is that **the results of your code may not be immediate if the game needs a tick to
/// process them**. A big example of this is key mappings, although some key mapping methods have built-in tick
/// waits to mitigate the issue. See the [TestInput][net.fabricmc.fabric.api.client.gametest.v1.TestInput]
/// documentation for details. Another pseudo-example is effects on the server need a tick to propagate to the client and
/// vice versa, although this is related to packets more than the fact the game is suspended (see the network
/// synchronization section below). A good strategy for debugging these issues is by
/// {@linkplain net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext#takeScreenshot(String) taking screenshots},
/// which capture the immediate state of the game.
///
/// A few changes have been made to how the vanilla game threads run, to make tests more reproducible. Notably, there
/// is exactly one server tick per client tick while a server is running (singleplayer or multiplayer). There is also a
/// limit of one client tick per frame.
/// # Network synchronization
///
/// Network packets are internally tracked and managed so that they are always handled at a consistent time, always
/// before the next tick. Calling `waitTick()` is always enough for a server packet to be handled on the client or
/// vice versa.
///
/// If your mod interacts with the network code at a low level, such as by directly hooking into the Netty pipeline to
/// send or handle packets, you may need to disable network synchronization. You can do this by setting the
/// `fabric.client.gametest.disableNetworkSynchronizer` system property.
/// # Default settings
/// The client gametest API adjusts some default settings, usually for consistency of tests. These settings can always be
/// changed back to the default value or a different value inside a gametest.
/// ## Game options
/// <table>
///     <tr>
///         <th>Setting name</th>
///         <th>Gametest default</th>
///         <th>Vanilla default</th>
///         <th>Reason</th>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.Options#tutorialStep Tutorial step}</td>
///         <td>[NONE][net.minecraft.client.tutorial.TutorialSteps#NONE]</td>
///         <td>[MOVEMENT][net.minecraft.client.tutorial.TutorialSteps#MOVEMENT]</td>
///         <td>Consistency of tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.Options#cloudStatus() Cloud status}</td>
///         <td>[OFF][net.minecraft.client.CloudStatus#OFF]</td>
///         <td>[FANCY][net.minecraft.client.CloudStatus#FANCY]</td>
///         <td>Consistency of tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.Options#onboardAccessibility Onboard accessibility}</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Would cause the game test runner to have to click through the onboard accessibility prompt</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.Options#renderDistance() Render distance}</td>
///         <td>`5`</td>
///         <td>`10`</td>
///         <td>Speeds up loading of chunks, especially for functions such as
///         [TestClientLevelContext.waitForChunksRender()][net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext#waitForChunksRender()]</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.Options#getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource) Music volume}</td>
///         <td>`0.0`</td>
///         <td>`1.0`</td>
///         <td>The game music is annoying while running gametests</td>
///     </tr>
/// </table>
/// ## World creation options
/// These adjusted defaults only apply if the world builder's
/// {@linkplain net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder#setUseConsistentSettings(boolean) consistent settings}
/// have not been set to `false`.
/// <table>
///     <tr>
///         <th>Setting name</th>
///         <th>Gametest default</th>
///         <th>Vanilla default</th>
///         <th>Reason</th>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setWorldType(net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.WorldTypeEntry) World type}</td>
///         <td>[FLAT][net.minecraft.world.level.levelgen.presets.WorldPresets#FLAT]</td>
///         <td>[DEFAULT][net.minecraft.world.level.levelgen.presets.WorldPresets#NORMAL]</td>
///         <td>Creates cleaner test cases</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setSeed(String) Seed}</td>
///         <td>`1`</td>
///         <td>Random value</td>
///         <td>Consistency of tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setGenerateStructures(boolean) Generate structures}</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Consistency of tests and creates cleaner tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.world.level.gamerules.GameRules#ADVANCE_TIME Do daylight cycle}</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Consistency of tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.world.level.gamerules.GameRules#ADVANCE_WEATHER Do weather cycle}</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Consistency of tests</td>
///     </tr>
///     <tr>
///         <td>{@linkplain net.minecraft.world.level.gamerules.GameRules#SPAWN_MOBS Do mob spawning}</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Consistency of tests</td>
///     </tr>
/// </table>
/// ## Dedicated server properties
/// <table>
///     <tr>
///         <th>Setting name</th>
///         <th>Gametest default</th>
///         <th>Vanilla default</th>
///         <th>Reason</th>
///     </tr>
///     <tr>
///         <td>`online-mode`</td>
///         <td>`false`</td>
///         <td>`true`</td>
///         <td>Allows the gametest client to connect to the dedicated server without being logged in to a Minecraft
///         account</td>
///     </tr>
///     <tr>
///         <td>`sync-chunk-writes`</td>
///         <td>`true` on Windows, `false` on other operating systems</td>
///         <td>`true`</td>
///         <td>Causes world saving and closing to be extremely slow (on the order of many seconds to minutes) on Unix
///         systems. The vanilla default is set correctly in singleplayer but not on dedicated servers.</td>
///     </tr>
///     <tr>
///         <td>`spawn-protection`</td>
///         <td>`0`</td>
///         <td>`16`</td>
///         <td>Spawn protection prevents non-opped players from modifying the world within a certain radius of the world
///         spawn point, a likely source of confusion when writing gametests</td>
///     </tr>
///     <tr>
///         <td>`max-players`</td>
///         <td>`1`</td>
///         <td>`20`</td>
///         <td>Stops other players from joining the server and interfering with the test</td>
///     </tr>
/// </table>
@ApiStatus.Experimental
@NullMarked
package net.fabricmc.fabric.api.client.gametest.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
