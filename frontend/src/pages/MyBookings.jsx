import { useEffect, useState } from "react";
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

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      alert("User not logged in");
      return;
    }
    loadBookings();
  }, [userId]);

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

  const normalizeStatus = (status) =>
    String(status || "").trim().toUpperCase();

  const normalizeCheckinStatus = (status) =>
    String(status || "").trim().toLowerCase();

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

  const canCancel = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const checkinStatus = normalizeCheckinStatus(booking.checkinStatus);

    if (
      status === "CANCELLED" ||
      status === "COMPLETED" ||
      status === "REJECTED" ||
      status === "AUTO_CANCELLED"
    ) {
      return false;
    }

    if (
      checkinStatus === "checked_in" ||
      checkinStatus === "completed" ||
      checkinStatus === "missed"
    ) {
      return false;
    }

    if (!booking.endAt) return false;

    const now = new Date();
    const end = new Date(booking.endAt);

    if (now >= end) {
      return false;
    }

    return true;
  };

  const canCheckIn = (booking) => {
    if (!booking) return false;

    const status = normalizeStatus(booking.status);
    const checkinStatus = normalizeCheckinStatus(booking.checkinStatus);

    if (status !== "APPROVED") return false;

    if (
      checkinStatus === "checked_in" ||
      checkinStatus === "completed" ||
      checkinStatus === "missed"
    ) {
      return false;
    }

    if (!booking.startAt || !booking.endAt) return false;

    const now = new Date();
    const start = new Date(booking.startAt);
    const end = new Date(booking.endAt);

    if (now > end) return false;

    const allowedFrom = new Date(start.getTime() - 15 * 60 * 1000);
    const allowedUntil = new Date(start.getTime() + 30 * 60 * 1000);

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
        return "status-badge missed";
      default:
        return "status-badge default";
    }
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
      ) : bookings.length === 0 ? (
        <div className="empty-box">No bookings found.</div>
      ) : (
        <div className="bookings-grid">
          {bookings.map((booking) => (
            <div className="booking-card" key={booking.bookingId}>
              <div className="card-top">
                <h3>{booking.roomName || "Room not available"}</h3>
                <span className={getStatusClass(booking.status)}>
                  {booking.status || "-"}
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
                  <span className="label">Check-in</span>
                  <span className={getCheckinClass(booking.checkinStatus)}>
                    {booking.checkinStatus || "-"}
                  </span>
                </div>
              </div>

              <div className="card-actions">
                {canCheckIn(booking) && (
                  <button
                    className="action-btn checkin-btn"
                    onClick={() => handleCheckIn(booking.bookingId)}
                    disabled={checkinId === booking.bookingId}
                  >
                    {checkinId === booking.bookingId
                      ? "Checking..."
                      : "Check-in"}
                  </button>
                )}

                {canCancel(booking) && (
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
          ))}
        </div>
      )}
    </div>
  );
}