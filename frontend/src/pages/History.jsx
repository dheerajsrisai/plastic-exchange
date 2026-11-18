import { useEffect, useState } from "react";
import api from "../api";
import PickupCard from "../components/PickupCard";

export default function History() {
  const [pickups, setPickups] = useState(null);
  useEffect(() => { api.get("/pickups/my").then((r) => setPickups(r.data)); }, []);

  if (!pickups) return <p className="muted">Loading…</p>;
  return (
    <>
      <h2>My pickups</h2>
      {pickups.length === 0 ? <div className="empty">Nothing here yet.</div> :
        <div className="grid">{pickups.map((p) => <PickupCard key={p.id} p={p} />)}</div>}
    </>
  );
}
