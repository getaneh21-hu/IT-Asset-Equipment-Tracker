
const API_BASE = 'http://localhost:8081/api';

export async function apiRequest(endpoint, options = {}, token = '') {
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const text = await response.text();

  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      typeof data === 'object' && data !== null
        ? data.message || data.error
        : data;

    throw new Error(
      message || `Request failed with status ${response.status}`
    );
  }

  return data;
}

export async function login(username, password) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export async function getAssets(token) {
  return apiRequest('/assets', {}, token);
}

export async function getCategories(token) {
  return apiRequest('/categories', {}, token);
}
 
//add,delete,and so

export async function createAsset(asset, token) {
  return apiRequest('/assets', {
    method: 'POST',
    body: JSON.stringify(asset),
  }, token);
}

export async function updateAsset(id, asset, token) {
  return apiRequest(`/assets/${id}`, {
    method: 'PUT',
    body: JSON.stringify(asset),
  }, token);
}

export async function deleteAsset(id, token) {
  return apiRequest(`/assets/${id}`, {
    method: 'DELETE',
  }, token);
}

export async function assignAsset(id, userId, token) {
  return apiRequest(`/assets/${id}/assign/${userId}`, {
    method: 'POST',
  }, token);
}

export async function returnAsset(id, token) {
  return apiRequest(`/assets/${id}/return`, {
    method: 'PATCH',
  }, token);
}

export async function updateAssetStatus(id, status, token) {
  return apiRequest(`/assets/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  }, token);
}
