import { Link, useNavigate } from "react-router-dom";
import NotificationBell from "../components/NotificationBell";

export default function AdminDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "Admin";
  const role = localStorage.getItem("role");

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("userId");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    localStorage.removeItem("name");
    navigate("/");
  };

  if (role !== "ADMIN") {
    return (
      <div style={styles.accessDeniedContainer}>
        <h2 style={styles.accessDeniedHeading}>Access Denied</h2>
        <p style={styles.accessDeniedText}>
          You are not authorized to view the Admin Dashboard.
        </p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.topBar}>
        <div>
          <h1 style={styles.heading}>Admin Dashboard</h1>
          <p style={styles.subtext}>Welcome, {name}</p>
        </div>

        <div style={styles.topBarRight}>
          <NotificationBell />
        </div>
      </div>

      <div style={styles.cardContainer}>
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Manage Study Rooms</h3>
          <p style={styles.cardText}>
            Add, update, and delete study room details.
          </p>
          <Link to="/admin/rooms" style={styles.button}>
            Go to Manage Rooms
          </Link>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Manage Users</h3>
          <p style={styles.cardText}>
            View and manage students, staff, and admin users.
          </p>
          <button style={styles.disabledButton}>Coming Soon</button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Manage Bookings</h3>
          <p style={styles.cardText}>
            View and control all room booking activities.
          </p>
          <button
            style={styles.buttonAsButton}
            onClick={() => navigate("/admin/bookings")}
          >
            Manage Bookings
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Booking Approval</h3>
          <p style={styles.cardText}>
            Approve or reject pending booking requests.
          </p>
          <button
            style={styles.buttonAsButton}
            onClick={() => navigate("/admin/booking-approval")}
          >
            View Requests
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Notifications</h3>
          <p style={styles.cardText}>
            View booking alerts, reminders, approvals, and updates.
          </p>
          <button
            style={styles.buttonAsButton}
            onClick={() => navigate("/notifications")}
          >
            Open Notifications
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Reports</h3>
          <p style={styles.cardText}>
            Check room usage reports and booking statistics.
          </p>
          <button style={styles.disabledButton}>Coming Soon</button>
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
    fontSize: "48px",
    fontWeight: "700",
  },
  subtext: {
    margin: 0,
    color: "#555",
    fontSize: "18px",
  },
  cardContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
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
    display: "inline-block",
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    textDecoration: "none",
    borderRadius: "6px",
    border: "none",
    cursor: "pointer",
    fontWeight: "500",
  },
  buttonAsButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "500",
  },
  disabledButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#999",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "not-allowed",
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