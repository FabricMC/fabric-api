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

package net.fabricmc.fabric.api.transfer.v1.transaction.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * A base participant implementation that modifies itself during transactions,
 * saving snapshots of its state in objects of type {@code T} in case it needs to revert to a previous state.
 *
 * <h2>How to use from subclasses</h2>
 * <ul>
 *     <li>Call {@link #updateSnapshots} right before the state of your subclass is modified in a transaction.</li>
 *     <li>Override {@link #createSnapshot}: it is called when necessary to create an object representing the state of your subclass.</li>
 *     <li>Override {@link #readSnapshot}: it is called when necessary to revert to a previous state of your subclass.</li>
 *     <li>You may optionally override {@link #onFinalCommit}: it is called at the of a transaction that modified the state.
 *     For example, it could contain a call to {@code setChanged()}.</li>
 *     <li>(Advanced!) You may optionally override {@link #releaseSnapshot}: it is called once a snapshot object will not be used,
 *     for example you may wish to pool expensive state objects.</li>
 * </ul>
 *
 * <h3>More technical explanation</h3>
 *
 * <p>{@link #updateSnapshots} should be called before any modification.
 * This will save the state of this participant using {@link #createSnapshot} if no state was already saved for that transaction.
 * When the transaction is aborted and changes need to be rolled back, {@link #readSnapshot} will be called
 * to signal that the current state should revert to that of the snapshot.
 * The snapshot object is then {@linkplain #releaseSnapshot released}, and can be cached for subsequent use, or discarded.
 *
 * <p>When an outer transaction is committed, {@link #readSnapshot} will not be called so that the current state of this participant
 * is retained. {@link #releaseSnapshot} will be called because the snapshot is not necessary anymore,
 * and {@link #onFinalCommit} will be called after the transaction is closed.
 *
 * @param <T> The objects that this participant uses to save its state snapshots.
 */
public abstract class SnapshotParticipant<T> implements Transaction.CloseCallback, Transaction.OuterCloseCallback {
	private final List<T> snapshots = new ArrayList<>();

	/**
	 * Return a new <b>nonnull</b> object containing the current state of this participant.
	 * <b>{@code null} may not be returned, or an exception will be thrown!</b>
	 */
	protected abstract T createSnapshot();

	/**
	 * Roll back to a state previously created by {@link #createSnapshot}.
	 */
	protected abstract void readSnapshot(T snapshot);

	/**
	 * Signals that the snapshot will not be used anymore, and is safe to cache for next calls to {@link #createSnapshot},
	 * or discard entirely.
	 */
	protected void releaseSnapshot(T snapshot) {
	}

	/**
	 * Called after an outer transaction succeeded,
	 * to perform irreversible actions such as {@code setChanged()} or neighbor updates.
	 */
	protected void onFinalCommit() {
	}

	/**
	 * Update the stored snapshots so that the changes happening as part of the passed transaction can be correctly
	 * committed or rolled back.
	 * This function should be called every time the participant is about to change its internal state as part of a transaction.
	 */
	public void updateSnapshots(TransactionContext transaction) {
		// Make sure we have enough storage for snapshots
		while (snapshots.size() <= transaction.nestingDepth()) {
			snapshots.add(null);
		}

		// If the snapshot is null, we need to create it, and we need to register a callback.
		if (snapshots.get(transaction.nestingDepth()) == null) {
			T snapshot = createSnapshot();
			Objects.requireNonNull(snapshot, "Snapshot may not be null!");

			snapshots.set(transaction.nestingDepth(), snapshot);
			transaction.addCloseCallback(this);
		}
	}

	@Override
	public void onClose(TransactionContext transaction, Transaction.Result result) {
		// Get and remove the relevant snapshot.
		T snapshot = snapshots.set(transaction.nestingDepth(), null);

		if (result.wasAborted()) {
			// If the transaction was aborted, we just revert to the state of the snapshot.
			readSnapshot(snapshot);
			releaseSnapshot(snapshot);
		} else if (transaction.nestingDepth() > 0) {
			if (snapshots.get(transaction.nestingDepth() - 1) == null) {
				// No snapshot yet, so move the snapshot one nesting level up.
				snapshots.set(transaction.nestingDepth() - 1, snapshot);
				// This is the first snapshot at this level: we need to call addCloseCallback.
				transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
			} else {
				// There is already an older snapshot at the nesting level above, just release the newer one.
				releaseSnapshot(snapshot);
			}
		} else {
			releaseSnapshot(snapshot);
			transaction.addOuterCloseCallback(this);
		}
	}

	@Override
	public void afterOuterClose(Transaction.Result result) {
		// The result is guaranteed to be COMMITTED,
		// as this is only scheduled during onClose() when the outer transaction is successful.
		onFinalCommit();
	}
}
