import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { homeFor, useAuth } from "../AuthContext";
import { errMsg } from "../api";

export default function Login() {
  const { user, login } = useAuth();
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  if (user) return <Navigate to={homeFor(user.role)} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const u = await login(email, password);
      nav(homeFor(u.role));
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <div className="auth">
      <div className="auth-intro">
        <h1>Trade your plastic for points.</h1>
        <p>Photograph it, book a pickup, and an agent collects it from your door. Every kilo earns rewards.</p>
      </div>
      <form className="card form" onSubmit={submit}>
        <h2>Log in</h2>
        {error && <div className="alert error">{error}</div>}
        <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
        <button className="btn">Log in</button>
        <p className="muted">New here? <Link to="/register">Create an account</Link></p>
      </form>
    </div>
  );
}
