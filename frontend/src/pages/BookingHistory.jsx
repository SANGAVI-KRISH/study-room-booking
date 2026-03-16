import { useEffect, useState } from "react";
import {
  getUserBookings,
  getCompletedBookings,
  getCancelledBookings,
  getBookingHistoryByDateRange,
  getBookingDetails,
  downloadBookingHistory,
} from "../api/bookingHistoryApi";

export default function BookingHistory() {
  const [bookings, setBookings] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [filterType, setFilterType] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    loadAllBookings();
  }, []);

  const loadAllBookings = async () => {
    try {
      const data = await getUserBookings(userId);
      setBookings(data);
      setSelectedBooking(null);
    } catch (error) {
      console.error(error);
      alert("Failed to load booking history");
    }
  };

  const loadCompleted = async () => {
    try {
      const data = await getCompletedBookings(userId);
      setBookings(data);
      setSelectedBooking(null);
    } catch (error) {
      console.error(error);
      alert("Failed to load completed bookings");
    }
  };

  const loadCancelled = async () => {
    try {
      const data = await getCancelledBookings(userId);
      setBookings(data);
      setSelectedBooking(null);
    } catch (error) {
      console.error(error);
      alert("Failed to load cancelled bookings");
    }
  };

  const handleFilter = async () => {
    try {
      if (filterType === "ALL") {
        await loadAllBookings();
      } else if (filterType === "COMPLETED") {
        await loadCompleted();
      } else if (filterType === "CANCELLED") {
        await loadCancelled();
      }
    } catch (error) {
      console.error(error);
    }
  };

  const handleDateFilter = async () => {
    if (!startDate || !endDate) {
      alert("Please select both start date and end date");
      return;
    }

    try {
      const data = await getBookingHistoryByDateRange(userId, startDate, endDate);
      setBookings(data);
      setSelectedBooking(null);
    } catch (error) {
      console.error(error);
      alert("Failed to filter by date");
    }
  };

  const handleViewDetails = async (bookingId) => {
    try {
      const data = await getBookingDetails(bookingId);
      setSelectedBooking(data);
    } catch (error) {
      console.error(error);
      alert("Failed to fetch booking details");
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Booking History</h2>

      <div style={{ marginBottom: "20px" }}>
        <select value={filterType} onChange={(e) => setFilterType(e.target.value)}>
          <option value="ALL">All Bookings</option>
          <option value="COMPLETED">Completed Bookings</option>
          <option value="CANCELLED">Cancelled Bookings</option>
        </select>
        <button onClick={handleFilter} style={{ marginLeft: "10px" }}>
          Apply Filter
        </button>
      </div>

      <div style={{ marginBottom: "20px" }}>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
        />
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          style={{ marginLeft: "10px" }}
        />
        <button onClick={handleDateFilter} style={{ marginLeft: "10px" }}>
          Filter by Date
        </button>
      </div>

      <div style={{ marginBottom: "20px" }}>
        <button onClick={() => downloadBookingHistory(userId)}>
          Download Booking History
        </button>
      </div>

      <table border="1" cellPadding="10" cellSpacing="0" width="100%">
        <thead>
          <tr>
            <th>ID</th>
            <th>Room</th>
            <th>Date</th>
            <th>Start</th>
            <th>End</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {bookings.length > 0 ? (
            bookings.map((booking) => (
              <tr key={booking.id}>
                <td>{booking.id}</td>
                <td>{booking.room?.roomName}</td>
                <td>{booking.bookingDate}</td>
                <td>{booking.startTime}</td>
                <td>{booking.endTime}</td>
                <td>{booking.status}</td>
                <td>
                  <button onClick={() => handleViewDetails(booking.id)}>
                    View Details
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" align="center">
                No booking history found
              </td>
            </tr>
          )}
        </tbody>
      </table>

      {selectedBooking && (
        <div style={{ marginTop: "30px", padding: "15px", border: "1px solid #ccc" }}>
          <h3>Booking Details</h3>
          <p><strong>Booking ID:</strong> {selectedBooking.id}</p>
          <p><strong>Room:</strong> {selectedBooking.room?.roomName}</p>
          <p><strong>User:</strong> {selectedBooking.user?.name}</p>
          <p><strong>Date:</strong> {selectedBooking.bookingDate}</p>
          <p><strong>Start Time:</strong> {selectedBooking.startTime}</p>
          <p><strong>End Time:</strong> {selectedBooking.endTime}</p>
          <p><strong>Status:</strong> {selectedBooking.status}</p>
          <p><strong>Created At:</strong> {selectedBooking.createdAt}</p>
        </div>
      )}
    </div>
  );
}