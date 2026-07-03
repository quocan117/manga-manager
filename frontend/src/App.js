import React, { useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RankingPage from "./pages/RankingPage";
import MangaDetailPage from "./Mangaka/pages/MangaDeatailPage";
import CreateChapterPage from "./Mangaka/pages/CreateChapterPage";
import DashboardMangaka from "./Mangaka/components/DashboardMangaka";
import MyManga from "./Mangaka/components/MyManga";
import Notification from "./Mangaka/components/Notification";
import MangakaLayout from "./Mangaka/components/MangakaLayout";
import BoardLayout from "./EditorialBoard/components/BoardLayout";
import ReviewSeriesPage from "./EditorialBoard/pages/ReviewSeriesPage";
import RankingDecisionPage from "./EditorialBoard/pages/RankingDecisionPage";
import Settings from "./Mangaka/components/Settings";
import Ranking from "./Mangaka/components/Ranking";
import PrivateRoute from "./Route/PrivateRoute";
import CreateSeriesPage from "./Mangaka/pages/CreateSeriesPage";
import AssistantTasks from "./Mangaka/components/AssistantTasks";
import AssistantLayout from "./Assistant/components/AssistantLayout";
import DashboardAssistant from "./Assistant/components/DashboardAssistant";
import MyTasks from "./Assistant/components/MyTasks";
import MySubmissions from "./Assistant/components/MySubmissions";
import DrawingPage from "./Mangaka/pages/DrawingPage";
import TantouDashboard from "./TantouEditor/pages/TantouDashboard";
import EditorReviewPage from "./TantouEditor/pages/EditorReviewPage";
import ScheduleManagement from "./TantouEditor/pages/ScheduleManagement";
import TantouLayout from "./TantouEditor/components/TantouLayout";
import ChapterPages from "./Mangaka/pages/ChapterPages";
import ManageUsersPage from "./EditorialBoard/pages/ManageUsersPage";
import ManageAssistants from "./Mangaka/pages/ManageAssistants";
import TantouNotifications from "./TantouEditor/pages/TantouNotifications";

function App() {
  useEffect(() => {
    const trackGuestAccess = async () => {
      let sessionToken = localStorage.getItem("guest_session_token");
      if (!sessionToken) {
        sessionToken = crypto.randomUUID();
        localStorage.setItem("guest_session_token", sessionToken);
      }
      try {
        await fetch("http://localhost:8080/api/guest/access", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            sessionToken: sessionToken,
            userAgent: navigator.userAgent,
          }),
        });
        console.log("Đã ghi nhận khách truy cập thành công!");
      } catch (error) {
        console.warn("API truy cập bị lỗi  chưa sẵn sàng.", error.message);
      }
    };
    trackGuestAccess();
  }, []);

  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/ranking" element={<RankingPage />} />
        <Route path="/manga/:id" element={<MangaDetailPage />} />

        {/**Route Mangaka */}
        <Route
          path="/mangaka"
          element={
            <PrivateRoute role="MANGAKA">
              <MangakaLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<DashboardMangaka />} />
          <Route path="manga" element={<MyManga />} />
          <Route
            path="/manga/:id/create-chapter"
            element={<CreateChapterPage />}
          />
          <Route path="notifications" element={<Notification />} />
          <Route path="settings" element={<Settings />} />
          <Route path="ranking" element={<Ranking />} />
          <Route path="create-series" element={<CreateSeriesPage />} />
          <Route path="tasks" element={<AssistantTasks />} />
          <Route path="pages/:pageId/drawing" element={<DrawingPage />} />
          <Route path="chapters/:chapterId/pages" element={<ChapterPages />} />
          <Route path="manage-assistants" element={<ManageAssistants />} />
        </Route>

        {/**Route Editorial Board */}
        <Route
          path="/board"
          element={
            <PrivateRoute role="EDITORIAL_BOARD">
              <BoardLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="ranking" replace />} />
          <Route path="ranking" element={<RankingDecisionPage />} />
          <Route path="review" element={<ReviewSeriesPage />} />
          <Route path="manage-users" element={<ManageUsersPage />} />
        </Route>

        {/**Route Assistant */}
        <Route
          path="/assistant"
          element={
            <PrivateRoute role="ASSISTANT">
              <AssistantLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<DashboardAssistant />} />
          <Route path="tasks" element={<MyTasks />} />
          <Route path="submissions" element={<MySubmissions />} />
        </Route>

        {/* Route tantou editor */}
        <Route
          path="/tantou"
          element={
            <PrivateRoute role="TANTOU_EDITOR">
              <TantouLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<TantouDashboard />} />
          <Route path="review/:id" element={<EditorReviewPage />} />
          <Route path="schedule" element={<ScheduleManagement />} />
          <Route path="notifications" element={<TantouNotifications />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
