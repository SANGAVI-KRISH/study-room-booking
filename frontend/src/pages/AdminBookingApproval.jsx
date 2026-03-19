import { useEffect, useState } from "react";
import {
  getBookingsByStatus,
  approveBooking,
  rejectBooking,
} from "../api/roomApi";

export default function AdminBookingApproval() {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState("");
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const approverId = localStorage.getItem("userId");

  const loadPendingBookings = async () => {
    try {
      const data = await getBookingsByStatus("PENDING");
      setBookings(Array.isArray(data) ? data : []);
      setMessage("");
    } catch (error) {
      console.error(error);
      setMessage(error.message || "Failed to load pending bookings");
      setBookings([]);
    }
  };

  useEffect(() => {
    loadPendingBookings();
  }, []);

  const handleApprove = async (bookingId) => {
    try {
      if (!approverId) {
        setMessage("Logged-in approver ID not found. Please login again.");
        return;
      }

      setActionLoadingId(bookingId);
      await approveBooking(bookingId, approverId);
      setMessage("Booking approved successfully");
      await loadPendingBookings();
    } catch (error) {
      console.error(error);
      setMessage(error.message || "Failed to approve booking");
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleReject = async (bookingId) => {
    try {
      setActionLoadingId(bookingId);
      await rejectBooking(bookingId);
      setMessage("Booking rejected successfully");
      await loadPendingBookings();
    } catch (error) {
      console.error(error);
      setMessage(error.message || "Failed to reject booking");
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

      {message && <p style={styles.message}>{message}</p>}

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
              {bookings.map((booking) => (
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
                    <button
                      style={{
                        ...styles.approveButton,
                        opacity: actionLoadingId === booking.bookingId ? 0.7 : 1,
                        cursor:
                          actionLoadingId === booking.bookingId
                            ? "not-allowed"
                            : "pointer",
                      }}
                      onClick={() => handleApprove(booking.bookingId)}
                      disabled={actionLoadingId === booking.bookingId}
                    >
                      {actionLoadingId === booking.bookingId ? "Processing..." : "Approve"}
                    </button>

                    <button
                      style={{
                        ...styles.rejectButton,
                        opacity: actionLoadingId === booking.bookingId ? 0.7 : 1,
                        cursor:
                          actionLoadingId === booking.bookingId
                            ? "not-allowed"
                            : "pointer",
                      }}
                      onClick={() => handleReject(booking.bookingId)}
                      disabled={actionLoadingId === booking.bookingId}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))}
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
    color: "#333",
    fontWeight: "500",
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
  approveButton: {
    padding: "8px 12px",
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    marginRight: "8px",
  },
  rejectButton: {
    padding: "8px 12px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
  },
};