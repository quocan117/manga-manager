import { useState } from "react";
import { login } from "../services/authService";
import { Link, Navigate, useNavigate } from "react-router-dom";

export default function LoginForm() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [loading, setLoading] = useState(false);

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

        try {
            setLoading(true);

            const data = await login(form);

            console.log("Response:", data);

            alert("Đăng nhập thành công!");

            navigate("/mangaka");
        } catch (error) {
            console.error(error);

            alert("Sai tài khoản hoặc mật khẩu!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="card login-card">

            <h2 className="login-title">
                Manga Manager
            </h2>

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

                <div className="text-center mt-3">
                    <span className="text-muted">
                        Bạn chưa có tài khoản?
                    </span>

                    {" "}

                    <Link
                        to="/register"
                        className="text-decoration-none fw-semibold"
                    >
                        Đăng ký làm Mangaka
                    </Link>
                </div>
            </form>
        </div>
    );
}