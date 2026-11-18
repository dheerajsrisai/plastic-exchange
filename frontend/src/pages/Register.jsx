import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { homeFor, useAuth } from "../AuthContext";
import { errMsg } from "../api";

export default function Register() {
  const { register } = useAuth();
  const nav = useNavigate();
  const [f, setF] = useState({ role: "USER", name: "", email: "", password: "", phone: "", address: "", area: "", vehicleNo: "" });
  const [error, setError] = useState("");
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const u = await register(f);
      nav(homeFor(u.role));
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <div className="auth single">
      <form className="card form" onSubmit={submit}>
        <h2>Create your account</h2>
        {error && <div className="alert error">{error}</div>}
        <div className="seg">
          <button type="button" className={f.role === "USER" ? "on" : ""} onClick={() => setF({ ...f, role: "USER" })}>I have plastic to recycle</button>
          <button type="button" className={f.role === "AGENT" ? "on" : ""} onClick={() => setF({ ...f, role: "AGENT" })}>I'm a delivery agent</button>
        </div>
        <label>Full name<input value={f.name} onChange={set("name")} required /></label>
        <label>Email<input type="email" value={f.email} onChange={set("email")} required /></label>
        <label>Password (6+ characters)<input type="password" value={f.password} onChange={set("password")} minLength={6} required /></label>
        <label>Phone<input value={f.phone} onChange={set("phone")} /></label>
        <label>Area / locality<input value={f.area} onChange={set("area")} placeholder="e.g. Madanapalle" required /></label>
        {f.role === "USER" && <label>Pickup address<input value={f.address} onChange={set("address")} required /></label>}
        {f.role === "AGENT" && (
          <>
            <label>Vehicle number<input value={f.vehicleNo} onChange={set("vehicleNo")} required /></label>
            <div className="alert info">An admin must approve your account before you receive pickups.</div>
          </>
        )}
        <button className="btn">Create account</button>
        <p className="muted">Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </div>
  );
}
