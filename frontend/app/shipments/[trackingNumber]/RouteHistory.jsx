"use client";

import { useEffect, useState } from "react";
import { API_BASE_URL, getStoredToken } from "@/lib/api";

// Renders the route history for a single shipment: the current route
// (origin, destination, distance, created date) clearly labeled, followed
// by any previous routes. Today the backend only ever has one route per
// shipment, so "previous routes" will typically be empty - the UI is
// built to handle a growing list once route reassignment is supported.
export default function RouteHistory({ shipmentId }) {
  const [history, setHistory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!shipmentId) return;

    const token = getStoredToken();
    if (!token) {
      setLoading(false);
      setError("You need to be logged in to view route history.");
      return;
    }

    let cancelled = false;

    async function loadHistory() {
      try {
        const response = await fetch(
          `${API_BASE_URL}/api/routes/${shipmentId}/history`,
          {
            method: "GET",
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        const data = await response.json().catch(() => null);

        if (!response.ok) {
          throw new Error(data?.message || `Request failed (status ${response.status})`);
        }

        if (!cancelled) setHistory(data);
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadHistory();
    return () => {
      cancelled = true;
    };
  }, [shipmentId]);

  if (loading) {
    return (
      <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p className="text-sm text-zinc-500">Loading route history...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p className="text-sm text-red-600">{error}</p>
      </div>
    );
  }

  const current = history?.currentRoute;
  const previous = history?.previousRoutes || [];

  return (
    <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-zinc-500">
        Route History
      </h2>

      {!current && (
        <p className="text-sm text-zinc-400">
          No route has been assigned to this shipment yet.
        </p>
      )}

      {current && <RouteEntry route={current} />}

      {previous.length > 0 && (
        <div className="mt-4 space-y-3">
          {previous.map((route) => (
            <RouteEntry key={route.routeId} route={route} />
          ))}
        </div>
      )}

      {current && previous.length === 0 && (
        <p className="mt-3 text-xs text-zinc-400">
          No previous routes for this shipment.
        </p>
      )}
    </div>
  );
}

function RouteEntry({ route }) {
  const isCurrent = route.current;

  return (
    <div
      className={`rounded-lg border p-4 ${
        isCurrent ? "border-blue-300 bg-blue-50" : "border-zinc-200 bg-zinc-50"
      }`}
    >
      <div className="mb-2 flex items-center justify-between">
        <span
          className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
            isCurrent
              ? "bg-blue-600 text-white"
              : "bg-zinc-200 text-zinc-600"
          }`}
        >
          {isCurrent ? "Current Route" : "Previous Route"}
        </span>
        {route.createdAt && (
          <span className="text-xs text-zinc-500">
            {new Date(route.createdAt).toLocaleString()}
          </span>
        )}
      </div>

      <div className="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
        <div>
          <p className="text-xs text-zinc-500">Origin</p>
          <p className="font-medium text-zinc-800">{route.origin || "—"}</p>
        </div>
        <div>
          <p className="text-xs text-zinc-500">Destination</p>
          <p className="font-medium text-zinc-800">{route.destination || "—"}</p>
        </div>
        <div>
          <p className="text-xs text-zinc-500">Distance</p>
          <p className="font-medium text-zinc-800">
            {route.distanceKm != null ? `${route.distanceKm} km` : "—"}
          </p>
        </div>
        <div>
          <p className="text-xs text-zinc-500">Estimated Time</p>
          <p className="font-medium text-zinc-800">
            {route.estimatedTimeMinutes != null
              ? `${route.estimatedTimeMinutes} min`
              : "—"}
          </p>
        </div>
      </div>
    </div>
  );
}
