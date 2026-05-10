import React, { useEffect, useState, useCallback } from 'react';
import api from '../../services/api';
import '../../styles/AssetsPage.css';

const ASSET_TYPES = ['LAPTOP', 'MONITOR', 'ACCESSORY'];
const ASSET_STATUSES = ['AVAILABLE', 'ASSIGNED', 'UNDER_MAINTENANCE', 'DECOMMISSIONED'];

const EMPTY_FORM = {
  name: '',
  serialNumber: '',
  brand: '',
  type: 'LAPTOP',
  purchaseDate: '',
  warrantyExpiryDate: '',
  ram: '',
  storage: '',
  status: 'AVAILABLE',
};

function getUser() {
  try { return JSON.parse(localStorage.getItem('user')) || {}; } catch { return {}; }
}

function normalizeRole(role) {
  return String(role || '').replace(/^ROLE_/, '').toUpperCase();
}

function statusColor(status) {
  switch (status) {
    case 'AVAILABLE': return 'status-available';
    case 'ASSIGNED': return 'status-assigned';
    case 'UNDER_MAINTENANCE': return 'status-maintenance';
    case 'DECOMMISSIONED': return 'status-decommissioned';
    default: return '';
  }
}

function typeIcon(type) {
  switch (type) {
    case 'LAPTOP': return '💻';
    case 'MONITOR': return '🖥️';
    case 'ACCESSORY': return '🖱️';
    default: return '📦';
  }
}

