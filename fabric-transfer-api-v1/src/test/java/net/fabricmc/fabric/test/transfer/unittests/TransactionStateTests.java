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

package net.fabricmc.fabric.test.transfer.unittests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.test.transfer.TestUtil;

class TransactionStateTests extends AbstractTransferApiTest {
	private int callbacksInvoked = 0;

	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	/// Make sure that transaction global state stays valid in case of exceptions.
	@Test
	public void testTransactionExceptions() {
		// Test exception inside the try.
		ensureException(() -> {
			try (Transaction tx = Transaction.openOuter()) {
				tx.addCloseCallback((t, result) -> {
					callbacksInvoked++; throw new RuntimeException("Close.");
				});
				throw new RuntimeException("Inside try.");
			}
		}, "Exception should have propagated through the transaction.");
		if (callbacksInvoked != 1) throw new AssertionError("Callback should have been invoked.");

		// Test exception inside the close.
		callbacksInvoked = 0;
		ensureException(() -> {
			try (Transaction tx = Transaction.openOuter()) {
				tx.addCloseCallback((t, result) -> {
					callbacksInvoked++; throw new RuntimeException("Close 1.");
				});
				tx.addCloseCallback((t, result) -> {
					callbacksInvoked++; throw new RuntimeException("Close 2.");
				});
				tx.addOuterCloseCallback(result -> {
					callbacksInvoked++; throw new RuntimeException("Outer close 1.");
				});
				tx.addOuterCloseCallback(result -> {
					callbacksInvoked++; throw new RuntimeException("Outer close 2.");
				});
			}
		}, "Exceptions in close callbacks should be propagated through the transaction.");
		if (callbacksInvoked != 4) throw new AssertionError("All 4 callbacks should have been invoked, only so many were: " + callbacksInvoked);

		// Test getCurrentUnsafe.
		if (Transaction.getCurrentUnsafe() != null) throw new AssertionError("Should have returned null.");

		try (Transaction tx = Transaction.openOuter()) {
			if (Transaction.getCurrentUnsafe() != tx) throw new AssertionError("Should have returned the current transaction.");

			tx.addCloseCallback(((transaction, result) -> {
				ensureException(Transaction::getCurrentUnsafe, "Should have thrown an exception in the close callback.");
			}));

			tx.addOuterCloseCallback((result) -> {
				ensureException(Transaction::getCurrentUnsafe, "Should have thrown an exception in the outer close callback.");
			});
		}

		// Test that transaction state is still OK after these exceptions.
		try (Transaction tx = Transaction.openOuter()) {
			tx.commit();
		}
	}

	private static void ensureException(Runnable runnable, String message) {
		boolean failed = false;

		try {
			runnable.run();
		} catch (Throwable t) {
			failed = true;
		}

		if (!failed) {
			throw new AssertionError(message);
		}
	}

	@Test
	public void testTransactionLifecycle() {
		TestUtil.assertEquals(Transaction.Lifecycle.NONE, Transaction.getLifecycle());

		try (Transaction transaction = Transaction.openOuter()) {
			TestUtil.assertEquals(Transaction.Lifecycle.OPEN, Transaction.getLifecycle());

			transaction.addCloseCallback((tx, result) -> {
				TestUtil.assertEquals(Transaction.Lifecycle.CLOSING, Transaction.getLifecycle());
			});

			transaction.addOuterCloseCallback(result -> {
				TestUtil.assertEquals(Transaction.Lifecycle.OUTER_CLOSING, Transaction.getLifecycle());
			});
		}

		TestUtil.assertEquals(Transaction.Lifecycle.NONE, Transaction.getLifecycle());
	}
}
