import React from "react";
import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import MangakaPage from "./pages/MangakaPage";
import RankingPage from "./pages/RankingPage";
import FloatingMenu from "./components/FloatingMenu";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/mangaka" element={<MangakaPage />} />
        <Route path="/ranking" element={<RankingPage />} />
      </Routes>
      <FloatingMenu />
    </>
  );
}

export default App;
