
import React from 'react';
import AssetTable from '../components/AssetTable.jsx';

const emptyForm = {
  name1: '',
  type: '',
  serialNumber: '',
  brand: '',
  model: '',
  department: '',
  categoryId: '',
  purchaseCost: '',
  purchaseDate: '',
  warrantyExpiryDate: '',
};

function DashboardPage({
  assets = [],
  categories = [],
  searchTerm,
  setSearchTerm,
  statusFilter,
  setStatusFilter,
  onRefresh,
  onLogout,
  dataLoading,
  message,
  onCreateAsset,
  onCreateCategory,
  onUpdateAsset,
  onDeleteAsset,
  onAssignAsset,
  onReturnAsset,
  onUpdateStatus,
}) {
  const [categoryFilter, setCategoryFilter] = React.useState('ALL');
  const [form, setForm] = React.useState(emptyForm);
  const [editingId, setEditingId] = React.useState(null);
  const [formError, setFormError] = React.useState('');
  const [saving, setSaving] = React.useState(false);
  const [showForm, setShowForm] = React.useState(false);

  // Category form state
  const [categoryName, setCategoryName] = React.useState('');
  const [categorySaving, setCategorySaving] = React.useState(false);
  const [categoryError, setCategoryError] = React.useState('');

  const availableCount = assets.filter(
    (asset) => String(asset.status ?? '').toUpperCase() === 'AVAILABLE'
  ).length;

  const assignedCount = assets.filter(
    (asset) => String(asset.status ?? '').toUpperCase() === 'ASSIGNED'
  ).length;

  const maintenanceCount = assets.filter(
    (asset) => String(asset.status ?? '').toUpperCase() === 'MAINTENANCE'
  ).length;

  const filteredAssets = assets.filter((asset) => {
    const assetStatus = String(asset.status ?? '').toUpperCase();

    const matchesStatus =
      statusFilter === 'ALL' || assetStatus === statusFilter;

    const assetCategoryId =
      asset.category?.id ?? asset.categoryId ?? asset.category_id;

    const assetCategoryName = String(
      asset.category?.name ??
      asset.categoryName ??
      asset.category_name ??
      ''
    ).toLowerCase();

    const selectedCategory = categories.find(
      (category) => String(category.id) === categoryFilter
    );

    const matchesCategory =
      categoryFilter === 'ALL' ||
      (selectedCategory &&
        (String(assetCategoryId) === String(selectedCategory.id) ||
          assetCategoryName === String(selectedCategory.name).toLowerCase()));

    const name = String(asset.name ?? asset.name1 ?? '').toLowerCase();

    const serialNumber = String(
      asset.serialNumber ?? asset.serial_number ?? ''
    ).toLowerCase();

    const search = (searchTerm ?? '').trim().toLowerCase();

    return (
      matchesStatus &&
      matchesCategory &&
      (name.includes(search) || serialNumber.includes(search))
    );
  });

  function startCreate() {
    setEditingId(null);
    setForm({ ...emptyForm });
    setFormError('');
    setShowForm(true);
  }

  function startEdit(asset) {
    setEditingId(asset.id);

    setForm({
      name1: asset.name ?? asset.name1 ?? '',
      type: asset.type ?? '',
      serialNumber: asset.serialNumber ?? asset.serial_number ?? '',
      brand: asset.brand ?? '',
      model: asset.model ?? '',
      department: asset.department ?? '',
      categoryId: String(
        asset.category?.id ?? asset.categoryId ?? asset.category_id ?? ''
      ),
      purchaseCost: String(asset.purchaseCost ?? asset.purchase_cost ?? ''),
      purchaseDate: String(
        asset.purchaseDate ?? asset.purchase_date ?? ''
      ).slice(0, 10),
      warrantyExpiryDate: String(
        asset.warrantyExpiryDate ?? asset.warranty_expiry_date ?? ''
      ).slice(0, 10),
    });

    setFormError('');
    setShowForm(true);
  }

  function cancelForm() {
    setShowForm(false);
    setEditingId(null);
    setForm({ ...emptyForm });
    setFormError('');
  }

  function handleFieldChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  // Create category
  async function handleCreateCategorySubmit(event) {
    event.preventDefault();
    setCategoryError('');

    const name = categoryName.trim();

    if (!name) {
      setCategoryError('Please enter a category name.');
      return;
    }

    setCategorySaving(true);

    try {
      const success = await onCreateCategory(name);

      if (success) {
        setCategoryName('');
      }
    } catch (error) {
      setCategoryError(
        error.message || 'Could not create category.'
      );
    } finally {
      setCategorySaving(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError('');

    if (
      !form.name1.trim() ||
      !form.type.trim() ||
      !form.serialNumber.trim() ||
      !form.categoryId ||
      form.purchaseCost === '' ||
      !form.purchaseDate
    ) {
      setFormError(
        'Please complete the asset name, type, serial number, category, purchase cost, and purchase date.'
      );
      return;
    }

    if (
      Number(form.purchaseCost) < 0 ||
      !Number.isFinite(Number(form.purchaseCost))
    ) {
      setFormError('Purchase cost must be a valid non-negative number.');
      return;
    }

    if (
      form.warrantyExpiryDate &&
      form.warrantyExpiryDate < form.purchaseDate
    ) {
      setFormError(
        'Warranty expiry date cannot be earlier than the purchase date.'
      );
      return;
    }

    const assetData = {
      name1: form.name1.trim(),
      type: form.type.trim(),
      serialNumber: form.serialNumber.trim(),
      brand: form.brand.trim() || null,
      model: form.model.trim() || null,
      department: form.department.trim() || null,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      purchaseCost: Number(form.purchaseCost),
      purchaseDate: form.purchaseDate,
      warrantyExpiryDate: form.warrantyExpiryDate || null,
    };

    setSaving(true);

    try {
      if (editingId !== null) {
        await onUpdateAsset(editingId, assetData);
      } else {
        await onCreateAsset(assetData);
      }

      cancelForm();
    } catch (error) {
      setFormError(error.message || 'Could not save the asset.');
    } finally {
      setSaving(false);
    }
  }

  async function handleAssign(asset) {
    const userId = window.prompt(
      `Enter the existing user's numeric ID to assign asset #${asset.id}:`
    );

    if (userId === null) return;

    if (!/^[1-9]\d*$/.test(userId.trim())) {
      window.alert('Enter a valid positive numeric user ID.');
      return;
    }

    await onAssignAsset(asset.id, Number(userId));
  }

  async function handleStatusChange(asset, event) {
    const newStatus = event.target.value;

    if (!newStatus) return;

    await onUpdateStatus(asset.id, newStatus);
    event.target.value = '';
  }

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>IT Asset Tracker</h1>
          <p>Equipment overview</p>
        </div>

        <button type="button" onClick={onLogout}>
          Log Out
        </button>
      </header>

      {message && <p role="alert">{message}</p>}

      <section className="dashboard-summary">
        <article className="summary-card">
          <h2>Total Assets</h2>
          <p>{assets.length}</p>
        </article>

        <article className="summary-card">
          <h2>Categories</h2>
          <p>{categories.length}</p>
        </article>

        <article className="summary-card">
          <h2>Available</h2>
          <p>{availableCount}</p>
        </article>

        <article className="summary-card">
          <h2>Assigned</h2>
          <p>{assignedCount}</p>
        </article>

        <article className="summary-card">
          <h2>Maintenance</h2>
          <p>{maintenanceCount}</p>
        </article>
      </section>

      <section className="dashboard-section">
        <h2>Categories</h2>

        <form onSubmit={handleCreateCategorySubmit}>
          <label>
            New Category Name
            <input
              type="text"
              value={categoryName}
              onChange={(event) => setCategoryName(event.target.value)}
              placeholder="e.g. Printers"
              maxLength={100}
              required
            />
          </label>

          <button type="submit" disabled={categorySaving}>
            {categorySaving ? 'Creating...' : '+ Add Category'}
          </button>

          {categoryError && <p role="alert">{categoryError}</p>}
        </form>

        {dataLoading ? (
          <p>Loading categories...</p>
        ) : categories.length === 0 ? (
          <p>No categories found.</p>
        ) : (
          <ul>
            {categories.map((category) => (
              <li key={category.id}>{category.name}</li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-section">
        <h2>Assets</h2>

        <button type="button" onClick={startCreate}>
          + Add Assets
        </button>

        {showForm && (
          <form onSubmit={handleSubmit} className="asset-form">
            <h3>{editingId !== null ? 'Edit Asset' : 'Add New Asset'}</h3>

            {formError && <p role="alert">{formError}</p>}

            <label>
              Asset Name *
              <input
                name="name1"
                value={form.name1}
                onChange={handleFieldChange}
                minLength={2}
                maxLength={100}
                required
              />
            </label>

            <label>
              Type *
              <input
                name="type"
                value={form.type}
                onChange={handleFieldChange}
                maxLength={50}
                required
                placeholder="e.g. Laptop, Monitor, Router"
              />
            </label>

            <label>
              Serial Number *
              <input
                name="serialNumber"
                value={form.serialNumber}
                onChange={handleFieldChange}
                maxLength={100}
                required
              />
            </label>

            <label>
              Brand
              <input
                name="brand"
                value={form.brand}
                onChange={handleFieldChange}
                maxLength={100}
              />
            </label>

            <label>
              Model
              <input
                name="model"
                value={form.model}
                onChange={handleFieldChange}
                maxLength={100}
              />
            </label>

            <label>
              Department
              <input
                name="department"
                value={form.department}
                onChange={handleFieldChange}
                maxLength={100}
              />
            </label>

            <label>
              Category
              <select
                name="categoryId"
                value={form.categoryId}
                onChange={handleFieldChange}
              >
                <option value="">No category</option>

                {categories.map((category) => (
                  <option key={category.id} value={String(category.id)}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Purchase Cost *
              <input
                name="purchaseCost"
                type="number"
                min="0"
                step="any"
                value={form.purchaseCost}
                onChange={handleFieldChange}
                required
              />
            </label>

            <label>
              Purchase Date *
              <input
                name="purchaseDate"
                type="date"
                value={form.purchaseDate}
                onChange={handleFieldChange}
                required
              />
            </label>

            <label>
              Warranty Expiry Date
              <input
                name="warrantyExpiryDate"
                type="date"
                value={form.warrantyExpiryDate}
                onChange={handleFieldChange}
              />
            </label>

            <div className="asset-form-actions">
              <button type="submit" disabled={saving}>
                {saving
                  ? 'Saving...'
                  : editingId !== null
                    ? 'Save Changes'
                    : 'Create Asset'}
              </button>

              <button
                type="button"
                onClick={cancelForm}
                disabled={saving}
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        <div className="asset-filters">
          <input
            id="asset-search"
            name="assetSearch"
            type="search"
            aria-label="Search assets"
            placeholder="Search by name ,serial number,category..."
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />

          <select
            id="asset-status-filter"
            name="assetStatus"
            aria-label="Filter assets by status"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="AVAILABLE">Available</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="MAINTENANCE">Maintenance</option>
          </select>

          <select
            id="asset-category-filter"
            name="assetCategory"
            aria-label="Filter assets by category"
            value={categoryFilter}
            onChange={(event) => setCategoryFilter(event.target.value)}
          >
            <option value="ALL">All Categories</option>

            {categories.map((category) => (
              <option key={category.id} value={String(category.id)}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <p>
          Showing {filteredAssets.length} of {assets.length} assets
        </p>

        {dataLoading ? (
          <p>Loading assets...</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Serial Number</th>
                  <th>Status</th>
                  <th>Category</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {filteredAssets.map((asset) => {
                  const status = String(asset.status ?? '').toUpperCase();

                  return (
                    <tr key={asset.id}>
                      <td>{asset.id}</td>
                      <td>{asset.name ?? asset.name1 ?? '—'}</td>
                      <td>
                        {asset.serialNumber ?? asset.serial_number ?? '—'}
                      </td>
                      <td>{status || '—'}</td>
                      <td>
                        {asset.category?.name ??
                          asset.categoryName ??
                          asset.category_name ??
                          '—'}
                      </td>
                      <td>
                        <div className="asset-actions">
                          <button
                            type="button"
                            onClick={() => startEdit(asset)}
                          >
                            Edit
                          </button>

                          <button
                            type="button"
                            onClick={() => onDeleteAsset(asset.id)}
                          >
                            Delete
                          </button>

                          {status === 'AVAILABLE' && (
                            <button
                              type="button"
                              onClick={() => handleAssign(asset)}
                            >
                              Assign
                            </button>
                          )}

                          {status === 'ASSIGNED' && (
                            <button
                              type="button"
                              onClick={() => onReturnAsset(asset.id)}
                            >
                              Return
                            </button>
                          )}

                          {status !== 'ASSIGNED' && (
                            <select
                              aria-label={`Change status for asset ${asset.id}`}
                              defaultValue=""
                              onChange={(event) =>
                                handleStatusChange(asset, event)
                              }
                            >
                              <option value="" disabled>
                                Change status
                              </option>
                              <option value="AVAILABLE">Available</option>
                              <option value="MAINTENANCE">Maintenance</option>
                            </select>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}

                {filteredAssets.length === 0 && (
                  <tr>
                    <td colSpan={6}>No assets found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <button
        type="button"
        onClick={onRefresh}
        disabled={dataLoading}
      >
        {dataLoading ? 'Refreshing...' : 'Refresh'}
      </button>
    </main>
  );
}

export default DashboardPage;
