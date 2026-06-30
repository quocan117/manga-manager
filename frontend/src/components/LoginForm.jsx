import { useState } from "react";
import { login } from "../services/authService";
import { useNavigate } from "react-router-dom";

export default function LoginForm() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value,
        });
    };

    /**Chạy và gọi API thì dổi sang cái này, sửa API ở bên authService.js*/
    // const handleSubmit = async (e) => {
    //     e.preventDefault();

    //     try {
    //         setLoading(true);

    //         const data = await login(form);

    //         console.log("Response:", data);

    //         alert("Đăng nhập thành công");
    //     } catch (error) {
    //         console.error(error);

    //         alert("Sai tài khoản hoặc mật khẩu");
    //     } finally {
    //         setLoading(false);
    //     }
    // };

    /**Handle Test */
    // const handleSubmit = (e) => {
    //     e.preventDefault();
    //     if (form.username === "mangaka" && form.password === "123456") {
    //         alert("Đăng nhập thành công!");
    //         navigate("/mangaka");
    //     }
    //     else {
    //         alert("Sai tài khoản hoặc mật khẩu!");
    //     }
    // };
    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");

        try {
            setLoading(true);
            const data = await login(form);

            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify({
                userId: data.userId,
                username: data.username,
                email: data.email,
                role: data.role
            }));

            alert("Đăng nhập thành công!");

            switch (data.role) {

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
        } catch (error) {
            const backendMessage = error.response?.data?.message || error.response?.data?.error || "";
            const lowerMessage = backendMessage.toLowerCase();

            if (lowerMessage.includes("not active")) {
                setErrorMessage("Tài khoản đã bị khóa.");
            } 
            else if (lowerMessage.includes("account does not exist") || lowerMessage.includes("invalid email")) {
                setErrorMessage("Tài khoản không tồn tại.");
            }
            else if (lowerMessage.includes("incorrect email or password")) {
                setErrorMessage("Sai mật khẩu. Vui lòng thử lại.");
            }
            else {
                setErrorMessage("Đăng nhập thất bại. Vui lòng thử lại sau.");
            }

        } finally {

            setLoading(false);

        }
    };

    return (
        <div className="card login-card">

            <h2 className="login-title">
                Manga Manager
            </h2>

            {errorMessage && (
                <div className="alert alert-danger d-flex align-items-center p-3 mb-4" role="alert" style={{ fontSize: '14px' }}>
                    <span className="me-2">⚠️</span>
                    <div>{errorMessage}</div>
                </div>
            )}

            <form onSubmit={handleSubmit}>

                <div className="mb-3">
                    <label className="form-label">
                        Email
                    </label>

                    <input
                        type="email"
                        name="email"
                        className="form-control login-input"
                        value={form.email}
                        onChange={handleChange}
                        placeholder="Nhập email"
                    />
                </div>

                <div className="mb-4">
                    <label className="form-label">
                        Password
                    </label>

                    <input
                        type="password"
                        name="password"
                        className="form-control login-input"
                        value={form.password}
                        onChange={handleChange}
                        placeholder="Nhập mật khẩu"
                    />
                </div>

                <button
                    type="submit"
                    className="btn btn-primary w-100 login-btn"
                    disabled={loading}>
                    {loading ? "Đang đăng nhập..." : "Đăng nhập"}
                </button>

            </form>
        </div>
    );
}
