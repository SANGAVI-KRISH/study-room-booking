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
      setNotifications(Array.isArray(data) ? data : []);
      setError("");
    } catch (err) {
      setError(err.message || "Failed to load notifications");
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkRead = async (id) => {
    try {
      const updatedNotification = await markNotificationRead(id);

      setNotifications((prev) =>
        prev.map((notification) =>
          notification.id === id
            ? {
                ...notification,
                ...(updatedNotification || {}),
                read: true,
                isRead: true,
              }
            : notification
        )
      );
    } catch (err) {
      alert(err.message || "Failed to mark notification as read");
    }
  };

  const formatType = (type) => {
    if (!type) return "";
    return type
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  };

  return (
    <div
      className="page-container"
      style={{
        maxWidth: "900px",
        margin: "0 auto",
        padding: "20px",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "20px",
          flexWrap: "wrap",
          gap: "10px",
        }}
      >
        <h2 style={{ margin: 0 }}>My Notifications</h2>

        <button
          onClick={loadNotifications}
          style={{
            padding: "8px 14px",
            borderRadius: "8px",
            border: "1px solid #ccc",
            backgroundColor: "#fff",
            cursor: "pointer",
          }}
        >
          Refresh
        </button>
      </div>

      {loading && (
        <p style={{ marginTop: "20px", color: "#555" }}>
          Loading notifications...
        </p>
      )}

      {!loading && error && (
        <p style={{ color: "red", marginTop: "20px" }}>{error}</p>
      )}

      {!loading && !error && notifications.length === 0 && (
        <div
          style={{
            border: "1px solid #e5e5e5",
            borderRadius: "12px",
            padding: "20px",
            backgroundColor: "#fafafa",
          }}
        >
          <p style={{ margin: 0 }}>No notifications found.</p>
        </div>
      )}

      <div style={{ display: "grid", gap: "14px", marginTop: "20px" }}>
        {notifications.map((notification) => {
          const isRead = notification.read || notification.isRead;

          return (
            <div
              key={notification.id}
              style={{
                border: "1px solid #ddd",
                borderRadius: "12px",
                padding: "16px",
                backgroundColor: isRead ? "#f8f8f8" : "#eef6ff",
                boxShadow: "0 2px 6px rgba(0,0,0,0.05)",
              }}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "flex-start",
                  gap: "12px",
                  flexWrap: "wrap",
                }}
              >
                <div style={{ flex: 1 }}>
                  <h4
                    style={{
                      margin: "0 0 8px 0",
                      fontSize: "18px",
                    }}
                  >
                    {notification.title || "Notification"}
                  </h4>

                  {notification.type && (
                    <p
                      style={{
                        margin: "0 0 8px 0",
                        fontSize: "13px",
                        fontWeight: "bold",
                        color: "#555",
                      }}
                    >
                      Type: {formatType(notification.type)}
                    </p>
                  )}
                </div>

                <span
                  style={{
                    padding: "4px 10px",
                    borderRadius: "999px",
                    fontSize: "12px",
                    fontWeight: "600",
                    backgroundColor: isRead ? "#e8f5e9" : "#dbeafe",
                    color: isRead ? "#2e7d32" : "#1d4ed8",
                  }}
                >
                  {isRead ? "Read" : "Unread"}
                </span>
              </div>

              <p
                style={{
                  margin: "8px 0 12px 0",
                  lineHeight: "1.5",
                  color: "#222",
                  whiteSpace: "pre-line",
                }}
              >
                {notification.message}
              </p>

              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  flexWrap: "wrap",
                  gap: "10px",
                }}
              >
                <small style={{ color: "#666" }}>
                  {notification.createdAt
                    ? new Date(notification.createdAt).toLocaleString()
                    : ""}
                </small>

                {!isRead && (
                  <button
                    onClick={() => handleMarkRead(notification.id)}
                    style={{
                      padding: "8px 14px",
                      borderRadius: "8px",
                      border: "none",
                      backgroundColor: "#2563eb",
                      color: "#fff",
                      cursor: "pointer",
                      fontWeight: "500",
                    }}
                  >
                    Mark as Read
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}