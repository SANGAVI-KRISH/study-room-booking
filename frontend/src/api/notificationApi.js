const API_BASE = "http://localhost:8080/api/notifications";

export async function getMyNotifications() {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_BASE}/my`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch notifications");
  }

  return response.json();
}

export async function getUnreadCount() {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_BASE}/my/unread-count`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch unread count");
  }

  return response.json();
}

export async function markNotificationRead(id) {
  const token = localStorage.getItem("token");

  const response = await fetch(`${API_BASE}/${id}/read`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to mark notification as read");
  }

  return response.text();
}