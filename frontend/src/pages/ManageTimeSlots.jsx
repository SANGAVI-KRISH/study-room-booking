import { useState } from "react";

export default function ManageTimeSlots() {
  const [roomId, setRoomId] = useState("");
  const [availability, setAvailability] = useState({
    dayOfWeek: 1,
    openTime: "09:00",
    closeTime: "18:00",
    slotDurationMins: 60,
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

  const token = localStorage.getItem("token");

  const saveAvailability = async () => {
    try {
      setMessage("");

      const response = await fetch(
        `http://localhost:8080/api/time-slots/admin/availability/${roomId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(availability),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to save room availability");
      }

      setMessage("Room availability saved successfully");
    } catch (error) {
      console.error(error);
      setMessage(error.message);
    }
  };

  const addMaintenanceBlock = async () => {
    try {
      setMessage("");

      const response = await fetch(
        `http://localhost:8080/api/time-slots/admin/maintenance/${roomId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(maintenance),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to add maintenance block");
      }

      setMessage("Maintenance block added successfully");
    } catch (error) {
      console.error(error);
      setMessage(error.message);
    }
  };

  const fetchSlots = async () => {
    try {
      setMessage("");
      setSlots([]);

      const response = await fetch(
        `http://localhost:8080/api/time-slots/${roomId}?date=${date}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch slots");
      }

      const data = await response.json();
      setSlots(data.slots || []);
      setMessage("Slots loaded successfully");
    } catch (error) {
      console.error(error);
      setMessage(error.message);
    }
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Time Slot Management</h1>

      {message && <div style={styles.messageBox}>{message}</div>}

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Select Room</h2>
        <input
          type="text"
          placeholder="Enter Room ID"
          value={roomId}
          onChange={(e) => setRoomId(e.target.value)}
          style={styles.input}
        />
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

        <input
          type="number"
          placeholder="Slot Duration (mins)"
          value={availability.slotDurationMins}
          onChange={(e) =>
            setAvailability({
              ...availability,
              slotDurationMins: Number(e.target.value),
            })
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

        <button style={styles.button} onClick={saveAvailability}>
          Save Availability
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

        <button style={styles.button} onClick={addMaintenanceBlock}>
          Add Maintenance Block
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

        <button style={styles.button} onClick={fetchSlots}>
          Load Slots
        </button>

        <div style={styles.slotContainer}>
          {slots.length === 0 ? (
            <p>No slots loaded</p>
          ) : (
            slots.map((slot, index) => (
              <div
                key={index}
                style={{
                  ...styles.slotCard,
                  backgroundColor: slot.available ? "#e8f5e9" : "#fdecea",
                  borderColor: slot.available ? "#2e7d32" : "#c62828",
                }}
              >
                <p style={styles.slotText}>
                  {slot.startTime} - {slot.endTime}
                </p>
                <p style={styles.slotStatus}>
                  {slot.available ? "Available" : "Unavailable"}
                </p>
              </div>
            ))
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
  heading: {
    marginBottom: "20px",
    color: "#222",
  },
  section: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "10px",
    marginBottom: "20px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
  },
  sectionTitle: {
    marginBottom: "15px",
    color: "#333",
  },
  input: {
    display: "block",
    width: "100%",
    maxWidth: "400px",
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
  messageBox: {
    backgroundColor: "#eef4ff",
    color: "#1f4b99",
    padding: "12px",
    borderRadius: "8px",
    marginBottom: "20px",
    fontWeight: "600",
  },
  slotContainer: {
    marginTop: "20px",
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "15px",
  },
  slotCard: {
    border: "2px solid",
    borderRadius: "8px",
    padding: "12px",
    textAlign: "center",
  },
  slotText: {
    margin: "0 0 6px 0",
    fontWeight: "600",
    color: "#222",
  },
  slotStatus: {
    margin: 0,
    fontSize: "14px",
    fontWeight: "700",
  },
};