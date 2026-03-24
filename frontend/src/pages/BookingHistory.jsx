import { useEffect, useState } from "react";
import {
  getBookingHistory,
  getPastBookings,
  getBookingById,
  downloadBookingHistoryPdf,
  checkInBooking,
} from "../api/roomApi";
import FeedbackForm from "../components/FeedbackForm";
import { hasBookingFeedback } from "../api/feedbackApi";
import { useNavigate } from "react-router-dom";

export default function BookingHistory() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [selectedBookingForFeedback, setSelectedBookingForFeedback] =
    useState(null);
  const [feedbackStatusMap, setFeedbackStatusMap] = useState({});
  const [filterType, setFilterType] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [actionMessageType, setActionMessageType] = useState("success");
  const [checkInLoadingMap, setCheckInLoadingMap] = useState({});

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

  useEffect(() => {
    async function loadFeedbackStatus() {
      if (!Array.isArray(bookings) || bookings.length === 0) {
        setFeedbackStatusMap({});
        return;
      }

      const result = {};

      for (const booking of bookings) {
        const bookingKey = booking.bookingId || booking.id;
        if (!bookingKey) continue;

        if (booking.feedbackSubmitted === true) {
          result[bookingKey] = true;
          continue;
        }

        try {
          const data = await hasBookingFeedback(bookingKey);
          result[bookingKey] = data?.exists === true;
        } catch {
          result[bookingKey] = false;
        }
      }

      setFeedbackStatusMap(result);
    }

    loadFeedbackStatus();
  }, [bookings]);

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
      setSelectedBookingForFeedback(null);
      navigate("/login");
      return;
    }

    setPageError(message);
  };

  const showActionMessage = (message, type = "success") => {
    setActionMessage(message);
    setActionMessageType(type);

    window.clearTimeout(showActionMessage._timer);
    showActionMessage._timer = window.setTimeout(() => {
      setActionMessage("");
    }, 3000);
  };

  const normalizeStatus = (status) => String(status || "").trim().toUpperCase();

  const loadAllBookings = async () => {
    try {
      setLoading(true);
      setPageError("");
      setActionMessage("");

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
      setActionMessage("");

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
      setActionMessage("");

      const data = await getBookingHistory(userId);
      const filtered = (Array.isArray(data) ? data : []).filter(
        (booking) => normalizeStatus(booking.status) === "COMPLETED"
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
      setActionMessage("");

      const data = await getBookingHistory(userId);
      const filtered = (Array.isArray(data) ? data : []).filter((booking) => {
        const status = normalizeStatus(booking.status);
        return status === "CANCELLED" || status === "AUTO_CANCELLED";
      });

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
      setActionMessage("");

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
    setActionMessage("");
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
      setActionMessage("");

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

  const handleCheckIn = async (booking) => {
    const bookingId = booking?.bookingId || booking?.id;

    if (!bookingId) {
      setPageError("Booking ID not found.");
      return;
    }

    try {
      setPageError("");
      setActionMessage("");
      setCheckInLoadingMap((prev) => ({ ...prev, [bookingId]: true }));

      const updatedBooking = await checkInBooking(bookingId, userId);

      setBookings((prev) =>
        prev.map((item) => {
          const itemId = item.bookingId || item.id;
          return itemId === bookingId ? { ...item, ...updatedBooking } : item;
        })
      );

      if (
        selectedBooking &&
        (selectedBooking.bookingId || selectedBooking.id) === bookingId
      ) {
        setSelectedBooking((prev) => ({ ...prev, ...updatedBooking }));
      }

      showActionMessage("Checked in successfully.", "success");
    } catch (error) {
      handleAuthError(error, "Failed to check in");
    } finally {
      setCheckInLoadingMap((prev) => ({ ...prev, [bookingId]: false }));
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

  const canCheckIn = (booking) => {
  if (!booking) return false;

  const status = String(booking.status || "").trim().toUpperCase();
  const checkinStatus = String(booking.checkinStatus || "").toLowerCase();

  if (status !== "APPROVED") return false;
  if (checkinStatus === "checked_in") return false;
  if (booking.checkedInAt) return false;
  if (!booking.startAt) return false;

  const now = new Date();
  const start = new Date(booking.startAt);

  const allowedFrom = new Date(start.getTime() - 15 * 60 * 1000);
  const allowedUntil = new Date(start.getTime() + 30 * 60 * 1000);

  console.log("NOW:", now);
  console.log("START:", start);

  return now >= allowedFrom && now <= allowedUntil;
};

  const isCheckedIn = (booking) => {
    const checkinStatus = String(booking?.checkinStatus || "").toLowerCase();
    return checkinStatus === "checked_in" || !!booking?.checkedInAt;
  };

  const isBookingFinished = (booking) => {
    if (!booking?.endAt) return false;
    return new Date() > new Date(booking.endAt);
  };

  const shouldShowFeedbackNotification = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const bookingId = booking.bookingId || booking.id;
    const feedbackSubmitted =
      booking.feedbackSubmitted === true || feedbackStatusMap[bookingId] === true;

    if (feedbackSubmitted) return false;
    if (!isCheckedIn(booking)) return false;
    if (!isBookingFinished(booking)) return false;

    if (
      status === "CANCELLED" ||
      status === "AUTO_CANCELLED" ||
      status === "REJECTED" ||
      status === "NO_SHOW"
    ) {
      return false;
    }

    return true;
  };

  const canLeaveFeedback = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const bookingId = booking.bookingId || booking.id;
    const alreadySubmitted =
      booking.feedbackSubmitted === true || feedbackStatusMap[bookingId] === true;

    if (
      status === "CANCELLED" ||
      status === "AUTO_CANCELLED" ||
      status === "REJECTED" ||
      status === "NO_SHOW"
    ) {
      return false;
    }

    if (alreadySubmitted) {
      return false;
    }

    if (status === "COMPLETED") return true;

    return shouldShowFeedbackNotification(booking);
  };

  const getStatusBadgeStyle = (status) => {
    const normalized = normalizeStatus(status);

    if (normalized === "COMPLETED") {
      return { ...styles.statusBadge, backgroundColor: "#dcfce7", color: "#166534" };
    }

    if (normalized === "CHECKED_IN") {
      return { ...styles.statusBadge, backgroundColor: "#dbeafe", color: "#1d4ed8" };
    }

    if (normalized === "APPROVED") {
      return { ...styles.statusBadge, backgroundColor: "#e0f2fe", color: "#0369a1" };
    }

    if (normalized === "PENDING") {
      return { ...styles.statusBadge, backgroundColor: "#fef3c7", color: "#92400e" };
    }

    if (
      normalized === "REJECTED" ||
      normalized === "CANCELLED" ||
      normalized === "AUTO_CANCELLED"
    ) {
      return { ...styles.statusBadge, backgroundColor: "#fee2e2", color: "#b91c1c" };
    }

    if (normalized === "NO_SHOW") {
      return { ...styles.statusBadge, backgroundColor: "#f3f4f6", color: "#374151" };
    }

    return styles.statusBadge;
  };

  const pendingFeedbackBookings = bookings.filter((booking) =>
    shouldShowFeedbackNotification(booking)
  );

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

      {actionMessage && (
        <div
          style={
            actionMessageType === "error"
              ? styles.errorBox
              : styles.successBox
          }
        >
          {actionMessage}
        </div>
      )}

      {pendingFeedbackBookings.length > 0 && (
        <div style={styles.notificationBox}>
          <h3 style={styles.notificationHeading}>Feedback Reminder</h3>
          {pendingFeedbackBookings.map((booking) => {
            const bookingId = booking.bookingId || booking.id;
            return (
              <div key={bookingId} style={styles.notificationItem}>
                <div>
                  Your booking for <strong>{booking.roomName || "Room"}</strong> on{" "}
                  <strong>{formatDate(booking.startAt)}</strong> has ended.
                  Please add your feedback and rating.
                </div>
                <button
                  style={styles.feedbackNowButton}
                  onClick={() => setSelectedBookingForFeedback(booking)}
                >
                  Add Feedback & Rating
                </button>
              </div>
            );
          })}
        </div>
      )}

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
              bookings.map((booking) => {
                const bookingId = booking.bookingId || booking.id;
                const feedbackSubmitted =
                  booking.feedbackSubmitted === true ||
                  feedbackStatusMap[bookingId] === true;
                const checkInLoading = checkInLoadingMap[bookingId] === true;

                return (
                  <tr key={getBookingKey(booking)}>
                    <td style={styles.td}>{bookingId || "-"}</td>
                    <td style={styles.td}>{booking.roomName || "-"}</td>
                    <td style={styles.td}>{formatDate(booking.startAt)}</td>
                    <td style={styles.td}>{formatTime(booking.startAt)}</td>
                    <td style={styles.td}>{formatTime(booking.endAt)}</td>
                    <td style={styles.td}>
                      <span style={getStatusBadgeStyle(booking.status)}>
                        {booking.status || "-"}
                      </span>
                    </td>
                    <td style={styles.td}>
                      <div style={styles.actionGroup}>
                        <button
                          onClick={() => handleViewDetails(bookingId)}
                          style={styles.viewButton}
                        >
                          View Details
                        </button>

                        {canCheckIn(booking) && (
                          <button
                            onClick={() => handleCheckIn(booking)}
                            style={styles.checkInButton}
                            disabled={checkInLoading}
                          >
                            {checkInLoading ? "Checking In..." : "Check In"}
                          </button>
                        )}

                        {canLeaveFeedback(booking) && !feedbackSubmitted && (
                          <button
                            onClick={() => setSelectedBookingForFeedback(booking)}
                            style={styles.feedbackButton}
                          >
                            Add Feedback & Rating
                          </button>
                        )}

                        {feedbackSubmitted && (
                          <span style={styles.feedbackDoneText}>
                            Feedback Submitted
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })
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

          <p>
            <strong>Booking ID:</strong>{" "}
            {selectedBooking.bookingId || selectedBooking.id || "-"}
          </p>
          <p>
            <strong>Room:</strong> {selectedBooking.roomName || "-"}
          </p>
          <p>
            <strong>User:</strong> {selectedBooking.userName || "-"}
          </p>
          <p>
            <strong>Start:</strong> {formatDateTime(selectedBooking.startAt)}
          </p>
          <p>
            <strong>End:</strong> {formatDateTime(selectedBooking.endAt)}
          </p>
          <p>
            <strong>Purpose:</strong> {selectedBooking.purpose || "-"}
          </p>
          <p>
            <strong>Attendee Count:</strong>{" "}
            {selectedBooking.attendeeCount ?? "-"}
          </p>
          <p>
            <strong>Duration:</strong>{" "}
            {selectedBooking.durationMinutes ?? 0} minutes
          </p>
          <p>
            <strong>Status:</strong> {selectedBooking.status || "-"}
          </p>
          <p>
            <strong>Check-in Status:</strong>{" "}
            {selectedBooking.checkinStatus || "-"}
          </p>
          <p>
            <strong>Checked In At:</strong>{" "}
            {formatDateTime(selectedBooking.checkedInAt)}
          </p>
          <p>
            <strong>Present:</strong>{" "}
            {selectedBooking.isPresent === true
              ? "Yes"
              : selectedBooking.isPresent === false
              ? "No"
              : "-"}
          </p>
          <p>
            <strong>Attendance Marked At:</strong>{" "}
            {formatDateTime(selectedBooking.attendanceMarkedAt)}
          </p>
          <p>
            <strong>Feedback Submitted:</strong>{" "}
            {selectedBooking.feedbackSubmitted === true ? "Yes" : "No"}
          </p>
          <p>
            <strong>Cancellation Reason:</strong>{" "}
            {selectedBooking.cancellationReason || "-"}
          </p>
          <p>
            <strong>Approval Time:</strong>{" "}
            {formatDateTime(selectedBooking.approvalTime)}
          </p>
          <p>
            <strong>Booked At:</strong>{" "}
            {formatDateTime(selectedBooking.bookedAt)}
          </p>
          <p>
            <strong>Created At:</strong>{" "}
            {formatDateTime(selectedBooking.createdAt)}
          </p>
          <p>
            <strong>Updated At:</strong>{" "}
            {formatDateTime(selectedBooking.updatedAt)}
          </p>

          {canLeaveFeedback(selectedBooking) &&
            !(selectedBooking.feedbackSubmitted === true) && (
              <button
                onClick={() => setSelectedBookingForFeedback(selectedBooking)}
                style={styles.feedbackButton}
              >
                Add Feedback & Rating
              </button>
            )}

          <button
            onClick={() => setSelectedBooking(null)}
            style={styles.closeButton}
          >
            Close Details
          </button>
        </div>
      )}

      {selectedBookingForFeedback && (
        <div style={styles.feedbackFormWrapper}>
          <h3 style={styles.feedbackHeading}>Enter Feedback and Rating</h3>

          <FeedbackForm
            booking={selectedBookingForFeedback}
            onSuccess={() => {
              const bookingId =
                selectedBookingForFeedback.bookingId ||
                selectedBookingForFeedback.id;

              setFeedbackStatusMap((prev) => ({
                ...prev,
                [bookingId]: true,
              }));

              setBookings((prev) =>
                prev.map((item) => {
                  const itemId = item.bookingId || item.id;
                  return itemId === bookingId
                    ? { ...item, feedbackSubmitted: true }
                    : item;
                })
              );

              if (
                selectedBooking &&
                (selectedBooking.bookingId || selectedBooking.id) === bookingId
              ) {
                setSelectedBooking((prev) => ({
                  ...prev,
                  feedbackSubmitted: true,
                }));
              }

              setSelectedBookingForFeedback(null);
              showActionMessage("Feedback submitted successfully.", "success");
            }}
            onCancel={() => setSelectedBookingForFeedback(null)}
          />
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
  successBox: {
    marginBottom: "16px",
    padding: "12px 14px",
    borderRadius: "8px",
    backgroundColor: "#e7f8ec",
    color: "#166534",
    border: "1px solid #b7ebc6",
    fontWeight: "600",
  },
  notificationBox: {
    marginBottom: "20px",
    padding: "16px",
    borderRadius: "10px",
    backgroundColor: "#fff7ed",
    border: "1px solid #fdba74",
    boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
  },
  notificationHeading: {
    margin: "0 0 12px 0",
    color: "#9a3412",
  },
  notificationItem: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "12px",
    flexWrap: "wrap",
    padding: "10px 0",
    borderBottom: "1px solid #fed7aa",
  },
  feedbackNowButton: {
    padding: "9px 14px",
    backgroundColor: "#ea580c",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
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
    verticalAlign: "top",
  },
  emptyCell: {
    padding: "20px",
    textAlign: "center",
    border: "1px solid #ddd",
  },
  actionGroup: {
    display: "flex",
    flexWrap: "wrap",
    gap: "8px",
    alignItems: "center",
  },
  viewButton: {
    padding: "8px 12px",
    backgroundColor: "#17a2b8",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  checkInButton: {
    padding: "8px 12px",
    backgroundColor: "#0d6efd",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  feedbackButton: {
    padding: "8px 12px",
    backgroundColor: "#fd7e14",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
  },
  feedbackDoneText: {
    color: "green",
    fontWeight: "600",
  },
  statusBadge: {
    display: "inline-block",
    padding: "6px 10px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: "700",
    backgroundColor: "#eef2f7",
    color: "#334155",
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
  feedbackFormWrapper: {
    marginTop: "30px",
    padding: "20px",
    borderRadius: "10px",
    backgroundColor: "#fff",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  feedbackHeading: {
    marginBottom: "16px",
    color: "#222",
  },
};