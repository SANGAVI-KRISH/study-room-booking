const ROOM_BASE_URL = "http://localhost:8080/api/rooms";
const BOOKING_BASE_URL = "http://localhost:8080/api/bookings";
const ADMIN_DASHBOARD_BASE_URL = "http://localhost:8080/api/admin/dashboard";
const REPORT_BASE_URL = "http://localhost:8080/api/reports";
const TIME_SLOT_BASE_URL = "http://localhost:8080/api/time-slots";

/* ---------------- AUTH HELPERS ---------------- */

function getToken() {
  const token = localStorage.getItem("token");

  if (!token || token === "null" || token === "undefined") {
    throw new Error("User not authenticated");
  }

  return token.trim();
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

function getOptionalAuthHeaders(isJson = true) {
  const token = localStorage.getItem("token");
  const headers = {};

  if (token && token !== "null" && token !== "undefined" && token.trim()) {
    headers.Authorization = `Bearer ${token.trim()}`;
  }

  if (isJson) {
    headers["Content-Type"] = "application/json";
  }

  return headers;
}

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

function isNonEmptyString(value) {
  return typeof value === "string" && value.trim() !== "";
}

function isValidDateTimeString(value) {
  if (!isNonEmptyString(value)) return false;
  const d = new Date(value);
  return !Number.isNaN(d.getTime());
}

/* ---------------- QUERY HELPERS ---------------- */

function appendParam(params, key, value) {
  if (value !== undefined && value !== null && String(value).trim() !== "") {
    params.append(key, String(value).trim());
  }
}

function buildQueryParams(filters = {}) {
  const params = new URLSearchParams();

  appendParam(params, "date", filters.date);
  appendParam(params, "startTime", filters.startTime);
  appendParam(params, "endTime", filters.endTime);
  appendParam(params, "district", filters.district);
  appendParam(params, "location", filters.location);
  appendParam(params, "time", filters.time);
  appendParam(params, "roomId", filters.roomId);

  if (filters.facility) {
    appendParam(params, "facility", filters.facility);
  }

  if (filters.facilities) {
    appendParam(params, "facility", filters.facilities);
  }

  if (
    filters.seatingCapacity !== undefined &&
    filters.seatingCapacity !== null &&
    String(filters.seatingCapacity).trim() !== ""
  ) {
    params.append("seatingCapacity", String(filters.seatingCapacity).trim());
  }

  if (
    filters.maxPrice !== undefined &&
    filters.maxPrice !== null &&
    String(filters.maxPrice).trim() !== ""
  ) {
    params.append("maxPrice", String(filters.maxPrice).trim());
  }

  if (
    filters.pricePerHour !== undefined &&
    filters.pricePerHour !== null &&
    String(filters.pricePerHour).trim() !== ""
  ) {
    params.append("pricePerHour", String(filters.pricePerHour).trim());
  }

  return params;
}

/* ---------------- RESPONSE HELPER ---------------- */

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

  if (
    response.status === 401 &&
    !String(errorMessage).toLowerCase().includes("unauthorized")
  ) {
    errorMessage = "Unauthorized";
  }

  throw new Error(errorMessage);
}

/* ---------------- ROOM PAYLOAD HELPERS ---------------- */

