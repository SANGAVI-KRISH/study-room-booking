import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAvailableRooms, bookRoom } from "../api/roomApi";
import "./RoomAvailability.css";

const API_BASE_URL = "http://localhost:8080";
const APP_TIMEZONE_OFFSET = "+05:30";

export default function RoomAvailability() {
  const navigate = useNavigate();

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

  const initialBookingForm = {
    purpose: "",
    attendeeCount: "1",
  };

  const [filters, setFilters] = useState(initialFilters);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookingRoomId, setBookingRoomId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [bookingModalOpen, setBookingModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [bookingForm, setBookingForm] = useState(initialBookingForm);

  const [galleryOpen, setGalleryOpen] = useState(false);
  const [galleryImages, setGalleryImages] = useState([]);
  const [galleryTitle, setGalleryTitle] = useState("");
  const [galleryIndex, setGalleryIndex] = useState(0);
  const [zoomStyle, setZoomStyle] = useState({
    backgroundImage: "",
    backgroundPosition: "center",
    opacity: 0,
  });

  const showSuccess = (text) => {
    setMessage(text);
    setError("");
  };

  const showError = (text) => {
    setError(text);
    setMessage("");
  };

  const clearAlerts = () => {
    setMessage("");
    setError("");
  };

  const getTodayString = () => {
    const today = new Date();
    return today.toISOString().split("T")[0];
  };

  const isFutureStartTime = (date, time) => {
    if (!date || !time) return false;
    const selected = new Date(`${date}T${time}:00`);
    return selected.getTime() > Date.now();
  };

  const buildOffsetDateTime = (date, time) => {
    if (!date || !time) return "";
    return `${date}T${time}:00${APP_TIMEZONE_OFFSET}`;
  };

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (galleryOpen) {
        if (e.key === "Escape") closeGallery();
        if (e.key === "ArrowLeft" && galleryImages.length > 1) showPrevImage();
        if (e.key === "ArrowRight" && galleryImages.length > 1) showNextImage();
      }

      if (bookingModalOpen && e.key === "Escape") {
        closeBookingModal();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [galleryOpen, galleryImages.length, bookingModalOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleBookingInputChange = (e) => {
    const { name, value } = e.target;
    setBookingForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleReset = () => {
    setFilters(initialFilters);
    setRooms([]);
    clearAlerts();
    closeBookingModal();
  };

  const validateSearchFilters = () => {
    if (!filters.date || !filters.startTime || !filters.endTime) {
      showError("Please select date, start time, and end time.");
      return false;
    }

    if (filters.startTime >= filters.endTime) {
      showError("End time must be greater than start time.");
      return false;
    }

    if (
      filters.seatingCapacity &&
      (Number(filters.seatingCapacity) <= 0 ||
        Number.isNaN(Number(filters.seatingCapacity)))
    ) {
      showError("Seating capacity must be greater than 0.");
      return false;
    }

    const todayStr = getTodayString();

    if (filters.date < todayStr) {
      showError("Past date booking is not allowed.");
      return false;
    }

    if (filters.date === todayStr && !isFutureStartTime(filters.date, filters.startTime)) {
      showError("Start time must be in the future.");
      return false;
    }

    return true;
  };

  const validateBookingForm = () => {
    if (!selectedRoom || !selectedRoom.id) {
      showError("Selected room is invalid.");
      return false;
    }

    if (!filters.date || !filters.startTime || !filters.endTime) {
      showError("Please search with valid date and time before booking.");
      return false;
    }

    if (filters.startTime >= filters.endTime) {
      showError("End time must be greater than start time.");
      return false;
    }

    if (!isFutureStartTime(filters.date, filters.startTime)) {
      showError("Start time must be in the future.");
      return false;
    }

    const attendeeCountNum = Number(bookingForm.attendeeCount);
    if (
      !bookingForm.attendeeCount ||
      Number.isNaN(attendeeCountNum) ||
      attendeeCountNum <= 0
    ) {
      showError("Attendee count must be greater than 0.");
      return false;
    }

    if (
      selectedRoom.seatingCapacity &&
      attendeeCountNum > Number(selectedRoom.seatingCapacity)
    ) {
      showError(
        `Attendee count cannot exceed room capacity (${selectedRoom.seatingCapacity}).`
      );
      return false;
    }

    return true;
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    clearAlerts();
    setRooms([]);

    if (!validateSearchFilters()) return;

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
        showError("No rooms available for the selected filters.");
        return;
      }

      setRooms(data);
      showSuccess("Available rooms loaded successfully.");
    } catch (err) {
      console.error("Error fetching available rooms:", err);
      showError(err.message || "Error fetching available rooms.");
    } finally {
      setLoading(false);
    }
  };

  const openBookingModal = (room) => {
    clearAlerts();

    if (!validateSearchFilters()) return;

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!token) {
      showError("Please login first to book a room.");
      return;
    }

    if (!userId) {
      showError("User ID not found. Please login again.");
      return;
    }

    setSelectedRoom(room);
    setBookingForm({
      purpose: "",
      attendeeCount: "1",
    });
    setBookingModalOpen(true);
  };

  const closeBookingModal = () => {
    setBookingModalOpen(false);
    setSelectedRoom(null);
    setBookingForm(initialBookingForm);
  };

  const handleConfirmBooking = async () => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    clearAlerts();

    if (!token) {
      showError("Please login first to book a room.");
      return;
    }

    if (!userId) {
      showError("User ID not found. Please login again.");
      return;
    }

    if (!validateBookingForm()) return;

    try {
      setBookingRoomId(selectedRoom.id);

      const bookingData = {
        roomId: selectedRoom.id,
        userId,
        startAt: buildOffsetDateTime(filters.date, filters.startTime),
        endAt: buildOffsetDateTime(filters.date, filters.endTime),
        purpose: bookingForm.purpose?.trim() || null,
        attendeeCount: Number(bookingForm.attendeeCount),
      };

      console.log("Booking payload:", bookingData);

      await bookRoom(bookingData);

      setRooms((prevRooms) =>
        prevRooms.filter((room) => room.id !== selectedRoom.id)
      );

      closeBookingModal();
      showSuccess("Room booked successfully.");
    } catch (err) {
      console.error("Booking error:", err);

      if (
        err?.message?.toLowerCase().includes("unauthorized") ||
        err?.message?.includes("401")
      ) {
        showError("Unauthorized. Please login again.");
      } else {
        showError(err.message || "Failed to book room.");
      }
    } finally {
      setBookingRoomId(null);
    }
  };

  const normalizeImagePath = (url) => {
    if (!url || typeof url !== "string") return "";

    const cleaned = url.trim().replace(/\\/g, "/");

    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
      return cleaned;
    }

    if (cleaned.startsWith("/")) {
      return `${API_BASE_URL}${cleaned}`;
    }

    return `${API_BASE_URL}/${cleaned}`;
  };

  const extractRoomImages = (room) => {
    if (!room) return [];

    const urls = [];

    if (Array.isArray(room.images)) {
      room.images.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.roomImages)) {
      room.roomImages.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.imageUrls)) {
      room.imageUrls.forEach((img) => {
        urls.push(normalizeImagePath(img));
      });
    }

    if (room.imageUrl) {
      urls.push(normalizeImagePath(room.imageUrl));
    }

    if (room.thumbnailUrl) {
      urls.push(normalizeImagePath(room.thumbnailUrl));
    }

    return [...new Set(urls.filter(Boolean))];
  };

  const getPrimaryImage = (room) => {
    const images = extractRoomImages(room);
    return images.length > 0 ? images[0] : null;
  };

  const getRoomTitle = (room) => {
    if (!room) return "Room";
    if (room.roomName) return room.roomName;
    return `${room.blockName || "Block"} - ${room.roomNumber || room.id}`;
  };

  const openGallery = (room, startIndex = 0) => {
    const images = extractRoomImages(room);
    if (images.length === 0) return;

    setGalleryImages(images);
    setGalleryTitle(getRoomTitle(room));
    setGalleryIndex(startIndex);
    setGalleryOpen(true);
    setZoomStyle({
      backgroundImage: `url(${images[startIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const closeGallery = () => {
    setGalleryOpen(false);
    setGalleryImages([]);
    setGalleryTitle("");
    setGalleryIndex(0);
    setZoomStyle({
      backgroundImage: "",
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const showPrevImage = () => {
    const newIndex =
      galleryIndex === 0 ? galleryImages.length - 1 : galleryIndex - 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const showNextImage = () => {
    const newIndex =
      galleryIndex === galleryImages.length - 1 ? 0 : galleryIndex + 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const handleThumbnailClick = (index) => {
    setGalleryIndex(index);
    setZoomStyle({
      backgroundImage: `url(${galleryImages[index]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const handleZoomMove = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width) * 100;
    const y = ((e.clientY - rect.top) / rect.height) * 100;

    setZoomStyle({
      backgroundImage: `url(${galleryImages[galleryIndex]})`,
      backgroundPosition: `${x}% ${y}%`,
      opacity: 1,
    });
  };

  const handleZoomLeave = () => {
    setZoomStyle((prev) => ({
      ...prev,
      opacity: 0,
      backgroundPosition: "center",
    }));
  };

  const handleImageError = (e) => {
    e.target.style.display = "none";
    const parent = e.target.parentElement;
    if (parent && !parent.querySelector(".image-error-placeholder")) {
      const placeholder = document.createElement("div");
      placeholder.className = "image-error-placeholder";
      placeholder.innerText = "Image not available";
      parent.appendChild(placeholder);
    }
  };

  const bookingSummary = useMemo(() => {
    if (!selectedRoom || !filters.startTime || !filters.endTime) {
      return {
        durationHours: 0,
        totalFee: 0,
      };
    }

    const [startHour, startMinute] = filters.startTime.split(":").map(Number);
    const [endHour, endMinute] = filters.endTime.split(":").map(Number);

    const totalMinutes =
      endHour * 60 + endMinute - (startHour * 60 + startMinute);
    const durationHours = totalMinutes / 60;

    const feePerHour = Number(selectedRoom.feePerHour || 0);
    const totalFee = durationHours * feePerHour;

    return {
      durationHours,
      totalFee,
    };
  }, [selectedRoom, filters.startTime, filters.endTime]);

  return (
    <div className="room-page">
      <div className="room-container">
        <div className="room-topBar">
          <div>
            <h1 className="room-heading">Room Availability Checking</h1>
            <p className="room-subText">
              Search available study rooms by date, time, capacity, district,
              location, and facility.
            </p>
          </div>

          <button
            type="button"
            className="room-backButton"
            onClick={() => navigate("/student")}
          >
            Back to Dashboard
          </button>
        </div>

        {message && <div className="room-successBox">{message}</div>}
        {error && <div className="room-errorBox">{error}</div>}

        <form onSubmit={handleSearch} className="room-formCard">
          <div className="room-formHeader">
            <h2 className="room-formTitle">Search Study Rooms</h2>
            <p className="room-formSubtitle">
              Fill in the search details carefully.
            </p>
          </div>

          <div className="room-formGrid">
            <div className="room-fieldGroup">
              <label className="room-label">Date</label>
              <input
                type="date"
                name="date"
                value={filters.date}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Start Time</label>
              <input
                type="time"
                name="startTime"
                value={filters.startTime}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">End Time</label>
              <input
                type="time"
                name="endTime"
                value={filters.endTime}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Seating Capacity</label>
              <input
                type="number"
                name="seatingCapacity"
                placeholder="Enter capacity"
                value={filters.seatingCapacity}
                onChange={handleChange}
                className="room-input"
                min="1"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">District</label>
              <select
                name="district"
                value={filters.district}
                onChange={handleChange}
                className="room-input"
              >
                <option value="">Select Tamil Nadu District</option>
                {tamilNaduDistricts.map((district) => (
                  <option key={district} value={district}>
                    {district}
                  </option>
                ))}
              </select>
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Exact Location</label>
              <input
                type="text"
                name="location"
                placeholder="Enter exact location"
                value={filters.location}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup room-fullWidth">
              <label className="room-label">Facilities</label>
              <textarea
                name="facility"
                rows="3"
                placeholder="Example: Wi-Fi, AC, Whiteboard, Charging Ports"
                value={filters.facility}
                onChange={handleChange}
                className="room-textarea"
              />
            </div>
          </div>

          <div className="room-actionRow">
            <button
              type="submit"
              className="room-addButton"
              disabled={loading}
            >
              {loading ? "Searching Rooms..." : "Search Available Rooms"}
            </button>

            <button
              type="button"
              onClick={handleReset}
              className="room-cancelButton"
              disabled={loading || bookingRoomId !== null}
            >
              Reset
            </button>
          </div>
        </form>

        <div className="room-listHeader">
          <h2 className="room-sectionTitle">Available Rooms</h2>
          <span className="room-roomCount">{rooms.length} rooms</span>
        </div>

        {loading ? (
          <div className="room-emptyBox">Loading available rooms...</div>
        ) : rooms.length === 0 ? (
          <div className="room-emptyBox">
            Search with filters to view available rooms.
          </div>
        ) : (
          <div className="room-roomGrid">
            {rooms.map((room) => {
              const images = extractRoomImages(room);
              const primaryImage = getPrimaryImage(room);

              return (
                <div key={room.id} className="room-card">
                  <div
                    className="room-imageContainer"
                    onClick={() => openGallery(room, 0)}
                    title={
                      images.length > 0
                        ? "Click to view images"
                        : "No image available"
                    }
                  >
                    {primaryImage ? (
                      <>
                        <img
                          src={primaryImage}
                          alt={getRoomTitle(room)}
                          className="room-cardImage"
                          onError={handleImageError}
                        />
                        {images.length > 1 && (
                          <div className="room-imageCountBadge">
                            +{images.length - 1} more
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="room-noImageBox">No Image</div>
                    )}
                  </div>

                  <div className="room-cardTop">
                    <h3 className="room-roomTitle">{getRoomTitle(room)}</h3>

                    <span className="room-feeBadge">
                      {room.feePerHour !== undefined && room.feePerHour !== null
                        ? `₹${room.feePerHour}/hr`
                        : "Available"}
                    </span>
                  </div>

                  <div className="room-infoList">
                    <p>
                      <strong>Floor / Block:</strong>{" "}
                      {room.floorBlock ||
                        room.blockName ||
                        room.floorNumber ||
                        "-"}
                    </p>
                    <p>
                      <strong>Capacity:</strong>{" "}
                      {room.seatingCapacity
                        ? `${room.seatingCapacity} seats`
                        : "-"}
                    </p>
                    <p>
                      <strong>Availability:</strong>{" "}
                      {room.availabilityTimings || "-"}
                    </p>
                    <p>
                      <strong>District:</strong> {room.district || "-"}
                    </p>
                    <p>
                      <strong>Location:</strong> {room.location || "-"}
                    </p>
                    <p>
                      <strong>Facilities:</strong> {room.facilities || "-"}
                    </p>
                    <p>
                      <strong>Approval Required:</strong>{" "}
                      {room.approvalRequired ? "Yes" : "No"}
                    </p>
                  </div>

                  <div className="room-cardButtons">
                    <button
                      type="button"
                      className="room-viewButton"
                      onClick={() => openGallery(room, 0)}
                      disabled={images.length === 0}
                    >
                      View Images
                    </button>

                    <button
                      type="button"
                      className="room-bookButton"
                      onClick={() => openBookingModal(room)}
                      disabled={bookingRoomId === room.id}
                    >
                      {bookingRoomId === room.id ? "Booking..." : "Book Now"}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {bookingModalOpen && selectedRoom && (
        <div className="room-modalOverlay" onClick={closeBookingModal}>
          <div
            className="room-modalContent"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="room-modalClose" onClick={closeBookingModal}>
              ×
            </button>

            <h3 className="room-modalTitle">Confirm Room Booking</h3>

            <div className="room-infoList" style={{ marginBottom: "16px" }}>
              <p>
                <strong>Room:</strong> {getRoomTitle(selectedRoom)}
              </p>
              <p>
                <strong>Date:</strong> {filters.date}
              </p>
              <p>
                <strong>Time:</strong> {filters.startTime} - {filters.endTime}
              </p>
              <p>
                <strong>Capacity:</strong>{" "}
                {selectedRoom.seatingCapacity || "-"} seats
              </p>
              <p>
                <strong>Approval Required:</strong>{" "}
                {selectedRoom.approvalRequired ? "Yes" : "No"}
              </p>
              <p>
                <strong>Duration:</strong> {bookingSummary.durationHours} hour(s)
              </p>
              <p>
                <strong>Total Fee:</strong> ₹
                {Number.isFinite(bookingSummary.totalFee)
                  ? bookingSummary.totalFee.toFixed(2)
                  : "0.00"}
              </p>
            </div>

            <div className="room-formGrid">
              <div className="room-fieldGroup">
                <label className="room-label">Attendee Count</label>
                <input
                  type="number"
                  name="attendeeCount"
                  min="1"
                  max={selectedRoom.seatingCapacity || undefined}
                  value={bookingForm.attendeeCount}
                  onChange={handleBookingInputChange}
                  className="room-input"
                />
              </div>

              <div className="room-fieldGroup room-fullWidth">
                <label className="room-label">Purpose</label>
                <textarea
                  name="purpose"
                  rows="3"
                  placeholder="Enter booking purpose"
                  value={bookingForm.purpose}
                  onChange={handleBookingInputChange}
                  className="room-textarea"
                />
              </div>
            </div>

            <div className="room-cardButtons" style={{ marginTop: "18px" }}>
              <button
                type="button"
                className="room-viewButton"
                onClick={closeBookingModal}
              >
                Cancel
              </button>

              <button
                type="button"
                className="room-bookButton"
                onClick={handleConfirmBooking}
                disabled={bookingRoomId === selectedRoom.id}
              >
                {bookingRoomId === selectedRoom.id
                  ? "Booking..."
                  : "Confirm Booking"}
              </button>
            </div>
          </div>
        </div>
      )}

      {galleryOpen && (
        <div className="room-modalOverlay" onClick={closeGallery}>
          <div
            className="room-modalContent"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="room-modalClose" onClick={closeGallery}>
              ×
            </button>

            <h3 className="room-modalTitle">{galleryTitle}</h3>

            <div
              className="room-productGalleryLayout"
              style={{
                gridTemplateColumns:
                  galleryImages.length > 1 ? "110px 1fr" : "1fr",
              }}
            >
              {galleryImages.length > 1 && (
                <div className="room-verticalThumbnailColumn">
                  {galleryImages.map((img, index) => (
                    <div
                      key={index}
                      className={`room-verticalThumbWrapper ${
                        index === galleryIndex ? "active" : ""
                      }`}
                      onClick={() => handleThumbnailClick(index)}
                    >
                      <img
                        src={img}
                        alt={`Thumbnail ${index + 1}`}
                        className="room-verticalThumbnail"
                        onError={handleImageError}
                      />
                    </div>
                  ))}
                </div>
              )}

              <div className="room-mainGalleryPanel">
                <div
                  className="room-mainImageStage"
                  onMouseMove={handleZoomMove}
                  onMouseLeave={handleZoomLeave}
                >
                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      className="room-arrowLeft"
                      onClick={showPrevImage}
                    >
                      ‹
                    </button>
                  )}

                  <img
                    src={galleryImages[galleryIndex]}
                    alt={`Room ${galleryIndex + 1}`}
                    className="room-modalImage"
                    onError={handleImageError}
                  />

                  <div
                    className="room-zoomLens"
                    style={{
                      backgroundImage: zoomStyle.backgroundImage,
                      backgroundPosition: zoomStyle.backgroundPosition,
                      opacity: zoomStyle.opacity,
                    }}
                  />

                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      className="room-arrowRight"
                      onClick={showNextImage}
                    >
                      ›
                    </button>
                  )}
                </div>

                <div className="room-imageCounter">
                  Image {galleryIndex + 1} of {galleryImages.length}
                </div>

                {galleryImages.length > 1 && (
                  <div className="room-bottomThumbnailRow">
                    {galleryImages.map((img, index) => (
                      <img
                        key={index}
                        src={img}
                        alt={`Bottom thumbnail ${index + 1}`}
                        className={`room-bottomThumbnail ${
                          index === galleryIndex ? "active" : ""
                        }`}
                        onClick={() => handleThumbnailClick(index)}
                        onError={handleImageError}
                      />
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}