import { useState } from "react";
import {
    useParams,
    useNavigate
} from "react-router-dom";

import {
    createChapter
} from "../../services/mangakaService";

export default function CreateChapterPage() {

    const { id } = useParams();

    const navigate = useNavigate();

    const [form, setForm] = useState({

        chapterNumber: "",
        title: ""

    });

    const [loading, setLoading] =
        useState(false);

    const handleChange = (e) => {

        setForm({

            ...form,

            [e.target.name]:
                e.target.value

        });

    };

    const handleSubmit =
        async (e) => {

            e.preventDefault();

            try {

                setLoading(true);

                await createChapter({

                    seriesId:
                        Number(id),

                    chapterNumber:
                        Number(
                            form.chapterNumber
                        ),

                    title:
                        form.title

                });

                alert(
                    "Tạo chapter thành công!"
                );

                navigate(
                    "/mangaka/manga"
                );

            } catch (error) {

                console.error(error);

                alert(
                    "Tạo chapter thất bại!"
                );

            } finally {

                setLoading(false);

            }

        };

    return (

        <div className="container mt-4">

            <div
                className="card shadow mx-auto"
                style={{
                    maxWidth: "700px"
                }}
            >

                <div className="card-body">

                    <h3 className="mb-4">

                        Create Chapter

                    </h3>

                    <form
                        onSubmit={
                            handleSubmit
                        }
                    >

                        <div className="mb-3">

                            <label>
                                Chapter Number
                            </label>

                            <input
                                type="number"
                                name="chapterNumber"
                                className="form-control"
                                value={
                                    form.chapterNumber
                                }
                                onChange={
                                    handleChange
                                }
                                required
                            />

                        </div>

                        <div className="mb-3">

                            <label>
                                Title
                            </label>

                            <input
                                type="text"
                                name="title"
                                className="form-control"
                                value={
                                    form.title
                                }
                                onChange={
                                    handleChange
                                }
                                required
                            />

                        </div>

                        <div
                            className="d-flex gap-2"
                        >

                            <button
                                type="submit"
                                className="btn btn-success"
                                disabled={
                                    loading
                                }
                            >

                                {
                                    loading
                                        ? "Creating..."
                                        : "Create Chapter"
                                }

                            </button>

                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() =>
                                    navigate(
                                        "/mangaka/manga"
                                    )
                                }
                            >

                                Cancel

                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </div>

    );

}