import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getDailyBookingReport,
  getWeeklyBookingReport,
  getMonthlyBookingReport,
  getRoomUtilizationReport,
  getFrequentlyUsedRoomsReport,
  getCancellationAnalysisReport,
  getUserActivityReport,
} from "../api/roomApi";

export default function AdminReports() {
  const navigate = useNavigate();
  const role = localStorage.getItem("role");

  const [daily, setDaily] = useState(null);
  const [weekly, setWeekly] = useState(null);
  const [monthly, setMonthly] = useState(null);
  const [roomUtilization, setRoomUtilization] = useState([]);
  const [frequentRooms, setFrequentRooms] = useState([]);
  const [cancellation, setCancellation] = useState(null);
  const [userActivity, setUserActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (role === "ADMIN") {
      loadReports();
    }
  }, [role]);

  const loadReports = async () => {
    try {
      setLoading(true);
      setError("");

      const [
        dailyData,
        weeklyData,
        monthlyData,
        roomUtilData,
        frequentRoomsData,
        cancellationData,
        userActivityData,
      ] = await Promise.all([
        getDailyBookingReport(),
        getWeeklyBookingReport(),
        getMonthlyBookingReport(),
        getRoomUtilizationReport(),
        getFrequentlyUsedRoomsReport(),
        getCancellationAnalysisReport(),
        getUserActivityReport(),
      ]);

      setDaily(dailyData);
      setWeekly(weeklyData);
      setMonthly(monthlyData);
      setRoomUtilization(Array.isArray(roomUtilData) ? roomUtilData : []);
      setFrequentRooms(Array.isArray(frequentRoomsData) ? frequentRoomsData : []);
      setCancellation(cancellationData);
      setUserActivity(Array.isArray(userActivityData) ? userActivityData : []);
    } catch (error) {
      console.error("Failed to load reports:", error);
      setError(error.message || "Failed to load reports");
    } finally {
      setLoading(false);
    }
  };

  if (role !== "ADMIN") {
    return (
      <div style={styles.accessDeniedContainer}>
        <h2 style={styles.accessDeniedHeading}>Access Denied</h2>
        <p style={styles.accessDeniedText}>
          You are not authorized to view Reports and Analytics.
        </p>
      </div>
    );
  }

  if (loading) {
    return <div style={styles.loadingBox}>Loading reports...</div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.topBar}>
        <div>
          <h1 style={styles.heading}>Reports and Analytics</h1>
          <p style={styles.subtext}>
            View booking insights, room usage, cancellations, and user activity.
          </p>
        </div>

        <div style={styles.topBarButtons}>
          <button style={styles.secondaryButton} onClick={loadReports}>
            Refresh
          </button>
          <button
            style={styles.primaryButton}
            onClick={() => navigate("/admin")}
          >
            Back to Dashboard
          </button>
        </div>
      </div>

      {error ? <div style={styles.errorBox}>{error}</div> : null}

      <div style={styles.cardRow}>
        <div style={styles.cardStyle}>
          <h3 style={styles.cardTitle}>Daily Bookings</h3>
          <p style={styles.cardValue}>{daily?.totalBookings ?? 0}</p>
        </div>

        <div style={styles.cardStyle}>
          <h3 style={styles.cardTitle}>Weekly Bookings</h3>
          <p style={styles.cardValue}>{weekly?.totalBookings ?? 0}</p>
        </div>

        <div style={styles.cardStyle}>
          <h3 style={styles.cardTitle}>Monthly Bookings</h3>
          <p style={styles.cardValue}>{monthly?.totalBookings ?? 0}</p>
        </div>
      </div>

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Room Utilization Report</h2>

        {roomUtilization.length === 0 ? (
          <p style={styles.noDataText}>No room utilization data available.</p>
        ) : (
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>Room Name</th>
                  <th style={styles.tableHeader}>Total Bookings</th>
                  <th style={styles.tableHeader}>Total Hours Booked</th>
                </tr>
              </thead>
              <tbody>
                {roomUtilization.map((room, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{room.roomName}</td>
                    <td style={styles.tableCell}>{room.totalBookings}</td>
                    <td style={styles.tableCell}>{room.totalHoursBooked}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Frequently Used Rooms</h2>

        {frequentRooms.length === 0 ? (
          <p style={styles.noDataText}>No frequently used room data available.</p>
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
                {frequentRooms.map((room, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{room.roomName}</td>
                    <td style={styles.tableCell}>{room.bookingCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Cancellation Analysis</h2>

        <div style={styles.cardRow}>
          <div style={styles.cardStyle}>
            <h3 style={styles.cardTitle}>Cancelled</h3>
            <p style={styles.cardValue}>
              {cancellation?.cancelledBookings ?? 0}
            </p>
          </div>

          <div style={styles.cardStyle}>
            <h3 style={styles.cardTitle}>Auto Cancelled</h3>
            <p style={styles.cardValue}>
              {cancellation?.autoCancelledBookings ?? 0}
            </p>
          </div>

          <div style={styles.cardStyle}>
            <h3 style={styles.cardTitle}>Total Cancellations</h3>
            <p style={styles.cardValue}>
              {cancellation?.totalCancellations ?? 0}
            </p>
          </div>
        </div>
      </section>

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>User Activity Report</h2>

        {userActivity.length === 0 ? (
          <p style={styles.noDataText}>No user activity data available.</p>
        ) : (
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>User Name</th>
                  <th style={styles.tableHeader}>Email</th>
                  <th style={styles.tableHeader}>Total Bookings</th>
                </tr>
              </thead>
              <tbody>
                {userActivity.map((user, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{user.userName}</td>
                    <td style={styles.tableCell}>{user.email}</td>
                    <td style={styles.tableCell}>{user.totalBookings}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    padding: "32px",
    backgroundColor: "#f4f6f8",
    fontFamily: "Arial, sans-serif",
  },
  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "20px",
    flexWrap: "wrap",
    marginBottom: "24px",
  },
  topBarButtons: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },
  heading: {
    margin: "0 0 8px 0",
    color: "#222",
    fontSize: "38px",
    fontWeight: "700",
  },
  subtext: {
    margin: 0,
    color: "#555",
    fontSize: "16px",
  },
  section: {
    marginBottom: "28px",
    backgroundColor: "#ffffff",
    borderRadius: "14px",
    padding: "24px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  sectionTitle: {
    margin: "0 0 16px 0",
    color: "#222",
    fontSize: "24px",
    fontWeight: "700",
  },
  cardRow: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "20px",
    marginBottom: "28px",
  },
  cardStyle: {
    border: "1px solid #dfe6ee",
    borderRadius: "12px",
    padding: "20px",
    textAlign: "center",
    backgroundColor: "#ffffff",
    boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
  },
  cardTitle: {
    margin: "0 0 10px 0",
    color: "#333",
    fontSize: "18px",
    fontWeight: "600",
  },
  cardValue: {
    margin: 0,
    fontSize: "30px",
    fontWeight: "700",
    color: "#007bff",
  },
  tableWrapper: {
    overflowX: "auto",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    backgroundColor: "#fff",
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
  noDataText: {
    color: "#666",
    fontSize: "16px",
    margin: 0,
  },
  loadingBox: {
    minHeight: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f4f6f8",
    color: "#1f4b99",
    fontWeight: "600",
    fontSize: "18px",
    fontFamily: "Arial, sans-serif",
  },
  errorBox: {
    padding: "16px",
    borderRadius: "10px",
    backgroundColor: "#fdecea",
    color: "#b42318",
    marginBottom: "20px",
    fontWeight: "600",
  },
  primaryButton: {
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "600",
  },
  secondaryButton: {
    padding: "10px 16px",
    backgroundColor: "#17a2b8",
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