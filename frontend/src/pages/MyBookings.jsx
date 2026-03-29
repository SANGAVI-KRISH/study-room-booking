import { useEffect, useMemo, useState } from "react";
import {
  getUserBookings,
  cancelBooking,
  checkInBooking,
} from "../api/roomApi";
import "./MyBookings.css";

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [checkinId, setCheckinId] = useState(null);
  const [now, setNow] = useState(Date.now());

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      alert("User not logged in");
      return;
    }

    loadBookings();
  }, [userId]);

  useEffect(() => {
    const timer = setInterval(() => {
      setNow(Date.now());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const loadBookings = async () => {
    try {
      setLoading(true);
      const data = await getUserBookings(userId);
      setBookings(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Failed to load bookings:", error);
      alert(error.message || "Failed to load bookings");
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const sortedBookings = useMemo(() => {
    return [...bookings].sort((a, b) => {
      const aTime = a?.startAt ? new Date(a.startAt).getTime() : 0;
      const bTime = b?.startAt ? new Date(b.startAt).getTime() : 0;
      return bTime - aTime;
    });
  }, [bookings]);

  const normalizeStatus = (status) =>
    String(status || "")
      .trim()
      .toUpperCase();

  const normalizeCheckinStatus = (status) =>
    String(status || "")
      .trim()
      .toLowerCase();

  const formatStatusText = (status) =>
    String(status || "-")
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());

  const handleCancel = async (bookingId) => {
    const confirmCancel = window.confirm(
      "Are you sure you want to cancel this booking?"
    );
    if (!confirmCancel) return;

    try {
      setCancellingId(bookingId);
      await cancelBooking(bookingId);
      alert("Booking cancelled successfully");
      await loadBookings();
    } catch (error) {
      console.error("Failed to cancel booking:", error);
      alert(error.message || "Failed to cancel booking");
    } finally {
      setCancellingId(null);
    }
  };

  const handleCheckIn = async (bookingId) => {
    try {
      setCheckinId(bookingId);
      await checkInBooking(bookingId, userId);
      alert("Checked in successfully");
      await loadBookings();
    } catch (error) {
      console.error("Failed to check in:", error);
      alert(error.message || "Failed to check in");
    } finally {
      setCheckinId(null);
    }
  };

  const formatDate = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "long",
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
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const getTimeLeft = (deadline) => {
    if (!deadline) return null;

    const diff = new Date(deadline).getTime() - now;

    if (diff <= 0) return "Expired";

    const totalSeconds = Math.floor(diff / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}h ${minutes}m ${seconds}s`;
    }

    return `${minutes}m ${seconds}s`;
  };

  const canCancel = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const checkinStatus = normalizeCheckinStatus(booking.checkinStatus);

    if (
      status === "CANCELLED" ||
      status === "COMPLETED" ||
      status === "REJECTED" ||
      status === "AUTO_CANCELLED" ||
      status === "NO_SHOW"
    ) {
      return false;
    }

    if (
      checkinStatus === "checked_in" ||
      checkinStatus === "completed" ||
      checkinStatus === "missed" ||
      checkinStatus === "late"
    ) {
      return false;
    }

    if (!booking.endAt) return false;

    const end = new Date(booking.endAt).getTime();

    return now < end;
  };

  const canCheckIn = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const checkinStatus = normalizeCheckinStatus(booking.checkinStatus);

    if (status !== "APPROVED") return false;

    if (
      checkinStatus === "checked_in" ||
      checkinStatus === "completed" ||
      checkinStatus === "missed" ||
      checkinStatus === "late"
    ) {
      return false;
    }

    if (!booking.startAt || !booking.endAt) return false;

    const start = new Date(booking.startAt).getTime();
    const end = new Date(booking.endAt).getTime();

    if (now > end) return false;

    const allowedFrom = start - 15 * 60 * 1000;
    const allowedUntil = start + 30 * 60 * 1000;

    return now >= allowedFrom && now <= allowedUntil;
  };

  const getStatusClass = (status) => {
    const normalized = normalizeStatus(status);

    switch (normalized) {
      case "APPROVED":
        return "status-badge approved";
      case "PENDING":
        return "status-badge pending";
      case "REJECTED":
        return "status-badge rejected";
      case "CANCELLED":
        return "status-badge cancelled";
      case "COMPLETED":
        return "status-badge completed";
      case "AUTO_CANCELLED":
        return "status-badge auto-cancelled";
      case "CHECKED_IN":
        return "status-badge checkedin";
      case "NO_SHOW":
        return "status-badge missed";
      default:
        return "status-badge default";
    }
  };

  const getCheckinClass = (status) => {
    const normalized = normalizeCheckinStatus(status);

    switch (normalized) {
      case "checked_in":
        return "status-badge checkedin";
      case "not_checked_in":
        return "status-badge notchecked";
      case "completed":
        return "status-badge completed";
      case "missed":
      case "late":
        return "status-badge missed";
      default:
        return "status-badge default";
    }
  };

  const getBookingMessage = (booking) => {
    const status = normalizeStatus(booking.status);

    if (status === "AUTO_CANCELLED") {
      return "This booking was automatically cancelled because check-in was not completed in time.";
    }

    if (status === "REJECTED") {
      return "This booking request was rejected by admin.";
    }

    if (status === "CANCELLED") {
      return booking.cancellationReason || "This booking was cancelled.";
    }

    if (status === "COMPLETED") {
      return "This booking has been completed.";
    }

    if (status === "CHECKED_IN") {
      return "You have successfully checked in for this booking.";
    }

    if (status === "NO_SHOW") {
      return "This booking was marked as no-show.";
    }

    if (canCheckIn(booking)) {
      return "Check-in is available now.";
    }

    return "";
  };

  if (loading) {
    return (
      <div className="bookings-page">
        <div className="bookings-header">
          <h2>My Bookings</h2>
          <p>Track your study room reservations easily</p>
        </div>
        <div className="loading-box">Loading bookings...</div>
      </div>
    );
  }

  return (
    <div className="bookings-page">
      <div className="bookings-header">
        <h2>My Bookings</h2>
        <p>Track your study room reservations easily</p>
      </div>

      {!userId ? (
        <div className="empty-box">User not logged in.</div>
      ) : sortedBookings.length === 0 ? (
        <div className="empty-box">No bookings found.</div>
      ) : (
        <div className="bookings-grid">
          {sortedBookings.map((booking) => {
            const showCheckIn = canCheckIn(booking);
            const showCancel = canCancel(booking);
            const deadlineText = getTimeLeft(booking.checkInDeadline);
            const infoMessage = getBookingMessage(booking);

            return (
              <div className="booking-card" key={booking.bookingId}>
                <div className="card-top">
                  <h3>{booking.roomName || "Room not available"}</h3>
                  <span className={getStatusClass(booking.status)}>
                    {formatStatusText(booking.status)}
                  </span>
                </div>

                <div className="booking-details">
                  <div className="detail-item">
                    <span className="label">Date</span>
                    <span className="value">{formatDate(booking.startAt)}</span>
                  </div>

                  <div className="detail-item">
                    <span className="label">Time</span>
                    <span className="value">
                      {formatTime(booking.startAt)} - {formatTime(booking.endAt)}
                    </span>
                  </div>

                  <div className="detail-item">
                    <span className="label">Purpose</span>
                    <span className="value">{booking.purpose || "-"}</span>
                  </div>

                  <div className="detail-item">
                    <span className="label">Attendees</span>
                    <span className="value">{booking.attendeeCount || "-"}</span>
                  </div>

                  <div className="detail-item">
                    <span className="label">Check-in</span>
                    <span className={getCheckinClass(booking.checkinStatus)}>
                      {formatStatusText(booking.checkinStatus)}
                    </span>
                  </div>

                  {booking.checkInDeadline && (
                    <div className="detail-item">
                      <span className="label">Check-in Deadline</span>
                      <span className="value">
                        {formatDateTime(booking.checkInDeadline)}
                      </span>
                    </div>
                  )}

                  {booking.checkInDeadline &&
                    normalizeStatus(booking.status) === "APPROVED" && (
                      <div className="detail-item">
                        <span className="label">Time Left</span>
                        <span
                          className={`value ${
                            deadlineText === "Expired" ? "expired-text" : ""
                          }`}
                        >
                          {deadlineText}
                        </span>
                      </div>
                    )}

                  {booking.checkedInAt && (
                    <div className="detail-item">
                      <span className="label">Checked-in At</span>
                      <span className="value">
                        {formatDateTime(booking.checkedInAt)}
                      </span>
                    </div>
                  )}

                  {booking.autoCancelledAt && (
                    <div className="detail-item">
                      <span className="label">Auto Cancelled At</span>
                      <span className="value">
                        {formatDateTime(booking.autoCancelledAt)}
                      </span>
                    </div>
                  )}

                  {booking.cancellationReason && (
                    <div className="detail-item full-width">
                      <span className="label">Reason</span>
                      <span className="value">{booking.cancellationReason}</span>
                    </div>
                  )}
                </div>

                {infoMessage && <div className="booking-info">{infoMessage}</div>}

                <div className="card-actions">
                  {showCheckIn && (
                    <button
                      className="action-btn checkin-btn"
                      onClick={() => handleCheckIn(booking.bookingId)}
                      disabled={checkinId === booking.bookingId}
                    >
                      {checkinId === booking.bookingId
                        ? "Checking..."
                        : "Check In"}
                    </button>
                  )}

                  {showCancel && (
                    <button
                      className="action-btn cancel-btn"
                      onClick={() => handleCancel(booking.bookingId)}
                      disabled={cancellingId === booking.bookingId}
                    >
                      {cancellingId === booking.bookingId
                        ? "Cancelling..."
                        : "Cancel Booking"}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}