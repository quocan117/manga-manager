import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import useGuestTracking from "./hooks/useGuestTracking";
import { ROLES } from "./constants/roles";
import PrivateRoute from "./routes/PrivateRoute";

// Trang công khai (không cần đăng nhập)
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RankingPage from "./pages/RankingPage";

// Feature: Mangaka
import MangakaLayout from "./features/mangaka/components/MangakaLayout";
import DashboardMangakaPage from "./features/mangaka/pages/DashboardMangakaPage";
import MyMangaPage from "./features/mangaka/pages/MyMangaPage";
import MangakaNotificationPage from "./features/mangaka/pages/MangakaNotificationPage";
import MangakaRankingPage from "./features/mangaka/pages/MangakaRankingPage";
import CreateSeriesPage from "./features/mangaka/pages/CreateSeriesPage";
import CreateChapterPage from "./features/mangaka/pages/CreateChapterPage";
import AssistantTasksPage from "./features/mangaka/pages/AssistantTasksPage";
import DrawingPage from "./features/mangaka/pages/DrawingPage";
import ChapterPagesPage from "./features/mangaka/pages/ChapterPagesPage";
import ChapterEditorSubmissionPage from "./features/mangaka/pages/ChapterEditorSubmissionPage";
import ManageAssistantsPage from "./features/mangaka/pages/ManageAssistantsPage";

// Feature: Editorial Board
import BoardLayout from "./features/editorialBoard/components/BoardLayout";
import RankingDecisionPage from "./features/editorialBoard/pages/RankingDecisionPage";
import ReviewSeriesPage from "./features/editorialBoard/pages/ReviewSeriesPage";
import ManageUsersPage from "./features/editorialBoard/pages/ManageUsersPage";
import PublishSchedulePage from "./features/editorialBoard/pages/PublishSchedulePage";
import ReaderVotesPage from "./features/editorialBoard/pages/ReaderVotesPage";
import BoardNotificationPage from "./features/editorialBoard/pages/BoardNotificationPage";

// Feature: Assistant
import AssistantLayout from "./features/assistant/components/AssistantLayout";
import DashboardAssistantPage from "./features/assistant/pages/DashboardAssistantPage";
import MyTasksPage from "./features/assistant/pages/MyTasksPage";
import MySubmissionsPage from "./features/assistant/pages/MySubmissionsPage";
import TaskDetailPage from "./features/assistant/pages/TaskDetailPage";
import AssistantNotificationPage from "./features/assistant/pages/AssistantNotificationPage";

// Feature: Tantou Editor
import TantouLayout from "./features/tantouEditor/components/TantouLayout";
import TantouDashboardPage from "./features/tantouEditor/pages/TantouDashboardPage";
import EditorReviewPage from "./features/tantouEditor/pages/EditorReviewPage";
import ChapterReviewPage from "./features/tantouEditor/pages/ChapterReviewPage";
import TantouNotificationsPage from "./features/tantouEditor/pages/TantouNotificationsPage";
import SeriesManagementPage from "./features/editorialBoard/pages/SeriesManagementPage";
import ChapterReviewBoardPage from "./features/editorialBoard/pages/ChapterReviewBoardPage";

function App() {
  useGuestTracking();

  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/ranking" element={<RankingPage />} />

      <Route
        path="/manga/:id/create-chapter"
        element={
          <PrivateRoute role={ROLES.MANGAKA}>
            <CreateChapterPage />
          </PrivateRoute>
        }
      />

      {/* Khu vực Mangaka */}
      <Route
        path="/mangaka"
        element={
          <PrivateRoute role={ROLES.MANGAKA}>
            <MangakaLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<DashboardMangakaPage />} />
        <Route path="manga" element={<MyMangaPage />} />
        <Route path="notifications" element={<MangakaNotificationPage />} />
        <Route path="ranking" element={<MangakaRankingPage />} />
        <Route path="create-series" element={<CreateSeriesPage />} />
        <Route path="tasks" element={<AssistantTasksPage />} />
        <Route path="pages/:pageId/drawing" element={<DrawingPage />} />
        <Route
          path="chapters/:chapterId/pages"
          element={<ChapterPagesPage />}
        />
        <Route
          path="chapters/:chapterId/editor-submission"
          element={<ChapterEditorSubmissionPage />}
        />
        <Route path="manage-assistants" element={<ManageAssistantsPage />} />
      </Route>

      {/* Khu vực Editorial Board */}
      <Route
        path="/board"
        element={
          <PrivateRoute role={ROLES.EDITORIAL_BOARD}>
            <BoardLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="ranking" replace />} />
        <Route path="ranking" element={<RankingDecisionPage />} />
        <Route path="review" element={<ReviewSeriesPage />} />
        <Route path="manage-users" element={<ManageUsersPage />} />
        <Route path="schedule" element={<PublishSchedulePage />} />
        <Route path="reader-votes" element={<ReaderVotesPage />} />
        <Route path="notifications" element={<BoardNotificationPage />} />
        <Route path="series-management" element={<SeriesManagementPage />} />
        <Route
          path="chapters/:chapterId/review"
          element={<ChapterReviewBoardPage />}
        />
      </Route>

      {/* Khu vực Assistant */}
      <Route
        path="/assistant"
        element={
          <PrivateRoute role={ROLES.ASSISTANT}>
            <AssistantLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<DashboardAssistantPage />} />
        <Route path="tasks" element={<MyTasksPage />} />
        <Route path="submissions" element={<MySubmissionsPage />} />
        <Route path="tasks/:taskId" element={<TaskDetailPage />} />
        <Route path="notifications" element={<AssistantNotificationPage />} />
      </Route>

      {/* Khu vực Tantou Editor */}
      <Route
        path="/tantou"
        element={
          <PrivateRoute role={ROLES.TANTOU_EDITOR}>
            <TantouLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<TantouDashboardPage />} />
        <Route path="review/:id" element={<EditorReviewPage />} />
        <Route
          path="chapters/:chapterId/review"
          element={<ChapterReviewPage />}
        />
        <Route path="notifications" element={<TantouNotificationsPage />} />
      </Route>
    </Routes>
  );
}

export default App;
