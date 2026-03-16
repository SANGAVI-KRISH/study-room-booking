import { Link, useNavigate } from "react-router-dom";

export default function AdminDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "Admin";
  const role = localStorage.getItem("role");

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    localStorage.removeItem("name");
    navigate("/");
  };

  if (role !== "ADMIN") {
    return (
      <div style={styles.container}>
        <h2>Access Denied</h2>
        <p>You are not authorized to view the Admin Dashboard.</p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Admin Dashboard</h1>
      <p style={styles.subtext}>Welcome, {name}</p>

      <div style={styles.cardContainer}>
        <div style={styles.card}>
          <h3>Manage Study Rooms</h3>
          <p>Add, update, and delete study room details.</p>
          <Link to="/admin/rooms" style={styles.button}>
            Go to Manage Rooms
          </Link>
        </div>

        <div style={styles.card}>
          <h3>Manage Users</h3>
          <p>View and manage students, staff, and admin users.</p>
          <button style={styles.disabledButton}>Coming Soon</button>
        </div>

        <div style={styles.card}>
          <h3>Manage Bookings</h3>
          <p>View and control all room booking activities.</p>
          <button style={styles.disabledButton}>Coming Soon</button>
        </div>

        <div style={styles.card}>
          <h3>Reports</h3>
          <p>Check room usage reports and booking statistics.</p>
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
  heading: {
    textAlign: "center",
    marginBottom: "10px",
    color: "#222",
  },
  subtext: {
    textAlign: "center",
    marginBottom: "30px",
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
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
    textAlign: "center",
  },
  button: {
    display: "inline-block",
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    textDecoration: "none",
    borderRadius: "6px",
  },
  disabledButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#999",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "not-allowed",
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
  },
};