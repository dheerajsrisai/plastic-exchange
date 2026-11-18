import { useEffect, useState } from "react";
import api, { errMsg } from "../api";

export default function Rewards() {
  const [data, setData] = useState(null);
  const [catalog, setCatalog] = useState([]);
  const [msg, setMsg] = useState(null);

  const load = () => api.get("/rewards/balance").then((r) => setData(r.data));
  useEffect(() => { load(); api.get("/rewards/catalog").then((r) => setCatalog(r.data)); }, []);

  const redeem = async (item) => {
    try {
      const r = await api.post("/rewards/redeem", { item: item.name });
      setMsg({ ok: true, text: r.data.message });
      load();
    } catch (e) {
      setMsg({ ok: false, text: errMsg(e) });
    }
  };

  if (!data) return <p className="muted">Loading…</p>;
  return (
    <>
      <section className="hero">
        <div>
          <h1>Rewards</h1>
          <p className="muted">Points are added when your plastic is weighed by the agent.</p>
        </div>
        <div className="cap static"><span className="cap-num">{data.points}</span><span className="cap-label">points</span></div>
      </section>

      {msg && <div className={`alert ${msg.ok ? "info" : "error"}`}>{msg.text}</div>}

      <h2>Redeem</h2>
      <div className="grid">
        {catalog.map((c) => (
          <div className="card" key={c.name}>
            <strong>{c.name}</strong>
            <p className="muted">{c.points} points</p>
            <button className="btn" disabled={data.points < c.points} onClick={() => redeem(c)}>Redeem</button>
          </div>
        ))}
      </div>

      <div className="two-col">
        <div>
          <h2>Points earned</h2>
          {data.earnings.length === 0 && <div className="empty">No points yet.</div>}
          {data.earnings.map((e) => (
            <div className="line" key={e.id}><span>{e.reason}</span><b>+{e.points}</b></div>
          ))}
        </div>
        <div>
          <h2>Redeemed</h2>
          {data.redemptions.length === 0 && <div className="empty">Nothing redeemed yet.</div>}
          {data.redemptions.map((r) => (
            <div className="line" key={r.id}><span>{r.item}</span><b>−{r.pointsUsed}</b></div>
          ))}
        </div>
      </div>
    </>
  );
}
