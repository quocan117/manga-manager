import { useState } from "react";

export default function RegisterMangakaForm() {
    const [form, setForm] = useState({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
        fullName: "",
        penName: "",
        phone: "",
        birthDate: "",
        bio: "",
        portfolio: ""
    });

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        console.log(form);

        alert("Đăng ký thành công!");
    };

    return (
        <div className="card shadow p-4">
            <h2 className="text-center mb-4">
                Đăng ký Mangaka
            </h2>

            <form onSubmit={handleSubmit}>

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
                        Email
                    </label>

                    <input
                        type="email"
                        name="email"
                        className="form-control"
                        value={form.email}
                        onChange={handleChange}
                    />
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
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

                    <div className="col-md-6 mb-3">
                        <label className="form-label">
                            Confirm Password
                        </label>

                        <input
                            type="password"
                            name="confirmPassword"
                            className="form-control"
                            value={form.confirmPassword}
                            onChange={handleChange}
                        />
                    </div>
                </div>

                <div className="mb-3">
                    <label className="form-label">
                        Full Name
                    </label>

                    <input
                        type="text"
                        name="fullName"
                        className="form-control"
                        value={form.fullName}
                        onChange={handleChange}
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">
                        Pen Name
                    </label>

                    <input
                        type="text"
                        name="penName"
                        className="form-control"
                        value={form.penName}
                        onChange={handleChange}
                    />
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label className="form-label">
                            Phone Number
                        </label>

                        <input
                            type="text"
                            name="phone"
                            className="form-control"
                            value={form.phone}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-6 mb-3">
                        <label className="form-label">
                            Date of Birth
                        </label>

                        <input
                            type="date"
                            name="birthDate"
                            className="form-control"
                            value={form.birthDate}
                            onChange={handleChange}
                        />
                    </div>
                </div>

                <div className="mb-3">
                    <label className="form-label">
                        Biography
                    </label>

                    <textarea
                        rows="4"
                        name="bio"
                        className="form-control"
                        value={form.bio}
                        onChange={handleChange}
                    />
                </div>

                <div className="mb-4">
                    <label className="form-label">
                        Portfolio Link
                    </label>

                    <input
                        type="url"
                        name="portfolio"
                        className="form-control"
                        value={form.portfolio}
                        onChange={handleChange}
                        placeholder="https://..."
                    />
                </div>

                <button
                    type="submit"
                    className="btn btn-primary w-100"
                >
                    Đăng ký
                </button>
            </form>
        </div>
    );
}