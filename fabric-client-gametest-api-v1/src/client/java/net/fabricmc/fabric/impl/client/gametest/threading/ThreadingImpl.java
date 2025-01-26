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

package net.fabricmc.fabric.impl.client.gametest.threading;

import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;

/**
 * <h1>Implementation notes</h1>
 *
 * <p>When a client test is running, ticks are run in a much more controlled way than in vanilla. A tick is split into 4
 * phases:
 * <ol>
 *     <li>{@linkplain #PHASE_TICK} - The client and server threads run a single tick in parallel, if they exist. The test thread waits.</li>
 *     <li>{@linkplain #PHASE_SERVER_TASKS} - The server runs its task queue, if the server exists. The other threads wait.</li>
 *     <li>{@linkplain #PHASE_CLIENT_TASKS} - The client runs its task queue, if the client exists. The other threads wait.</li>
 *     <li>{@linkplain #PHASE_TEST} - The test thread runs test code while the client and server threads wait for tasks to be handed off.</li>
 * </ol>
 *
 * <p>In {@code PHASE_TEST}, the client and server threads (if they exist) are blocked on semaphores waiting for tasks
 * to be handed to them from the test thread. When the test thread wants to send one of the other threads a task to run,
 * it sets {@linkplain #taskToRun} to the task runnable and releases the semaphore of the thread that should run the
 * task. It then blocks on its own semaphore until the task is complete, at which point the thread which completed the
 * task will release the test thread semaphore and re-block on its own semaphore again and the cycle continues. When the
 * test phase is over (i.e. when the test thread wants to wait a tick), the client and server semaphores will be
 * released while leaving {@linkplain #taskToRun} as {@code null}, which they will interpret to mean they are to
 * continue into {@linkplain #PHASE_TICK}.
 *
 * <p>The reason these phases were chosen are to make client-server communication as consistent as possible. The task
 * queues are when most packets are handled, and without them being run in sequence it would be unspecified whether a
 * packet would be handled on the current tick until the next one. The server task queue is before the client so that
 * changes on the server appear on the client more readily. The test phase is run after the task queues rather than at
 * the end of the physical tick (i.e. {@code MinecraftClient}'s and {@code MinecraftServer}'s {@code tick} methods), for
 * no particular reason other than to avoid needing a 5th phase, and having a power of 2 number of phases is convenient
 * when using {@linkplain Phaser}, as it doesn't break when the phase counter overflows.
 *
 * <p>Other challenges include that a client or server can be started during {@linkplain #PHASE_TEST} but haven't
 * reached their semaphore code yet meaning they are unable to accept tasks. This is solved by setting a flag to true
 * when the client/server is ready to accept tasks. Also the client will block on the integrated server starting and
 * stopping. This is solved by first deferring those operations until {@linkplain #PHASE_TICK} if they are being run
 * inside a test phase task (which is a minor difference from vanilla), and then ensuring the client is still running
 * the phase logic and is able to accept tasks while it is waiting for the server.
 */
