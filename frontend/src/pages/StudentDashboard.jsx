import { useNavigate } from "react-router-dom";
import NotificationBell from "../components/NotificationBell";

export default function StudentDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "User";
  const role = (localStorage.getItem("role") || "").toUpperCase();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  if (!["STUDENT", "ADMIN", "STAFF"].includes(role)) {
    return (
      <div style={styles.accessDeniedContainer}>
        <h2 style={styles.accessDeniedHeading}>Access Denied</h2>
        <p style={styles.accessDeniedText}>
          You are not authorized to view this Dashboard.
        </p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      {/* Top Bar */}
      <div style={styles.topBar}>
        <div>
          <h1 style={styles.heading}>Student Dashboard</h1>
          <p style={styles.subtext}>Welcome, {name}</p>
        </div>
        <div style={styles.topBarRight}>
          <NotificationBell />
        </div>
      </div>

      {/* Cards Section */}
      <div style={styles.cardContainer}>
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Book Study Room</h3>
          <p style={styles.cardText}>
            Reserve a study room for your study session quickly and easily.
          </p>
          <button
            style={styles.button}
            onClick={() => navigate("/rooms/availability")}
          >
            Book Now
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>My Bookings</h3>
          <p style={styles.cardText}>
            View your current, upcoming, checked-in, and auto-cancelled bookings.
          </p>
          <button
            style={styles.secondaryButton}
            onClick={() => navigate("/my-bookings")}
          >
            View My Bookings
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Booking History</h3>
          <p style={styles.cardText}>
            Check completed, cancelled, rejected, and past bookings anytime.
          </p>
          <button
            style={styles.secondaryButton}
            onClick={() => navigate("/booking-history")}
          >
            View History
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Notifications</h3>
          <p style={styles.cardText}>
            View reminders, approvals, cancellations, waitlist alerts, and updates.
          </p>
          <button
            style={styles.secondaryButton}
            onClick={() => navigate("/notifications")}
          >
            Open Notifications
          </button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Join Waitlist</h3>
          <p style={styles.cardText}>
            If a slot is full, join the waitlist and get a chance when it becomes available.
          </p>
          <button
            style={styles.waitlistButton}
            onClick={() => navigate("/waitlist")}
          >
            Open Waitlist
          </button>
        </div>

        {/* My Profile Card */}
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>My Profile</h3>
          <p style={styles.cardText}>
            View and manage your personal details and account information.
          </p>
          <button
            style={styles.secondaryButton}
            onClick={() => navigate("/profile")}
          >
            Open Profile
          </button>
        </div>
      </div>

      {/* Logout Button */}
      <button onClick={handleLogout} style={styles.logoutButton}>
        Logout
      </button>
    </div>
  );
}

// Styles
const styles = {
  container: {
    minHeight: "100vh",
    padding: "40px",
    background: "linear-gradient(135deg, rgb(244, 246, 248) 0%, rgb(232, 240, 254) 100%)",
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
    color: "#1f2937",
    fontSize: "42px",
    fontWeight: "700",
  },
  subtext: {
    margin: 0,
    color: "#4b5563",
    fontSize: "18px",
  },
  cardContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
    gap: "20px",
    marginTop: "30px",
  },
  card: {
    backgroundColor: "#ffffff",
    padding: "24px",
    borderRadius: "14px",
    boxShadow: "0 6px 16px rgba(0,0,0,0.08)",
    textAlign: "center",
    transition: "transform 0.2s ease, box-shadow 0.2s ease",
  },
  cardTitle: {
    marginBottom: "10px",
    color: "#1f2937",
    fontSize: "20px",
    fontWeight: "700",
  },
  cardText: {
    color: "#555",
    minHeight: "60px",
    lineHeight: "1.5",
    marginBottom: "10px",
  },
  button: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    minWidth: "170px",
    fontWeight: "600",
    fontSize: "14px",
  },
  secondaryButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    minWidth: "170px",
    fontWeight: "600",
    fontSize: "14px",
  },
  waitlistButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#6f42c1",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    minWidth: "170px",
    fontWeight: "600",
    fontSize: "14px",
  },
  logoutButton: {
    display: "block",
    margin: "40px auto 0",
    padding: "12px 22px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    fontSize: "16px",
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
    fontSize: "32px",
    fontWeight: "700",
  },
  accessDeniedText: {
    color: "#555",
    fontSize: "16px",
  },
};