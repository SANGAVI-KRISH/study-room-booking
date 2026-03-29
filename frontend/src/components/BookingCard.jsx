export default function BookingCard({ booking, onCancel, onCheckin }) {
  return (
    <div className="card">
      <h3>{booking.roomName}</h3>

      <p>Status: {booking.status}</p>
      <p>
        Time: {booking.startAt} - {booking.endAt}
      </p>

      {/* Check-in */}
      {booking.status === "approved" && (
        <button onClick={() => onCheckin(booking.bookingId)}>
          Check In
        </button>
      )}

      {/* Cancel */}
      {booking.status !== "cancelled" &&
        booking.status !== "completed" && (
          <button onClick={() => onCancel(booking.bookingId)}>
            Cancel
          </button>
        )}
    </div>
  );
}