const API_BASE_URL = "http://localhost:8080/api/bookings";

export async function getUserBookings(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}`);
  if (!response.ok) throw new Error("Failed to fetch bookings");
  return response.json();
}

export async function getCompletedBookings(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}/completed`);
  if (!response.ok) throw new Error("Failed to fetch completed bookings");
  return response.json();
}

export async function getCancelledBookings(userId) {
  const response = await fetch(`${API_BASE_URL}/user/${userId}/cancelled`);
  if (!response.ok) throw new Error("Failed to fetch cancelled bookings");
  return response.json();
}

export async function getBookingHistoryByDateRange(userId, startDate, endDate) {
  const response = await fetch(
    `${API_BASE_URL}/user/${userId}/history?startDate=${startDate}&endDate=${endDate}`
  );
  if (!response.ok) throw new Error("Failed to fetch filtered history");
  return response.json();
}

export async function getBookingDetails(bookingId) {
  const response = await fetch(`${API_BASE_URL}/details/${bookingId}`);
  if (!response.ok) throw new Error("Failed to fetch booking details");
  return response.json();
}

export function downloadBookingHistory(userId) {
  window.open(`${API_BASE_URL}/user/${userId}/download`, "_blank");
}