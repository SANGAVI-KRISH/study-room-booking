import "./RoomAvailability.css";
import { useState } from "react";
import { getAvailableRooms, bookRoom } from "../api/roomApi";

export default function RoomAvailability() {
  const tamilNaduDistricts = [
    "Ariyalur",
    "Chengalpattu",
    "Chennai",
    "Coimbatore",
    "Cuddalore",
    "Dharmapuri",
    "Dindigul",
    "Erode",
    "Kallakurichi",
    "Kanchipuram",
    "Kanyakumari",
    "Karur",
    "Krishnagiri",
    "Madurai",
    "Mayiladuthurai",
    "Nagapattinam",
    "Namakkal",
    "Nilgiris",
    "Perambalur",
    "Pudukkottai",
    "Ramanathapuram",
    "Ranipet",
    "Salem",
    "Sivaganga",
    "Tenkasi",
    "Thanjavur",
    "Theni",
    "Thoothukudi",
    "Tiruchirappalli",
    "Tirunelveli",
    "Tirupathur",
    "Tiruppur",
    "Tiruvallur",
    "Tiruvannamalai",
    "Tiruvarur",
    "Vellore",
    "Viluppuram",
    "Virudhunagar",
  ];

  const initialFilters = {
    date: "",
    startTime: "",
    endTime: "",
    seatingCapacity: "",
    district: "",
    location: "",
    facility: "",
  };

  const [filters, setFilters] = useState(initialFilters);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookingRoomId, setBookingRoomId] = useState(null);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  const showMessage = (text, type) => {
    setMessage(text);
    setMessageType(type);
  };

  const clearMessage = () => {
    setMessage("");
    setMessageType("");
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleReset = () => {
    setFilters(initialFilters);
    setRooms([]);
    clearMessage();
  };

  const validateSearchFilters = () => {
    if (!filters.date || !filters.startTime || !filters.endTime) {
      showMessage("Please select date, start time, and end time.", "error");
      return false;
    }

    if (filters.startTime >= filters.endTime) {
      showMessage("End time must be greater than start time.", "error");
      return false;
    }

    if (
      filters.seatingCapacity &&
      (Number(filters.seatingCapacity) <= 0 || Number.isNaN(Number(filters.seatingCapacity)))
    ) {
      showMessage("Seating capacity must be greater than 0.", "error");
      return false;
    }

    return true;
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    clearMessage();
    setRooms([]);

    if (!validateSearchFilters()) {
      return;
    }

    try {
      setLoading(true);

      const payload = {
        ...filters,
        seatingCapacity: filters.seatingCapacity
          ? Number(filters.seatingCapacity)
          : "",
      };

      const data = await getAvailableRooms(payload);

      if (!data || data.length === 0) {
        setRooms([]);
        showMessage("No rooms available for the selected filters.", "warning");
        return;
      }

      setRooms(data);
      showMessage("Available rooms loaded successfully.", "success");
    } catch (error) {
      console.error("Error fetching available rooms:", error);
      showMessage(error.message || "Error fetching available rooms.", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleBookRoom = async (roomId) => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    clearMessage();

    if (!token) {
      showMessage("Please login first to book a room.", "error");
      return;
    }

    if (!userId) {
      showMessage("User ID not found. Please login again.", "error");
      return;
    }

    if (!validateSearchFilters()) {
      return;
    }

    try {
      setBookingRoomId(roomId);

      const bookingData = {
        roomId: Number(roomId),
        userId: Number(userId),
        bookingDate: filters.date,
        startTime: filters.startTime,
        endTime: filters.endTime,
      };

      await bookRoom(bookingData);

      setRooms((prevRooms) => prevRooms.filter((room) => room.id !== roomId));
      showMessage("Room booked successfully.", "success");
    } catch (error) {
      console.error("Booking error:", error);
      showMessage(error.message || "Failed to book room.", "error");
    } finally {
      setBookingRoomId(null);
    }
  };

  return (
    <div className="availability-page">
      <div className="availability-container">
        <div className="availability-header">
          <h1>Room Availability Checking</h1>
          <p>
            Search available study rooms by date, time, capacity, district,
            location, and facility.
          </p>
        </div>

        <form className="availability-form-card" onSubmit={handleSearch}>
          <div className="availability-grid">
            <div className="availability-field">
              <label htmlFor="date">Date</label>
              <input
                id="date"
                type="date"
                name="date"
                value={filters.date}
                onChange={handleChange}
              />
            </div>

            <div className="availability-field">
              <label htmlFor="startTime">Start Time</label>
              <input
                id="startTime"
                type="time"
                name="startTime"
                value={filters.startTime}
                onChange={handleChange}
              />
            </div>

            <div className="availability-field">
              <label htmlFor="endTime">End Time</label>
              <input
                id="endTime"
                type="time"
                name="endTime"
                value={filters.endTime}
                onChange={handleChange}
              />
            </div>

            <div className="availability-field">
              <label htmlFor="seatingCapacity">Seating Capacity</label>
              <input
                id="seatingCapacity"
                type="number"
                name="seatingCapacity"
                placeholder="Enter capacity"
                value={filters.seatingCapacity}
                onChange={handleChange}
                min="1"
              />
            </div>

            <div className="availability-field">
              <label htmlFor="district">District</label>
              <select
                id="district"
                name="district"
                value={filters.district}
                onChange={handleChange}
              >
                <option value="">Select District</option>
                {tamilNaduDistricts.map((district) => (
                  <option key={district} value={district}>
                    {district}
                  </option>
                ))}
              </select>
            </div>

            <div className="availability-field">
              <label htmlFor="location">Location</label>
              <input
                id="location"
                type="text"
                name="location"
                placeholder="Enter location"
                value={filters.location}
                onChange={handleChange}
              />
            </div>

            <div className="availability-field availability-field-full">
              <label htmlFor="facility">Required Facility</label>
              <input
                id="facility"
                type="text"
                name="facility"
                placeholder="Example: WiFi, AC, Projector"
                value={filters.facility}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="availability-action-row">
            <button
              type="submit"
              className="availability-search-button"
              disabled={loading}
            >
              {loading ? "Searching..." : "Search Available Rooms"}
            </button>

            <button
              type="button"
              className="availability-reset-button"
              onClick={handleReset}
              disabled={loading || bookingRoomId !== null}
            >
              Reset
            </button>
          </div>
        </form>

        {message && (
          <div className={`availability-message ${messageType}`}>
            {message}
          </div>
        )}

        <div className="availability-results">
          <h2>Available Rooms</h2>

          {loading ? (
            <p className="availability-info-text">Loading available rooms...</p>
          ) : rooms.length > 0 ? (
            <div className="availability-room-grid">
              {rooms.map((room) => (
                <div key={room.id} className="availability-room-card">
                  <h3>
                    {room.roomName ||
                      `${room.blockName || "Block"} - Room ${room.roomNumber || room.id}`}
                  </h3>

                  <p>
                    <strong>Floor / Block:</strong>{" "}
                    {room.floorBlock ||
                      room.blockName ||
                      room.floorNumber ||
                      "Not specified"}
                  </p>

                  <p>
                    <strong>Capacity:</strong>{" "}
                    {room.seatingCapacity || "Not specified"} seats
                  </p>

                  <p>
                    <strong>Availability:</strong>{" "}
                    {room.availabilityTimings || "Not specified"}
                  </p>

                  <p>
                    <strong>Facilities:</strong>{" "}
                    {room.facilities || "Not specified"}
                  </p>

                  <p>
                    <strong>District:</strong> {room.district || "Not specified"}
                  </p>

                  <p>
                    <strong>Location:</strong> {room.location || "Not specified"}
                  </p>

                  <button
                    type="button"
                    className="availability-book-button"
                    onClick={() => handleBookRoom(room.id)}
                    disabled={bookingRoomId === room.id}
                  >
                    {bookingRoomId === room.id ? "Booking..." : "Book Now"}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="availability-info-text">
              Search with filters to view available rooms.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}