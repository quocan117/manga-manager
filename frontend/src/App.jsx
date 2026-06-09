import { useEffect, useState } from "react";

function App() {
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetch("http://localhost:8080/api/test")
      .then((res) => res.text())
      .then((data) => setMessage(data))
      .catch((err) => console.error(err));
  }, []);

  return (
    <div>
      <h1>Frontend React</h1>
      <p>{message}</p>
    </div>
  );
}
// alooo
export default App;