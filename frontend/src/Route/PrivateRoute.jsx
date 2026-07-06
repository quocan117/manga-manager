import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function PrivateRoute({ children, role }) {
  const { user, token, loading } = useAuth();

  if (loading) {
    return null;
  }
  // Chưa đăng nhập
  if (!token || !user) {
    return <Navigate to="/" replace />;
  }
  // Sai role
  if (role && user.role !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}
