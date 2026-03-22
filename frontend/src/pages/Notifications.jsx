import { useEffect, useState } from "react";
import {
  getMyNotifications,
  markNotificationRead,
  deleteNotification,
  markAllNotificationsRead,
  clearAllNotifications,
} from "../api/notificationApi";

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

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

  const handleDelete = async (id) => {
    try {
      setActionLoading(true);
      await deleteNotification(id);

      setNotifications((prev) =>
        prev.filter((notification) => notification.id !== id)
      );
    } catch (err) {
      alert(err.message || "Failed to delete notification");
    } finally {
      setActionLoading(false);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      setActionLoading(true);
      await markAllNotificationsRead();

      setNotifications((prev) =>
        prev.map((notification) => ({
          ...notification,
          read: true,
          isRead: true,
        }))
      );
    } catch (err) {
      alert(err.message || "Failed to mark all notifications as read");
    } finally {
      setActionLoading(false);
    }
  };

  const handleClearAll = async () => {
    const confirmed = window.confirm(
      "Are you sure you want to clear all notifications?"
    );

    if (!confirmed) return;

    try {
      setActionLoading(true);
      await clearAllNotifications();
      setNotifications([]);
    } catch (err) {
      alert(err.message || "Failed to clear all notifications");
    } finally {
      setActionLoading(false);
    }
  };

  const formatType = (type) => {
    if (!type) return "";
    return type
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  };

  const unreadCount = notifications.filter(
    (notification) => !(notification.read || notification.isRead)
  ).length;

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
        <div>
          <h2 style={{ margin: 0 }}>My Notifications</h2>
          <p
            style={{
              margin: "6px 0 0 0",
              color: "#666",
              fontSize: "14px",
            }}
          >
            Total: {notifications.length} | Unread: {unreadCount}
          </p>
        </div>

        <div
          style={{
            display: "flex",
            gap: "10px",
            flexWrap: "wrap",
          }}
        >
          <button
            onClick={loadNotifications}
            disabled={loading || actionLoading}
            style={{
              padding: "8px 14px",
              borderRadius: "8px",
              border: "1px solid #ccc",
              backgroundColor: "#fff",
              cursor: loading || actionLoading ? "not-allowed" : "pointer",
              opacity: loading || actionLoading ? 0.7 : 1,
            }}
          >
            Refresh
          </button>

          <button
            onClick={handleMarkAllRead}
            disabled={loading || actionLoading || notifications.length === 0 || unreadCount === 0}
            style={{
              padding: "8px 14px",
              borderRadius: "8px",
              border: "none",
              backgroundColor: "#2563eb",
              color: "#fff",
              cursor:
                loading || actionLoading || notifications.length === 0 || unreadCount === 0
                  ? "not-allowed"
                  : "pointer",
              opacity:
                loading || actionLoading || notifications.length === 0 || unreadCount === 0
                  ? 0.7
                  : 1,
              fontWeight: "500",
            }}
          >
            Mark All Read
          </button>

          <button
            onClick={handleClearAll}
            disabled={loading || actionLoading || notifications.length === 0}
            style={{
              padding: "8px 14px",
              borderRadius: "8px",
              border: "none",
              backgroundColor: "#dc2626",
              color: "#fff",
              cursor:
                loading || actionLoading || notifications.length === 0
                  ? "not-allowed"
                  : "pointer",
              opacity:
                loading || actionLoading || notifications.length === 0 ? 0.7 : 1,
              fontWeight: "500",
            }}
          >
            Clear All
          </button>
        </div>
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
                position: "relative",
              }}
            >
              <button
                onClick={() => handleDelete(notification.id)}
                disabled={actionLoading}
                title="Delete notification"
                style={{
                  position: "absolute",
                  top: "10px",
                  right: "10px",
                  width: "30px",
                  height: "30px",
                  borderRadius: "50%",
                  border: "none",
                  backgroundColor: "transparent",
                  color: "#dc2626",
                  fontSize: "20px",
                  fontWeight: "bold",
                  cursor: actionLoading ? "not-allowed" : "pointer",
                  lineHeight: 1,
                }}
              >
                ×
              </button>

              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "flex-start",
                  gap: "12px",
                  flexWrap: "wrap",
                  paddingRight: "40px",
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
                    disabled={actionLoading}
                    style={{
                      padding: "8px 14px",
                      borderRadius: "8px",
                      border: "none",
                      backgroundColor: "#2563eb",
                      color: "#fff",
                      cursor: actionLoading ? "not-allowed" : "pointer",
                      fontWeight: "500",
                      opacity: actionLoading ? 0.7 : 1,
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