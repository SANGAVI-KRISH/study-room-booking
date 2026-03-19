import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NotificationBell from "../components/NotificationBell";
import { getAdminDashboardStats } from "../api/roomApi";

export default function AdminDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "Admin";
  const role = localStorage.getItem("role");

  const [dashboardStats, setDashboardStats] = useState({
    totalRooms: 0,
    totalUsers: 0,
    totalBookings: 0,
    activeBookings: 0,
    cancelledBookings: 0,
    mostBookedRoom: "No data",
    peakBookingHour: "No data",
    roomUsageTrends: [],
  });

  const [loadingStats, setLoadingStats] = useState(true);
  const [statsError, setStatsError] = useState("");

  useEffect(() => {
    if (role === "ADMIN") {
      loadDashboardStats();
    }
  }, [role]);

  const loadDashboardStats = async () => {
    try {
      setLoadingStats(true);
      setStatsError("");

      const data = await getAdminDashboardStats();

      setDashboardStats({
        totalRooms: data?.totalRooms ?? 0,
        totalUsers: data?.totalUsers ?? 0,
        totalBookings: data?.totalBookings ?? 0,
        activeBookings: data?.activeBookings ?? 0,
        cancelledBookings: data?.cancelledBookings ?? 0,
        mostBookedRoom: data?.mostBookedRoom || "No data",
        peakBookingHour: data?.peakBookingHour || "No data",
        roomUsageTrends: Array.isArray(data?.roomUsageTrends)
          ? data.roomUsageTrends
          : [],
      });
    } catch (error) {
      console.error("Failed to load admin dashboard stats:", error);
      setStatsError(error.message || "Failed to load dashboard statistics");
    } finally {
      setLoadingStats(false);
    }
  };

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

      <div style={styles.section}>
        <div style={styles.sectionHeader}>
          <h2 style={styles.sectionTitle}>System Overview</h2>
          <button style={styles.refreshButton} onClick={loadDashboardStats}>
            Refresh
          </button>
        </div>

        {loadingStats ? (
          <div style={styles.loadingBox}>Loading dashboard statistics...</div>
        ) : statsError ? (
          <div style={styles.errorBox}>{statsError}</div>
        ) : (
          <>
            <div style={styles.statsGrid}>
              <div style={styles.statCard}>
                <h3 style={styles.statTitle}>Total Rooms</h3>
                <p style={styles.statValue}>{dashboardStats.totalRooms}</p>
              </div>

              <div style={styles.statCard}>
                <h3 style={styles.statTitle}>Total Users</h3>
                <p style={styles.statValue}>{dashboardStats.totalUsers}</p>
              </div>

              <div style={styles.statCard}>
                <h3 style={styles.statTitle}>Total Bookings</h3>
                <p style={styles.statValue}>{dashboardStats.totalBookings}</p>
              </div>

              <div style={styles.statCard}>
                <h3 style={styles.statTitle}>Active Bookings</h3>
                <p style={styles.statValue}>{dashboardStats.activeBookings}</p>
              </div>

              <div style={styles.statCard}>
                <h3 style={styles.statTitle}>Cancelled Bookings</h3>
                <p style={styles.statValue}>{dashboardStats.cancelledBookings}</p>
              </div>

              <div style={styles.infoCard}>
                <h3 style={styles.statTitle}>Most Booked Room</h3>
                <p style={styles.infoValue}>{dashboardStats.mostBookedRoom}</p>
              </div>

              <div style={styles.infoCard}>
                <h3 style={styles.statTitle}>Peak Booking Hours</h3>
                <p style={styles.infoValue}>{dashboardStats.peakBookingHour}</p>
              </div>
            </div>

            <div style={styles.trendContainer}>
              <h3 style={styles.trendTitle}>Room Usage Trends</h3>

              {dashboardStats.roomUsageTrends.length === 0 ? (
                <p style={styles.noDataText}>No room usage trend data available.</p>
              ) : (
                <div style={styles.tableWrapper}>
                  <table style={styles.table}>
                    <thead>
                      <tr>
                        <th style={styles.tableHeader}>Room Name</th>
                        <th style={styles.tableHeader}>Booking Count</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboardStats.roomUsageTrends.map((trend, index) => (
                        <tr key={index}>
                          <td style={styles.tableCell}>
                            {trend.roomName || "Unknown Room"}
                          </td>
                          <td style={styles.tableCell}>
                            {trend.bookingCount ?? 0}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </>
        )}
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
  <h3 style={styles.cardTitle}>Time Slot Management</h3>
  <p style={styles.cardText}>
    Configure room timings, slot duration, and maintenance blocks.
  </p>
  <button
    style={styles.buttonAsButton}
    onClick={() => navigate("/manage-time-slots")}
  >
    Manage Time Slots
  </button>
</div>

<div style={styles.card}>
  <h3 style={styles.cardTitle}>View Room Slots</h3>
  <p style={styles.cardText}>
    Check available time slots for rooms before booking.
  </p>
  <button
    style={styles.buttonAsButton}
    onClick={() => navigate("/room-slots")}
  >
    View Slots
  </button>
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
          <button
            style={styles.buttonAsButton}
            onClick={() => navigate("/admin/reports")}
          >
            View Reports
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
    fontSize: "48px",
    fontWeight: "700",
  },
  subtext: {
    margin: 0,
    color: "#555",
    fontSize: "18px",
  },
  section: {
    marginTop: "30px",
    marginBottom: "35px",
    backgroundColor: "#ffffff",
    borderRadius: "14px",
    padding: "24px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "16px",
    flexWrap: "wrap",
    marginBottom: "20px",
  },
  sectionTitle: {
    margin: 0,
    color: "#222",
    fontSize: "28px",
    fontWeight: "700",
  },
  refreshButton: {
    padding: "10px 16px",
    backgroundColor: "#17a2b8",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "600",
  },
  loadingBox: {
    padding: "20px",
    borderRadius: "10px",
    backgroundColor: "#eef4ff",
    color: "#1f4b99",
    textAlign: "center",
    fontWeight: "600",
  },
  errorBox: {
    padding: "20px",
    borderRadius: "10px",
    backgroundColor: "#fdecea",
    color: "#b42318",
    textAlign: "center",
    fontWeight: "600",
  },
  statsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "20px",
  },
  statCard: {
    backgroundColor: "#f8fbff",
    borderRadius: "12px",
    padding: "20px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
    textAlign: "center",
    border: "1px solid #e3eefc",
  },
  infoCard: {
    backgroundColor: "#fffaf3",
    borderRadius: "12px",
    padding: "20px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
    textAlign: "center",
    border: "1px solid #f4e2b8",
  },
  statTitle: {
    margin: "0 0 10px 0",
    color: "#333",
    fontSize: "18px",
    fontWeight: "600",
  },
  statValue: {
    margin: 0,
    fontSize: "34px",
    fontWeight: "700",
    color: "#007bff",
  },
  infoValue: {
    margin: 0,
    fontSize: "18px",
    fontWeight: "600",
    color: "#8a5a00",
    lineHeight: "1.5",
  },
  trendContainer: {
    marginTop: "30px",
  },
  trendTitle: {
    marginBottom: "14px",
    color: "#222",
    fontSize: "22px",
    fontWeight: "700",
  },
  noDataText: {
    color: "#666",
    fontSize: "16px",
  },
  tableWrapper: {
    overflowX: "auto",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    backgroundColor: "#fff",
    borderRadius: "10px",
    overflow: "hidden",
  },
  tableHeader: {
    backgroundColor: "#007bff",
    color: "#fff",
    padding: "14px",
    textAlign: "left",
    fontSize: "15px",
  },
  tableCell: {
    padding: "14px",
    borderBottom: "1px solid #e5e7eb",
    color: "#333",
    fontSize: "15px",
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