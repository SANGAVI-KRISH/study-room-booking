import { useNavigate } from "react-router-dom";
import NotificationBell from "../components/NotificationBell";

export default function StaffDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "Staff";
  const role = localStorage.getItem("role");

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  if (role !== "STAFF" && role !== "ADMIN") {
    return (
      <div style={styles.accessDeniedContainer}>
        <h2 style={styles.accessDeniedHeading}>Access Denied</h2>
        <p style={styles.accessDeniedText}>
          You are not authorized to view the Staff Dashboard.
        </p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.topBar}>
        <div>
          <h1 style={styles.heading}>Staff Dashboard</h1>
          <p style={styles.subtext}>Welcome, {name}</p>
        </div>

        <div style={styles.topBarRight}>
          <NotificationBell />
        </div>
      </div>

      <div style={styles.cardContainer}>
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>View Study Rooms</h3>
          <p style={styles.cardText}>Check available rooms and room details.</p>
          <button style={styles.button} onClick={() => navigate("/rooms")}>
            View Rooms
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Booking Requests</h3>
          <p style={styles.cardText}>Approve or reject booking requests.</p>
          <button
            style={styles.button}
            onClick={() => navigate("/admin/booking-approval")}
          >
            View Requests
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>All Bookings</h3>
          <p style={styles.cardText}>View all student bookings and history.</p>
          <button
            style={styles.button}
            onClick={() => navigate("/admin/bookings")}
          >
            View Bookings
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Notifications</h3>
          <p style={styles.cardText}>
            View alerts, reminders, approvals, and booking updates.
          </p>
          <button
            style={styles.button}
            onClick={() => navigate("/notifications")}
          >
            Open Notifications
          </button>
        </div>
      </div>

      <button onClick={handleLogout} style={styles.logoutButton}>
        Logout
      </button>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    padding: "40px",
    backgroundColor: "#f4f6f8",
    fontFamily: "Arial, sans-serif",
  },
  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "20px",
    flexWrap: "wrap",
    marginBottom: "10px",
  },
  topBarRight: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    paddingTop: "8px",
  },
  heading: {
    margin: "0 0 10px 0",
    color: "#222",
    fontSize: "42px",
    fontWeight: "700",
  },
  subtext: {
    margin: 0,
    color: "#555",
    fontSize: "18px",
  },
  cardContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
    gap: "20px",
    marginTop: "30px",
  },
  card: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "12px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
    textAlign: "center",
  },
  cardTitle: {
    marginBottom: "10px",
    color: "#222",
  },
  cardText: {
    color: "#555",
    minHeight: "48px",
  },
  button: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "500",
  },
  logoutButton: {
    display: "block",
    margin: "40px auto 0",
    padding: "12px 20px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "600",
  },
  accessDeniedContainer: {
    minHeight: "100vh",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f4f6f8",
    fontFamily: "Arial, sans-serif",
    padding: "20px",
    textAlign: "center",
  },
  accessDeniedHeading: {
    marginBottom: "10px",
    color: "#222",
  },
  accessDeniedText: {
    color: "#555",
  },
};