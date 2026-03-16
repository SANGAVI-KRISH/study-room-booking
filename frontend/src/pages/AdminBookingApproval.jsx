import { useEffect, useState } from "react";
import { getPendingBookings, approveBooking, rejectBooking } from "../api/bookingApi";

export default function AdminBookingApproval() {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState("");

  const loadPendingBookings = async () => {
    try {
      const data = await getPendingBookings();
      setBookings(data);
    } catch (error) {
      console.error(error);
      setMessage("Failed to load pending bookings");
    }
  };

  useEffect(() => {
    loadPendingBookings();
  }, []);

  const handleApprove = async (id) => {
    try {
      await approveBooking(id);
      setMessage("Booking approved successfully");
      loadPendingBookings();
    } catch (error) {
      console.error(error);
      setMessage("Failed to approve booking");
    }
  };

  const handleReject = async (id) => {
    try {
      await rejectBooking(id);
      setMessage("Booking rejected successfully");
      loadPendingBookings();
    } catch (error) {
      console.error(error);
      setMessage("Failed to reject booking");
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Pending Booking Requests</h2>
      {message && <p>{message}</p>}

      {bookings.length === 0 ? (
        <p>No pending bookings</p>
      ) : (
        <table border="1" cellPadding="10" cellSpacing="0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Room</th>
              <th>User</th>
              <th>Date</th>
              <th>Start</th>
              <th>End</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <tr key={booking.id}>
                <td>{booking.id}</td>
                <td>{booking.room?.roomName}</td>
                <td>{booking.user?.name}</td>
                <td>{booking.bookingDate}</td>
                <td>{booking.startTime}</td>
                <td>{booking.endTime}</td>
                <td>{booking.status}</td>
                <td>
                  <button onClick={() => handleApprove(booking.id)}>Approve</button>
                  <button onClick={() => handleReject(booking.id)} style={{ marginLeft: "10px" }}>
                    Reject
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}