public final class ThreadingImpl {
	private ThreadingImpl() {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	private static final String THREAD_IMPL_CLASS_NAME = ThreadingImpl.class.getName();
	private static final String TASK_ON_THIS_THREAD_METHOD_NAME = "runTaskOnThisThread";
	private static final String TASK_ON_OTHER_THREAD_METHOD_NAME = "runTaskOnOtherThread";

	public static final int PHASE_TICK = 0;
	public static final int PHASE_SERVER_TASKS = 1;
	public static final int PHASE_CLIENT_TASKS = 2;
	public static final int PHASE_TEST = 3;
	private static final int PHASE_MASK = 3;

	public static final Phaser PHASER = new Phaser();
	private static volatile boolean enablePhases = true;

	public static volatile boolean isClientRunning = false;
	public static volatile boolean clientCanAcceptTasks = false;
	public static final Semaphore CLIENT_SEMAPHORE = new Semaphore(0);

	public static volatile boolean isServerRunning = false;
	public static volatile boolean serverCanAcceptTasks = false;
	public static final Semaphore SERVER_SEMAPHORE = new Semaphore(0);

	@Nullable
	public static Thread testThread = null;
	public static final Semaphore TEST_SEMAPHORE = new Semaphore(0);
	@Nullable
	public static Throwable testFailureException = null;

	@Nullable
	public static Runnable taskToRun = null;

	private static volatile boolean gameCrashed = false;

	public static void enterPhase(int phase) {
		while (enablePhases && getNextPhase() != phase) {
			PHASER.arriveAndAwaitAdvance();
		}

		if (enablePhases) {
			PHASER.arriveAndAwaitAdvance();
		}
	}

	public static int getCurrentPhase() {
		return (getNextPhase() - 1) & PHASE_MASK;
	}

	private static int getNextPhase() {
		return PHASER.getPhase() & PHASE_MASK;
	}

	public static boolean isGameCrashed() {
		return gameCrashed;
	}

	public static void setGameCrashed() {
		enablePhases = false;
		gameCrashed = true;
	}

	public static void runTestThread(Runnable testRunner) {
		Preconditions.checkState(testThread == null, "There is already a test thread running");

		testThread = new Thread(() -> {
			PHASER.register();
			enterPhase(PHASE_TEST);

			try {
				testRunner.run();
			} catch (Throwable e) {
				testFailureException = e;
			} finally {
				if (clientCanAcceptTasks) {
					runOnClient(() -> MinecraftClient.getInstance().scheduleStop());
				}

				if (testFailureException != null) {
					// Log this now in case the client has stopped or is otherwise unable to rethrow our exception
					LOGGER.error("Client gametests failed with an exception", testFailureException);
				}

				deregisterTestThread();
			}
		});
		testThread.setName("Test thread");
		testThread.setDaemon(true);
		testThread.start();
	}

	private static void deregisterTestThread() {
		testThread = null;
		enablePhases = false;
		PHASER.arriveAndDeregister();

		if (clientCanAcceptTasks) {
			CLIENT_SEMAPHORE.release();
		}

		if (serverCanAcceptTasks) {
			SERVER_SEMAPHORE.release();
		}
	}

	public static void checkOnGametestThread(String methodName) {
		Preconditions.checkState(Thread.currentThread() == testThread, "%s can only be called from the client gametest thread", methodName);
	}

	public static <E extends Throwable> void runOnClient(FailableRunnable<E> action) throws E {
		Preconditions.checkNotNull(action, "action");
		checkOnGametestThread("runOnClient");
		Preconditions.checkState(clientCanAcceptTasks, "runOnClient called when no client is running");
		runTaskOnOtherThread(action, CLIENT_SEMAPHORE);
	}

	public static <E extends Throwable> void runOnServer(FailableRunnable<E> action) throws E {
		Preconditions.checkNotNull(action, "action");
		checkOnGametestThread("runOnServer");
		Preconditions.checkState(serverCanAcceptTasks, "runOnServer called when no server is running");
		runTaskOnOtherThread(action, SERVER_SEMAPHORE);
	}

	private static <E extends Throwable> void runTaskOnOtherThread(FailableRunnable<E> action, Semaphore clientOrServerSemaphore) throws E {
		MutableObject<E> thrown = new MutableObject<>();
		taskToRun = () -> runTaskOnThisThread(action, thrown);

		clientOrServerSemaphore.release();

		try {
			TEST_SEMAPHORE.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (thrown.getValue() != null) {
			joinAsyncStackTrace(thrown.getValue());
			throw thrown.getValue();
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void runTaskOnThisThread(FailableRunnable<E> action, MutableObject<E> thrown) {
		try {
			action.run();
		} catch (Throwable e) {
			thrown.setValue((E) e);
		} finally {
			taskToRun = null;
			TEST_SEMAPHORE.release();
		}
	}

	private static void joinAsyncStackTrace(Throwable e) {
		if (TestSystemProperties.DISABLE_JOIN_ASYNC_STACK_TRACES) {
			return;
		}

		// find the end of the relevant part of the stack trace on the other thread
		StackTraceElement[] otherThreadStackTrace = e.getStackTrace();

		if (otherThreadStackTrace == null) {
			return;
		}

		int otherThreadIndex = otherThreadStackTrace.length - 1;

		for (; otherThreadIndex >= 0; otherThreadIndex--) {
			StackTraceElement element = otherThreadStackTrace[otherThreadIndex];

			if (THREAD_IMPL_CLASS_NAME.equals(element.getClassName()) && TASK_ON_THIS_THREAD_METHOD_NAME.equals(element.getMethodName())) {
				break;
			}
		}

		if (otherThreadIndex == -1) {
			// couldn't find stack trace element
			return;
		}

		// find the start of the relevant part of the stack trace on the test thread
		StackTraceElement[] thisThreadStackTrace = Thread.currentThread().getStackTrace();
		int thisThreadIndex = 0;

		for (; thisThreadIndex < thisThreadStackTrace.length; thisThreadIndex++) {
			StackTraceElement element = thisThreadStackTrace[thisThreadIndex];

			if (THREAD_IMPL_CLASS_NAME.equals(element.getClassName()) && TASK_ON_OTHER_THREAD_METHOD_NAME.equals(element.getMethodName())) {
				break;
			}
		}

		if (thisThreadIndex == thisThreadStackTrace.length) {
			// couldn't find stack trace element
			return;
		}

		// join the stack traces
		StackTraceElement[] joinedStackTrace = new StackTraceElement[(otherThreadIndex + 1) + 1 + (thisThreadStackTrace.length - thisThreadIndex)];
		System.arraycopy(otherThreadStackTrace, 0, joinedStackTrace, 0, otherThreadIndex + 1);
		joinedStackTrace[otherThreadIndex + 1] = new StackTraceElement("Async Stack Trace", ".", null, 1);
		System.arraycopy(thisThreadStackTrace, thisThreadIndex, joinedStackTrace, otherThreadIndex + 2, thisThreadStackTrace.length - thisThreadIndex);
		e.setStackTrace(joinedStackTrace);
	}

	public static void runTick() {
		checkOnGametestThread("runTick");

		if (clientCanAcceptTasks) {
			CLIENT_SEMAPHORE.release();
		}

		if (serverCanAcceptTasks) {
			SERVER_SEMAPHORE.release();
		}

		enterPhase(PHASE_TEST);

		// Check if the game has crashed during this tick. If so, don't do any more work in the test
		if (gameCrashed) {
			deregisterTestThread();

			try {
				// wait until game is closed
				new Semaphore(0).acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
