const API_BASE = "http://localhost:8080/api/notifications";

function getToken() {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("User not authenticated");
  }

  return token;
}

function getAuthHeaders(isJson = true) {
  const token = getToken();

  const headers = {
    Authorization: `Bearer ${token}`,
  };

  if (isJson) {
    headers["Content-Type"] = "application/json";
  }

  return headers;
}

async function handleResponse(response, defaultMessage) {
  if (response.ok) {
    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
      return response.json();
    }

    return response.text();
  }

  let errorMessage = defaultMessage;

  try {
    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
      const errorData = await response.json();
      errorMessage =
        errorData?.message ||
        errorData?.error ||
        errorData?.details ||
        JSON.stringify(errorData) ||
        defaultMessage;
    } else {
      const errorText = await response.text();
      if (errorText) {
        errorMessage = errorText;
      }
    }
  } catch {
    errorMessage = defaultMessage;
  }

  if (response.status === 401) {
    errorMessage = "Unauthorized";
  } else if (response.status === 403) {
    errorMessage = "Forbidden";
  } else if (response.status === 500 && errorMessage === defaultMessage) {
    errorMessage = "Internal server error";
  }

  throw new Error(errorMessage);
}

export async function getMyNotifications() {
  const response = await fetch(`${API_BASE}/my`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch notifications");
}

export async function getUnreadNotifications() {
  const response = await fetch(`${API_BASE}/my/unread`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch unread notifications");
}

export async function getUnreadCount() {
  const response = await fetch(`${API_BASE}/my/unread-count`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch unread count");
}

export async function markNotificationRead(id) {
  const response = await fetch(`${API_BASE}/${id}/read`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to mark notification as read");
}

export async function markNotificationUnread(id) {
  const response = await fetch(`${API_BASE}/${id}/unread`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to mark notification as unread");
}

export async function markAllNotificationsRead() {
  const response = await fetch(`${API_BASE}/my/read-all`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to mark all notifications as read");
}

export async function deleteNotification(id) {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to delete notification");
}

export async function clearAllNotifications() {
  const response = await fetch(`${API_BASE}/my/clear-all`, {
    method: "DELETE",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to clear all notifications");
}