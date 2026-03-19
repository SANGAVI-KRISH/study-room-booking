import { useEffect, useState } from "react";
import { getUserBookings, cancelBooking } from "../api/roomApi";

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);

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
      alert("Failed to load bookings");
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (bookingId) => {
    const confirmCancel = window.confirm("Are you sure you want to cancel this booking?");
    if (!confirmCancel) return;

    try {
      setCancellingId(bookingId);
      await cancelBooking(bookingId);
      alert("Booking cancelled successfully");
      await loadBookings();
    } catch (error) {
      console.error("Failed to cancel booking:", error);
      alert("Failed to cancel booking");
    } finally {
      setCancellingId(null);
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

  const canCancel = (status) => {
    return status !== "CANCELLED" &&
           status !== "COMPLETED" &&
           status !== "REJECTED" &&
           status !== "AUTO_CANCELLED";
  };

  if (loading) {
    return (
      <div style={{ padding: "20px" }}>
        <h2>My Bookings</h2>
        <p>Loading bookings...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: "20px" }}>
      <h2>My Bookings</h2>

      {!userId ? (
        <p>User not logged in.</p>
      ) : bookings.length === 0 ? (
        <p>No bookings found.</p>
      ) : (
        bookings.map((booking) => (
          <div
            key={booking.bookingId}
            style={{
              border: "1px solid #ccc",
              borderRadius: "10px",
              padding: "15px",
              marginBottom: "15px",
              backgroundColor: "#fff",
              boxShadow: "0 2px 6px rgba(0,0,0,0.08)"
            }}
          >
            <p>
              <strong>Room:</strong> {booking.roomName || "-"}
            </p>

            <p>
              <strong>Date:</strong> {formatDate(booking.startAt)}
            </p>

            <p>
              <strong>Time:</strong> {formatTime(booking.startAt)} - {formatTime(booking.endAt)}
            </p>

            <p>
              <strong>Start:</strong> {formatDateTime(booking.startAt)}
            </p>

            <p>
              <strong>End:</strong> {formatDateTime(booking.endAt)}
            </p>

            <p>
              <strong>Purpose:</strong> {booking.purpose || "-"}
            </p>

            <p>
              <strong>Attendee Count:</strong> {booking.attendeeCount ?? "-"}
            </p>

            <p>
              <strong>Duration:</strong> {booking.durationMinutes ?? 0} minutes
            </p>

            <p>
              <strong>Status:</strong> {booking.status || "-"}
            </p>

            <p>
              <strong>Check-in Status:</strong> {booking.checkinStatus || "-"}
            </p>

            {booking.cancellationReason && (
              <p>
                <strong>Cancellation Reason:</strong> {booking.cancellationReason}
              </p>
            )}

            {canCancel(booking.status) && (
              <button
                onClick={() => handleCancel(booking.bookingId)}
                disabled={cancellingId === booking.bookingId}
                style={{
                  marginTop: "10px",
                  padding: "10px 16px",
                  border: "none",
                  borderRadius: "6px",
                  backgroundColor: cancellingId === booking.bookingId ? "#999" : "#d9534f",
                  color: "#fff",
                  cursor: cancellingId === booking.bookingId ? "not-allowed" : "pointer"
                }}
              >
                {cancellingId === booking.bookingId ? "Cancelling..." : "Cancel Booking"}
              </button>
            )}
          </div>
        ))
      )}
    </div>
  );
}