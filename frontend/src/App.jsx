
import { useEffect, useState } from 'react';
import './App.css';

import {
  login,
  getAssets,
  getCategories,
  createAsset,
  updateAsset,
  deleteAsset,
  assignAsset,
  returnAsset,
  updateAssetStatus,
} from './api/api.js';

import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';

function App() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const [token, setToken] = useState(
    () => localStorage.getItem('token') || ''
  );

  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [dataLoading, setDataLoading] = useState(false);

  const [assets, setAssets] = useState([]);
  const [categories, setCategories] = useState([]);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  async function loadDashboard(savedToken) {
    setDataLoading(true);

    try {
      const [assetsData, categoriesData] = await Promise.all([
        getAssets(savedToken),
        getCategories(savedToken),
      ]);

      setAssets(Array.isArray(assetsData) ? assetsData : []);

      setCategories(
        Array.isArray(categoriesData) ? categoriesData : []
      );
    } catch (error) {
      const errorMessage = error.message || '';

      if (
        errorMessage.includes('401') ||
        errorMessage.includes('403') ||
        errorMessage.toLowerCase().includes('unauthorized')
      ) {
        handleLogout();

        setMessage(
          'Your session has expired or you are not authorized. Please sign in again.'
        );
      } else {
        setMessage(
          errorMessage.includes('Failed to fetch')
            ? 'Cannot connect to the backend. Check that it is running and CORS is configured.'
            : errorMessage
        );
      }
    } finally {
      setDataLoading(false);
    }
  }

  useEffect(() => {
    const savedToken = localStorage.getItem('token');

    if (savedToken) {
      loadDashboard(savedToken);
    }
  }, []);

  async function handleLogin(event) {
    event.preventDefault();

    setMessage('');
    setLoading(true);

    try {
      const data = await login(username, password);

      if (!data?.token) {
        throw new Error(
          'Login succeeded, but no token was returned.'
        );
      }

      localStorage.setItem('token', data.token);

      setToken(data.token);
      setPassword('');

      await loadDashboard(data.token);
    } catch (error) {
      setMessage(
        error.message?.includes('Failed to fetch')
          ? 'Cannot connect to the backend. Check the backend and CORS settings.'
          : error.message || 'Login failed. Check your credentials.'
      );
    } finally {
      setLoading(false);
    }
  }

  function handleLogout() {
    localStorage.removeItem('token');

    setToken('');
    setAssets([]);
    setCategories([]);
    setMessage('');
    setUsername('');
    setPassword('');
    setSearchTerm('');
    setStatusFilter('ALL');
  }

  async function performAssetAction(action, successMessage) {
    setMessage('');

    try {
      await action();
      await loadDashboard(token);

      setMessage(successMessage);

      return true;
    } catch (error) {
      setMessage(
        error.message || 'The asset operation failed.'
      );

      return false;
    }
  }

  async function handleCreateAsset(asset) {
    return performAssetAction(
      () => createAsset(asset, token),
      'Asset created successfully.'
    );
  }

  async function handleUpdateAsset(id, asset) {
    return performAssetAction(
      () => updateAsset(id, asset, token),
      'Asset updated successfully.'
    );
  }

  async function handleDeleteAsset(id) {
    if (
      !window.confirm(
        'Are you sure you want to delete this asset?'
      )
    ) {
      return;
    }

    return performAssetAction(
      () => deleteAsset(id, token),
      'Asset deleted successfully.'
    );
  }

  async function handleAssignAsset(id, userId) {
    return performAssetAction(
      () => assignAsset(id, userId, token),
      'Asset assigned successfully.'
    );
  }

  async function handleReturnAsset(id) {
    return performAssetAction(
      () => returnAsset(id, token),
      'Asset returned successfully.'
    );
  }

  async function handleUpdateStatus(id, status) {
    return performAssetAction(
      () => updateAssetStatus(id, status, token),
      'Asset status updated successfully.'
    );
  }

  if (!token) {
    return (
      <LoginPage
        username={username}
        setUsername={setUsername}
        password={password}
        setPassword={setPassword}
        onLogin={handleLogin}
        loading={loading}
        message={message}
      />
    );
  }

  return (
    <DashboardPage
      assets={assets}
      categories={categories}
      searchTerm={searchTerm}
      setSearchTerm={setSearchTerm}
      statusFilter={statusFilter}
      setStatusFilter={setStatusFilter}
      onRefresh={() => loadDashboard(token)}
      onLogout={handleLogout}
      dataLoading={dataLoading}
      message={message}
      onCreateAsset={handleCreateAsset}
      onUpdateAsset={handleUpdateAsset}
      onDeleteAsset={handleDeleteAsset}
      onAssignAsset={handleAssignAsset}
      onReturnAsset={handleReturnAsset}
      onUpdateStatus={handleUpdateStatus}
    />
  );
}

export default App;
