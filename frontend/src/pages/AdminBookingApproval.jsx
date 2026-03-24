import { useEffect, useState } from "react";
import {
  getBookingsByStatus,
  approveBooking,
  rejectBooking,
} from "../api/roomApi";

export default function AdminBookingApproval() {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const approverId = localStorage.getItem("userId");

  const showSuccess = (text) => {
    setMessage(text);
    setMessageType("success");
  };

  const showError = (text) => {
    setMessage(text);
    setMessageType("error");
  };

  const clearMessage = () => {
    setMessage("");
    setMessageType("");
  };

  const loadPendingBookings = async (preserveMessage = false) => {
    try {
      const data = await getBookingsByStatus("PENDING");
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
  }, []);

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

  return (
    <div style={styles.container}>
      <h2 style={styles.heading}>Pending Booking Requests</h2>

      {message && (
        <p
          style={{
            ...styles.message,
            ...(messageType === "success"
              ? styles.successMessage
              : styles.errorMessage),
          }}
        >
          {message}
        </p>
      )}

      {bookings.length === 0 ? (
        <p style={styles.emptyText}>No pending bookings</p>
      ) : (
        <div style={styles.tableWrapper}>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Booking ID</th>
                <th style={styles.th}>Room</th>
                <th style={styles.th}>User</th>
                <th style={styles.th}>Date</th>
                <th style={styles.th}>Start</th>
                <th style={styles.th}>End</th>
                <th style={styles.th}>Purpose</th>
                <th style={styles.th}>Status</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>

            <tbody>
              {bookings.map((booking) => {
                const isLoading = actionLoadingId === booking.bookingId;

                return (
                  <tr key={booking.bookingId}>
                    <td style={styles.td}>{booking.bookingId}</td>
                    <td style={styles.td}>{booking.roomName || "-"}</td>
                    <td style={styles.td}>{booking.userName || "-"}</td>
                    <td style={styles.td}>{formatDate(booking.startAt)}</td>
                    <td style={styles.td}>{formatTime(booking.startAt)}</td>
                    <td style={styles.td}>{formatTime(booking.endAt)}</td>
                    <td style={styles.td}>{booking.purpose || "-"}</td>
                    <td style={styles.td}>{booking.status || "-"}</td>
                    <td style={styles.td}>
                      <div style={styles.actionGroup}>
                        <button
                          style={{
                            ...styles.approveButton,
                            ...(isLoading ? styles.disabledButton : {}),
                          }}
                          onClick={() => handleApprove(booking.bookingId)}
                          disabled={isLoading}
                        >
                          {isLoading ? "Processing..." : "Approve"}
                        </button>

                        <button
                          style={{
                            ...styles.rejectButton,
                            ...(isLoading ? styles.disabledButton : {}),
                          }}
                          onClick={() => handleReject(booking.bookingId)}
                          disabled={isLoading}
                        >
                          {isLoading ? "Processing..." : "Reject"}
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

const styles = {
  container: {
    padding: "20px",
    backgroundColor: "#f8f9fa",
    minHeight: "100vh",
    fontFamily: "Arial, sans-serif",
  },
  heading: {
    marginBottom: "20px",
    color: "#222",
  },
  message: {
    marginBottom: "15px",
    fontWeight: "600",
    padding: "10px 12px",
    borderRadius: "8px",
    border: "1px solid transparent",
  },
  successMessage: {
    color: "#155724",
    backgroundColor: "#d4edda",
    borderColor: "#c3e6cb",
  },
  errorMessage: {
    color: "#721c24",
    backgroundColor: "#f8d7da",
    borderColor: "#f5c6cb",
  },
  emptyText: {
    color: "#666",
  },
  tableWrapper: {
    overflowX: "auto",
    backgroundColor: "#fff",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
    padding: "10px",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
  },
  th: {
    border: "1px solid #ddd",
    padding: "12px",
    backgroundColor: "#f1f3f5",
    textAlign: "left",
  },
  td: {
    border: "1px solid #ddd",
    padding: "12px",
    verticalAlign: "top",
  },
  actionGroup: {
    display: "flex",
    gap: "8px",
    flexWrap: "wrap",
  },
  approveButton: {
    padding: "8px 12px",
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  rejectButton: {
    padding: "8px 12px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  disabledButton: {
    opacity: 0.7,
    cursor: "not-allowed",
  },
};