import { useState } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import { login, googleLogin } from "../services/authService";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { GoogleLogin } from "@react-oauth/google";

const validationSchema = Yup.object({
  email: Yup.string()
    .email("Email không hợp lệ")
    .required("Vui lòng nhập email"),
  password: Yup.string()
    .min(6, "Mật khẩu phải có ít nhất 6 ký tự")
    .required("Vui lòng nhập mật khẩu"),
});

export default function LoginForm() {
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const redirectByRole = (role) => {
    switch (role) {
      case "MANGAKA":
        navigate("/mangaka");
        break;
      case "ASSISTANT":
        navigate("/assistant");
        break;
      case "TANTOU_EDITOR":
        navigate("/tantou");
        break;
      case "EDITORIAL_BOARD":
        navigate("/board");
        break;
      default:
        navigate("/");
    }
  };

  const handleLoginSuccess = (data) => {
    setAuth(
      {
        userId: data.userId,
        username: data.username,
        email: data.email,
        role: data.role,
      },
      data.token,
    );
    alert("Đăng nhập thành công!");
    redirectByRole(data.role);
  };

  const formik = useFormik({
    initialValues: {
      email: "",
      password: "",
    },
    validationSchema,
    onSubmit: async (values) => {
      setErrorMessage("");
      try {
        setLoading(true);
        const data = await login(values);
        handleLoginSuccess(data);
      } catch (error) {
        const backendMessage =
          error.response?.data?.message || error.response?.data?.error || "";
        const lowerMessage = backendMessage.toLowerCase();
        if (lowerMessage.includes("not active")) {
          setErrorMessage("Tài khoản đã bị khóa.");
        } else if (
          lowerMessage.includes("account does not exist") ||
          lowerMessage.includes("invalid email")
        ) {
          setErrorMessage("Tài khoản không tồn tại.");
        } else if (lowerMessage.includes("incorrect email or password")) {
          setErrorMessage("Sai mật khẩu. Vui lòng thử lại.");
        } else {
          setErrorMessage("Đăng nhập thất bại. Vui lòng thử lại sau.");
        }
      } finally {
        setLoading(false);
      }
    },
  });

  const handleGoogleSuccess = async (credentialResponse) => {
    setErrorMessage("");
    try {
      setLoading(true);
      const data = await googleLogin(credentialResponse.credential);
      handleLoginSuccess(data);
    } catch (error) {
      const backendMessage = error.response?.data?.message || "";
      const lowerMessage = backendMessage.toLowerCase();
      if (lowerMessage.includes("chưa được tạo")) {
        setErrorMessage(
          "Tài khoản Google này chưa được Hội đồng biên tập cấp quyền truy cập.",
        );
      } else if (lowerMessage.includes("not active")) {
        setErrorMessage("Tài khoản đã bị khóa.");
      } else {
        setErrorMessage("Đăng nhập bằng Google thất bại. Vui lòng thử lại.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card login-card">
      <h2 className="login-title">Manga Manager</h2>
      {errorMessage && (
        <div
          className="alert alert-danger d-flex align-items-center p-3 mb-4"
          role="alert"
          style={{ fontSize: "14px" }}
        >
          <span className="me-2">⚠️</span>
          <div>{errorMessage}</div>
        </div>
      )}
      <form onSubmit={formik.handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Email</label>
          <input
            type="email"
            name="email"
            className={`form-control login-input ${formik.touched.email && formik.errors.email ? "is-invalid" : ""}`}
            value={formik.values.email}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            placeholder="Nhập email"
          />
          {formik.touched.email && formik.errors.email && (
            <div className="invalid-feedback">{formik.errors.email}</div>
          )}
        </div>
        <div className="mb-4">
          <label className="form-label">Password</label>
          <input
            type="password"
            name="password"
            className={`form-control login-input ${formik.touched.password && formik.errors.password ? "is-invalid" : ""}`}
            value={formik.values.password}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            placeholder="Nhập mật khẩu"
          />
          {formik.touched.password && formik.errors.password && (
            <div className="invalid-feedback">{formik.errors.password}</div>
          )}
        </div>
        <button
          type="submit"
          className="btn btn-primary w-100 login-btn"
          disabled={loading}
        >
          {loading ? "Đang đăng nhập..." : "Đăng nhập"}
        </button>
      </form>

      <div className="d-flex align-items-center my-3">
        <hr className="flex-grow-1" />
        <span className="mx-2 text-muted" style={{ fontSize: "13px" }}>
          hoặc
        </span>
        <hr className="flex-grow-1" />
      </div>

      <div className="d-flex justify-content-center">
        <GoogleLogin
          onSuccess={handleGoogleSuccess}
          onError={() => setErrorMessage("Đăng nhập bằng Google thất bại.")}
          width="100%"
        />
      </div>
    </div>
  );
}