export default function AssetsPage() {
  const user = getUser();
  const role = normalizeRole(user.role) || 'DEVELOPER';
  const canEdit = role === 'ADMIN' || role === 'MANAGER';

  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null); // null = create
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  // Search / filter
  const [search, setSearch] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // Detail view
  const [selected, setSelected] = useState(null);

  const fetchAssets = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/api/assets');
      setAssets(res.data || []);
    } catch (e) {
      setError(e.message || 'Failed to load assets');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAssets(); }, [fetchAssets]);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormError('');
    setShowModal(true);
  };

  const openEdit = (asset) => {
    setEditing(asset);
    setForm({
      name: asset.model || asset.name || '',
      serialNumber: asset.serialNumber || '',
      brand: asset.brand || '',
      type: asset.type || 'LAPTOP',
      purchaseDate: asset.purchaseDate || '',
      warrantyExpiryDate: asset.warrantyExpiryDate || '',
      ram: asset.ram || '',
      storage: asset.storage || '',
      status: asset.status || 'AVAILABLE',
    });
    setFormError('');
    setShowModal(true);
  };

  const closeModal = () => { setShowModal(false); setEditing(null); };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      const payload = {
        ...form,
        ram: form.ram ? Number(form.ram) : null,
        storage: form.storage ? Number(form.storage) : null,
      };
      if (editing) {
        await api.put(`/api/assets/${editing.id}`, payload);
      } else {
        await api.post('/api/assets', payload);
      }
      closeModal();
      fetchAssets();
    } catch (e) {
      setFormError(e.message || 'Failed to save asset');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this asset? This cannot be undone.')) return;
    try {
      await api.delete(`/api/assets/${id}`);
      setAssets((prev) => prev.filter((a) => a.id !== id));
      if (selected?.id === id) setSelected(null);
    } catch (e) {
      alert(e.message || 'Failed to delete asset');
    }
  };

  // Filtered assets
  const filtered = assets.filter((a) => {
    const q = search.toLowerCase();
    const matchSearch = !q || (
      a.name?.toLowerCase().includes(q) ||
      a.serialNumber?.toLowerCase().includes(q) ||
      a.brand?.toLowerCase().includes(q)
    );
    const matchType = !filterType || a.type === filterType;
    const matchStatus = !filterStatus || a.status === filterStatus;
    return matchSearch && matchType && matchStatus;
  });

  const now = new Date();
  const warningThreshold = 30 * 24 * 60 * 60 * 1000; // 30 days

  function warrantyStatus(asset) {
    if (!asset.warrantyExpiryDate) return null;
    const exp = new Date(asset.warrantyExpiryDate);
    const diff = exp - now;
    if (diff < 0) return 'expired';
    if (diff < warningThreshold) return 'expiring';
    return 'ok';
  }

  return (
    <div className="assets-page">
      {/* Header */}
      <div className="assets-header">
        <div>
          <h1 className="assets-title">Assets</h1>
          <p className="assets-subtitle">
            {assets.length} assets registered
          </p>
        </div>
        {canEdit && (
          <button className="btn-primary" onClick={openCreate}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Add Asset
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="assets-filters">
        <div className="search-wrap">
          <svg className="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            className="search-input"
            placeholder="Search by name, serial, brand…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select className="filter-select" value={filterType} onChange={(e) => setFilterType(e.target.value)}>
          <option value="">All types</option>
          {ASSET_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
        <select className="filter-select" value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
          <option value="">All statuses</option>
          {ASSET_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
        </select>
      </div>

      {/* Error */}
      {error && <div className="assets-error">{error}</div>}

      {/* Table */}
      {loading ? (
        <div className="assets-loading">Loading assets…</div>
      ) : (
        <div className="assets-table-wrap">
          <table className="assets-table">
            <thead>
              <tr>
                <th>Asset</th>
                <th>Serial</th>
                <th>Brand</th>
                <th>Type</th>
                <th>Status</th>
                <th>Warranty</th>
                <th>Assigned to</th>
                {canEdit && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={canEdit ? 8 : 7} className="assets-empty">
                    No assets found.
                  </td>
                </tr>
              ) : filtered.map((asset) => {
                const ws = warrantyStatus(asset);
                return (
                  <tr
                    key={asset.id}
                    className={selected?.id === asset.id ? 'row-selected' : ''}
                    onClick={() => setSelected(selected?.id === asset.id ? null : asset)}
                  >
                    <td>
                      <div className="asset-name-cell">
                        <span className="asset-type-icon">{typeIcon(asset.type)}</span>
                        <span className="asset-name">{asset.model || asset.name}</span>
                      </div>
                    </td>
                    <td className="mono">{asset.serialNumber}</td>
                    <td>{asset.brand || '—'}</td>
                    <td>{asset.type}</td>
                    <td>
                      <span className={`status-badge ${statusColor(asset.status)}`}>
                        {asset.status?.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td>
                      {asset.warrantyExpiryDate ? (
                        <span className={`warranty-badge warranty-${ws}`}>
                          {ws === 'expired' && '⚠ Expired'}
                          {ws === 'expiring' && '⚠ Expiring soon'}
                          {ws === 'ok' && asset.warrantyExpiryDate}
                        </span>
                      ) : '—'}
                    </td>
                    <td>{asset.lastAssignedTo || <span className="text-dim">Unassigned</span>}</td>
                    {canEdit && (
                      <td onClick={(e) => e.stopPropagation()}>
                        <div className="action-btns">
                          <button className="btn-icon btn-edit" onClick={() => openEdit(asset)} title="Edit asset">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                            <span>Edit</span>
                          </button>
                          <button className="btn-icon btn-delete" onClick={() => handleDelete(asset.id)} title="Delete asset">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                            </svg>
                            <span>Delete</span>
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Detail panel */}
      {selected && (
        <div className="asset-detail-panel">
          <div className="detail-header">
            <span className="detail-type-icon">{typeIcon(selected.type)}</span>
            <div>
              <h3 className="detail-title">{selected.model || selected.name}</h3>
              <span className="mono detail-serial">{selected.serialNumber}</span>
            </div>
            <button className="detail-close" onClick={() => setSelected(null)}>✕</button>
          </div>
          <div className="detail-grid">
            <div className="detail-item"><span className="detail-label">Brand</span><span>{selected.brand || '—'}</span></div>
            <div className="detail-item"><span className="detail-label">Type</span><span>{selected.type}</span></div>
            <div className="detail-item"><span className="detail-label">Status</span>
              <span className={`status-badge ${statusColor(selected.status)}`}>{selected.status?.replace(/_/g, ' ')}</span>
            </div>
            <div className="detail-item"><span className="detail-label">Assigned to</span><span>{selected.lastAssignedTo || 'Unassigned'}</span></div>
            <div className="detail-item"><span className="detail-label">Purchase date</span><span>{selected.purchaseDate || '—'}</span></div>
            <div className="detail-item"><span className="detail-label">Warranty expiry</span><span>{selected.warrantyExpiryDate || '—'}</span></div>
            {selected.ram && <div className="detail-item"><span className="detail-label">RAM</span><span>{selected.ram} GB</span></div>}
            {selected.storage && <div className="detail-item"><span className="detail-label">Storage</span><span>{selected.storage} GB</span></div>}
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editing ? 'Edit Asset' : 'Add New Asset'}</h2>
              <button className="modal-close" onClick={closeModal}>✕</button>
            </div>
            {formError && <div className="form-error">{formError}</div>}
            <form onSubmit={handleSave} className="asset-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Name *</label>
                  <input name="name" value={form.name} onChange={handleFormChange} required placeholder="e.g. MacBook Pro 14" />
                </div>
                <div className="form-group">
                  <label>Serial Number *</label>
                  <input name="serialNumber" value={form.serialNumber} onChange={handleFormChange} required placeholder="SN-XXXXXXX" />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Brand</label>
                  <input name="brand" value={form.brand} onChange={handleFormChange} placeholder="e.g. Apple" />
                </div>
                <div className="form-group">
                  <label>Type *</label>
                  <select name="type" value={form.type} onChange={handleFormChange} required>
                    {ASSET_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Purchase Date</label>
                  <input type="date" name="purchaseDate" value={form.purchaseDate} onChange={handleFormChange} />
                </div>
                <div className="form-group">
                  <label>Warranty Expiry</label>
                  <input type="date" name="warrantyExpiryDate" value={form.warrantyExpiryDate} onChange={handleFormChange} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>RAM (GB)</label>
                  <input type="number" name="ram" value={form.ram} onChange={handleFormChange} placeholder="16" min="0" />
                </div>
                <div className="form-group">
                  <label>Storage (GB)</label>
                  <input type="number" name="storage" value={form.storage} onChange={handleFormChange} placeholder="512" min="0" />
                </div>
              </div>
              <div className="form-group">
                <label>Status *</label>
                <select name="status" value={form.status} onChange={handleFormChange} required>
                  {ASSET_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={closeModal}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editing ? 'Save Changes' : 'Add Asset'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}