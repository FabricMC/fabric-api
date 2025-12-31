/// Provides support for client gametests. To register a client gametest, add an entry to the
/// `fabric-client-gametest` entrypoint in your `fabric.mod.json`. Your gametest class should implement
/// [FabricClientGameTest][net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest].
///
/// Loom provides an API to configure client gametests in your `build.gradle`. It is recommended to run
/// gametests from a separate source set and test mod:
///
/// ```gradle
/// fabricApi.configureTests{createSourceSet = truemodId = 'your-gametest-mod-id'}
/// ```
///
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
///
/// | Setting name | Gametest default | Vanilla default | Reason |
/// |--------------|------------------|-----------------|--------|
/// | {@linkplain net.minecraft.client.Options#tutorialStep Tutorial step} | [NONE][net.minecraft.client.tutorial.TutorialSteps#NONE] | [MOVEMENT][net.minecraft.client.tutorial.TutorialSteps#MOVEMENT] | Consistency of tests |
/// | {@linkplain net.minecraft.client.Options#cloudStatus() Cloud status} | [OFF][net.minecraft.client.CloudStatus#OFF] | [FANCY][net.minecraft.client.CloudStatus#FANCY] | Consistency of tests |
/// | {@linkplain net.minecraft.client.Options#onboardAccessibility Onboard accessibility} | `false` | `true` | Would cause the game test runner to have to click through the onboard accessibility prompt |
/// | {@linkplain net.minecraft.client.Options#renderDistance() Render distance} | `5` | `10` | Speeds up loading of chunks, especially for functions such as [TestClientLevelContext.waitForChunksRender()][net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext#waitForChunksRender()] |
/// | {@linkplain net.minecraft.client.Options#getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource) Music volume} | `0.0` | `1.0` | The game music is annoying while running gametests |
/// ## World creation options
/// These adjusted defaults only apply if the world builder's
/// {@linkplain net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder#setUseConsistentSettings(boolean) consistent settings}
/// have not been set to `false`.
///
/// | Setting name | Gametest default | Vanilla default | Reason |
/// |--------------|------------------|-----------------|--------|
/// | {@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setWorldType(net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.WorldTypeEntry) World type} | [FLAT][net.minecraft.world.level.levelgen.presets.WorldPresets#FLAT] | [DEFAULT][net.minecraft.world.level.levelgen.presets.WorldPresets#NORMAL] | Creates cleaner test cases |
/// | {@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setSeed(String) Seed} | `1` | Random value | Consistency of tests |
/// | {@linkplain net.minecraft.client.gui.screens.worldselection.WorldCreationUiState#setGenerateStructures(boolean) Generate structures} | `false` | `true` | Consistency of tests and creates cleaner tests |
/// | {@linkplain net.minecraft.world.level.gamerules.GameRules#ADVANCE_TIME Do daylight cycle} | `false` | `true` | Consistency of tests |
/// | {@linkplain net.minecraft.world.level.gamerules.GameRules#ADVANCE_WEATHER Do weather cycle} | `false` | `true` | Consistency of tests |
/// | {@linkplain net.minecraft.world.level.gamerules.GameRules#SPAWN_MOBS Do mob spawning} | `false` | `true` | Consistency of tests |
/// ## Dedicated server properties
///
/// | Setting name | Gametest default | Vanilla default | Reason |
/// |--------------|------------------|-----------------|--------|
/// | `online-mode` | `false` | `true` | Allows the gametest client to connect to the dedicated server without being logged in to a Minecraft account |
/// | `sync-chunk-writes` | `true` on Windows, `false` on other operating systems | `true` | Causes world saving and closing to be extremely slow (on the order of many seconds to minutes) on Unix systems. The vanilla default is set correctly in singleplayer but not on dedicated servers. |
/// | `spawn-protection` | `0` | `16` | Spawn protection prevents non-opped players from modifying the world within a certain radius of the world spawn point, a likely source of confusion when writing gametests |
/// | `max-players` | `1` | `20` | Stops other players from joining the server and interfering with the test |
@ApiStatus.Experimental
@NullMarked
package net.fabricmc.fabric.api.client.gametest.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
