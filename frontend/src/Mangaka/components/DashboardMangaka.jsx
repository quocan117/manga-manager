import { useState } from "react";
import { trendingSeries } from "../../data/mockData";

export default function DashboardMangaka() {
    const [mangas, setMangas] = useState(trendingSeries);
    return (
        <div className="row mt-4">

            <div className="col-md-4">
                <div className="card shadow">
                    <div className="card-body text-center">
                        <h5>Total Manga</h5>
                        <h2>{mangas.length}</h2>
                    </div>
                </div>
            </div>

            <div className="col-md-4">
                <div className="card shadow">
                    <div className="card-body text-center">
                        <h5>Published</h5>
                        <h2>
                            {
                                mangas.filter(
                                    m =>
                                        m.status ===
                                        "Published"
                                ).length
                            }
                        </h2>
                    </div>
                </div>
            </div>

            <div className="col-md-4">
                <div className="card shadow">
                    <div className="card-body text-center">
                        <h5>Draft</h5>
                        <h2>
                            {
                                mangas.filter(
                                    m =>
                                        m.status ===
                                        "Draft"
                                ).length
                            }
                        </h2>
                    </div>
                </div>
            </div>

        </div>
    );
}