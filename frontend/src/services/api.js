// JWT-aware API utility
const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

function getToken() {
  return localStorage.getItem('token');
}

function buildUrl(path, params) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const base = normalizedPath.startsWith('/api/')
    ? `${API_BASE_URL}${normalizedPath}`
    : `${API_BASE_URL}/api${normalizedPath}`;

  // Append query params if provided
  if (params && typeof params === 'object') {
    const query = Object.entries(params)
      // eslint-disable-next-line no-unused-vars
      .filter(([_, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&');
    if (query) return `${base}?${query}`;
  }

  return base;
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/**
 * @param {string} method
 * @param {string} path
 * @param {object|undefined} data - request body for POST/PUT/PATCH
 * @param {object|undefined} params - query string params for GET
 */
async function request(method, path, data, params) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(buildUrl(path, params), {
    method,
    headers,
    body: data !== undefined ? JSON.stringify(data) : undefined,
  });

  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    return;
  }

  const payload = await parseResponse(response);

  if (!response.ok) {
    const message = payload?.message || payload?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return {
    data: payload,
    status: response.status,
  };
}

const api = {
  // Supports: api.get('/api/assets', { params: { page: 0, size: 10 } })
  get: (path, options) => request('GET', path, undefined, options?.params),
  post: (path, data) => request('POST', path, data),
  put: (path, data) => request('PUT', path, data),
  patch: (path, data) => request('PATCH', path, data),
  delete: (path) => request('DELETE', path),
};

export default api;