function normalizeRoomPayload(roomData) {
  if (!roomData) {
    throw new Error("Room data is required");
  }

  const payload = {
    displayName: roomData.displayName?.trim() || "",
    blockName: roomData.blockName?.trim() || "",
    roomNumber: roomData.roomNumber?.trim() || "",
    floorNumber: roomData.floorNumber?.trim() || "",
    seatingCapacity:
      roomData.seatingCapacity !== undefined &&
      roomData.seatingCapacity !== null &&
      roomData.seatingCapacity !== ""
        ? Number(roomData.seatingCapacity)
        : null,
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

/* ---------------- LOCAL FILTER HELPERS ---------------- */

function normalizeArrayResponse(data) {
  return Array.isArray(data) ? data : [];
}

function filterRoomsLocally(rooms, filters = {}) {
  const roomList = normalizeArrayResponse(rooms);

  return roomList.filter((room) => {
    const districtMatch = filters.district
      ? String(room.district || "").toLowerCase() ===
        String(filters.district).trim().toLowerCase()
      : true;

    const locationSearch = String(filters.location || "").trim().toLowerCase();
    const locationMatch = locationSearch
      ? [
          room.location,
          room.displayName,
          room.roomName,
          room.blockName,
          room.roomNumber,
          room.floorBlock,
          room.floorNumber,
        ]
          .filter(Boolean)
          .some((value) =>
            String(value).toLowerCase().includes(locationSearch)
          )
      : true;

    const facilitiesSearch = String(
      filters.facilities || filters.facility || ""
    )
      .trim()
      .toLowerCase();

    const facilitiesMatch = facilitiesSearch
      ? String(room.facilities || "")
          .toLowerCase()
          .includes(facilitiesSearch)
      : true;

    const maxPriceMatch =
      filters.maxPrice !== undefined &&
      filters.maxPrice !== null &&
      String(filters.maxPrice).trim() !== ""
        ? Number(room.feePerHour || 0) <= Number(filters.maxPrice)
        : true;

    const roomMatch = filters.roomId
      ? String(room.id) === String(filters.roomId)
      : true;

    return (
      districtMatch &&
      locationMatch &&
      facilitiesMatch &&
      maxPriceMatch &&
      roomMatch
    );
  });
}

function normalizeSlotList(slots) {
  return normalizeArrayResponse(slots)
    .map((slot) => ({
      ...slot,
      remainingSeats:
        slot?.remainingSeats !== undefined && slot?.remainingSeats !== null
          ? Number(slot.remainingSeats)
          : 0,
    }))
    .filter(
      (slot) =>
        slot &&
        slot.startAt &&
        slot.endAt &&
        slot.isActive !== false &&
        Number(slot.remainingSeats || 0) > 0
    );
}

function isSameDate(dateTime, selectedDate) {
  if (!selectedDate) return true;
  if (!dateTime) return false;

  const d = new Date(dateTime);
  if (Number.isNaN(d.getTime())) return false;

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const localDate = `${year}-${month}-${day}`;

  return localDate === String(selectedDate).trim();
}

function isTimeWithinSlot(slot, selectedTime) {
  if (!selectedTime || !String(selectedTime).trim()) return true;
  if (!slot?.startAt || !slot?.endAt) return false;

  const start = new Date(slot.startAt);
  const end = new Date(slot.endAt);

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return false;
  }

  const [hh, mm] = String(selectedTime)
    .trim()
    .split(":")
    .map(Number);

  if (Number.isNaN(hh) || Number.isNaN(mm)) return false;

  const selectedMinutes = hh * 60 + mm;
  const startMinutes = start.getHours() * 60 + start.getMinutes();
  const endMinutes = end.getHours() * 60 + end.getMinutes();

  if (endMinutes >= startMinutes) {
    return selectedMinutes >= startMinutes && selectedMinutes < endMinutes;
  }

  return selectedMinutes >= startMinutes || selectedMinutes < endMinutes;
}

function filterSlotsLocally(slots, filters = {}) {
  const slotList = normalizeSlotList(slots);

  return slotList.filter((slot) => {
    const dateMatch = filters.date ? isSameDate(slot.startAt, filters.date) : true;
    const timeMatch = filters.time
      ? isTimeWithinSlot(slot, filters.time)
      : true;

    return dateMatch && timeMatch;
  });
}

/* ---------------- ROOM APIs ---------------- */

export async function getAllRooms() {
  const response = await fetch(ROOM_BASE_URL, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch rooms");
}

export async function getRoomById(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${ROOM_BASE_URL}/${roomId}`, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
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
    headers: getAuthHeaders(false),
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
    headers: getAuthHeaders(false),
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

  const message = await response.text();

  if (!response.ok) {
    throw new Error(message || "Failed to delete room");
  }

  return message || "Room deleted successfully";
}

export async function getAvailableRooms(filters = {}) {
  const params = buildQueryParams(filters);
  const queryString = params.toString();
  const url = queryString
    ? `${ROOM_BASE_URL}/available?${queryString}`
    : `${ROOM_BASE_URL}/available`;

  const response = await fetch(url, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch available rooms");
}

export async function searchRooms(filters = {}) {
  const rooms = await getAllRooms();
  return filterRoomsLocally(rooms, filters);
}

/* ---------------- TIME SLOT APIs ---------------- */

export async function getAllTimeSlots() {
  const response = await fetch(TIME_SLOT_BASE_URL, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch time slots");
}

export async function getTimeSlotsByRoom(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${TIME_SLOT_BASE_URL}/room/${roomId}`, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch room time slots");
}

export async function getActiveTimeSlotsByRoom(roomId) {
  validateUuid(roomId, "Room ID");

  const response = await fetch(`${TIME_SLOT_BASE_URL}/room/${roomId}/active`, {
    method: "GET",
    headers: getOptionalAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch active room time slots");
}

/*
  GET /api/time-slots/{roomId}?date=yyyy-MM-dd
  Returns AvailableSlotsResponse
*/
export async function getAvailableSlotsByRoomAndDate(roomId, date) {
  validateUuid(roomId, "Room ID");

  if (!date || !String(date).trim()) {
    throw new Error("Date is required");
  }

  const response = await fetch(
    `${TIME_SLOT_BASE_URL}/${roomId}?date=${encodeURIComponent(
      String(date).trim()
    )}`,
    {
      method: "GET",
      headers: getOptionalAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to fetch available room slots");
}

/*
  Returns only slot array from AvailableSlotsResponse
*/
export async function getAvailableSlotItemsByRoomAndDate(roomId, date) {
  const response = await getAvailableSlotsByRoomAndDate(roomId, date);

  if (Array.isArray(response)) {
    return response;
  }

  if (Array.isArray(response?.slots)) {
    return response.slots;
  }

  if (Array.isArray(response?.availableSlots)) {
    return response.availableSlots;
  }

  if (Array.isArray(response?.timeSlots)) {
    return response.timeSlots;
  }

  return [];
}

export async function getFilteredSlotsByRoom(roomId, filters = {}) {
  const slots = await getTimeSlotsByRoom(roomId);
  return filterSlotsLocally(slots, filters);
}

export async function searchRoomsWithAvailability(filters = {}) {
  const matchedRooms = await searchRooms(filters);

  if (!filters.date && !filters.time) {
    return matchedRooms.map((room) => ({
      room,
      slots: [],
    }));
  }

  const results = await Promise.all(
    matchedRooms.map(async (room) => {
      try {
        let slots = [];

        if (filters.date) {
          slots = await getAvailableSlotItemsByRoomAndDate(room.id, filters.date);
        } else {
          slots = await getTimeSlotsByRoom(room.id);
        }

        const filteredSlots = filterSlotsLocally(slots, filters);

        return {
          room,
          slots: filteredSlots,
        };
      } catch {
        return {
          room,
          slots: [],
        };
      }
    })
  );

  return results.filter((item) => item.slots.length > 0);
}

/* ---------------- BOOKING HELPERS ---------------- */

function normalizeBookingPayload(bookingData) {
  if (!bookingData) {
    throw new Error("Booking data is required");
  }

  validateUuid(bookingData.roomId, "Room ID");
  validateUuid(bookingData.userId, "User ID");

  const hasTimeSlotId = isNonEmptyString(bookingData.timeSlotId);
  const hasStartAt = isNonEmptyString(bookingData.startAt);
  const hasEndAt = isNonEmptyString(bookingData.endAt);

  if (hasTimeSlotId) {
    validateUuid(bookingData.timeSlotId, "Time Slot ID");
  }

  if (hasStartAt || hasEndAt) {
    if (!hasStartAt || !hasEndAt) {
      throw new Error("Both startAt and endAt are required");
    }

    if (!isValidDateTimeString(bookingData.startAt)) {
      throw new Error("startAt must be a valid date-time");
    }

    if (!isValidDateTimeString(bookingData.endAt)) {
      throw new Error("endAt must be a valid date-time");
    }

    const start = new Date(bookingData.startAt);
    const end = new Date(bookingData.endAt);

    if (end <= start) {
      throw new Error("endAt must be after startAt");
    }
  } else if (!hasTimeSlotId) {
    throw new Error("Either Time Slot ID or start/end time is required");
  }

  const attendeeCount =
    bookingData.attendeeCount !== undefined &&
    bookingData.attendeeCount !== null &&
    bookingData.attendeeCount !== ""
      ? Number(bookingData.attendeeCount)
      : null;

  if (
    attendeeCount === null ||
    Number.isNaN(attendeeCount) ||
    attendeeCount <= 0
  ) {
    throw new Error("Attendee count must be greater than 0");
  }

  const payload = {
    roomId: bookingData.roomId.trim(),
    userId: bookingData.userId.trim(),
    purpose: bookingData.purpose?.trim() || null,
    attendeeCount,
  };

  if (hasTimeSlotId) {
    payload.timeSlotId = bookingData.timeSlotId.trim();
  }

  if (hasStartAt && hasEndAt) {
    payload.startAt = String(bookingData.startAt).trim();
    payload.endAt = String(bookingData.endAt).trim();
  }

  return payload;
}

function normalizeReschedulePayload(rescheduleData) {
  if (!rescheduleData) {
    throw new Error("Reschedule data is required");
  }

  const hasTimeSlotId = isNonEmptyString(rescheduleData.timeSlotId);
  const hasStartAt = isNonEmptyString(rescheduleData.startAt);
  const hasEndAt = isNonEmptyString(rescheduleData.endAt);

  if (hasTimeSlotId) {
    validateUuid(rescheduleData.timeSlotId, "Time Slot ID");
  }

  if (hasStartAt || hasEndAt) {
    if (!hasStartAt || !hasEndAt) {
      throw new Error("Both startAt and endAt are required");
    }

    if (!isValidDateTimeString(rescheduleData.startAt)) {
      throw new Error("startAt must be a valid date-time");
    }

    if (!isValidDateTimeString(rescheduleData.endAt)) {
      throw new Error("endAt must be a valid date-time");
    }

    const start = new Date(rescheduleData.startAt);
    const end = new Date(rescheduleData.endAt);

    if (end <= start) {
      throw new Error("endAt must be after startAt");
    }
  } else if (!hasTimeSlotId) {
    throw new Error("Either Time Slot ID or start/end time is required");
  }

  const attendeeCount =
    rescheduleData.attendeeCount !== undefined &&
    rescheduleData.attendeeCount !== null &&
    rescheduleData.attendeeCount !== ""
      ? Number(rescheduleData.attendeeCount)
      : null;

  if (
    attendeeCount !== null &&
    (Number.isNaN(attendeeCount) || attendeeCount <= 0)
  ) {
    throw new Error("Attendee count must be greater than 0");
  }

  const payload = {
    purpose: rescheduleData.purpose?.trim() || null,
  };

  if (attendeeCount !== null) {
    payload.attendeeCount = attendeeCount;
  }

  if (hasTimeSlotId) {
    payload.timeSlotId = rescheduleData.timeSlotId.trim();
  }

  if (hasStartAt && hasEndAt) {
    payload.startAt = String(rescheduleData.startAt).trim();
    payload.endAt = String(rescheduleData.endAt).trim();
  }

  return payload;
}

/* ---------------- BOOKING APIs ---------------- */

export async function bookRoom(bookingData) {
  const payload = normalizeBookingPayload(bookingData);
  const headers = getAuthHeaders(true);

  console.log("BOOKING_BASE_URL:", BOOKING_BASE_URL);
  console.log("Booking headers:", headers);
  console.log("Booking payload:", payload);

  const response = await fetch(BOOKING_BASE_URL, {
    method: "POST",
    headers,
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

export async function getAllBookings() {
  const response = await fetch(BOOKING_BASE_URL, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch all bookings");
}

export async function getBookingsByUserId(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(`${BOOKING_BASE_URL}/user/${userId}`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch user bookings");
}

export async function getUserBookings(userId) {
  return getBookingsByUserId(userId);
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

export async function getBookingsByStatus(status) {
  if (!status || typeof status !== "string" || !status.trim()) {
    throw new Error("Booking status is required");
  }

  const response = await fetch(
    `${BOOKING_BASE_URL}/status/${encodeURIComponent(status.trim())}`,
    {
      method: "GET",
      headers: getAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to fetch bookings by status");
}

export async function getPendingBookings() {
  return getBookingsByStatus("PENDING");
}

export async function updateBookingStatus(bookingId, status) {
  validateUuid(bookingId, "Booking ID");

  if (!status) {
    throw new Error("Booking status is required");
  }

  const response = await fetch(
    `${BOOKING_BASE_URL}/${bookingId}/status?status=${encodeURIComponent(
      status
    )}`,
    {
      method: "PUT",
      headers: getAuthHeaders(false),
    }
  );

  return handleResponse(response, "Failed to update booking status");
}

export async function approveBooking(bookingId, approvedBy) {
  validateUuid(bookingId, "Booking ID");
  validateUuid(approvedBy, "Approved by");

  const response = await fetch(
    `${BOOKING_BASE_URL}/${bookingId}/approve?approvedBy=${encodeURIComponent(
      approvedBy
    )}`,
    {
      method: "PUT",
      headers: getAuthHeaders(false),
    }
  );

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

  const payload = normalizeReschedulePayload(rescheduleData);

  const response = await fetch(`${BOOKING_BASE_URL}/${bookingId}/reschedule`, {
    method: "PUT",
    headers: getAuthHeaders(true),
    body: JSON.stringify(payload),
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

export async function downloadBookingHistoryPdf(userId) {
  validateUuid(userId, "User ID");

  const response = await fetch(
    `${BOOKING_BASE_URL}/user/${userId}/history/download/pdf`,
    {
      method: "GET",
      headers: getAuthHeaders(false),
    }
  );

  if (!response.ok) {
    throw new Error(
      `Failed to download booking history PDF: ${response.status}`
    );
  }

  return response.blob();
}

/* ---------------- ADMIN DASHBOARD APIs ---------------- */

export async function getAdminDashboardStats() {
  const response = await fetch(ADMIN_DASHBOARD_BASE_URL, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch admin dashboard stats");
}

/* ---------------- REPORT APIs ---------------- */

export async function getDailyBookingReport() {
  const response = await fetch(`${REPORT_BASE_URL}/daily`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch daily booking report");
}

export async function getWeeklyBookingReport() {
  const response = await fetch(`${REPORT_BASE_URL}/weekly`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch weekly booking report");
}

export async function getMonthlyBookingReport() {
  const response = await fetch(`${REPORT_BASE_URL}/monthly`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch monthly booking report");
}

export async function getRoomUtilizationReport() {
  const response = await fetch(`${REPORT_BASE_URL}/room-utilization`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch room utilization report");
}

export async function getFrequentlyUsedRoomsReport() {
  const response = await fetch(`${REPORT_BASE_URL}/frequently-used-rooms`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(
    response,
    "Failed to fetch frequently used rooms report"
  );
}

export async function getCancellationAnalysisReport() {
  const response = await fetch(`${REPORT_BASE_URL}/cancellation-analysis`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch cancellation analysis report");
}

export async function getUserActivityReport() {
  const response = await fetch(`${REPORT_BASE_URL}/user-activity`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch user activity report");
}

export async function getRoomUsageTrendReport() {
  const response = await fetch(`${REPORT_BASE_URL}/room-usage-trend`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch room usage trend report");
}

export async function getPeakBookingHoursReport() {
  const response = await fetch(`${REPORT_BASE_URL}/peak-booking-hours`, {
    method: "GET",
    headers: getAuthHeaders(false),
  });

  return handleResponse(response, "Failed to fetch peak booking hours report");
}