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
 * classes to run code on the correct thread. The game remains paused unless you explicitly unpause it using various
 * waiting functions such as
 * {@link net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext#waitTick() ClientGameTestContext.waitTick()}.
 *
 * <p>A few changes have been made to how the vanilla game threads run, to make tests more reproducible. Notably, there
 * is exactly one server tick per client tick while a server is running (singleplayer or multiplayer). On singleplayer,
 * packets will always arrive on a consistent tick.
 */
@ApiStatus.Experimental
package net.fabricmc.fabric.api.client.gametest.v1;

import org.jetbrains.annotations.ApiStatus;
