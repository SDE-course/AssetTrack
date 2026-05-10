// JWT-aware API utility
const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

function getToken() {
  return localStorage.getItem('token');
}

function buildUrl(path) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;

  if (normalizedPath.startsWith('/api/')) {
    return `${API_BASE_URL}${normalizedPath}`;
  }

  return `${API_BASE_URL}/api${normalizedPath}`;
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function request(method, path, data) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(buildUrl(path), {
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
  get: (path) => request('GET', path),
  post: (path, data) => request('POST', path, data),
  put: (path, data) => request('PUT', path, data),
  patch: (path, data) => request('PATCH', path, data),
  delete: (path) => request('DELETE', path),
};

export default api;