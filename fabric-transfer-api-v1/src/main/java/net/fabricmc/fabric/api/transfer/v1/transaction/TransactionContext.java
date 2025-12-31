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

package net.fabricmc.fabric.api.transfer.v1.transaction;

import org.jetbrains.annotations.ApiStatus;

/**
 * A subset of a {@link Transaction} that lets participants properly take part in transactions, manage their state,
 * or open nested transactions, but does not allow them to close the transaction they are passed.
 */
@ApiStatus.NonExtendable
public interface TransactionContext {
	/**
	 * Open a new nested transaction.
	 *
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 * @throws IllegalStateException If this transaction is not the current transaction.
	 * @throws IllegalStateException If this transaction was closed.
	 */
	Transaction openNested();

	/**
	 * @return The nesting depth of this transaction: 0 if it was opened with {@link Transaction#openOuter},
	 * 1 if its parent was opened with {@link Transaction#openOuter}, and so on...
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 */
	int nestingDepth();

	/**
	 * Return the transaction with the specific nesting depth.
	 *
	 * @param nestingDepth Queried nesting depth.
	 * @throws IndexOutOfBoundsException If there is no open transaction with the request nesting depth.
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 */
	Transaction getOpenTransaction(int nestingDepth);

	/**
	 * Register a callback that will be invoked when this transaction is closed.
	 * Registered callbacks are invoked last-to-first: the last callback to be registered will be the first to be invoked, and so on...
	 *
	 * <p>Updates that may change the state of other participants should be deferred until after the outermost transaction is closed
	 * using {@link #addOuterCloseCallback}.
	 *
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 */
	void addCloseCallback(CloseCallback closeCallback);

	/**
	 * A callback that is invoked when a transaction is closed.
	 */
	@FunctionalInterface
	interface CloseCallback {
		/**
		 * Perform an action when a transaction is closed.
		 *
		 * @param transaction The closed transaction. Only {@link #nestingDepth}, {@link #getOpenTransaction} and {@link #addOuterCloseCallback}
		 *                    may be called on that transaction.
		 *                    {@link #addCloseCallback} may additionally be called on parent transactions
		 *                    (accessed through {@link #getOpenTransaction} for lower nesting depths).
		 * @param result The result of this transaction: whether it was committed or aborted.
		 */
		void onClose(TransactionContext transaction, Result result);
	}

	/**
	 * Register a callback that will be invoked after the outermost transaction is closed,
	 * and after callbacks registered with {@link #addCloseCallback} are ran.
	 * Registered callbacks are invoked last-to-first.
	 *
	 * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
	 */
	void addOuterCloseCallback(OuterCloseCallback outerCloseCallback);

	/**
	 * A callback that is invoked after the outer transaction is closed.
	 */
	@FunctionalInterface
	interface OuterCloseCallback {
		/**
		 * Perform an action after the top-level transaction is closed.
		 *
		 * @param result The result of the top-level transaction.
		 */
		void afterOuterClose(Result result);
	}

	/**
	 * The result of a transaction operation.
	 */
	enum Result {
		ABORTED,
		COMMITTED;

		/**
		 * @return {@code true} if the transaction was aborted, {@code false} if it was committed.
		 */
		public boolean wasAborted() {
			return this == ABORTED;
		}

		/**
		 * @return {@code true} if the transaction was committed, {@code false} if it was aborted.
		 */
		public boolean wasCommitted() {
			return this == COMMITTED;
		}
	}
}
