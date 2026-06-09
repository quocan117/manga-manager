import { useEffect, useState } from "react";

function App() {
  const [message, setMessage] = useState("Đang chờ phản hồi từ Backend...");
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    // Gọi thẳng vào API vừa tạo bên Spring Boot
    fetch("http://localhost:8080/api/ping")
      .then((res) => {
        if (!res.ok) throw new Error("Mạng bị lỗi");
        return res.text();
      })
      .then((data) => {
        setMessage(data);
        setIsSuccess(true);
      })
      .catch((err) => {
        console.error("Lỗi:", err);
        setMessage("❌ Lỗi kết nối! Hãy kiểm tra xem Backend đã bật chưa.");
        setIsSuccess(false);
      });
  }, []);

  return (
    <div style={{ textAlign: "center", marginTop: "100px", fontFamily: "Arial" }}>
      <h1>Manga Manager - Test Hệ Thống</h1>
      
      <div style={{ 
        padding: "20px", 
        border: isSuccess ? "2px solid green" : "2px solid red",
        display: "inline-block",
        borderRadius: "10px",
        backgroundColor: isSuccess ? "#e8f5e9" : "#ffebee"
      }}>
        <h2>{message}</h2>
      </div>
    </div>
  );
}

export default App;