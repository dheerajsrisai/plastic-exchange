import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../AuthContext";
import PickupCard from "../components/PickupCard";

export default function Dashboard() {
  const { user } = useAuth();
  const [pickups, setPickups] = useState([]);
  const [points, setPoints] = useState(0);

  useEffect(() => {
    api.get("/pickups/my").then((r) => setPickups(r.data));
    api.get("/rewards/balance").then((r) => setPoints(r.data.points));
  }, []);

  const kg = pickups.filter((p) => ["COLLECTED", "COMPLETED"].includes(p.status)).reduce((s, p) => s + p.weightKg, 0);
  const active = pickups.filter((p) => !["COMPLETED", "REJECTED"].includes(p.status)).length;

  return (
    <>
      <section className="hero">
        <div>
          <h1>Hello, {user.name.split(" ")[0]}.</h1>
          <p className="muted">You have recycled {kg.toFixed(1)} kg of plastic with {active} pickup{active === 1 ? "" : "s"} in progress.</p>
          <Link className="btn" to="/new-pickup">Schedule a pickup</Link>
        </div>
        <Link to="/rewards" className="cap" aria-label="View rewards">
          <span className="cap-num">{points}</span>
          <span className="cap-label">points</span>
        </Link>
      </section>

      <h2>Recent pickups</h2>
      {pickups.length === 0 ? (
        <div className="empty">No pickups yet. Photograph a bottle or bag and schedule your first one.</div>
      ) : (
        <div className="grid">{pickups.slice(0, 4).map((p) => <PickupCard key={p.id} p={p} />)}</div>
      )}
    </>
  );
}
