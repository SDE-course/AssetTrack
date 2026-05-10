const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

function getToken() {
  return localStorage.getItem('token');
}

async function request(path, options = {}) {
  const token = getToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  const text = await response.text();
  let payload = null;

  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  if (!response.ok) {
    const errorMessage = payload?.message || payload?.error || `Request failed with status ${response.status}`;
    throw new Error(errorMessage);
  }

  return payload;
}

export const getCurrentOwner = (assetId) => request(`/api/allocations/current-owner/${assetId}`);

export const getAllocationHistory = (assetId) => request(`/api/allocations/asset/${assetId}/history`);

export const assignAsset = (body) => request('/api/allocations/assign', {
  method: 'POST',
  body: JSON.stringify(body),
});

export const returnAsset = (allocationId) => request(`/api/allocations/${allocationId}/return`, {
  method: 'POST',
});

export const transferAsset = (body) => request('/api/allocations/transfer', {
  method: 'POST',
  body: JSON.stringify(body),
});