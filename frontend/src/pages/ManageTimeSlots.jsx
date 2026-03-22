import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080";

export default function ManageTimeSlots() {
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
    "Kancheepuram",
    "Kanniyakumari",
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

  const [district, setDistrict] = useState("");
  const [location, setLocation] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);

  const [availability, setAvailability] = useState({
    dayOfWeek: 1,
    openTime: "09:00",
    closeTime: "18:00",
    isAvailable: true,
  });

  const [maintenance, setMaintenance] = useState({
    startAt: "",
    endAt: "",
    reason: "",
    createdBy: localStorage.getItem("userId") || "",
  });

  const [date, setDate] = useState("");
  const [slots, setSlots] = useState([]);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("info");
  const [loading, setLoading] = useState(false);

  const getToken = () => localStorage.getItem("token");
  const getRole = () => localStorage.getItem("role");

  const setSuccess = (text) => {
    setMessageType("success");
    setMessage(text);
  };

  const setError = (text) => {
    setMessageType("error");
    setMessage(text);
  };

  const clearMessage = () => {
    setMessage("");
    setMessageType("info");
  };

  const isAdminRole = (role) => role === "ADMIN" || role === "ROLE_ADMIN";

  const handleUnauthorized = () => {
    localStorage.removeItem("token");
    setError("Session expired or unauthorized. Please login again.");
    navigate("/login");
  };

  const getErrorMessage = async (response, fallback) => {
    try {
      const text = await response.text();
      if (!text) return fallback;

      try {
        const data = JSON.parse(text);
        return data.message || data.error || text || fallback;
      } catch {
        return text;
      }
    } catch {
      return fallback;
    }
  };

  const getRoomLabel = (room) => {
    const displayName = room?.displayName?.trim();
    const blockName = room?.blockName?.trim();
    const roomNumber = room?.roomNumber?.trim();
    const floorNumber = room?.floorNumber?.trim();
    const districtValue = room?.district?.trim();
    const locationValue = room?.location?.trim();

    if (displayName) {
      return `${displayName}${districtValue ? ` - ${districtValue}` : ""}${
        locationValue ? ` - ${locationValue}` : ""
      }`;
    }

    return [
      blockName || "Block",
      roomNumber ? `Room ${roomNumber}` : "",
      floorNumber ? `Floor ${floorNumber}` : "",
      districtValue || "",
      locationValue || "",
    ]
      .filter(Boolean)
      .join(" - ");
  };

  const isCrossMidnightAvailability = useMemo(() => {
    if (!availability.openTime || !availability.closeTime) return false;
    return availability.closeTime < availability.openTime;
  }, [availability.openTime, availability.closeTime]);

  const fetchRooms = async () => {
    try {
      const token = getToken();
      if (!token) {
        handleUnauthorized();
        return;
      }

      setRoomsLoading(true);

      const response = await fetch(`${API_BASE_URL}/api/rooms`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401 || response.status === 403) {
        handleUnauthorized();
        return;
      }

      if (!response.ok) {
        throw new Error(await getErrorMessage(response, "Failed to fetch rooms"));
      }

      const data = await response.json();
      const roomList = Array.isArray(data) ? data : [];
      setRooms(roomList);
    } catch (error) {
      console.error("fetchRooms error:", error);
      setError(error.message || "Failed to fetch rooms");
    } finally {
      setRoomsLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, []);

  useEffect(() => {
    setSelectedRoomId("");
    setSlots([]);
  }, [district, location]);

  const filteredRooms = useMemo(() => {
    return rooms.filter((room) => {
      const roomDistrict = String(room?.district || "").trim().toLowerCase();
      const roomLocation = String(room?.location || "").trim().toLowerCase();

      const districtMatch = district
        ? roomDistrict === district.trim().toLowerCase()
        : true;

      const locationMatch = location.trim()
        ? roomLocation.includes(location.trim().toLowerCase())
        : true;

      return districtMatch && locationMatch;
    });
  }, [rooms, district, location]);

  const selectedRoom = useMemo(() => {
    return filteredRooms.find((room) => String(room.id) === String(selectedRoomId)) || null;
  }, [filteredRooms, selectedRoomId]);

  const validateSelectedRoom = () => {
    if (!district.trim()) {
      setError("Please select district.");
      return false;
    }

    if (!location.trim()) {
      setError("Please enter location.");
      return false;
    }

    if (!selectedRoomId.trim()) {
      setError("Please select room details.");
      return false;
    }

    return true;
  };

  const validateAvailability = () => {
    if (!validateSelectedRoom()) return false;

    if (!availability.openTime || !availability.closeTime) {
      setError("Open time and close time are required.");
      return false;
    }

    if (availability.openTime === availability.closeTime) {
      setError("Open time and close time cannot be the same.");
      return false;
    }

    return true;
  };

  const validateMaintenance = () => {
    if (!validateSelectedRoom()) return false;

    if (!maintenance.startAt || !maintenance.endAt) {
      setError("Maintenance start and end time are required.");
      return false;
    }

    if (maintenance.endAt <= maintenance.startAt) {
      setError("Maintenance end time must be later than start time.");
      return false;
    }

    if (!maintenance.reason.trim()) {
      setError("Maintenance reason is required.");
      return false;
    }

    return true;
  };

  const validatePreview = () => {
    if (!validateSelectedRoom()) return false;

    if (!date) {
      setError("Please select a date to load slots.");
      return false;
    }

    return true;
  };

  const formatTime = (value) => {
    if (!value) return "-";

    try {
      if (typeof value === "string" && value.includes("T")) {
        return new Date(value).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        });
      }

      if (typeof value === "string" && value.length >= 5 && value.includes(":")) {
        return value.slice(0, 5);
      }

      return String(value);
    } catch {
      return String(value);
    }
  };

  const normalizeSlotStatus = (slot) => {
    const rawStatus =
      slot?.availabilityStatus ||
      slot?.status ||
      slot?.slotStatus ||
      slot?.bookingStatus ||
      "";

    const upperStatus = String(rawStatus).toUpperCase();

    if (upperStatus.includes("BLOCK")) return "BLOCKED";
    if (upperStatus.includes("BOOK")) return "BOOKED";
    if (upperStatus.includes("AVAIL")) return "AVAILABLE";

    const remainingSeats = Number(
      slot?.remainingSeats ??
        slot?.remainingSeatCount ??
        slot?.availableSeats ??
        0
    );

    const capacity = Number(slot?.capacity ?? slot?.totalSeats ?? 0);
    const bookedCount = Number(slot?.bookedCount ?? slot?.bookingsCount ?? 0);

    if (slot?.blocked === true || slot?.isBlocked === true) return "BLOCKED";
    if (capacity > 0 && remainingSeats <= 0) return "BOOKED";
    if (capacity > 0 && bookedCount >= capacity) return "BOOKED";
    if (slot?.available === true || slot?.isAvailable === true) return "AVAILABLE";
    if (slot?.available === false || slot?.isAvailable === false) return "BLOCKED";

    return "AVAILABLE";
  };

  const getSlotNumbers = (slot) => {
    const capacity = Number(slot?.capacity ?? slot?.totalSeats ?? 0);
    const bookedCount = Number(slot?.bookedCount ?? slot?.bookingsCount ?? 0);

    let remainingSeats = slot?.remainingSeats ?? slot?.remainingSeatCount ?? slot?.availableSeats;

    if (remainingSeats === undefined || remainingSeats === null || remainingSeats === "") {
      if (capacity > 0) {
        remainingSeats = Math.max(capacity - bookedCount, 0);
      } else {
        remainingSeats = 0;
      }
    }

    return {
      capacity,
      bookedCount,
      remainingSeats: Number(remainingSeats),
    };
  };

  const getSlotTheme = (status) => {
    switch (status) {
      case "AVAILABLE":
        return {
          backgroundColor: "#ecfdf5",
          borderColor: "#16a34a",
          badgeBackground: "#dcfce7",
          badgeColor: "#166534",
        };
      case "BOOKED":
        return {
          backgroundColor: "#fff7ed",
          borderColor: "#ea580c",
          badgeBackground: "#fed7aa",
          badgeColor: "#9a3412",
        };
      case "BLOCKED":
        return {
          backgroundColor: "#f5f5f5",
          borderColor: "#6b7280",
          badgeBackground: "#e5e7eb",
          badgeColor: "#374151",
        };
      default:
        return {
          backgroundColor: "#eff6ff",
          borderColor: "#2563eb",
          badgeBackground: "#dbeafe",
          badgeColor: "#1d4ed8",
        };
    }
  };

  const saveAvailability = async () => {
    try {
      clearMessage();

      const token = getToken();
      const role = getRole();

      if (!token) {
        handleUnauthorized();
        return;
      }

      if (!isAdminRole(role)) {
        setError("Access denied. Admin only.");
        return;
      }

      if (!validateAvailability()) return;

      setLoading(true);

      const payload = {
        dayOfWeek: Number(availability.dayOfWeek),
        openTime: availability.openTime,
        closeTime: availability.closeTime,
        isAvailable: Boolean(availability.isAvailable),
      };

      console.log("Saving availability for room:", selectedRoomId);
      console.log("Availability payload:", payload);

      const response = await fetch(
        `${API_BASE_URL}/api/time-slots/admin/availability/${selectedRoomId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (response.status === 401 || response.status === 403) {
        handleUnauthorized();
        return;
      }

      if (!response.ok) {
        throw new Error(
          await getErrorMessage(response, "Failed to save room availability")
        );
      }

      if (isCrossMidnightAvailability) {
        setSuccess(
          "Room availability saved successfully. This timing continues to the next day."
        );
      } else {
        setSuccess("Room availability saved successfully.");
      }

      setSlots([]);
    } catch (error) {
      console.error("saveAvailability error:", error);
      setError(error.message || "Failed to save room availability");
    } finally {
      setLoading(false);
    }
  };

  const addMaintenanceBlock = async () => {
    try {
      clearMessage();

      const token = getToken();
      const role = getRole();

      if (!token) {
        handleUnauthorized();
        return;
      }

      if (!isAdminRole(role)) {
        setError("Access denied. Admin only.");
        return;
      }

      if (!validateMaintenance()) return;

      setLoading(true);

      const payload = {
        startAt: maintenance.startAt,
        endAt: maintenance.endAt,
        reason: maintenance.reason.trim(),
        createdBy: maintenance.createdBy,
      };

      console.log("Adding maintenance for room:", selectedRoomId);
      console.log("Maintenance payload:", payload);

      const response = await fetch(
        `${API_BASE_URL}/api/time-slots/admin/maintenance/${selectedRoomId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (response.status === 401 || response.status === 403) {
        handleUnauthorized();
        return;
      }

      if (!response.ok) {
        throw new Error(
          await getErrorMessage(response, "Failed to add maintenance block")
        );
      }

      setSuccess("Maintenance block added successfully.");
      setMaintenance((prev) => ({
        ...prev,
        startAt: "",
        endAt: "",
        reason: "",
      }));
    } catch (error) {
      console.error("addMaintenanceBlock error:", error);
      setError(error.message || "Failed to add maintenance block");
    } finally {
      setLoading(false);
    }
  };

  const fetchSlots = async () => {
    try {
      clearMessage();
      setSlots([]);

      const token = getToken();
      if (!token) {
        handleUnauthorized();
        return;
      }

      if (!validatePreview()) return;

      setLoading(true);

      const response = await fetch(
        `${API_BASE_URL}/api/time-slots/${selectedRoomId}?date=${date}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.status === 401 || response.status === 403) {
        handleUnauthorized();
        return;
      }

      if (!response.ok) {
        throw new Error(await getErrorMessage(response, "Failed to fetch slots"));
      }

      const data = await response.json();
      const normalizedSlots = Array.isArray(data?.slots)
        ? data.slots
        : Array.isArray(data)
        ? data
        : [];

      setSlots(normalizedSlots);
      setSuccess("Slots loaded successfully.");
    } catch (error) {
      console.error("fetchSlots error:", error);
      setError(error.message || "Failed to fetch slots");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.topBar}>
        <h1 style={styles.heading}>Time Slot Management</h1>
        <button style={styles.backButton} onClick={() => navigate("/admin")}>
          Back to Dashboard
        </button>
      </div>

      {message && (
        <div
          style={{
            ...styles.messageBox,
            ...(messageType === "error" ? styles.errorBox : styles.successBox),
          }}
        >
          {message}
        </div>
      )}

      <div style={styles.section}>
        <div style={styles.sectionHeaderRow}>
          <h2 style={styles.sectionTitle}>Select Room</h2>
          <button
            type="button"
            style={{
              ...styles.secondaryButton,
              ...(roomsLoading ? styles.disabledButton : {}),
            }}
            onClick={fetchRooms}
            disabled={roomsLoading}
          >
            {roomsLoading ? "Refreshing..." : "Refresh Rooms"}
          </button>
        </div>

        <select
          value={district}
          onChange={(e) => setDistrict(e.target.value)}
          style={styles.input}
        >
          <option value="">Select District</option>
          {tamilNaduDistricts.map((districtName) => (
            <option key={districtName} value={districtName}>
              {districtName}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Enter Location"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          style={styles.input}
        />

        <select
          value={selectedRoomId}
          onChange={(e) => setSelectedRoomId(e.target.value)}
          style={styles.input}
          disabled={!district || !location.trim() || roomsLoading}
        >
          <option value="">
            {!district
              ? "Select district first"
              : !location.trim()
              ? "Enter location first"
              : roomsLoading
              ? "Loading rooms..."
              : filteredRooms.length === 0
              ? "No rooms found"
              : "Select Room Details"}
          </option>

          {filteredRooms.map((room) => (
            <option key={room.id} value={room.id}>
              {getRoomLabel(room)}
            </option>
          ))}
        </select>

        {selectedRoom && (
          <div style={styles.selectedRoomCard}>
            <p style={styles.selectedRoomText}>
              <strong>Room:</strong> {selectedRoom.displayName || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>Block:</strong> {selectedRoom.blockName || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>Room No:</strong> {selectedRoom.roomNumber || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>Floor:</strong> {selectedRoom.floorNumber || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>District:</strong> {selectedRoom.district || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>Location:</strong> {selectedRoom.location || "-"}
            </p>
            <p style={styles.selectedRoomText}>
              <strong>Room ID:</strong> {selectedRoom.id || "-"}
            </p>
          </div>
        )}
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Set Weekly Availability</h2>

        <select
          value={availability.dayOfWeek}
          onChange={(e) =>
            setAvailability({
              ...availability,
              dayOfWeek: Number(e.target.value),
            })
          }
          style={styles.input}
        >
          <option value={0}>Sunday</option>
          <option value={1}>Monday</option>
          <option value={2}>Tuesday</option>
          <option value={3}>Wednesday</option>
          <option value={4}>Thursday</option>
          <option value={5}>Friday</option>
          <option value={6}>Saturday</option>
        </select>

        <input
          type="time"
          value={availability.openTime}
          onChange={(e) =>
            setAvailability({ ...availability, openTime: e.target.value })
          }
          style={styles.input}
        />

        <input
          type="time"
          value={availability.closeTime}
          onChange={(e) =>
            setAvailability({ ...availability, closeTime: e.target.value })
          }
          style={styles.input}
        />

        <label style={styles.checkboxLabel}>
          <input
            type="checkbox"
            checked={availability.isAvailable}
            onChange={(e) =>
              setAvailability({
                ...availability,
                isAvailable: e.target.checked,
              })
            }
          />
          Room Available
        </label>

        {availability.openTime &&
          availability.closeTime &&
          availability.openTime !== availability.closeTime &&
          isCrossMidnightAvailability && (
            <div style={styles.infoBox}>
              Close time is earlier than open time, so this availability will
              continue to the next day.
            </div>
          )}

        <button
          style={{
            ...styles.button,
            ...(loading ? styles.disabledButton : {}),
          }}
          onClick={saveAvailability}
          disabled={loading}
        >
          {loading ? "Saving..." : "Save Availability"}
        </button>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Add Maintenance Block</h2>

        <input
          type="datetime-local"
          value={maintenance.startAt}
          onChange={(e) =>
            setMaintenance({ ...maintenance, startAt: e.target.value })
          }
          style={styles.input}
        />

        <input
          type="datetime-local"
          value={maintenance.endAt}
          onChange={(e) =>
            setMaintenance({ ...maintenance, endAt: e.target.value })
          }
          style={styles.input}
        />

        <input
          type="text"
          placeholder="Reason"
          value={maintenance.reason}
          onChange={(e) =>
            setMaintenance({ ...maintenance, reason: e.target.value })
          }
          style={styles.input}
        />

        <button
          style={{
            ...styles.button,
            ...(loading ? styles.disabledButton : {}),
          }}
          onClick={addMaintenanceBlock}
          disabled={loading}
        >
          {loading ? "Saving..." : "Add Maintenance Block"}
        </button>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Preview Slots</h2>

        <input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          style={styles.input}
        />

        <button
          style={{
            ...styles.button,
            ...(loading ? styles.disabledButton : {}),
          }}
          onClick={fetchSlots}
          disabled={loading}
        >
          {loading ? "Loading..." : "Load Slots"}
        </button>

        <div style={styles.legendContainer}>
          <div style={styles.legendItem}>
            <span
              style={{
                ...styles.legendColor,
                backgroundColor: "#16a34a",
              }}
            />
            <span>Available</span>
          </div>

          <div style={styles.legendItem}>
            <span
              style={{
                ...styles.legendColor,
                backgroundColor: "#ea580c",
              }}
            />
            <span>Booked</span>
          </div>

          <div style={styles.legendItem}>
            <span
              style={{
                ...styles.legendColor,
                backgroundColor: "#6b7280",
              }}
            />
            <span>Blocked</span>
          </div>
        </div>

        <div style={styles.slotContainer}>
          {slots.length === 0 ? (
            <p>No slots loaded</p>
          ) : (
            slots.map((slot, index) => {
              const status = normalizeSlotStatus(slot);
              const theme = getSlotTheme(status);
              const { capacity, bookedCount, remainingSeats } = getSlotNumbers(slot);

              return (
                <div
                  key={slot.id || index}
                  style={{
                    ...styles.slotCard,
                    backgroundColor: theme.backgroundColor,
                    borderColor: theme.borderColor,
                  }}
                >
                  <div
                    style={{
                      ...styles.statusBadge,
                      backgroundColor: theme.badgeBackground,
                      color: theme.badgeColor,
                    }}
                  >
                    {status}
                  </div>

                  <p style={styles.slotText}>
                    {formatTime(slot.startAt || slot.startTime)} -{" "}
                    {formatTime(slot.endAt || slot.endTime)}
                  </p>

                  <p style={styles.detailText}>
                    Remaining Seats: <strong>{remainingSeats}</strong>
                  </p>

                  <p style={styles.detailText}>
                    Booked Count: <strong>{bookedCount}</strong>
                  </p>

                  <p style={styles.detailText}>
                    Capacity: <strong>{capacity}</strong>
                  </p>

                  {slot.reason && (
                    <p style={styles.reasonText}>
                      Reason: <strong>{slot.reason}</strong>
                    </p>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    padding: "30px",
    minHeight: "100vh",
    backgroundColor: "#f4f6f8",
    fontFamily: "Arial, sans-serif",
  },
  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "16px",
    marginBottom: "20px",
    flexWrap: "wrap",
  },
  heading: {
    margin: 0,
    color: "#222",
  },
  backButton: {
    padding: "10px 16px",
    backgroundColor: "#64748b",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },
  section: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "10px",
    marginBottom: "20px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  sectionHeaderRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "12px",
    flexWrap: "wrap",
    marginBottom: "15px",
  },
  sectionTitle: {
    margin: 0,
    color: "#333",
  },
  input: {
    display: "block",
    width: "100%",
    maxWidth: "420px",
    marginBottom: "12px",
    padding: "10px",
    borderRadius: "6px",
    border: "1px solid #ccc",
  },
  checkboxLabel: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    marginBottom: "12px",
    color: "#333",
  },
  button: {
    padding: "10px 16px",
    backgroundColor: "#007bff",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "600",
  },
  secondaryButton: {
    padding: "10px 16px",
    backgroundColor: "#0f766e",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "600",
  },
  disabledButton: {
    opacity: 0.7,
    cursor: "not-allowed",
  },
  messageBox: {
    padding: "12px",
    borderRadius: "8px",
    marginBottom: "20px",
    fontWeight: "600",
  },
  successBox: {
    backgroundColor: "#ecfdf5",
    color: "#166534",
    border: "1px solid #86efac",
  },
  errorBox: {
    backgroundColor: "#fef2f2",
    color: "#991b1b",
    border: "1px solid #fca5a5",
  },
  infoBox: {
    maxWidth: "420px",
    marginBottom: "12px",
    padding: "10px 12px",
    borderRadius: "8px",
    backgroundColor: "#eff6ff",
    color: "#1d4ed8",
    border: "1px solid #93c5fd",
    fontSize: "14px",
    fontWeight: "600",
  },
  selectedRoomCard: {
    marginTop: "8px",
    padding: "14px",
    borderRadius: "8px",
    border: "1px solid #cbd5e1",
    backgroundColor: "#f8fafc",
    maxWidth: "700px",
  },
  selectedRoomText: {
    margin: "6px 0",
    fontSize: "14px",
    color: "#334155",
  },
  legendContainer: {
    display: "flex",
    gap: "16px",
    flexWrap: "wrap",
    marginTop: "16px",
    marginBottom: "18px",
  },
  legendItem: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    fontSize: "14px",
    color: "#374151",
    fontWeight: "600",
  },
  legendColor: {
    width: "14px",
    height: "14px",
    borderRadius: "50%",
    display: "inline-block",
  },
  slotContainer: {
    marginTop: "20px",
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
    gap: "15px",
  },
  slotCard: {
    border: "2px solid",
    borderRadius: "10px",
    padding: "14px",
    textAlign: "left",
  },
  statusBadge: {
    display: "inline-block",
    padding: "6px 10px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: "700",
    marginBottom: "10px",
  },
  slotText: {
    margin: "0 0 10px 0",
    fontWeight: "700",
    color: "#111827",
    fontSize: "16px",
  },
  detailText: {
    margin: "6px 0",
    fontSize: "14px",
    color: "#374151",
  },
  reasonText: {
    marginTop: "10px",
    fontSize: "14px",
    color: "#7c2d12",
    fontWeight: "600",
  },
};