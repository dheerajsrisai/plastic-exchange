import { useState } from "react";
import { Link } from "react-router-dom";
import api, { errMsg } from "../api";
import { useAuth } from "../AuthContext";
import PickupCard from "../components/PickupCard";

const TYPES = ["PET", "HDPE", "PP", "LDPE", "PS", "PVC", "OTHER"];

export default function NewPickup() {
  const { user } = useAuth();
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState("");
  const [f, setF] = useState({ plasticType: "PET", estimatedWeightKg: 1, address: "", area: "", scheduledAt: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const pick = (e) => {
    const file = e.target.files[0];
    setFile(file || null);
    setPreview(file ? URL.createObjectURL(file) : "");
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!file) return setError("Add a photo of your plastic first.");
    setError("");
    setBusy(true);
    const fd = new FormData();
    fd.append("image", file);
    fd.append("plasticType", f.plasticType);
    fd.append("estimatedWeightKg", f.estimatedWeightKg);
    if (f.address) fd.append("address", f.address);
    if (f.area) fd.append("area", f.area);
    if (f.scheduledAt) fd.append("scheduledAt", f.scheduledAt + ":00");
    try {
      setResult((await api.post("/pickups", fd)).data);
    } catch (err) {
      setError(errMsg(err));
    } finally {
      setBusy(false);
    }
  };

  if (result)
    return (
      <div className="narrow">
        <h2>{result.needsReview ? "Sent for review" : "Pickup booked"}</h2>
        <p className="muted">
          {result.needsReview
            ? "Our AI wasn't confident about this photo, so an admin will check it shortly."
            : "The AI verified your plastic. An agent will collect it at the scheduled time."}
        </p>
        <PickupCard p={result} />
        <div className="row"><Link className="btn" to="/history">View my pickups</Link></div>
      </div>
    );

  return (
    <form className="card form narrow" onSubmit={submit}>
      <h2>Schedule a pickup</h2>
      {error && <div className="alert error">{error}</div>}
      <label className="drop">
        {preview ? <img src={preview} alt="Preview" /> : <span>Choose a clear photo of the plastic</span>}
        <input type="file" accept="image/*" onChange={pick} hidden />
      </label>
      <label>Plastic type (the AI will double-check)
        <select value={f.plasticType} onChange={set("plasticType")}>{TYPES.map((t) => <option key={t}>{t}</option>)}</select>
      </label>
      <label>Estimated weight (kg)<input type="number" min="0.1" step="0.1" value={f.estimatedWeightKg} onChange={set("estimatedWeightKg")} required /></label>
      <label>Pickup time<input type="datetime-local" value={f.scheduledAt} onChange={set("scheduledAt")} required /></label>
      <label>Address<input value={f.address} onChange={set("address")} placeholder={user.address || "Uses your saved address if empty"} /></label>
      <label>Area<input value={f.area} onChange={set("area")} placeholder="Uses your saved area if empty" /></label>
      <button className="btn" disabled={busy}>{busy ? "Verifying photo…" : "Book pickup"}</button>
    </form>
  );
}
