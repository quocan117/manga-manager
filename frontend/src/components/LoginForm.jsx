import { useState } from "react";
import axios from "axios";

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


/**Chạy và gọi API thì dổi sang cái này */
    //   const handleSubmit = async (e) => {
    //     e.preventDefault();

    //     try {
    //       setLoading(true);

    //       const response = await axios.post(
    //         "http://localhost:8080/api/login",
    //         form
    //       );

    //       console.log(response.data);

    //       alert("Đăng nhập thành công!");
    //     } catch (error) {
    //       alert("Sai tài khoản hoặc mật khẩu!");
    //     } finally {
    //       setLoading(false);
    //     }
    //   };


/**Đây là handle test thử, khi chay với backend nhớ đổi sang cái ở trên */
    const handleSubmit = async (e) => {
        e.preventDefault();

        console.log(form);

        alert("Đã submit");
    };

    return (
        <form
            onSubmit={handleSubmit}
            className="card p-4 shadow"
            style={{ maxWidth: "400px" }}
        >
            <h3 className="mb-3 text-center">
                Đăng nhập
            </h3>

            <div className="mb-3">
                <label className="form-label">
                    Username
                </label>

                <input
                    type="text"
                    name="username"
                    className="form-control"
                    value={form.username}
                    onChange={handleChange}
                />
            </div>

            <div className="mb-3">
                <label className="form-label">
                    Password
                </label>

                <input
                    type="password"
                    name="password"
                    className="form-control"
                    value={form.password}
                    onChange={handleChange}
                />
            </div>

            <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
            >
                {loading
                    ? "Đang đăng nhập..."
                    : "Đăng nhập"}
            </button>
        </form>
    );
}