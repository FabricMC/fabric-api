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

package net.fabricmc.fabric.impl.transfer.transaction;

import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class TransactionManagerImpl {
	public static final ThreadLocal<TransactionManagerImpl> MANAGERS = ThreadLocal.withInitial(TransactionManagerImpl::new);

	private final Thread thread = Thread.currentThread();
	private final ArrayList<TransactionImpl> stack = new ArrayList<>();
	private final ArrayList<Transaction.OuterCloseCallback> outerCloseCallbacks = new ArrayList<>();
	private int currentDepth = -1;

	public boolean isOpen() {
		return currentDepth > -1;
	}

	public Transaction openOuter() {
		if (isOpen()) {
			throw new IllegalStateException("An outer transaction is already active on this thread.");
		}

		return open();
	}

	@Nullable
	public TransactionContext getCurrentUnsafe() {
		if (currentDepth == -1) {
			return null;
		} else if (stack.get(currentDepth).lifecycle == Transaction.Lifecycle.OPEN) {
			return stack.get(currentDepth);
		} else {
			throw new IllegalStateException("May not call getCurrentUnsafe() from a close callback.");
		}
	}

	/**
	 * Open a new transaction, outer or nested, without performing any state check.
	 */
	Transaction open() {
		currentDepth++;

		if (stack.size() == currentDepth) {
			stack.add(new TransactionImpl(currentDepth));
		}

		TransactionImpl current = stack.get(currentDepth);
		current.lifecycle = Transaction.Lifecycle.OPEN;
		return current;
	}

	void validateCurrentThread() {
		if (Thread.currentThread() != thread) {
			String errorMessage = String.format(
					"Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.",
					Thread.currentThread().getName(),
					thread.getName());
			throw new IllegalStateException(errorMessage);
		}
	}

	public Transaction.Lifecycle getLifecycle() {
		if (currentDepth == -1) {
			return Transaction.Lifecycle.NONE;
		} else {
			return stack.get(currentDepth).lifecycle;
		}
	}

	private class TransactionImpl implements Transaction {
		final int nestingDepth;
		final ArrayList<CloseCallback> closeCallbacks = new ArrayList<>();
		Lifecycle lifecycle = Lifecycle.NONE;

		TransactionImpl(int nestingDepth) {
			this.nestingDepth = nestingDepth;
		}

		void validateCurrentTransaction() {
			validateCurrentThread();

			if (currentDepth == -1 || stack.get(currentDepth) != this) {
				String errorMessage = String.format(
						"Transaction function was called on a transaction with depth %d, but the current transaction has depth %d.",
						nestingDepth,
						currentDepth);
				throw new IllegalStateException(errorMessage);
			}
		}

		// Validate that this transaction is open.
		private void validateOpen() {
			if (lifecycle != Lifecycle.OPEN) {
				throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
			}
		}

		@Override
		public Transaction openNested() {
			validateCurrentTransaction();
			validateOpen();
			return open();
		}

		private void close(Result result) {
			validateCurrentTransaction();
			validateOpen();
			// Block transaction operations
			lifecycle = Lifecycle.CLOSING;

			// Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
			// That is why any callback has to run inside a try block.
			RuntimeException closeException = null;

			// Invoke callbacks in reverse order
			for (int i = closeCallbacks.size()-1; i >= 0; i--) {
				try {
					closeCallbacks.get(i).onClose(this, result);
				} catch (Exception exception) {
					if (closeException == null) {
						closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
					} else {
						closeException.addSuppressed(exception);
					}
				}
			}

			closeCallbacks.clear();

			if (currentDepth == 0) {
				lifecycle = Lifecycle.OUTER_CLOSING;

				// Invoke outer close callbacks in reverse order
				for (int i = outerCloseCallbacks.size() - 1; i >= 0; i--) {
					try {
						outerCloseCallbacks.get(i).afterOuterClose(result);
					} catch (Exception exception) {
						if (closeException == null) {
							closeException = new RuntimeException("Encountered an exception while invoking a transaction outer close callback.", exception);
						} else {
							closeException.addSuppressed(exception);
						}
					}
				}

				outerCloseCallbacks.clear();
			}

			// Only this check will allow openOuter operations.
			currentDepth--;
			lifecycle = Lifecycle.NONE;

			// Throw exception if necessary
			if (closeException != null) {
				throw closeException;
			}
		}

		@Override
		public void abort() {
			close(Result.ABORTED);
		}

		@Override
		public void commit() {
			close(Result.COMMITTED);
		}

		@Override
		public void close() {
			if (isOpen() && lifecycle == Lifecycle.OPEN) { // check that a transaction is open on this thread and that this transaction is open.
				abort();
			}
		}

		@Override
		public int nestingDepth() {
			validateCurrentThread();
			return nestingDepth;
		}

		@Override
		public Transaction getOpenTransaction(int nestingDepth) {
			validateCurrentThread();

			if (nestingDepth < 0) {
				throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
			}

			if (nestingDepth > currentDepth) {
				throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);
			}

			TransactionImpl transaction = stack.get(nestingDepth);
			transaction.validateOpen();
			return transaction;
		}

		@Override
		public void addCloseCallback(CloseCallback closeCallback) {
			validateCurrentThread();
			validateOpen();
			closeCallbacks.add(closeCallback);
		}

		@Override
		public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
			validateCurrentThread();
			// Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
			// We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

			if (currentDepth == -1) {
				throw new IllegalStateException("There is no open transaction on this thread.");
			}

			outerCloseCallbacks.add(outerCloseCallback);
		}

		@Override
		public String toString() {
			return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(nestingDepth, lifecycle.name(), thread.getName());
		}
	}
}
