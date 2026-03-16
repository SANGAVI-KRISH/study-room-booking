import { useEffect, useState } from "react";
import {
  getMyNotifications,
  markNotificationRead,
} from "../api/notificationApi";

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await getMyNotifications();
      setNotifications(data);
      setError("");
    } catch (err) {
      setError(err.message || "Failed to load notifications");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkRead = async (id) => {
    try {
      await markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true, isRead: true } : n))
      );
    } catch (err) {
      alert("Failed to mark as read");
    }
  };

  return (
    <div className="page-container">
      <h2>My Notifications</h2>

      {loading && <p>Loading notifications...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      {!loading && notifications.length === 0 && <p>No notifications found.</p>}

      <div style={{ display: "grid", gap: "12px", marginTop: "20px" }}>
        {notifications.map((n) => (
          <div
            key={n.id}
            style={{
              border: "1px solid #ddd",
              borderRadius: "10px",
              padding: "15px",
              background: n.read || n.isRead ? "#f8f8f8" : "#eef6ff",
            }}
          >
            <h4>{n.title}</h4>
            <p>{n.message}</p>
            <small>
              {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
            </small>
            <br />
            {!(n.read || n.isRead) && (
              <button
                style={{ marginTop: "10px" }}
                onClick={() => handleMarkRead(n.id)}
              >
                Mark as Read
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}