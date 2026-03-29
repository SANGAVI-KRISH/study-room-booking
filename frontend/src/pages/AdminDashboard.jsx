import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NotificationBell from "../components/NotificationBell";
import { getAdminDashboardStats } from "../api/roomApi";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const navigate = useNavigate();

  const name = localStorage.getItem("name") || "Admin";
  const email = localStorage.getItem("email") || "Not available";
  const role = (localStorage.getItem("role") || "").trim().toUpperCase();

  const [dashboardStats, setDashboardStats] = useState({
    totalRooms: 0,
    totalUsers: 0,
    totalBookings: 0,
    activeBookings: 0,
    cancelledBookings: 0,
    autoCancelledBookings: 0,
    pendingBookings: 0,
    approvedBookings: 0,
    mostBookedRoom: "No data",
    peakBookingHour: "No data",
    roomUsageTrends: [],
  });

  const [loadingStats, setLoadingStats] = useState(true);
  const [statsError, setStatsError] = useState("");
  const [lastUpdated, setLastUpdated] = useState(new Date());

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
        autoCancelledBookings: data?.autoCancelledBookings ?? 0,
        pendingBookings: data?.pendingBookings ?? 0,
        approvedBookings: data?.approvedBookings ?? 0,
        mostBookedRoom: data?.mostBookedRoom || "No data",
        peakBookingHour: data?.peakBookingHour || "No data",
        roomUsageTrends: Array.isArray(data?.roomUsageTrends)
          ? data.roomUsageTrends
          : [],
      });
      setLastUpdated(new Date());
    } catch (error) {
      console.error("Failed to load admin dashboard stats:", error);
      setStatsError(error.message || "Failed to load dashboard statistics");
    } finally {
      setLoadingStats(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  if (role !== "ADMIN") {
    return (
      <div className="access-denied-container">
        <div className="access-denied-content">
          <div className="access-denied-icon">🚫</div>
          <h2 className="access-denied-heading">Access Denied</h2>
          <p className="access-denied-text">
            You are not authorized to view the Admin Dashboard.
          </p>
          <button className="back-button" onClick={() => navigate("/")}>
            Go to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      {/* Top Bar */}
      <div className="top-bar">
        <div className="welcome-section">
          <h1 className="dashboard-heading">Admin Dashboard</h1>
          <p className="welcome-text">Welcome back, {name}</p>
          <p className="email-text">{email}</p>
        </div>
        <div className="top-bar-right">
          <NotificationBell />
        </div>
      </div>

      {/* Dashboard Stats Section */}
      <div className="stats-section">
        <div className="section-header">
          <div>
            <h2 className="section-title">System Overview</h2>
            <p className="last-updated">Last updated: {lastUpdated.toLocaleTimeString()}</p>
          </div>
          <button className="refresh-button" onClick={loadDashboardStats}>
            <span className="refresh-icon">⟳</span> Refresh
          </button>
        </div>

        {loadingStats ? (
          <div className="loading-container">
            <div className="spinner"></div>
            <p>Loading dashboard statistics...</p>
          </div>
        ) : statsError ? (
          <div className="error-container">
            <span className="error-icon">⚠️</span>
            <p>{statsError}</p>
            <button className="retry-button" onClick={loadDashboardStats}>Retry</button>
          </div>
        ) : (
          <>
            <div className="stats-grid">
              <StatCard 
                title="Total Rooms" 
                value={dashboardStats.totalRooms} 
                icon="🏢"
                trend="+12%"
              />
              <StatCard 
                title="Total Users" 
                value={dashboardStats.totalUsers} 
                icon="👥"
                trend="+8%"
              />
              <StatCard 
                title="Total Bookings" 
                value={dashboardStats.totalBookings} 
                icon="📅"
                trend="+23%"
              />
              <StatCard 
                title="Active Bookings" 
                value={dashboardStats.activeBookings} 
                icon="🟢"
                trend="+5%"
              />
              <StatCard 
                title="Pending Bookings" 
                value={dashboardStats.pendingBookings} 
                icon="⏳"
                warning
              />
              <StatCard 
                title="Approved Bookings" 
                value={dashboardStats.approvedBookings} 
                icon="✅"
                success
              />
              <StatCard 
                title="Cancelled Bookings" 
                value={dashboardStats.cancelledBookings} 
                icon="❌"
                danger
              />
              <StatCardWarning 
                title="Auto Cancelled" 
                value={dashboardStats.autoCancelledBookings} 
                icon="⚠️"
              />
              <InfoCard 
                title="Most Booked Room" 
                value={dashboardStats.mostBookedRoom} 
                icon="🏆"
              />
              <InfoCard 
                title="Peak Booking Hours" 
                value={dashboardStats.peakBookingHour} 
                icon="⏰"
              />
            </div>

            <TrendTable trends={dashboardStats.roomUsageTrends} />
          </>
        )}
      </div>

      {/* Management Cards */}
      <div className="management-section">
        <h2 className="management-title">Quick Actions</h2>
        <div className="card-container">
          <ManagementCard
            title="Manage Study Rooms"
            description="Add, update, and delete study room details."
            icon="🏢"
            onClick={() => navigate("/admin/rooms")}
          />
          <ManagementCard
            title="Time Slot Management"
            description="Configure room timings, slot duration, and maintenance blocks."
            icon="⏰"
            onClick={() => navigate("/manage-time-slots")}
          />
          <ManagementCard
            title="View Room Slots"
            description="Check available time slots for rooms before booking."
            icon="👁️"
            onClick={() => navigate("/room-slots")}
          />
          <ManagementCard 
            title="Manage Users" 
            description="View and manage students, staff, and admin users."
            icon="👥"
            onClick={() => navigate("/admin/users")}
          />
          <ManagementCard
            title="Booking Approval"
            description="Approve or reject pending booking requests."
            icon="✓"
            primary
            onClick={() => navigate("/admin/booking-approval")}
          />
          <ManagementCard
            title="Waitlist Management"
            description="View all waitlist entries."
            icon="📋"
            purple
            onClick={() => navigate("/waitlist")}
          />
          <ManagementCard
            title="View Feedback"
            description="View ratings, comments, and maintenance issues submitted by students."
            icon="💬"
            onClick={() => navigate("/admin/feedback")}
          />
          <ManagementCard
            title="Notifications"
            description="View booking alerts, reminders, approvals, waitlist and updates."
            icon="🔔"
            onClick={() => navigate("/notifications")}
          />
          <ManagementCard
            title="Reports"
            description="Check room usage reports and booking statistics."
            icon="📊"
            onClick={() => navigate("/admin/reports")}
          />
          <ManagementCard
            title="View Profile"
            description="Check your admin profile details."
            icon="👤"
            onClick={() => navigate("/profile")}
          />
        </div>
      </div>

      {/* Logout Button */}
      <button onClick={handleLogout} className="logout-button">
        <span className="logout-icon">🚪</span> Logout
      </button>
    </div>
  );
}

/* ---------- Reusable Components ---------- */
const StatCard = ({ title, value, icon, trend, warning, success, danger }) => {
  let cardClass = "stat-card";
  if (warning) cardClass = "stat-card-warning";
  if (success) cardClass = "stat-card-success";
  if (danger) cardClass = "stat-card-danger";
  
  return (
    <div className={cardClass}>
      <div className="stat-card-header">
        <span className="stat-icon">{icon}</span>
        <h3 className="stat-title">{title}</h3>
      </div>
      <p className="stat-value">{value}</p>
      {trend && <span className="stat-trend">{trend}</span>}
    </div>
  );
};

const StatCardWarning = ({ title, value, icon }) => (
  <div className="stat-card-warning">
    <div className="stat-card-header">
      <span className="stat-icon">{icon}</span>
      <h3 className="stat-title">{title}</h3>
    </div>
    <p className="warning-value">{value}</p>
  </div>
);

const InfoCard = ({ title, value, icon }) => (
  <div className="info-card">
    <div className="stat-card-header">
      <span className="stat-icon">{icon}</span>
      <h3 className="stat-title">{title}</h3>
    </div>
    <p className="info-value">{value}</p>
  </div>
);

const TrendTable = ({ trends }) => (
  <div className="trend-container">
    <h3 className="trend-title">Room Usage Trends</h3>
    {trends.length === 0 ? (
      <div className="no-data-container">
        <span className="no-data-icon">📊</span>
        <p className="no-data-text">No room usage trend data available.</p>
      </div>
    ) : (
      <div className="table-wrapper">
        <table className="trend-table">
          <thead>
            <tr>
              <th className="table-header">Room Name</th>
              <th className="table-header">Booking Count</th>
              <th className="table-header">Usage %</th>
            </tr>
          </thead>
          <tbody>
            {trends.map((trend, index) => {
              const total = trends.reduce((sum, t) => sum + (t.bookingCount || 0), 0);
              const percentage = total > 0 ? ((trend.bookingCount || 0) / total * 100).toFixed(1) : 0;
              return (
                <tr key={index} className="trend-row">
                  <td className="table-cell">
                    <span className="room-name">{trend.roomName || "Unknown Room"}</span>
                  </td>
                  <td className="table-cell">{trend.bookingCount ?? 0}</td>
                  <td className="table-cell">
                    <div className="progress-bar-container">
                      <div 
                        className="progress-bar-fill" 
                        style={{ width: `${percentage}%` }}
                      ></div>
                      <span className="percentage-text">{percentage}%</span>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    )}
  </div>
);

const ManagementCard = ({ title, description, icon, onClick, disabled, purple, primary }) => {
  let cardClass = "management-card";
  if (purple) cardClass = "management-card management-card-purple";
  if (primary) cardClass = "management-card management-card-primary";
  
  return (
    <div
      className={cardClass}
      onClick={disabled ? null : onClick}
      style={{ cursor: disabled ? "not-allowed" : "pointer" }}
    >
      <div className="card-icon">{icon}</div>
      <h3 className="card-title">{title}</h3>
      <p className="card-description">{description}</p>
      <button
        className="card-button"
        disabled={disabled}
      >
        {disabled ? "Coming Soon" : "Open →"}
      </button>
    </div>
  );
};