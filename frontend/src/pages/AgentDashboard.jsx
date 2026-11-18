import { useEffect, useState } from "react";
import api, { errMsg } from "../api";
import PickupCard from "../components/PickupCard";

export default function AgentDashboard() {
  const [agent, setAgent] = useState(null);
  const [pickups, setPickups] = useState([]);
  const [weights, setWeights] = useState({});
  const [error, setError] = useState("");

  const load = () => {
    api.get("/agent/me").then((r) => setAgent(r.data));
    api.get("/agent/pickups").then((r) => setPickups(r.data));
  };
  useEffect(load, []);

  const act = async (fn) => {
    setError("");
    try { await fn(); load(); } catch (e) { setError(errMsg(e)); }
  };

  const toggle = () => act(() => api.put("/agent/availability", { available: !agent.available }));
  const collect = (p) => act(() => api.put(`/agent/pickups/${p.id}/collect`, { weightKg: parseFloat(weights[p.id]), plasticType: p.plasticType }));
  const complete = (p) => act(() => api.put(`/agent/pickups/${p.id}/complete`));

  if (!agent) return <p className="muted">Loading…</p>;
  return (
    <>
      <section className="hero">
        <div>
          <h1>{agent.name}</h1>
          <p className="muted">Vehicle {agent.vehicleNo} · {agent.area}</p>
          {!agent.approved && <div className="alert info">Waiting for admin approval. You'll receive pickups once approved.</div>}
        </div>
        <button className={`btn ${agent.available ? "" : "ghost"}`} onClick={toggle}>
          {agent.available ? "Available – tap to go offline" : "Offline – tap to go available"}
        </button>
      </section>

      {error && <div className="alert error">{error}</div>}
      <h2>Assigned pickups</h2>
      {pickups.length === 0 ? <div className="empty">No pickups assigned yet.</div> : (
        <div className="grid">
          {pickups.map((p) => (
            <PickupCard key={p.id} p={p}>
              {p.status === "ASSIGNED" && (
                <div className="row">
                  <input type="number" min="0.1" step="0.1" placeholder="Weighed kg" value={weights[p.id] || ""}
                         onChange={(e) => setWeights({ ...weights, [p.id]: e.target.value })} />
                  <button className="btn" disabled={!weights[p.id]} onClick={() => collect(p)}>Confirm weight</button>
                </div>
              )}
              {p.status === "COLLECTED" && <button className="btn" onClick={() => complete(p)}>Mark completed</button>}
            </PickupCard>
          ))}
        </div>
      )}
    </>
  );
}
