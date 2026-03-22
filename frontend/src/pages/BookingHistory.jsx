import { useEffect, useState } from "react";
import {
  getBookingHistory,
  getPastBookings,
  getBookingById,
  downloadBookingHistoryPdf,
} from "../api/roomApi";
import { useNavigate } from "react-router-dom";

export default function BookingHistory() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [filterType, setFilterType] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState("");

  const userId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token || !userId) {
      setLoading(false);
      setPageError("User not logged in. Please login again.");
      navigate("/login");
      return;
    }

    loadAllBookings();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, token]);

  const handleAuthError = (error, fallbackMessage) => {
    console.error(fallbackMessage, error);

    const message = error?.message || fallbackMessage;

    if (
      message.toLowerCase().includes("unauthorized") ||
      message.toLowerCase().includes("authenticated") ||
      message.toLowerCase().includes("401")
    ) {
      setPageError("Session expired or unauthorized. Please login again.");
      localStorage.removeItem("token");
      localStorage.removeItem("role");
      localStorage.removeItem("userId");
      localStorage.removeItem("name");
      setBookings([]);
      setSelectedBooking(null);
      navigate("/login");
      return;
    }

    setPageError(message);
  };

  const loadAllBookings = async () => {
    try {
      setLoading(true);
      setPageError("");

      const data = await getBookingHistory(userId);
      setBookings(Array.isArray(data) ? data : []);
      setSelectedBooking(null);
    } catch (error) {
      handleAuthError(error, "Failed to load booking history");
    } finally {
      setLoading(false);
    }
  };

  const loadPastBookings = async () => {
    try {
      setLoading(true);
      setPageError("");

      const data = await getPastBookings(userId);
      setBookings(Array.isArray(data) ? data : []);
      setSelectedBooking(null);
    } catch (error) {
      handleAuthError(error, "Failed to load past bookings");
    } finally {
      setLoading(false);
    }
  };

  const loadCompleted = async () => {
    try {
      setLoading(true);
      setPageError("");

      const data = await getBookingHistory(userId);
      const filtered = (Array.isArray(data) ? data : []).filter(
        (booking) => booking.status === "COMPLETED"
      );

      setBookings(filtered);
      setSelectedBooking(null);
    } catch (error) {
      handleAuthError(error, "Failed to load completed bookings");
    } finally {
      setLoading(false);
    }
  };

  const loadCancelled = async () => {
    try {
      setLoading(true);
      setPageError("");

      const data = await getBookingHistory(userId);
      const filtered = (Array.isArray(data) ? data : []).filter(
        (booking) =>
          booking.status === "CANCELLED" ||
          booking.status === "AUTO_CANCELLED"
      );

      setBookings(filtered);
      setSelectedBooking(null);
    } catch (error) {
      handleAuthError(error, "Failed to load cancelled bookings");
    } finally {
      setLoading(false);
    }
  };

  const handleFilter = async () => {
    if (filterType === "ALL") {
      await loadAllBookings();
    } else if (filterType === "PAST") {
      await loadPastBookings();
    } else if (filterType === "COMPLETED") {
      await loadCompleted();
    } else if (filterType === "CANCELLED") {
      await loadCancelled();
    }
  };

  const handleDateFilter = async () => {
    if (!startDate || !endDate) {
      setPageError("Please select both start date and end date.");
      return;
    }

    if (new Date(endDate) < new Date(startDate)) {
      setPageError("End date cannot be before start date.");
      return;
    }

    try {
      setLoading(true);
      setPageError("");

      const data = await getBookingHistory(userId);

      const filtered = (Array.isArray(data) ? data : []).filter((booking) => {
        if (!booking.startAt) return false;

        const bookingDate = new Date(booking.startAt);
        const fromDate = new Date(`${startDate}T00:00:00`);
        const toDate = new Date(`${endDate}T23:59:59`);

        return bookingDate >= fromDate && bookingDate <= toDate;
      });

      setBookings(filtered);
      setSelectedBooking(null);
    } catch (error) {
      handleAuthError(error, "Failed to filter by date");
    } finally {
      setLoading(false);
    }
  };

  const handleResetFilters = async () => {
    setFilterType("ALL");
    setStartDate("");
    setEndDate("");
    setPageError("");
    await loadAllBookings();
  };

  const handleViewDetails = async (bookingId) => {
    try {
      setPageError("");
      const data = await getBookingById(bookingId);
      setSelectedBooking(data);
    } catch (error) {
      handleAuthError(error, "Failed to fetch booking details");
    }
  };

  const handleDownloadPdf = async () => {
    try {
      if (!userId || !token) {
        setPageError("User not logged in. Please login again.");
        navigate("/login");
        return;
      }

      setPageError("");

      const blob = await downloadBookingHistoryPdf(userId);
      const url = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = url;
      a.download = "booking-history.pdf";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      window.URL.revokeObjectURL(url);
    } catch (error) {
      handleAuthError(error, "Failed to download booking history PDF");
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

  const formatDateTime = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleString("en-IN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const getBookingKey = (booking) =>
    booking.bookingId || booking.id || `${booking.startAt}-${booking.endAt}`;

  if (loading) {
    return (
      <div style={styles.container}>
        <h2 style={styles.heading}>Booking History</h2>
        <p style={styles.infoText}>Loading booking history...</p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.heading}>Booking History</h2>

      {pageError && <div style={styles.errorBox}>{pageError}</div>}

      <div style={styles.filterRow}>
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          style={styles.select}
        >
          <option value="ALL">All History</option>
          <option value="PAST">Past Bookings</option>
          <option value="COMPLETED">Completed Bookings</option>
          <option value="CANCELLED">Cancelled Bookings</option>
        </select>

        <button onClick={handleFilter} style={styles.primaryButton}>
          Apply Filter
        </button>
      </div>

      <div style={styles.filterRow}>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          style={styles.input}
        />

        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          style={styles.input}
        />

        <button onClick={handleDateFilter} style={styles.primaryButton}>
          Filter by Date
        </button>

        <button onClick={handleResetFilters} style={styles.secondaryButton}>
          Reset
        </button>
      </div>

      <div style={styles.filterRow}>
        <button onClick={handleDownloadPdf} style={styles.downloadButton}>
          Download Booking History PDF
        </button>
      </div>

      <div style={styles.tableWrapper}>
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>Booking ID</th>
              <th style={styles.th}>Room</th>
              <th style={styles.th}>Date</th>
              <th style={styles.th}>Start</th>
              <th style={styles.th}>End</th>
              <th style={styles.th}>Status</th>
              <th style={styles.th}>Action</th>
            </tr>
          </thead>

          <tbody>
            {bookings.length > 0 ? (
              bookings.map((booking) => (
                <tr key={getBookingKey(booking)}>
                  <td style={styles.td}>{booking.bookingId || booking.id || "-"}</td>
                  <td style={styles.td}>{booking.roomName || "-"}</td>
                  <td style={styles.td}>{formatDate(booking.startAt)}</td>
                  <td style={styles.td}>{formatTime(booking.startAt)}</td>
                  <td style={styles.td}>{formatTime(booking.endAt)}</td>
                  <td style={styles.td}>{booking.status || "-"}</td>
                  <td style={styles.td}>
                    <button
                      onClick={() => handleViewDetails(booking.bookingId || booking.id)}
                      style={styles.viewButton}
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" style={styles.emptyCell}>
                  No booking history found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selectedBooking && (
        <div style={styles.detailsCard}>
          <h3 style={styles.detailsHeading}>Booking Details</h3>

          <p><strong>Booking ID:</strong> {selectedBooking.bookingId || selectedBooking.id || "-"}</p>
          <p><strong>Room:</strong> {selectedBooking.roomName || "-"}</p>
          <p><strong>User:</strong> {selectedBooking.userName || "-"}</p>
          <p><strong>Start:</strong> {formatDateTime(selectedBooking.startAt)}</p>
          <p><strong>End:</strong> {formatDateTime(selectedBooking.endAt)}</p>
          <p><strong>Purpose:</strong> {selectedBooking.purpose || "-"}</p>
          <p><strong>Attendee Count:</strong> {selectedBooking.attendeeCount ?? "-"}</p>
          <p><strong>Duration:</strong> {selectedBooking.durationMinutes ?? 0} minutes</p>
          <p><strong>Status:</strong> {selectedBooking.status || "-"}</p>
          <p><strong>Check-in Status:</strong> {selectedBooking.checkinStatus || "-"}</p>
          <p><strong>Cancellation Reason:</strong> {selectedBooking.cancellationReason || "-"}</p>
          <p><strong>Approval Time:</strong> {formatDateTime(selectedBooking.approvalTime)}</p>
          <p><strong>Booked At:</strong> {formatDateTime(selectedBooking.bookedAt)}</p>
          <p><strong>Created At:</strong> {formatDateTime(selectedBooking.createdAt)}</p>
          <p><strong>Updated At:</strong> {formatDateTime(selectedBooking.updatedAt)}</p>

          <button
            onClick={() => setSelectedBooking(null)}
            style={styles.closeButton}
          >
            Close Details
          </button>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    padding: "20px",
    backgroundColor: "#f4f6f8",
    fontFamily: "Arial, sans-serif",
  },
  heading: {
    marginBottom: "20px",
    color: "#222",
  },
  infoText: {
    color: "#444",
  },
  errorBox: {
    marginBottom: "16px",
    padding: "12px 14px",
    borderRadius: "8px",
    backgroundColor: "#fde2e2",
    color: "#9b1c1c",
    border: "1px solid #f5b5b5",
    fontWeight: "600",
  },
  filterRow: {
    display: "flex",
    flexWrap: "wrap",
    gap: "10px",
    marginBottom: "20px",
    alignItems: "center",
  },
  select: {
    padding: "10px",
    borderRadius: "6px",
    border: "1px solid #ccc",
    minWidth: "180px",
  },
  input: {
    padding: "10px",
    borderRadius: "6px",
    border: "1px solid #ccc",
  },
  primaryButton: {
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  secondaryButton: {
    padding: "10px 16px",
    backgroundColor: "#6c757d",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  downloadButton: {
    padding: "10px 16px",
    backgroundColor: "#28a745",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  tableWrapper: {
    overflowX: "auto",
    backgroundColor: "#fff",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
  },
  th: {
    backgroundColor: "#e9ecef",
    padding: "12px",
    border: "1px solid #ddd",
    textAlign: "left",
  },
  td: {
    padding: "12px",
    border: "1px solid #ddd",
  },
  emptyCell: {
    padding: "20px",
    textAlign: "center",
    border: "1px solid #ddd",
  },
  viewButton: {
    padding: "8px 12px",
    backgroundColor: "#17a2b8",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  detailsCard: {
    marginTop: "30px",
    padding: "20px",
    border: "1px solid #ccc",
    borderRadius: "10px",
    backgroundColor: "#fff",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  detailsHeading: {
    marginBottom: "15px",
    color: "#222",
  },
  closeButton: {
    marginTop: "15px",
    padding: "10px 16px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
};