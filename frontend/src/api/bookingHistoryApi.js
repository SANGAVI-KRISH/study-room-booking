const API_BASE_URL = "http://localhost:8080/api/bookings";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Authentication token not found");
  }

  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function getUserBookings(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}`, {
    headers: getAuthHeaders(),
  });
  if (!response.ok) throw new Error("Failed to fetch bookings");
  return response.json();
}

export async function getBookingHistory(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}/history`, {
    headers: getAuthHeaders(),
  });
  if (!response.ok) throw new Error("Failed to fetch booking history");
  return response.json();
}

export async function getPastBookings(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}/past`, {
    headers: getAuthHeaders(),
  });
  if (!response.ok) throw new Error("Failed to fetch past bookings");
  return response.json();
}

export async function getCompletedBookings(userId) {
  const response = await fetch(
    `${API_BASE_URL}/user/${userId}/history/completed`,
    {
      headers: getAuthHeaders(),
    }
  );
  if (!response.ok) throw new Error("Failed to fetch completed bookings");
  return response.json();
}

export async function getCancelledBookings(userId) {
  const response = await fetch(
    `${API_BASE_URL}/user/${userId}/history/cancelled`,
    {
      headers: getAuthHeaders(),
    }
  );
  if (!response.ok) throw new Error("Failed to fetch cancelled bookings");
  return response.json();
}

export async function getBookingHistoryByDateRange(userId, startDate, endDate) {
  const response = await fetch(
    `${API_BASE_URL}/user/${userId}/history/filter?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: getAuthHeaders(),
    }
  );
  if (!response.ok) throw new Error("Failed to fetch filtered history");
  return response.json();
}

export async function getBookingById(bookingId) {
  const response = await fetch(`${API_BASE_URL}/${bookingId}`, {
    headers: getAuthHeaders(),
  });
  if (!response.ok) throw new Error("Failed to fetch booking details");
  return response.json();
}

export async function downloadBookingHistoryPdf(userId) {
  const response = await fetch(
    `${API_BASE_URL}/user/${userId}/history/download/pdf`,
    {
      method: "GET",
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    throw new Error(`Failed to download booking history PDF: ${response.status}`);
  }

  return response.blob();
}