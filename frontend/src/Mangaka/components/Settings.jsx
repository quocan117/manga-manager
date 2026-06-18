import ThemeSwitcher from "./ThemeSwitcher";

export default function Settings() {
    return (
        <div className="card shadow">

            <div className="card-header">
                Settings
            </div>

            <div className="card-body">

                <h5 className="mb-3">
                    Appearance
                </h5>

                <ThemeSwitcher/>

            </div>

        </div>
    );
}