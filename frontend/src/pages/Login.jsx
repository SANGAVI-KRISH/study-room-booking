import { useState } from "react";
import { loginUser } from "../api/api";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      alert("Please enter email and password");
      return;
    }

    try {
      setLoading(true);

      const data = await loginUser(email.trim(), password);

      console.log("Login response:", data);

      localStorage.removeItem("token");
      localStorage.removeItem("role");
      localStorage.removeItem("email");
      localStorage.removeItem("userId");

      if (!data?.token) {
        throw new Error("Token not returned from backend");
      }

      if (!data?.role) {
        throw new Error("Role not returned from backend");
      }

      const userId = data.userId ?? data.id ?? data.user?.id ?? null;
      const userEmail = data.email ?? email.trim();

      localStorage.setItem("token", data.token);
      localStorage.setItem("role", data.role);
      localStorage.setItem("email", userEmail);

      if (userId !== null && userId !== undefined) {
        localStorage.setItem("userId", String(userId));
      } else {
        alert("Login successful, but user ID was not returned from backend. Please update backend login response.");
        return;
      }

      alert("Login successful");

      if (data.role === "ADMIN") {
        window.location.href = "/admin";
      } else if (data.role === "STAFF") {
        window.location.href = "/staff";
      } else if (data.role === "STUDENT") {
        window.location.href = "/student";
      } else {
        alert("Unknown role");
      }
    } catch (error) {
      console.error("Login error:", error);
      alert(error.message || "Invalid email or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <form onSubmit={handleLogin} style={styles.form}>
        <h2 style={styles.heading}>Login</h2>

        <input
          type="email"
          placeholder="Enter email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={styles.input}
        />

        <input
          type="password"
          placeholder="Enter password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          style={styles.input}
        />

        <button type="submit" style={styles.button} disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "100vh",
    backgroundColor: "#f4f4f4",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    width: "300px",
    padding: "30px",
    borderRadius: "10px",
    backgroundColor: "#fff",
    boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
  },
  heading: {
    textAlign: "center",
    marginBottom: "20px",
  },
  input: {
    marginBottom: "15px",
    padding: "10px",
    fontSize: "16px",
    borderRadius: "5px",
    border: "1px solid #ccc",
  },
  button: {
    padding: "10px",
    fontSize: "16px",
    border: "none",
    borderRadius: "5px",
    backgroundColor: "#007bff",
    color: "#fff",
    cursor: "pointer",
  },
};

export default Login;