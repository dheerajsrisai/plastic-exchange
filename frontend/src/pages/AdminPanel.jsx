import { useEffect, useState } from "react";
import api, { errMsg } from "../api";
import PickupCard from "../components/PickupCard";

export default function AdminPanel() {
  const [tab, setTab] = useState("pickups");
  const [stats, setStats] = useState({});
  const [agents, setAgents] = useState([]);
  const [pickups, setPickups] = useState([]);
  const [rates, setRates] = useState([]);
  const [error, setError] = useState("");

  const load = () => {
    api.get("/admin/stats").then((r) => setStats(r.data));
    api.get("/admin/agents").then((r) => setAgents(r.data));
    api.get("/admin/pickups").then((r) => setPickups(r.data));
    api.get("/admin/rates").then((r) => setRates(r.data));
  };
  useEffect(load, []);

  const act = async (fn) => { setError(""); try { await fn(); load(); } catch (e) { setError(errMsg(e)); } };

  const tiles = [
    ["Users", stats.users], ["Agents", stats.agents], ["Pickups", stats.pickups],
    ["Need review", stats.pendingReview], ["Plastic collected", `${(stats.totalKg || 0).toFixed(1)} kg`], ["Points issued", stats.pointsIssued],
  ];

  return (
    <>
      <h1>Admin panel</h1>
      <div className="tiles">{tiles.map(([k, v]) => <div className="tile" key={k}><b>{v ?? 0}</b><span>{k}</span></div>)}</div>
      {error && <div className="alert error">{error}</div>}

      <div className="seg">
        {["pickups", "agents", "rates"].map((t) => (
          <button key={t} className={tab === t ? "on" : ""} onClick={() => setTab(t)}>
            {t === "pickups" ? "Pickups" : t === "agents" ? "Delivery agents" : "Reward rates"}
          </button>
        ))}
      </div>

      {tab === "pickups" && (
        <div className="grid">
          {pickups.map((p) => (
            <PickupCard key={p.id} p={p}>
              {p.status === "REQUESTED" && (
                <div className="row">
                  <button className="btn" onClick={() => act(() => api.put(`/admin/pickups/${p.id}/review`, null, { params: { approve: true, type: p.plasticType } }))}>Approve</button>
                  <button className="btn ghost" onClick={() => act(() => api.put(`/admin/pickups/${p.id}/review`, null, { params: { approve: false } }))}>Reject</button>
                </div>
              )}
            </PickupCard>
          ))}
          {pickups.length === 0 && <div className="empty">No pickups yet.</div>}
        </div>
      )}

      {tab === "agents" && (
        <table className="table">
          <thead><tr><th>Name</th><th>Phone</th><th>Vehicle</th><th>Area</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {agents.map((a) => (
              <tr key={a.id}>
                <td>{a.name}</td><td>{a.phone}</td><td>{a.vehicleNo}</td><td>{a.area}</td>
                <td>{a.approved ? (a.available ? "Available" : "Offline") : "Pending approval"}</td>
                <td>{!a.approved && <button className="btn" onClick={() => act(() => api.put(`/admin/agents/${a.id}/approve`))}>Approve</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {tab === "rates" && (
        <table className="table">
          <thead><tr><th>Plastic type</th><th>Points per kg</th></tr></thead>
          <tbody>
            {rates.map((r) => (
              <tr key={r.type}>
                <td>{r.type}</td>
                <td>
                  <input type="number" min="0" defaultValue={r.pointsPerKg} style={{ width: 90 }}
                         onBlur={(e) => act(() => api.put(`/admin/rates/${r.type}`, { pointsPerKg: parseInt(e.target.value || "0", 10) }))} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
