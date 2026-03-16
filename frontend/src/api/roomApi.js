const ROOM_BASE_URL = "http://localhost:8080/api/rooms";
const BOOKING_BASE_URL = "http://localhost:8080/api/bookings";

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

function buildQueryParams(filters) {
  const params = new URLSearchParams();

  if (!filters?.date) {
    throw new Error("Date is required");
  }

  if (!filters?.startTime) {
    throw new Error("Start time is required");
  }

  if (!filters?.endTime) {
    throw new Error("End time is required");
  }

  params.append("date", filters.date);
  params.append("startTime", filters.startTime);
  params.append("endTime", filters.endTime);

  if (
    filters.seatingCapacity !== undefined &&
    filters.seatingCapacity !== null &&
    filters.seatingCapacity !== ""
  ) {
    params.append("seatingCapacity", String(filters.seatingCapacity));
  }

  if (filters.district && filters.district.trim() !== "") {
    params.append("district", filters.district.trim());
  }

  if (filters.location && filters.location.trim() !== "") {
    params.append("location", filters.location.trim());
  }

  if (filters.facility && filters.facility.trim() !== "") {
    params.append("facility", filters.facility.trim());
  }

  if (filters.facilities && filters.facilities.trim() !== "") {
    params.append("facility", filters.facilities.trim());
  }

  return params;
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

  throw new Error(errorMessage);
}

function validateUuid(value, fieldName) {
  if (!value || typeof value !== "string") {
    throw new Error(`${fieldName} is required`);
  }
}

function normalizeRoomPayload(roomData) {
  if (!roomData) {
    throw new Error("Room data is required");
  }

  const payload = {
    blockName: roomData.blockName?.trim() || "",
    roomNumber: roomData.roomNumber?.trim() || "",
    floorNumber: roomData.floorNumber?.trim() || "",
    seatingCapacity:
      roomData.seatingCapacity !== undefined &&
      roomData.seatingCapacity !== null &&
      roomData.seatingCapacity !== ""
        ? Number(roomData.seatingCapacity)
        : null,
    availabilityTimings: roomData.availabilityTimings?.trim() || "",
    facilities: roomData.facilities?.trim() || "",
    district: roomData.district?.trim() || "",
    location: roomData.location?.trim() || "",
    feePerHour:
      roomData.feePerHour !== undefined &&
      roomData.feePerHour !== null &&
      roomData.feePerHour !== ""
        ? Number(roomData.feePerHour)
        : 0,
    approvalRequired: Boolean(roomData.approvalRequired),
  };

  if (!payload.blockName) throw new Error("Block name is required");
  if (!payload.roomNumber) throw new Error("Room number is required");
  if (!payload.floorNumber) throw new Error("Floor number is required");
  if (
    payload.seatingCapacity === null ||
    Number.isNaN(payload.seatingCapacity) ||
    payload.seatingCapacity <= 0
  ) {
    throw new Error("Valid seating capacity is required");
  }
  if (!payload.availabilityTimings) {
    throw new Error("Availability timings are required");
  }
  if (!payload.facilities) throw new Error("Facilities are required");
  if (!payload.district) throw new Error("District is required");
  if (!payload.location) throw new Error("Location is required");
  if (Number.isNaN(payload.feePerHour) || payload.feePerHour < 0) {
    throw new Error("Valid fee per hour is required");
  }

  return payload;
}

function buildRoomFormData(roomData, imageFiles = []) {
  const payload = normalizeRoomPayload(roomData);
  const formData = new FormData();

  Object.entries(payload).forEach(([key, value]) => {
    formData.append(key, String(value));
  });

  imageFiles.forEach((file) => {
    formData.append("images", file);
  });

  return formData;
}

/* ---------------- ROOM APIs ---------------- */

export async function getAllRooms() {
  const response = await fetch(ROOM_BASE_URL, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch rooms");
}

export async function getRoomById(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${ROOM_BASE_URL}/${roomId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch room");
}

export async function addRoom(roomData) {
  const payload = normalizeRoomPayload(roomData);

  const response = await fetch(ROOM_BASE_URL, {
    method: "POST",
    headers: getAuthHeaders(true),
    body: JSON.stringify(payload),
  });

  return handleResponse(response, "Failed to add room");
}

export async function addRoomWithImages(roomData, imageFiles = []) {
  const formData = buildRoomFormData(roomData, imageFiles);

  const response = await fetch(ROOM_BASE_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    body: formData,
  });

  return handleResponse(response, "Failed to add room");
}

export async function updateRoom(roomId, roomData) {
  validateUuid(roomId, "Room ID");
  const payload = normalizeRoomPayload(roomData);

  const response = await fetch(`${ROOM_BASE_URL}/${roomId}`, {
    method: "PUT",
    headers: getAuthHeaders(true),
    body: JSON.stringify(payload),
  });

  return handleResponse(response, "Failed to update room");
}

export async function updateRoomWithImages(roomId, roomData, imageFiles = []) {
  validateUuid(roomId, "Room ID");
  const formData = buildRoomFormData(roomData, imageFiles);

  const response = await fetch(`${ROOM_BASE_URL}/${roomId}`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
    body: formData,
  });

  return handleResponse(response, "Failed to update room");
}

