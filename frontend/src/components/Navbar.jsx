import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

const links = {
  USER: [["/dashboard", "Home"], ["/new-pickup", "Schedule pickup"], ["/history", "My pickups"], ["/rewards", "Rewards"]],
  AGENT: [["/agent", "My pickups"]],
  ADMIN: [["/admin", "Admin panel"]],
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  return (
    <header className="nav">
      <div className="nav-inner">
        <span className="brand">Plasti<b>Swap</b></span>
        {user && (
          <nav className="nav-links">
            {links[user.role].map(([to, label]) => (
              <NavLink key={to} to={to}>{label}</NavLink>
            ))}
          </nav>
        )}
        <div className="nav-user">
          {user ? (
            <>
              <span>{user.name}</span>
              <button className="btn ghost" onClick={() => { logout(); nav("/login"); }}>Log out</button>
            </>
          ) : (
            <NavLink to="/register">Create account</NavLink>
          )}
        </div>
      </div>
    </header>
  );
}
