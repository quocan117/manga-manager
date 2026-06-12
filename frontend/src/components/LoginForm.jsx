import { useState } from "react";
import { login } from "../services/authService";

export default function LoginForm() {
    const [form, setForm] = useState({
        username: "",
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

    const handleSubmit = (e) => {
        e.preventDefault();
        if (form.username === "admin" && form.password === "123456") {
            alert("Đăng nhập thành công!");
        } 
        else {
            alert("Sai tài khoản hoặc mật khẩu!");
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
                        Username
                    </label>

                    <input
                        type="text"
                        name="username"
                        className="form-control login-input"
                        value={form.username}
                        onChange={handleChange}
                        placeholder="Nhập username"
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