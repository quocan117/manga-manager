import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RankingPage from "./pages/RankingPage";
import FloatingMenu from "./components/FloatingMenu";
import MangaDetailPage from "./Mangaka/pages/MangaDeatailPage";
import CreateChapterPage from "./Mangaka/pages/CreateChapterPage";
import DashboardMangaka from "./Mangaka/components/DashboardMangaka";
import MyManga from "./Mangaka/components/MyManga";
import Notification from "./Mangaka/components/Notification";
import MangakaLayout from "./Mangaka/components/MangakaLayout";
import BoardLayout from "./EditorialBoard/components/BoardLayout";
import RankingDecisionPage from "./EditorialBoard/pages/RankingDecisionPage";
import ReviewSeriesPage from "./EditorialBoard/pages/ReviewSeriesPage";
import ReviewMangakaPage from "./EditorialBoard/pages/ReviewMangakaPage";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/mangaka" element={<MangakaLayout />} />
        <Route path="/ranking" element={<RankingPage />} />
        <Route path="/manga/:id" element={<MangaDetailPage />} />
        <Route
          path="/manga/:id/create-chapter"
          element={<CreateChapterPage />}
        />

        <Route path="/mangaka" element={<MangakaLayout />}>
          <Route index element={<DashboardMangaka />} />
          <Route path="manga" element={<MyManga />} />
          <Route path="notifications" element={<Notification />} />
        </Route>

        <Route path="/board" element={<BoardLayout />}>
          <Route index element={<Navigate to="ranking" replace />} />
          <Route path="ranking" element={<RankingDecisionPage />} />
          <Route path="review-series" element={<ReviewSeriesPage />} />
          <Route path="review-mangaka" element={<ReviewMangakaPage />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
