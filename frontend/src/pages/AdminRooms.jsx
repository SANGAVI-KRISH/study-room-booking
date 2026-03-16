import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function AdminRooms() {
  const navigate = useNavigate();
  const [name, setName] = useState("");

  useEffect(() => {
    const storedName = localStorage.getItem("name");
    const storedRole = localStorage.getItem("role");
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/");
      return;
    }

    if (storedRole !== "ADMIN") {
      alert("Access denied. Admin only.");
      navigate("/");
      return;
    }

    setName(storedName || "Admin");
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    localStorage.removeItem("name");
    navigate("/");
  };

  const goToManageRooms = () => {
    navigate("/manage-rooms");
  };

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <h1 style={styles.heading}>Admin Dashboard</h1>
        <p style={styles.welcome}>Welcome, {name}</p>

        <div style={styles.cardContainer}>
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Manage Study Rooms</h2>
            <p style={styles.cardText}>
              Add, update, and delete study room details.
            </p>
            <button style={styles.primaryButton} onClick={goToManageRooms}>
              Go to Manage Rooms
            </button>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Manage Users</h2>
            <p style={styles.cardText}>
              View and manage students, staff, and admin users.
            </p>
            <button style={styles.disabledButton} disabled>
              Coming Soon
            </button>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Manage Bookings</h2>
            <p style={styles.cardText}>
              View and control all room booking activities.
            </p>
            <button style={styles.disabledButton} disabled>
              Coming Soon
            </button>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Reports</h2>
            <p style={styles.cardText}>
              Check room usage reports and booking statistics.
            </p>
            <button style={styles.disabledButton} disabled>
              Coming Soon
            </button>
          </div>
        </div>

        <div style={styles.logoutWrapper}>
          <button style={styles.logoutButton} onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    backgroundColor: "#eef1f5",
    padding: "40px 20px",
    fontFamily: "Arial, sans-serif",
  },
  container: {
    maxWidth: "1300px",
    margin: "0 auto",
    textAlign: "center",
  },
  heading: {
    fontSize: "52px",
    fontWeight: "700",
    color: "#1f2937",
    marginBottom: "12px",
  },
  welcome: {
    fontSize: "22px",
    color: "#4b5563",
    marginBottom: "40px",
  },
  cardContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
    gap: "24px",
    marginTop: "20px",
  },
  card: {
    backgroundColor: "#ffffff",
    borderRadius: "16px",
    padding: "30px 22px",
    boxShadow: "0 4px 14px rgba(0, 0, 0, 0.08)",
    minHeight: "220px",
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
  },
  cardTitle: {
    fontSize: "22px",
    fontWeight: "700",
    color: "#111827",
    marginBottom: "16px",
  },
  cardText: {
    fontSize: "17px",
    color: "#374151",
    lineHeight: "1.5",
    marginBottom: "24px",
  },
  primaryButton: {
    backgroundColor: "#1d72f3",
    color: "#fff",
    border: "none",
    borderRadius: "10px",
    padding: "12px 18px",
    fontSize: "16px",
    cursor: "pointer",
    fontWeight: "600",
  },
  disabledButton: {
    backgroundColor: "#a3a3a3",
    color: "#fff",
    border: "none",
    borderRadius: "10px",
    padding: "12px 18px",
    fontSize: "16px",
    cursor: "not-allowed",
    fontWeight: "600",
  },
  logoutWrapper: {
    marginTop: "40px",
  },
  logoutButton: {
    backgroundColor: "#e63946",
    color: "#fff",
    border: "none",
    borderRadius: "10px",
    padding: "12px 28px",
    fontSize: "16px",
    cursor: "pointer",
    fontWeight: "600",
  },
};