const API_BASE = "http://localhost:8080/api/notifications";

/* ---------------- AUTH HELPERS ---------------- */

function sanitizeToken(token) {
  if (!token || token === "null" || token === "undefined") {
    return "";
  }

  return String(token).replace(/^Bearer\s+/i, "").trim();
}

function getToken() {
  const token = sanitizeToken(localStorage.getItem("token"));

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

/* ---------------- VALIDATION HELPERS ---------------- */

function isValidUuid(value) {
  return (
    typeof value === "string" &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
      value.trim()
    )
  );
}

function validateUuid(value, fieldName) {
  if (!value || typeof value !== "string" || !value.trim()) {
    throw new Error(`${fieldName} is required`);
  }

  if (!isValidUuid(value.trim())) {
    throw new Error(`${fieldName} must be a valid UUID`);
  }
}

/* ---------------- RESPONSE HELPER ---------------- */

async function handleResponse(response, defaultMessage) {
  const contentType = response.headers.get("content-type") || "";
  let data = null;

  try {
    if (contentType.includes("application/json")) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = text ? { message: text } : null;
    }
  } catch {
    data = null;
  }

  if (!response.ok) {
    let errorMessage =
      data?.message || data?.error || data?.details || defaultMessage;

    if (response.status === 401) {
      errorMessage = "Unauthorized";
    } else if (response.status === 403) {
      errorMessage = "Forbidden";
    } else if (response.status === 404) {
      errorMessage = "Not found";
    } else if (response.status === 500 && errorMessage === defaultMessage) {
      errorMessage = "Internal server error";
    }

    throw new Error(errorMessage);
  }

  return data;
}

/* ---------------- NOTIFICATION APIs ---------------- */

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
  validateUuid(id, "Notification ID");

  const response = await fetch(`${API_BASE}/${id.trim()}/read`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to mark notification as read");
}

export async function markNotificationUnread(id) {
  validateUuid(id, "Notification ID");

  const response = await fetch(`${API_BASE}/${id.trim()}/unread`, {
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
  validateUuid(id, "Notification ID");

  const response = await fetch(`${API_BASE}/${id.trim()}`, {
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