export async function deleteRoom(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${ROOM_BASE_URL}/${roomId}`, {
    method: "DELETE",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to delete room");
}

export async function getAvailableRooms(filters) {
  const params = buildQueryParams(filters);

  const response = await fetch(
    `${ROOM_BASE_URL}/available?${params.toString()}`,
    {
      method: "GET",
      headers: getAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to fetch available rooms");
}

/* ---------------- BOOKING APIs ---------------- */

function normalizeBookingPayload(bookingData) {
  if (!bookingData) {
    throw new Error("Booking data is required");
  }

  validateUuid(bookingData.roomId, "Room ID");
  validateUuid(bookingData.userId, "User ID");

  if (!bookingData.bookingDate) {
    throw new Error("Booking date is required");
  }

  if (!bookingData.startTime) {
    throw new Error("Start time is required");
  }

  if (!bookingData.endTime) {
    throw new Error("End time is required");
  }

  return {
    roomId: bookingData.roomId,
    userId: bookingData.userId,
    bookingDate: bookingData.bookingDate,
    startTime: bookingData.startTime,
    endTime: bookingData.endTime,
  };
}

export async function bookRoom(bookingData) {
  const payload = normalizeBookingPayload(bookingData);

  const response = await fetch(BOOKING_BASE_URL, {
    method: "POST",
    headers: getAuthHeaders(true),
    body: JSON.stringify(payload),
  });

  return handleResponse(response, "Failed to book room");
}

export async function getBookingSummary(bookingData) {
  const payload = normalizeBookingPayload(bookingData);

  const response = await fetch(`${BOOKING_BASE_URL}/summary`, {
    method: "POST",
    headers: getAuthHeaders(true),
    body: JSON.stringify(payload),
  });

  return handleResponse(response, "Failed to generate booking summary");
}

export async function getBookingById(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch booking");
}

export async function getBookingsByUserId(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${BOOKING_BASE_URL}/user/${userId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch user bookings");
}

export async function getCurrentBookings(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${BOOKING_BASE_URL}/user/${userId}/current`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch current bookings");
}

export async function getPastBookings(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${BOOKING_BASE_URL}/user/${userId}/past`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch past bookings");
}

export async function getBookingHistory(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${BOOKING_BASE_URL}/user/${userId}/history`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch booking history");
}

export async function updateBookingStatus(bookingId, status) {
  validateUuid(bookingId, "Booking ID");

  if (!status) {
    throw new Error("Booking status is required");
  }

  const response = await fetch(
    `${BOOKING_BASE_URL}/${bookingId}/status?status=${encodeURIComponent(status)}`,
    {
      method: "PUT",
      headers: getAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to update booking status");
}

export async function approveBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/approve`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to approve booking");
}

export async function rejectBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/reject`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to reject booking");
}

export async function cancelBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/cancel`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to cancel booking");
}

export async function completeBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/complete`, {
    method: "PUT",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to complete booking");
}

export async function rescheduleBooking(bookingId, rescheduleData) {
  validateUuid(bookingId, "Booking ID");

  if (!rescheduleData?.bookingDate) {
    throw new Error("Booking date is required");
  }

  if (!rescheduleData?.startTime) {
    throw new Error("Start time is required");
  }

  if (!rescheduleData?.endTime) {
    throw new Error("End time is required");
  }

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/reschedule`, {
    method: "PUT",
    headers: getAuthHeaders(true),
    body: JSON.stringify({
      bookingDate: rescheduleData.bookingDate,
      startTime: rescheduleData.startTime,
      endTime: rescheduleData.endTime,
    }),
  });

  return handleResponse(response, "Failed to reschedule booking");
}

export async function deleteBooking(bookingId) {
  validateUuid(bookingId, "Booking ID");

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}`, {
    method: "DELETE",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to delete booking");
}