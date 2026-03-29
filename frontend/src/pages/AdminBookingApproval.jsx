import { useEffect, useState } from "react";
import {
  getBookingsByStatus,
  approveBooking,
  rejectBooking,
} from "../api/roomApi";
import "./AdminBookingApproval.css";

export default function AdminBookingApproval() {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [filterStatus, setFilterStatus] = useState("PENDING");
  const [searchTerm, setSearchTerm] = useState("");

  const approverId = localStorage.getItem("userId");

  const showSuccess = (text) => {
    setMessage(text);
    setMessageType("success");
    setTimeout(() => clearMessage(), 3000);
  };

  const showError = (text) => {
    setMessage(text);
    setMessageType("error");
    setTimeout(() => clearMessage(), 3000);
  };

  const clearMessage = () => {
    setMessage("");
    setMessageType("");
  };

  const loadPendingBookings = async (preserveMessage = false) => {
    try {
      const data = await getBookingsByStatus(filterStatus);
      setBookings(Array.isArray(data) ? data : []);

      if (!preserveMessage) {
        clearMessage();
      }
    } catch (error) {
      console.error("Failed to load pending bookings:", error);
      setBookings([]);
      showError("Failed to load pending bookings");
    }
  };

  useEffect(() => {
    loadPendingBookings();
  }, [filterStatus]);

  const handleApprove = async (bookingId) => {
    try {
      if (!approverId) {
        showError("Logged-in approver ID not found. Please login again.");
        return;
      }

      setActionLoadingId(bookingId);
      clearMessage();

      await approveBooking(bookingId, approverId);
      await loadPendingBookings(true);
      showSuccess("Booking approved successfully");
    } catch (error) {
      console.error("Approve booking failed:", error);
      showError("Unable to approve booking");
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleReject = async (bookingId) => {
    try {
      setActionLoadingId(bookingId);
      clearMessage();

      await rejectBooking(bookingId);
      await loadPendingBookings(true);
      showSuccess("Booking rejected successfully");
    } catch (error) {
      console.error("Reject booking failed:", error);
      showError("Unable to reject booking");
    } finally {
      setActionLoadingId(null);
    }
  };

  const formatDate = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const formatTime = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleTimeString("en-IN", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const filteredBookings = bookings.filter((booking) => {
    if (!searchTerm) return true;
    return (
      booking.bookingId?.toString().includes(searchTerm) ||
      booking.roomName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      booking.userName?.toLowerCase().includes(searchTerm.toLowerCase())
    );
  });

  return (
    <div className="admin-booking-container">
      <div className="header-section">
        <h2 className="heading">Booking Management Dashboard</h2>
        <p className="subheading">Manage and approve pending booking requests</p>
      </div>

      {message && (
        <div className={`message message-${messageType}`}>
          <span className="message-icon">
            {messageType === "success" ? "✓" : "✗"}
          </span>
          <span className="message-text">{message}</span>
          <button className="message-close" onClick={clearMessage}>×</button>
        </div>
      )}

      <div className="controls-section">
        <div className="filter-group">
          <label className="filter-label">Status Filter:</label>
          <select
            className="filter-select"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="ALL">All</option>
          </select>
        </div>

        <div className="search-group">
          <input
            type="text"
            className="search-input"
            placeholder="Search by ID, room or user..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <span className="search-icon">🔍</span>
        </div>

        <button className="refresh-button" onClick={() => loadPendingBookings()}>
          ⟳ Refresh
        </button>
      </div>

      {filteredBookings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <p className="empty-text">No bookings found</p>
          <p className="empty-subtext">
            {searchTerm ? "Try adjusting your search" : "All caught up!"}
          </p>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="booking-table">
            <thead>
              <tr>
                <th className="table-header">Booking ID</th>
                <th className="table-header">Room</th>
                <th className="table-header">User</th>
                <th className="table-header">Date</th>
                <th className="table-header">Start</th>
                <th className="table-header">End</th>
                <th className="table-header">Purpose</th>
                <th className="table-header">Status</th>
                <th className="table-header">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredBookings.map((booking, index) => {
                const isLoading = actionLoadingId === booking.bookingId;
                const statusClass = booking.status?.toLowerCase();

                return (
                  <tr key={booking.bookingId} className="table-row">
                    <td className="table-cell" data-label="Booking ID">
                      <span className="booking-id">#{booking.bookingId}</span>
                    </td>
                    <td className="table-cell" data-label="Room">
                      <span className="room-name">{booking.roomName || "-"}</span>
                    </td>
                    <td className="table-cell" data-label="User">
                      {booking.userName || "-"}
                    </td>
                    <td className="table-cell" data-label="Date">
                      {formatDate(booking.startAt)}
                    </td>
                    <td className="table-cell" data-label="Start">
                      {formatTime(booking.startAt)}
                    </td>
                    <td className="table-cell" data-label="End">
                      {formatTime(booking.endAt)}
                    </td>
                    <td className="table-cell purpose-cell" data-label="Purpose">
                      {booking.purpose || "-"}
                    </td>
                    <td className="table-cell" data-label="Status">
                      <span className={`status-badge status-${statusClass}`}>
                        {booking.status || "-"}
                      </span>
                    </td>
                    <td className="table-cell actions-cell" data-label="Actions">
                      <div className="action-group">
                        <button
                          className={`action-button approve-button ${
                            isLoading ? "loading" : ""
                          }`}
                          onClick={() => handleApprove(booking.bookingId)}
                          disabled={isLoading}
                        >
                          {isLoading ? (
                            <>
                              <span className="spinner-small"></span>
                              Processing...
                            </>
                          ) : (
                            "✓ Approve"
                          )}
                        </button>

                        <button
                          className={`action-button reject-button ${
                            isLoading ? "loading" : ""
                          }`}
                          onClick={() => handleReject(booking.bookingId)}
                          disabled={isLoading}
                        >
                          {isLoading ? (
                            <>
                              <span className="spinner-small"></span>
                              Processing...
                            </>
                          ) : (
                            "✗ Reject"
                          )}
                        </button>
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
}