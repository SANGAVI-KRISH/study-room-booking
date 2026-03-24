const FEEDBACK_BASE_URL = "http://localhost:8080/api/feedback";

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

/* ---------------- PAYLOAD HELPER ---------------- */

function normalizeFeedbackPayload(feedbackData) {
  if (!feedbackData) {
    throw new Error("Feedback data is required");
  }

  validateUuid(feedbackData.bookingId, "Booking ID");

  const rating = Number(feedbackData.rating);
  const cleanlinessRating = Number(feedbackData.cleanlinessRating);
  const usefulnessRating = Number(feedbackData.usefulnessRating);

  if (Number.isNaN(rating) || rating < 1 || rating > 5) {
    throw new Error("Overall rating must be between 1 and 5");
  }

  if (
    Number.isNaN(cleanlinessRating) ||
    cleanlinessRating < 1 ||
    cleanlinessRating > 5
  ) {
    throw new Error("Cleanliness rating must be between 1 and 5");
  }

  if (
    Number.isNaN(usefulnessRating) ||
    usefulnessRating < 1 ||
    usefulnessRating > 5
  ) {
    throw new Error("Usefulness rating must be between 1 and 5");
  }

  return {
    bookingId: feedbackData.bookingId.trim(),
    rating,
    cleanlinessRating,
    usefulnessRating,
    comments: feedbackData.comments?.trim() || "",
    maintenanceIssue: feedbackData.maintenanceIssue?.trim() || "",
  };
}

/* ---------------- FEEDBACK APIs ---------------- */

// Submit feedback
export async function submitFeedback(feedbackData, userId) {
  validateUuid(userId, "User ID");
  const payload = normalizeFeedbackPayload(feedbackData);

  const response = await fetch(
    `${FEEDBACK_BASE_URL}?userId=${encodeURIComponent(userId.trim())}`,
    {
      method: "POST",
      headers: getAuthHeaders(true),
      body: JSON.stringify(payload),
    }
  );

  return handleResponse(response, "Failed to submit feedback");
}

// Get feedback for a room (avg + list)
export async function getRoomFeedback(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${FEEDBACK_BASE_URL}/room/${roomId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch room feedback");
}

// Get feedback submitted by user
export async function getMyFeedback(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${FEEDBACK_BASE_URL}/user/${userId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch your feedback");
}

// Check if booking already has feedback
export async function hasBookingFeedback(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(
    `${FEEDBACK_BASE_URL}/booking/${bookingId}/exists`,
    {
      method: "GET",
      headers: getAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to check feedback status");
}

// Get feedback by booking
export async function getFeedbackByBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${FEEDBACK_BASE_URL}/booking/${bookingId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch booking feedback");
}

// Get all feedback
export async function getAllFeedback() {
  const response = await fetch(`${FEEDBACK_BASE_URL}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch all feedback");
}

// Admin page helper
// This keeps your AdminFeedback.jsx working without changing much
export async function getAllFeedbacks() {
  return getAllFeedback();
}

// Delete feedback
export async function deleteFeedback(feedbackId) {
  validateUuid(feedbackId, "Feedback ID");

  const response = await fetch(`${FEEDBACK_BASE_URL}/${feedbackId}`, {
    method: "DELETE",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to delete feedback");
}