import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getUnreadCount } from "../api/notificationApi";

export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({
    show: false,
    message: "",
  });

  const navigate = useNavigate();
  const previousUnreadCount = useRef(0);
  const toastTimerRef = useRef(null);
  const firstLoadRef = useRef(true);

  const showToast = (message) => {
    if (toastTimerRef.current) {
      clearTimeout(toastTimerRef.current);
    }

    setToast({
      show: true,
      message,
    });

    toastTimerRef.current = setTimeout(() => {
      setToast({
        show: false,
        message: "",
      });
    }, 4000);
  };

  const loadUnreadCount = async () => {
    try {
      setLoading(true);
      const count = await getUnreadCount();
      const safeCount = Number(count || 0);

      if (!firstLoadRef.current && safeCount > previousUnreadCount.current) {
        const diff = safeCount - previousUnreadCount.current;

        showToast(
          diff === 1
            ? "You have 1 new notification"
            : `You have ${diff} new notifications`
        );
      }

      setUnreadCount(safeCount);
      previousUnreadCount.current = safeCount;
      firstLoadRef.current = false;
    } catch (error) {
      console.error("Failed to load unread notification count:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUnreadCount();

    const interval = setInterval(() => {
      loadUnreadCount();
    }, 30000);

    const handleFocus = () => {
      loadUnreadCount();
    };

    window.addEventListener("focus", handleFocus);

    return () => {
      clearInterval(interval);
      window.removeEventListener("focus", handleFocus);

      if (toastTimerRef.current) {
        clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  const handleBellClick = () => {
    setToast({
      show: false,
      message: "",
    });
    navigate("/notifications");
  };

  const handleToastClick = () => {
    setToast({
      show: false,
      message: "",
    });
    navigate("/notifications");
  };

  const handleToastClose = () => {
    setToast({
      show: false,
      message: "",
    });
  };

  return (
    <>
      <button
        onClick={handleBellClick}
        title="Notifications"
        style={{
          position: "relative",
          background: "transparent",
          border: "none",
          fontSize: "24px",
          cursor: "pointer",
          padding: "6px",
          borderRadius: "50%",
          transition: "background 0.2s",
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = "#f3f4f6";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = "transparent";
        }}
      >
        🔔

        {unreadCount > 0 && (
          <span
            style={{
              position: "absolute",
              top: "-4px",
              right: "-6px",
              background: "red",
              color: "white",
              borderRadius: "50%",
              minWidth: "20px",
              height: "20px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: "11px",
              fontWeight: "bold",
              padding: "2px",
              boxShadow: "0 0 0 2px white",
            }}
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}

        {loading && (
          <span
            style={{
              position: "absolute",
              bottom: "-6px",
              right: "-6px",
              width: "8px",
              height: "8px",
              borderRadius: "50%",
              backgroundColor: "#2563eb",
            }}
          />
        )}
      </button>

      {toast.show && (
        <div
          onClick={handleToastClick}
          style={{
            position: "fixed",
            top: "20px",
            right: "20px",
            zIndex: 9999,
            minWidth: "280px",
            maxWidth: "360px",
            backgroundColor: "#111827",
            color: "#ffffff",
            borderRadius: "12px",
            padding: "14px 16px",
            boxShadow: "0 8px 24px rgba(0,0,0,0.2)",
            cursor: "pointer",
            display: "flex",
            alignItems: "flex-start",
            justifyContent: "space-between",
            gap: "12px",
          }}
        >
          <div style={{ flex: 1 }}>
            <div
              style={{
                fontWeight: "700",
                fontSize: "14px",
                marginBottom: "4px",
              }}
            >
              New Notification
            </div>

            <div
              style={{
                fontSize: "13px",
                lineHeight: "1.4",
                color: "#e5e7eb",
              }}
            >
              {toast.message}
            </div>
          </div>

          <button
            onClick={(e) => {
              e.stopPropagation();
              handleToastClose();
            }}
            style={{
              background: "transparent",
              border: "none",
              color: "#ffffff",
              fontSize: "16px",
              cursor: "pointer",
              padding: 0,
              lineHeight: 1,
            }}
            title="Close"
          >
            ×
          </button>
        </div>
      )}
    </>
  );
}