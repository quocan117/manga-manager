import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getDrawing } from "../../services/drawingService";


export default function DrawingPage() {

    const { pageId } =
        useParams();

    const [drawing,
        setDrawing] =
        useState(null);

    const [loading,
        setLoading] =
        useState(true);

    useEffect(() => {

        loadDrawing();

    }, [pageId]);

    const loadDrawing =
        async () => {

            try {

                setLoading(true);

                const data =
                    await getDrawing(
                        pageId
                    );

                setDrawing(data);

            }
            catch (error) {

                console.log(error);

                if (
                    error.response?.status === 404
                ) {

                    setDrawing(null);

                }

            }
            finally {

                setLoading(false);

            }

        };

    if (loading) {

        return (

            <div className="container mt-5">

                <div className="spinner-border"/>

                <p className="mt-3">
                    Loading Drawing...
                </p>

            </div>

        );

    }

    return (

        <div className="container-fluid mt-4">

            <div className="row">

                {/* Toolbar */}

                <div
                    className="col-md-2"
                >

                    <div className="card shadow">

                        <div className="card-body">

                            <h5>

                                Toolbar

                            </h5>

                            <hr/>

                            <button
                                className="btn btn-primary w-100 mb-2"
                            >

                                Pen

                            </button>

                            <button
                                className="btn btn-secondary w-100 mb-2"
                            >

                                Eraser

                            </button>

                            <button
                                className="btn btn-warning w-100 mb-2"
                            >

                                Rectangle

                            </button>

                            <button
                                className="btn btn-success w-100"
                            >

                                Circle

                            </button>

                        </div>

                    </div>

                </div>

                {/* Canvas */}

                <div
                    className="col-md-8"
                >

                    <div className="card shadow">

                        <div className="card-header">

                            Drawing Canvas

                        </div>

                        <div
                            className="card-body"
                            style={{
                                height:"700px"
                            }}
                        >

                            {/*

                                Phần 2
                                sẽ thay bằng Fabric Canvas

                            */}

                            <div
                                style={{
                                    height:"100%",
                                    border:"2px dashed gray",
                                    display:"flex",
                                    justifyContent:"center",
                                    alignItems:"center"
                                }}
                            >

                                Canvas Here

                            </div>

                        </div>

                    </div>

                </div>

                {/* Sidebar */}

                <div
                    className="col-md-2"
                >

                    <div className="card shadow">

                        <div className="card-body">

                            <h5>

                                Drawing Info

                            </h5>

                            <hr/>

                            <p>

                                Status :

                                {

                                    drawing
                                        ?.status ??

                                    "New"

                                }

                            </p>

                            <p>

                                Version :

                                {

                                    drawing
                                        ?.version ??

                                    0

                                }

                            </p>

                            <button
                                className="btn btn-success w-100 mb-2"
                            >

                                Save Draft

                            </button>

                            <button
                                className="btn btn-danger w-100"
                            >

                                Finalize

                            </button>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    );

}