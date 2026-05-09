import { useEffect, useMemo, useState } from 'react';
import { assignAsset, getAllocationHistory, getCurrentOwner, returnAsset, transferAsset } from '../../services/allocationService';
import './allocation.css';

const emptyAssignForm = {
  userId: '',
  notes: '',
};

const emptyTransferForm = {
  allocationId: '',
  newUserId: '',
  notes: '',
};

const formatDateTime = (value) => {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
};

export default function AssetAllocationConsole() {
  const [assetId, setAssetId] = useState('');
  const [assignForm, setAssignForm] = useState(emptyAssignForm);
  const [transferForm, setTransferForm] = useState(emptyTransferForm);
  const [currentOwner, setCurrentOwner] = useState(null);
  const [history, setHistory] = useState([]);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [loadingAction, setLoadingAction] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const isActive = Boolean(currentOwner?.active);
  const currentAllocationId = currentOwner?.id ?? currentOwner?.allocationId ?? null;

  useEffect(() => {
    if (currentAllocationId && !transferForm.allocationId) {
      setTransferForm((prev) => ({ ...prev, allocationId: String(currentAllocationId) }));
    }
  }, [currentAllocationId, transferForm.allocationId]);

  const summaryStatus = useMemo(() => {
    if (!currentOwner) {
      return 'No asset loaded';
    }

    if (currentOwner.message) {
      return 'Inactive / available';
    }

    return currentOwner.active ? 'Active allocation' : 'Inactive / available';
  }, [currentOwner]);

  const assetDisplayName = currentOwner?.assetName || 'No asset loaded yet';
  const assetSerialNumber = currentOwner?.serialNumber || '-';
  const currentOwnerName = currentOwner?.assignedToName || currentOwner?.message || '-';
  const allocationCountLabel = `${history.length} record${history.length === 1 ? '' : 's'}`;

  const refreshAssetData = async (id) => {
    const targetAssetId = id ?? assetId;
    if (!targetAssetId) {
      setError('Enter an asset ID first.');
      return;
    }

    setLoadingDetails(true);
    setError('');
    setMessage('');

    try {
      const [ownerResponse, historyResponse] = await Promise.all([
        getCurrentOwner(targetAssetId),
        getAllocationHistory(targetAssetId),
      ]);

      setCurrentOwner(ownerResponse);
      setHistory(historyResponse);
      setTransferForm((prev) => ({
        ...prev,
        allocationId: ownerResponse?.id ? String(ownerResponse.id) : '',
      }));
      setMessage('Allocation data loaded.');
    } catch (requestError) {
      setCurrentOwner(null);
      setHistory([]);
      setError(requestError.message || 'Failed to load allocation details.');
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleLookup = async (event) => {
    event.preventDefault();
    await refreshAssetData();
  };

  const handleAssign = async (event) => {
    event.preventDefault();
    setLoadingAction('assign');
    setError('');
    setMessage('');

    try {
      await assignAsset({
        assetId: Number(assetId),
        userId: Number(assignForm.userId),
        notes: assignForm.notes,
      });
      setAssignForm(emptyAssignForm);
      setMessage('Asset assigned successfully.');
      await refreshAssetData();
    } catch (requestError) {
      setError(requestError.message || 'Failed to assign the asset.');
    } finally {
      setLoadingAction('');
    }
  };

  const handleReturn = async () => {
    if (!currentAllocationId) {
      setError('Load an active allocation before returning the asset.');
      return;
    }

    setLoadingAction('return');
    setError('');
    setMessage('');

    try {
      await returnAsset(currentAllocationId);
      setMessage('Asset returned successfully.');
      await refreshAssetData();
    } catch (requestError) {
      setError(requestError.message || 'Failed to return the asset.');
    } finally {
      setLoadingAction('');
    }
  };

  const handleTransfer = async (event) => {
    event.preventDefault();
    setLoadingAction('transfer');
    setError('');
    setMessage('');

    try {
      await transferAsset({
        allocationId: Number(transferForm.allocationId),
        newUserId: Number(transferForm.newUserId),
        notes: transferForm.notes,
      });
      setTransferForm((prev) => ({
        ...emptyTransferForm,
        allocationId: prev.allocationId,
      }));
      setMessage('Asset transferred successfully.');
      await refreshAssetData();
    } catch (requestError) {
      setError(requestError.message || 'Failed to transfer the asset.');
    } finally {
      setLoadingAction('');
    }
  };

  return (
    <main className="app-shell">
      <section className="hero-shell">
        <div className="hero-copy-block">
          <p className="eyebrow">Asset Allocation Module</p>
          <h1>Asset allocation workspace</h1>
          <p className="hero-copy">
            Search an asset, review its current owner, then assign, return, or transfer it from one
            structured workspace.
          </p>

          <div className="hero-badges">
            <span className={`status-pill ${isActive ? 'active' : 'inactive'}`}>
              {summaryStatus}
            </span>
            <span className="info-pill">Asset ID: {assetId || '—'}</span>
            <span className="info-pill">History: {allocationCountLabel}</span>
          </div>
        </div>

        <form className="lookup-card" onSubmit={handleLookup}>
          <div className="card-header">
            <div>
              <p className="card-eyebrow">Load asset</p>
              <h2>View allocation state</h2>
            </div>
            <span className="card-subtle">Lookup current owner and history</span>
          </div>

          <label className="field-label" htmlFor="assetId">Asset ID</label>
          <div className="lookup-row">
            <input
              id="assetId"
              type="number"
              min="1"
              value={assetId}
              onChange={(event) => setAssetId(event.target.value)}
              placeholder="Enter asset ID"
            />
            <button type="submit" disabled={loadingDetails}>
              {loadingDetails ? 'Loading...' : 'Load Asset'}
            </button>
          </div>
        </form>
      </section>

      {(message || error) && (
        <section className="alerts">
          {message && <div className="alert success">{message}</div>}
          {error && <div className="alert error">{error}</div>}
        </section>
      )}

      <section className="content-grid">
        <article className="panel summary-panel featured-panel">
          <div className="panel-heading">
            <div>
              <p className="panel-label">Asset details</p>
              <h2>Current allocation overview</h2>
            </div>
            <span className={`status-pill ${isActive ? 'active' : 'inactive'}`}>
              {summaryStatus}
            </span>
          </div>

          <div className="featured-summary">
            <div className="featured-card primary">
              <span className="featured-label">Asset name</span>
              <strong>{assetDisplayName}</strong>
            </div>
            <div className="featured-card">
              <span className="featured-label">Serial number</span>
              <strong>{assetSerialNumber}</strong>
            </div>
            <div className="featured-card">
              <span className="featured-label">Current owner</span>
              <strong>{currentOwnerName}</strong>
            </div>
          </div>

          <dl className="details-grid compact-grid">
            <div>
              <dt>Assigned date</dt>
              <dd>{formatDateTime(currentOwner?.assignedDate)}</dd>
            </div>
            <div>
              <dt>Returned date</dt>
              <dd>{formatDateTime(currentOwner?.returnedDate)}</dd>
            </div>
            <div className="full-width">
              <dt>Notes</dt>
              <dd>{currentOwner?.notes || 'No notes recorded for this allocation.'}</dd>
            </div>
          </dl>
        </article>

        <aside className="stack-column">
          <article className="panel action-panel">
            <div className="panel-heading">
              <div>
                <p className="panel-label">Assign asset</p>
                <h2>Give an asset to a user</h2>
              </div>
            </div>

            <form className="stacked-form" onSubmit={handleAssign}>
              <label>
                <span className="field-label">User ID</span>
                <input
                  type="number"
                  min="1"
                  value={assignForm.userId}
                  onChange={(event) => setAssignForm((prev) => ({ ...prev, userId: event.target.value }))}
                  placeholder="User ID"
                  required
                />
              </label>
              <label>
                <span className="field-label">Notes</span>
                <textarea
                  value={assignForm.notes}
                  onChange={(event) => setAssignForm((prev) => ({ ...prev, notes: event.target.value }))}
                  placeholder="Optional assignment notes"
                  rows="4"
                />
              </label>
              <button type="submit" disabled={loadingAction === 'assign' || !assetId}>
                {loadingAction === 'assign' ? 'Assigning...' : 'Assign Asset'}
              </button>
            </form>
          </article>

          <article className="panel action-panel muted-panel">
            <div className="panel-heading">
              <div>
                <p className="panel-label">Return asset</p>
                <h2>Close the active allocation</h2>
              </div>
            </div>

            <p className="panel-copy">
              Return is available only when the loaded asset has an active allocation.
            </p>

            {isActive ? (
              <button
                type="button"
                className="secondary-button"
                onClick={handleReturn}
                disabled={loadingAction === 'return'}
              >
                {loadingAction === 'return' ? 'Returning...' : 'Return Asset'}
              </button>
            ) : (
              <div className="empty-state">Load an active asset to enable the return action.</div>
            )}
          </article>

          <article className="panel action-panel">
            <div className="panel-heading">
              <div>
                <p className="panel-label">Transfer asset</p>
                <h2>Move the active allocation</h2>
              </div>
            </div>

            <form className="stacked-form" onSubmit={handleTransfer}>
              <label>
                <span className="field-label">Allocation ID</span>
                <input
                  type="number"
                  min="1"
                  value={transferForm.allocationId}
                  onChange={(event) => setTransferForm((prev) => ({ ...prev, allocationId: event.target.value }))}
                  placeholder="Active allocation ID"
                  required
                />
              </label>
              <label>
                <span className="field-label">New user ID</span>
                <input
                  type="number"
                  min="1"
                  value={transferForm.newUserId}
                  onChange={(event) => setTransferForm((prev) => ({ ...prev, newUserId: event.target.value }))}
                  placeholder="New user ID"
                  required
                />
              </label>
              <label>
                <span className="field-label">Notes</span>
                <textarea
                  value={transferForm.notes}
                  onChange={(event) => setTransferForm((prev) => ({ ...prev, notes: event.target.value }))}
                  placeholder="Optional transfer notes"
                  rows="4"
                />
              </label>
              <button type="submit" disabled={!isActive || loadingAction === 'transfer'}>
                {loadingAction === 'transfer' ? 'Transferring...' : 'Transfer Asset'}
              </button>
            </form>
          </article>
        </aside>
      </section>

      <section className="panel history-panel">
        <div className="panel-heading history-heading">
          <div>
            <p className="panel-label">Allocation history</p>
            <h2>All allocation records for the selected asset</h2>
          </div>
          <span className="history-count">{allocationCountLabel}</span>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Assigned by</th>
                <th>Assigned date</th>
                <th>Returned date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {history.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-state">
                    Load an asset to view its allocation history.
                  </td>
                </tr>
              ) : (
                history.map((record) => (
                  <tr key={record.allocationId}>
                    <td>{record.user || '-'}</td>
                    <td>{record.assignedBy || '-'}</td>
                    <td>{formatDateTime(record.assignedDate)}</td>
                    <td>{formatDateTime(record.returnedDate)}</td>
                    <td>
                      <span className={`status-pill ${record.active ? 'active' : 'inactive'}`}>
                        {record.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}