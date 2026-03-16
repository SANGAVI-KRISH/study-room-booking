import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Rooms() {
  const navigate = useNavigate();

  const [rooms, setRooms] = useState([]);
  const [form, setForm] = useState({
    roomName: "",
    floorBlock: "",
    seatingCapacity: "",
    availabilityTimings: "",
    facilities: "",
    location: "",
  });

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  useEffect(() => {
    if (!token) {
      navigate("/");
      return;
    }

    if (role !== "ADMIN") {
      alert("Access denied. Admin only.");
      navigate("/");
      return;
    }

    fetchRooms();
  }, [navigate]);

  const fetchRooms = async () => {
    try {
      setLoading(true);
      setError("");

      const res = await fetch("http://localhost:8080/api/rooms", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        throw new Error("Failed to fetch rooms");
      }

      const data = await res.json();
      setRooms(data);
    } catch (err) {
      setError(err.message || "Error fetching rooms");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAddRoom = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    if (
      !form.roomName.trim() ||
      !form.floorBlock.trim() ||
      !form.seatingCapacity ||
      !form.availabilityTimings.trim() ||
      !form.facilities.trim() ||
      !form.location.trim()
    ) {
      setError("Please fill all room details.");
      return;
    }

    if (Number(form.seatingCapacity) <= 0) {
      setError("Seating capacity must be greater than 0.");
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/rooms", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          ...form,
          seatingCapacity: Number(form.seatingCapacity),
        }),
      });

      if (!res.ok) {
        throw new Error("Failed to add room");
      }

      setMessage("Room added successfully.");

      setForm({
        roomName: "",
        floorBlock: "",
        seatingCapacity: "",
        availabilityTimings: "",
        facilities: "",
        location: "",
      });

      fetchRooms();
    } catch (err) {
      setError(err.message || "Error adding room");
    }
  };

  const handleDelete = async (id) => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete this room?"
    );

    if (!confirmDelete) return;

    try {
      setMessage("");
      setError("");

      const res = await fetch(`http://localhost:8080/api/rooms/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        throw new Error("Failed to delete room");
      }

      setMessage("Room deleted successfully.");
      fetchRooms();
    } catch (err) {
      setError(err.message || "Error deleting room");
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <div style={styles.topBar}>
          <h1 style={styles.heading}>Manage Study Rooms</h1>
          <button style={styles.backButton} onClick={() => navigate("/admin")}>
            Back to Dashboard
          </button>
        </div>

        {message && <p style={styles.success}>{message}</p>}
        {error && <p style={styles.error}>{error}</p>}

        <form onSubmit={handleAddRoom} style={styles.form}>
          <input
            type="text"
            name="roomName"
            placeholder="Room Name"
            value={form.roomName}
            onChange={handleChange}
            style={styles.input}
          />

          <input
            type="text"
            name="floorBlock"
            placeholder="Floor / Block"
            value={form.floorBlock}
            onChange={handleChange}
            style={styles.input}
          />

          <input
            type="number"
            name="seatingCapacity"
            placeholder="Seating Capacity"
            value={form.seatingCapacity}
            onChange={handleChange}
            style={styles.input}
          />

          <input
            type="text"
            name="availabilityTimings"
            placeholder="Availability Timings"
            value={form.availabilityTimings}
            onChange={handleChange}
            style={styles.input}
          />

          <input
            type="text"
            name="facilities"
            placeholder="Facilities"
            value={form.facilities}
            onChange={handleChange}
            style={styles.input}
          />

          <input
            type="text"
            name="location"
            placeholder="Location"
            value={form.location}
            onChange={handleChange}
            style={styles.input}
          />

          <button type="submit" style={styles.addButton}>
            Add Room
          </button>
        </form>

        <h2 style={styles.subHeading}>Room List</h2>

        {loading ? (
          <p style={styles.infoText}>Loading rooms...</p>
        ) : rooms.length === 0 ? (
          <p style={styles.infoText}>No rooms added yet.</p>
        ) : (
          <div style={styles.roomGrid}>
            {rooms.map((room) => (
              <div key={room.id} style={styles.card}>
                <h3 style={styles.roomTitle}>{room.roomName}</h3>
                <p style={styles.cardText}>
                  <strong>Floor / Block:</strong> {room.floorBlock}
                </p>
                <p style={styles.cardText}>
                  <strong>Capacity:</strong> {room.seatingCapacity} seats
                </p>
                <p style={styles.cardText}>
                  <strong>Availability:</strong> {room.availabilityTimings}
                </p>
                <p style={styles.cardText}>
                  <strong>Facilities:</strong> {room.facilities}
                </p>
                <p style={styles.cardText}>
                  <strong>Location:</strong> {room.location}
                </p>

                <button
                  style={styles.deleteButton}
                  onClick={() => handleDelete(room.id)}
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    backgroundColor: "#eef2f7",
    padding: "30px 20px",
    fontFamily: "Arial, sans-serif",
  },
  container: {
    maxWidth: "1200px",
    margin: "0 auto",
  },
  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    flexWrap: "wrap",
    gap: "12px",
    marginBottom: "20px",
  },
  heading: {
    fontSize: "38px",
    color: "#1f2937",
    margin: 0,
  },
  backButton: {
    backgroundColor: "#6c757d",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
  },
  form: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "15px",
    backgroundColor: "#ffffff",
    padding: "22px",
    borderRadius: "14px",
    boxShadow: "0 4px 14px rgba(0, 0, 0, 0.08)",
    marginBottom: "30px",
  },
  input: {
    padding: "12px",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    fontSize: "15px",
    outline: "none",
  },
  addButton: {
    gridColumn: "1 / -1",
    backgroundColor: "#0d6efd",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    padding: "12px",
    fontSize: "16px",
    fontWeight: "600",
    cursor: "pointer",
  },
  subHeading: {
    fontSize: "28px",
    color: "#1f2937",
    marginBottom: "18px",
  },
  roomGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
    gap: "20px",
  },
  card: {
    backgroundColor: "#ffffff",
    borderRadius: "14px",
    padding: "20px",
    boxShadow: "0 4px 14px rgba(0, 0, 0, 0.08)",
  },
  roomTitle: {
    fontSize: "22px",
    color: "#111827",
    marginBottom: "12px",
  },
  cardText: {
    margin: "8px 0",
    color: "#374151",
    lineHeight: "1.5",
  },
  deleteButton: {
    marginTop: "14px",
    backgroundColor: "#dc3545",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    padding: "10px 14px",
    fontSize: "15px",
    cursor: "pointer",
  },
  success: {
    color: "green",
    backgroundColor: "#eafaf1",
    padding: "10px 14px",
    borderRadius: "8px",
    marginBottom: "16px",
  },
  error: {
    color: "#b00020",
    backgroundColor: "#fdeaea",
    padding: "10px 14px",
    borderRadius: "8px",
    marginBottom: "16px",
  },
  infoText: {
    color: "#4b5563",
    fontSize: "16px",
  },
};