import { Navigate, Route, Routes } from "react-router-dom";
import { homeFor, useAuth } from "./AuthContext";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import NewPickup from "./pages/NewPickup";
import History from "./pages/History";
import Rewards from "./pages/Rewards";
import AgentDashboard from "./pages/AgentDashboard";
import AdminPanel from "./pages/AdminPanel";

function Guard({ role, children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to={homeFor(user.role)} replace />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  return (
    <>
      <Navbar />
      <main className="page">
        <Routes>
          <Route path="/" element={<Navigate to={user ? homeFor(user.role) : "/login"} replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Guard role="USER"><Dashboard /></Guard>} />
          <Route path="/new-pickup" element={<Guard role="USER"><NewPickup /></Guard>} />
          <Route path="/history" element={<Guard role="USER"><History /></Guard>} />
          <Route path="/rewards" element={<Guard role="USER"><Rewards /></Guard>} />
          <Route path="/agent" element={<Guard role="AGENT"><AgentDashboard /></Guard>} />
          <Route path="/admin" element={<Guard role="ADMIN"><AdminPanel /></Guard>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
