import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
export default function PrivateRoute({ children, role }) {
  const { user, token, loading } = useAuth();
  if (loading) {
    return null;
  }

  if (!token || !user) {
    return <Navigate to="/" replace />;
  }

  if (role && user.role !== role) {
    return <Navigate to="/" replace />;
  }
  
  return children;
}