import { createContext, useContext, useState } from "react";
import api from "./api";

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export const homeFor = (role) => (role === "ADMIN" ? "/admin" : role === "AGENT" ? "/agent" : "/dashboard");

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem("user") || "null"));

  const save = (data) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(data));
    setUser(data);
    return data;
  };

  const login = async (email, password) => save((await api.post("/auth/login", { email, password })).data);
  const register = async (form) => save((await api.post("/auth/register", form)).data);
  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>;
}
