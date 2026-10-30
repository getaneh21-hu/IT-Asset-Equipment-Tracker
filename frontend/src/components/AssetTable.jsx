
import React from 'react';
function AssetTable({ assets = [] }) {
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Serial Number</th>
            <th>Status</th>
            <th>Category</th>
          </tr>
        </thead>

        <tbody>
          {assets.map((asset) => (
            <tr key={asset.id}>
              <td>{asset.id}</td>

              <td>{asset.name ?? asset.name1 ?? '—'}</td>

              <td>
                {asset.serialNumber ?? asset.serial_number ?? '—'}
              </td>

              <td>
                <span
                  className={`status-badge status-${String(
                    asset.status ?? 'unknown'
                  ).toLowerCase()}`}
                >
                  {asset.status ?? '—'}
                </span>
              </td>

              <td>
                {asset.category?.name ??
                  asset.categoryName ??
                  asset.category_name ??
                  '—'}
              </td>
            </tr>
          ))}

          {assets.length === 0 && (
            <tr>
              <td colSpan={5}>No assets found.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default AssetTable;
