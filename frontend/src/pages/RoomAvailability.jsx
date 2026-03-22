import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { bookRoom } from "../api/roomApi";
import "./RoomAvailability.css";

const API_BASE_URL = "http://localhost:8080";
const APP_TIMEZONE_OFFSET = "+05:30";
const BOOKING_STEP_SECONDS = 1800;

export default function RoomAvailability() {
  const navigate = useNavigate();

  const tamilNaduDistricts = [
    "Ariyalur",
    "Chengalpattu",
    "Chennai",
    "Coimbatore",
    "Cuddalore",
    "Dharmapuri",
    "Dindigul",
    "Erode",
    "Kallakurichi",
    "Kanchipuram",
    "Kanyakumari",
    "Karur",
    "Krishnagiri",
    "Madurai",
    "Mayiladuthurai",
    "Nagapattinam",
    "Namakkal",
    "Nilgiris",
    "Perambalur",
    "Pudukkottai",
    "Ramanathapuram",
    "Ranipet",
    "Salem",
    "Sivaganga",
    "Tenkasi",
    "Thanjavur",
    "Theni",
    "Thoothukudi",
    "Tiruchirappalli",
    "Tirunelveli",
    "Tirupathur",
    "Tiruppur",
    "Tiruvallur",
    "Tiruvannamalai",
    "Tiruvarur",
    "Vellore",
    "Viluppuram",
    "Virudhunagar",
  ];

  const initialFilters = {
    district: "",
    location: "",
    roomId: "",
    date: "",
    time: "",
    maxPrice: "",
    facilities: "",
  };

  const initialBookingForm = {
    selectedWindowId: "",
    startAt: "",
    endAt: "",
    purpose: "",
    attendeeCount: "1",
  };

  const [filters, setFilters] = useState(initialFilters);
  const [allRooms, setAllRooms] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [roomSlotsMap, setRoomSlotsMap] = useState({});
  const [selectedRoomSlots, setSelectedRoomSlots] = useState([]);

  const [loading, setLoading] = useState(false);
  const [slotLoading, setSlotLoading] = useState(false);
  const [searchingAvailability, setSearchingAvailability] = useState(false);
  const [bookingRoomId, setBookingRoomId] = useState(null);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [bookingModalOpen, setBookingModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [bookingForm, setBookingForm] = useState(initialBookingForm);
  const [lastBookingDetails, setLastBookingDetails] = useState(null);

  const [galleryOpen, setGalleryOpen] = useState(false);
  const [galleryImages, setGalleryImages] = useState([]);
  const [galleryTitle, setGalleryTitle] = useState("");
  const [galleryIndex, setGalleryIndex] = useState(0);
  const [zoomStyle, setZoomStyle] = useState({
    backgroundImage: "",
    backgroundPosition: "center",
    opacity: 0,
  });

  const showSuccess = (text) => {
    setMessage(text);
    setError("");
  };

  const showError = (text) => {
    setError(text);
    setMessage("");
  };

  const clearAlerts = () => {
    setMessage("");
    setError("");
  };

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (galleryOpen) {
        if (e.key === "Escape") closeGallery();
        if (e.key === "ArrowLeft" && galleryImages.length > 1) showPrevImage();
        if (e.key === "ArrowRight" && galleryImages.length > 1) showNextImage();
      }

      if (bookingModalOpen && e.key === "Escape") {
        closeBookingModal();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [galleryOpen, galleryImages.length, bookingModalOpen]);

  useEffect(() => {
    fetchRooms();
  }, []);

  useEffect(() => {
    const locallyFilteredRooms = allRooms.filter((room) => {
      const districtMatch = filters.district
        ? String(room.district || "").toLowerCase() ===
          filters.district.toLowerCase()
        : true;

      const locationValue = filters.location.trim().toLowerCase();
      const locationMatch = locationValue
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
              String(value).toLowerCase().includes(locationValue)
            )
        : true;

      const maxPriceMatch =
        filters.maxPrice !== ""
          ? Number(room.feePerHour || 0) <= Number(filters.maxPrice)
          : true;

      const facilitiesValue = filters.facilities.trim().toLowerCase();
      const facilitiesMatch = facilitiesValue
        ? String(room.facilities || "")
            .toLowerCase()
            .includes(facilitiesValue)
        : true;

      return districtMatch && locationMatch && maxPriceMatch && facilitiesMatch;
    });

    setRooms(locallyFilteredRooms);

    if (
      filters.roomId &&
      !locallyFilteredRooms.some(
        (room) => String(room.id) === String(filters.roomId)
      )
    ) {
      setFilters((prev) => ({
        ...prev,
        roomId: "",
      }));
      setSelectedRoomSlots([]);
    }
  }, [
    allRooms,
    filters.district,
    filters.location,
    filters.maxPrice,
    filters.facilities,
    filters.roomId,
  ]);

  useEffect(() => {
    if (!filters.roomId) {
      setSelectedRoomSlots([]);
      return;
    }

    const matchedRoom =
      rooms.find((room) => String(room.id) === String(filters.roomId)) || null;

    if (!matchedRoom) {
      setSelectedRoomSlots([]);
      return;
    }

    loadSlotsForSelectedRoom(filters.roomId);
  }, [filters.roomId, filters.date, filters.time]);

  const hasAnyFilter = useMemo(() => {
    return Boolean(
      filters.district ||
        filters.location.trim() ||
        filters.roomId ||
        filters.date ||
        filters.time ||
        filters.maxPrice !== "" ||
        filters.facilities.trim()
    );
  }, [filters]);

  const fetchRooms = async () => {
    try {
      setLoading(true);
      clearAlerts();

      const token = localStorage.getItem("token");

      const res = await fetch(`${API_BASE_URL}/api/rooms/active`, {
        method: "GET",
        headers: token
          ? {
              Authorization: `Bearer ${token}`,
            }
          : {},
      });

      if (!res.ok) {
        throw new Error("Failed to fetch rooms.");
      }

      const data = await res.json();
      const roomList = Array.isArray(data) ? data : [];
      setAllRooms(roomList);
      setRooms(roomList);
    } catch (err) {
      console.error("Error fetching rooms:", err);
      showError(err.message || "Error fetching rooms.");
    } finally {
      setLoading(false);
    }
  };

  const pad2 = (n) => String(n).padStart(2, "0");

  const isUuid = (value) =>
    typeof value === "string" &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
      value
    );

  const addOneDay = (dateStr) => {
    const d = new Date(`${dateStr}T00:00:00`);
    d.setDate(d.getDate() + 1);
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(
      d.getDate()
    )}`;
  };

  const parseTimeString = (timeValue) => {
    if (!timeValue) return null;

    if (typeof timeValue === "string") {
      const trimmed = timeValue.trim();
      const parts = trimmed.split(":");
      if (parts.length >= 2) {
        return `${pad2(Number(parts[0]))}:${pad2(Number(parts[1]))}:00`;
      }
    }

    if (
      typeof timeValue === "object" &&
      timeValue.hour !== undefined &&
      timeValue.minute !== undefined
    ) {
      return `${pad2(Number(timeValue.hour))}:${pad2(
        Number(timeValue.minute)
      )}:00`;
    }

    return null;
  };

  const buildDateTimeFromDateAndTime = (dateStr, timeValue) => {
    const normalizedTime = parseTimeString(timeValue);
    if (!dateStr || !normalizedTime) return null;
    return `${dateStr}T${normalizedTime}${APP_TIMEZONE_OFFSET}`;
  };

  const needsNextDay = (startTime, endTime) => {
    const s = parseTimeString(startTime);
    const e = parseTimeString(endTime);
    if (!s || !e) return false;
    return e <= s;
  };

  const normalizeStoredSlots = (slotList) => {
  const list = Array.isArray(slotList) ? slotList : [];

  return list
    .filter((slot) => slot && slot.startAt && slot.endAt && slot.isActive !== false)
    .map((slot) => {
      const remainingSeats = Number(
        slot.remainingSeats ??
          slot.remaining ??
          slot.availableSeats ??
          slot.seatsRemaining ??
          slot.capacityLeft ??
          slot.capacity ??
          slot.seatingCapacity ??
          1
      );

      const available =
        slot.available !== false &&
        slot.status !== "UNAVAILABLE" &&
        slot.availabilityStatus !== "UNAVAILABLE" &&
        slot.isActive !== false;

      return {
        ...slot,
        remainingSeats,
        available,
        sourceType: "stored",
      };
    })
    .filter((slot) => slot.available);
};
  const normalizeAvailableDateSlots = (payload, selectedDate) => {
  const rawSlots =
    payload?.slots ||
    payload?.availableSlots ||
    payload?.timeSlots ||
    payload?.data ||
    (Array.isArray(payload) ? payload : []);

  const list = Array.isArray(rawSlots) ? rawSlots : [];

  return list
    .map((slot, index) => {
      if (!slot) return null;

      let startAt = slot.startAt || slot.start || null;
      let endAt = slot.endAt || slot.end || null;

      if (!startAt && slot.startTime) {
        startAt = buildDateTimeFromDateAndTime(selectedDate, slot.startTime);
      }

      if (!endAt && slot.endTime) {
        endAt = buildDateTimeFromDateAndTime(
          needsNextDay(slot.startTime, slot.endTime)
            ? addOneDay(selectedDate)
            : selectedDate,
          slot.endTime
        );
      }

      if (!startAt || !endAt) return null;

      const remainingSeats = Number(
        slot.remainingSeats ??
          slot.remaining ??
          slot.availableSeats ??
          slot.seatsRemaining ??
          slot.capacityLeft ??
          slot.capacity ??
          slot.seatingCapacity ??
          1
      );

      const available =
        slot.available !== false &&
        slot.status !== "UNAVAILABLE" &&
        slot.availabilityStatus !== "UNAVAILABLE" &&
        slot.isActive !== false;

      return {
        ...slot,
        id: slot.id || `generated-${selectedDate}-${startAt}-${endAt}-${index}`,
        startAt,
        endAt,
        remainingSeats,
        available,
        sourceType: "date-window",
      };
    })
    .filter(Boolean)
    .filter((slot) => slot.available);
};


  const fetchStoredRoomSlots = async (roomId) => {
    const token = localStorage.getItem("token");

    const res = await fetch(`${API_BASE_URL}/api/time-slots/room/${roomId}`, {
      method: "GET",
      headers: token
        ? {
            Authorization: `Bearer ${token}`,
          }
        : {},
    });

    if (!res.ok) {
      throw new Error("Failed to fetch room slots.");
    }

    const data = await res.json();
    return normalizeStoredSlots(data);
  };

  const fetchAvailableSlotsByDate = async (roomId, date) => {
    const token = localStorage.getItem("token");

    const res = await fetch(
      `${API_BASE_URL}/api/time-slots/${roomId}?date=${encodeURIComponent(
        date
      )}`,
      {
        method: "GET",
        headers: token
          ? {
              Authorization: `Bearer ${token}`,
            }
          : {},
      }
    );

    if (!res.ok) {
      throw new Error("Failed to fetch available slots for selected date.");
    }

    const data = await res.json();
    return normalizeAvailableDateSlots(data, date);
  };

  const fetchRoomSlots = async (roomId, date = "") => {
  if (!date) {
    return fetchStoredRoomSlots(roomId);
  }

  try {
    const dateSlots = await fetchAvailableSlotsByDate(roomId, date);

    if (Array.isArray(dateSlots) && dateSlots.length > 0) {
      return dateSlots;
    }

    const storedSlots = await fetchStoredRoomSlots(roomId);
    return applyDateTimeFilterToSlots(storedSlots);
  } catch (error) {
    console.warn("Date-based slot fetch failed, falling back to stored slots:", error);
    const storedSlots = await fetchStoredRoomSlots(roomId);
    return applyDateTimeFilterToSlots(storedSlots);
  }
};

  const isSameDate = (dateTime, selectedDate) => {
    if (!dateTime || !selectedDate) return true;

    const d = new Date(dateTime);
    if (Number.isNaN(d.getTime())) return false;

    const localYear = d.getFullYear();
    const localMonth = String(d.getMonth() + 1).padStart(2, "0");
    const localDay = String(d.getDate()).padStart(2, "0");
    const slotDate = `${localYear}-${localMonth}-${localDay}`;

    return slotDate === selectedDate;
  };

  const isTimeWithinSlot = (slot, selectedTime) => {
    if (!selectedTime) return true;
    if (!slot?.startAt || !slot?.endAt) return false;

    const start = new Date(slot.startAt);
    const end = new Date(slot.endAt);

    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
      return false;
    }

    const [hh, mm] = selectedTime.split(":").map(Number);
    const selectedMinutes = hh * 60 + mm;
    const startMinutes = start.getHours() * 60 + start.getMinutes();
    const endMinutes = end.getHours() * 60 + end.getMinutes();

    if (endMinutes >= startMinutes) {
      return selectedMinutes >= startMinutes && selectedMinutes < endMinutes;
    }

    return selectedMinutes >= startMinutes || selectedMinutes < endMinutes;
  };

  const applyDateTimeFilterToSlots = (slotList) => {
    return (slotList || []).filter((slot) => {
      const dateMatch = filters.date
        ? isSameDate(slot.startAt, filters.date)
        : true;

      const timeMatch = filters.time
        ? isTimeWithinSlot(slot, filters.time)
        : true;

      return dateMatch && timeMatch;
    });
  };

  const loadSlotsForSelectedRoom = async (roomId) => {
    try {
      setSlotLoading(true);
      clearAlerts();

      const cacheKey = filters.date
        ? `${roomId}_${filters.date}`
        : `${roomId}_all`;

      let slotsForRoom = roomSlotsMap[cacheKey];

      if (!slotsForRoom) {
        slotsForRoom = await fetchRoomSlots(roomId, filters.date);
        setRoomSlotsMap((prev) => ({
          ...prev,
          [cacheKey]: slotsForRoom,
        }));
      }

      setSelectedRoomSlots(applyDateTimeFilterToSlots(slotsForRoom));
    } catch (err) {
      console.error("Error fetching room slots:", err);
      showError(err.message || "Error fetching room slots.");
      setSelectedRoomSlots([]);
    } finally {
      setSlotLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFilters((prev) => {
      const updated = {
        ...prev,
        [name]: value,
      };

      if (
        name === "district" ||
        name === "location" ||
        name === "maxPrice" ||
        name === "facilities"
      ) {
        updated.roomId = "";
      }

      return updated;
    });

    if (
      name === "district" ||
      name === "location" ||
      name === "maxPrice" ||
      name === "facilities"
    ) {
      setSelectedRoomSlots([]);
    }
  };

  const roundUpToNext30Minutes = (date) => {
    const rounded = new Date(date);
    rounded.setSeconds(0, 0);

    const minutes = rounded.getMinutes();
    const remainder = minutes % 30;

    if (remainder !== 0) {
      rounded.setMinutes(minutes + (30 - remainder));
    }

    return rounded;
  };

  const toInputDateTimeValue = (dateTime) => {
    if (!dateTime) return "";
    const d = new Date(dateTime);
    if (Number.isNaN(d.getTime())) return "";

    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(
      d.getDate()
    )}T${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
  };

  const inputValueToIsoWithOffset = (value) => {
    if (!value) return null;
    return `${value}:00${APP_TIMEZONE_OFFSET}`;
  };

  const getMinBookableStartInput = (windowSlot) => {
    if (!windowSlot?.startAt) return "";
    const now = roundUpToNext30Minutes(new Date());
    const slotStart = new Date(windowSlot.startAt);
    const effective = now > slotStart ? now : slotStart;
    return toInputDateTimeValue(effective);
  };

  const getWindowEndInput = (windowSlot) => {
    return windowSlot?.endAt ? toInputDateTimeValue(windowSlot.endAt) : "";
  };

  const getWindowStartInput = (windowSlot) => {
    return windowSlot?.startAt ? toInputDateTimeValue(windowSlot.startAt) : "";
  };

  const bookingModalSlots = useMemo(() => {
  const roomId = selectedRoom?.id;
  if (!roomId) return [];

  const cacheKey = filters.date
    ? `${roomId}_${filters.date}`
    : `${roomId}_all`;

  const slotsForRoom = roomSlotsMap[cacheKey] || [];
  const filteredSlots = applyDateTimeFilterToSlots(slotsForRoom);

  return filteredSlots.map((slot) => {
    const fallbackCapacity = Number(
      selectedRoom?.seatingCapacity ??
        selectedRoom?.capacity ??
        1
    );

    const normalizedRemainingSeats = Number(
      slot.remainingSeats ??
        slot.remaining ??
        slot.availableSeats ??
        slot.seatsRemaining ??
        slot.capacityLeft ??
        fallbackCapacity
    );

    return {
      ...slot,
      remainingSeats:
        normalizedRemainingSeats > 0 ? normalizedRemainingSeats : fallbackCapacity,
    };
  });
}, [selectedRoom, roomSlotsMap, filters.date, filters.time]);

  const selectedWindow = useMemo(() => {
  return (
    bookingModalSlots.find(
      (slot) => String(slot.id) === String(bookingForm.selectedWindowId)
    ) || null
  );
}, [bookingModalSlots, bookingForm.selectedWindowId]);

const attendeeCountNumber = useMemo(() => {
  const parsed = Number(bookingForm.attendeeCount);
  if (Number.isNaN(parsed) || parsed < 1) return 1;
  return parsed;
}, [bookingForm.attendeeCount]);

const initialRemainingSeats = useMemo(() => {
  if (!selectedWindow) return null;

  return Number(
    selectedWindow.remainingSeats ??
      selectedRoom?.seatingCapacity ??
      selectedRoom?.capacity ??
      1
  );
}, [selectedWindow, selectedRoom]);

const remainingSeatsAfterSelection = useMemo(() => {
  if (initialRemainingSeats === null) return null;
  return Math.max(initialRemainingSeats - attendeeCountNumber, 0);
}, [initialRemainingSeats, attendeeCountNumber]);


  const selectedCustomRange = useMemo(() => {
    if (!bookingForm.startAt || !bookingForm.endAt) return null;

    const startIso = inputValueToIsoWithOffset(bookingForm.startAt);
    const endIso = inputValueToIsoWithOffset(bookingForm.endAt);

    if (!startIso || !endIso) return null;

    return {
      startAt: startIso,
      endAt: endIso,
    };
  }, [bookingForm.startAt, bookingForm.endAt]);

  const handleBookingInputChange = (e) => {
  const { name, value } = e.target;

  if (name === "selectedWindowId") {
    const nextWindow =
      bookingModalSlots.find((slot) => String(slot.id) === String(value)) ||
      null;

    if (!nextWindow) {
      setBookingForm((prev) => ({
        ...prev,
        selectedWindowId: "",
        startAt: "",
        endAt: "",
        attendeeCount: "1",
      }));
      return;
    }

    const minStartValue = getMinBookableStartInput(nextWindow);
    const windowEndValue = getWindowEndInput(nextWindow);

    const maxSeats = Number(
      nextWindow.remainingSeats ??
        selectedRoom?.seatingCapacity ??
        selectedRoom?.capacity ??
        1
    );

    setBookingForm((prev) => ({
      ...prev,
      selectedWindowId: value,
      startAt: minStartValue,
      endAt: windowEndValue,
      attendeeCount: String(
        Math.min(Math.max(Number(prev.attendeeCount || 1), 1), Math.max(maxSeats, 1))
      ),
    }));
    return;
  }

  if (name === "attendeeCount") {
    if (value === "") {
      setBookingForm((prev) => ({
        ...prev,
        attendeeCount: "1",
      }));
      return;
    }

    let nextCount = Number(value);
    if (Number.isNaN(nextCount)) return;

    if (nextCount < 1) nextCount = 1;

    const maxSeats = Number(
      selectedWindow?.remainingSeats ??
        selectedRoom?.seatingCapacity ??
        selectedRoom?.capacity ??
        1
    );

    if (maxSeats > 0) {
      nextCount = Math.min(nextCount, maxSeats);
    }

    setBookingForm((prev) => ({
      ...prev,
      attendeeCount: String(nextCount),
    }));
    return;
  }

  setBookingForm((prev) => ({
    ...prev,
    [name]: value,
  }));
};

  const handleReset = () => {
    setFilters(initialFilters);
    setRooms(allRooms);
    setSelectedRoomSlots([]);
    setLastBookingDetails(null);
    clearAlerts();
    closeBookingModal();
  };

  const validateBookingForm = () => {
  if (!selectedRoom || !selectedRoom.id) {
    showError("Selected room is invalid.");
    return false;
  }

  if (!bookingForm.selectedWindowId) {
    showError("Please select an available slot window.");
    return false;
  }

  if (!selectedWindow) {
    showError("Selected slot window is invalid.");
    return false;
  }

  if (!bookingForm.startAt || !bookingForm.endAt) {
    showError("Please select booking start and end time.");
    return false;
  }

  const bookingStart = new Date(inputValueToIsoWithOffset(bookingForm.startAt));
  const bookingEnd = new Date(inputValueToIsoWithOffset(bookingForm.endAt));
  const windowStart = new Date(selectedWindow.startAt);
  const windowEnd = new Date(selectedWindow.endAt);

  if (
    Number.isNaN(bookingStart.getTime()) ||
    Number.isNaN(bookingEnd.getTime()) ||
    Number.isNaN(windowStart.getTime()) ||
    Number.isNaN(windowEnd.getTime())
  ) {
    showError("Invalid booking date or time.");
    return false;
  }

  if (bookingStart >= bookingEnd) {
    showError("End time must be after start time.");
    return false;
  }

  if (bookingStart < windowStart || bookingEnd > windowEnd) {
    showError("Booking time must be inside the selected available window.");
    return false;
  }

  const durationMinutes =
    (bookingEnd.getTime() - bookingStart.getTime()) / 60000;

  if (durationMinutes <= 0) {
    showError("Booking duration must be greater than 0.");
    return false;
  }

  if (
    bookingStart.getMinutes() % 30 !== 0 ||
    bookingEnd.getMinutes() % 30 !== 0
  ) {
    showError("Booking time must be in 30-minute steps.");
    return false;
  }

  const attendeeCountNum = Number(bookingForm.attendeeCount);

  if (
    !bookingForm.attendeeCount ||
    Number.isNaN(attendeeCountNum) ||
    attendeeCountNum <= 0
  ) {
    showError("Attendee count must be greater than 0.");
    return false;
  }

  const maxAllowedSeats = Number(
    selectedWindow?.remainingSeats ??
      selectedWindow?.remaining ??
      selectedWindow?.availableSeats ??
      selectedRoom?.seatingCapacity ??
      selectedRoom?.capacity ??
      1
  );

  if (attendeeCountNum > maxAllowedSeats) {
    showError(
      `Attendee count cannot exceed remaining seats (${maxAllowedSeats}).`
    );
    return false;
  }

  return true;
};
  const handleSearch = async (e) => {
    e.preventDefault();
    clearAlerts();

    if (!hasAnyFilter) {
      setRooms(allRooms);
      setSelectedRoomSlots([]);
      showSuccess("Showing all rooms.");
      return;
    }

    const locallyFilteredRooms = allRooms.filter((room) => {
      const districtMatch = filters.district
        ? String(room.district || "").toLowerCase() ===
          filters.district.toLowerCase()
        : true;

      const locationValue = filters.location.trim().toLowerCase();
      const locationMatch = locationValue
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
              String(value).toLowerCase().includes(locationValue)
            )
        : true;

      const maxPriceMatch =
        filters.maxPrice !== ""
          ? Number(room.feePerHour || 0) <= Number(filters.maxPrice)
          : true;

      const facilitiesValue = filters.facilities.trim().toLowerCase();
      const facilitiesMatch = facilitiesValue
        ? String(room.facilities || "")
            .toLowerCase()
            .includes(facilitiesValue)
        : true;

      const roomMatch = filters.roomId
        ? String(room.id) === String(filters.roomId)
        : true;

      return (
        districtMatch &&
        locationMatch &&
        maxPriceMatch &&
        facilitiesMatch &&
        roomMatch
      );
    });

    if (!filters.date && !filters.time) {
      setRooms(locallyFilteredRooms);

      if (filters.roomId) {
        const selected =
          locallyFilteredRooms.find(
            (room) => String(room.id) === String(filters.roomId)
          ) || null;

        setSelectedRoom(selected);
        if (selected) {
          await loadSlotsForSelectedRoom(selected.id);
        } else {
          setSelectedRoomSlots([]);
        }
      } else {
        setSelectedRoomSlots([]);
      }

      showSuccess(
        `${locallyFilteredRooms.length} room(s) found based on the applied filters.`
      );
      return;
    }

    try {
      setSearchingAvailability(true);

      const slotResults = await Promise.all(
        locallyFilteredRooms.map(async (room) => {
          try {
            const cacheKey = filters.date
              ? `${room.id}_${filters.date}`
              : `${room.id}_all`;

            const existing = roomSlotsMap[cacheKey];
            const slotList =
              existing || (await fetchRoomSlots(room.id, filters.date));

            return {
              room,
              cacheKey,
              slots: slotList,
            };
          } catch (err) {
            console.error(`Slot fetch failed for room ${room.id}:`, err);
            return {
              room,
              cacheKey: filters.date
                ? `${room.id}_${filters.date}`
                : `${room.id}_all`,
              slots: [],
            };
          }
        })
      );

      const updatedSlotMap = { ...roomSlotsMap };
      slotResults.forEach((item) => {
        updatedSlotMap[item.cacheKey] = item.slots;
      });
      setRoomSlotsMap(updatedSlotMap);

      const matchedByDateTime = slotResults
        .map((item) => ({
          room: item.room,
          filteredSlots: applyDateTimeFilterToSlots(item.slots),
        }))
        .filter((item) => item.filteredSlots.length > 0);

      const matchedRooms = matchedByDateTime.map((item) => item.room);
      setRooms(matchedRooms);

      if (filters.roomId) {
        const selectedMatch =
          matchedByDateTime.find(
            (item) => String(item.room.id) === String(filters.roomId)
          ) || null;

        if (selectedMatch) {
          setSelectedRoom(selectedMatch.room);
          setSelectedRoomSlots(selectedMatch.filteredSlots);
        } else {
          setSelectedRoomSlots([]);
        }
      } else {
        setSelectedRoomSlots([]);
      }

      showSuccess(
        `${matchedRooms.length} room(s) found with available slots for the selected filter(s).`
      );
    } catch (err) {
      console.error("Availability search error:", err);
      showError(err.message || "Failed to search room availability.");
    } finally {
      setSearchingAvailability(false);
    }
  };

  const openBookingModal = async (room) => {
    clearAlerts();

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!token) {
      showError("Please login first to book a room.");
      return;
    }

    if (!userId) {
      showError("User ID not found. Please login again.");
      return;
    }

    try {
      setSlotLoading(true);

      const cacheKey = filters.date
        ? `${room.id}_${filters.date}`
        : `${room.id}_all`;

      let slotList = roomSlotsMap[cacheKey];
      if (!slotList) {
        slotList = await fetchRoomSlots(room.id, filters.date);
        setRoomSlotsMap((prev) => ({
          ...prev,
          [cacheKey]: slotList,
        }));
      }

      const filteredForBooking = applyDateTimeFilterToSlots(slotList);

      setSelectedRoom(room);
      setBookingForm(initialBookingForm);
      setBookingModalOpen(true);

      if (String(filters.roomId) !== String(room.id)) {
        setFilters((prev) => ({
          ...prev,
          roomId: room.id,
        }));
      }

      setSelectedRoomSlots(filteredForBooking);
    } catch (err) {
      console.error("Open booking modal slot error:", err);
      showError(err.message || "Failed to load room slots.");
    } finally {
      setSlotLoading(false);
    }
  };

  const closeBookingModal = () => {
    setBookingModalOpen(false);
    setSelectedRoom(null);
    setBookingForm(initialBookingForm);
  };

  const formatDateTime = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleString("en-IN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const formatDateOnly = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const formatTimeOnly = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleTimeString("en-IN", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const formatSlotTimeRange = (slot) => {
    if (!slot?.startAt || !slot?.endAt) return "Slot time not available";
    return `${formatTimeOnly(slot.startAt)} - ${formatTimeOnly(slot.endAt)}`;
  };

  const formatSlotDateLabel = (slot) => {
    if (!slot?.startAt || !slot?.endAt) return "-";

    const startDate = formatDateOnly(slot.startAt);
    const endDate = formatDateOnly(slot.endAt);

    if (startDate === endDate) {
      return startDate;
    }

    return `${startDate} - ${endDate}`;
  };

  const formatWindowLabel = (slot) => {
    if (!slot?.startAt || !slot?.endAt) return "Window not available";
    return `${formatSlotDateLabel(slot)} | ${formatSlotTimeRange(slot)}`;
  };

  const handleConfirmBooking = async () => {
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");

  clearAlerts();

  if (!token) {
    showError("Please login first to book a room.");
    return;
  }

  if (!userId) {
    showError("User ID not found. Please login again.");
    return;
  }

  if (!validateBookingForm()) return;

  try {
    setBookingRoomId(selectedRoom.id);

    const bookingStartIso = inputValueToIsoWithOffset(bookingForm.startAt);
    const bookingEndIso = inputValueToIsoWithOffset(bookingForm.endAt);

    const attendeeCount = Number(bookingForm.attendeeCount || 1);

    const bookingData = {
      roomId: selectedRoom.id,
      userId,
      timeSlotId: isUuid(bookingForm.selectedWindowId)
        ? bookingForm.selectedWindowId
        : null,
      startAt: bookingStartIso,
      endAt: bookingEndIso,
      purpose: bookingForm.purpose?.trim() || null,
      attendeeCount,
    };

    await bookRoom(bookingData);

    const refreshedSlots = await fetchRoomSlots(
      selectedRoom.id,
      filters.date
    );

    const refreshCacheKey = filters.date
      ? `${selectedRoom.id}_${filters.date}`
      : `${selectedRoom.id}_all`;

    setRoomSlotsMap((prev) => ({
      ...prev,
      [refreshCacheKey]: refreshedSlots,
    }));

    const refreshedFilteredSlots = applyDateTimeFilterToSlots(refreshedSlots);
    setSelectedRoomSlots(refreshedFilteredSlots);

    const start = new Date(bookingStartIso);
    const end = new Date(bookingEndIso);
    const durationMs = end.getTime() - start.getTime();
    const durationHours = durationMs > 0 ? durationMs / (1000 * 60 * 60) : 0;

    const feePerHour = Number(selectedRoom.feePerHour || 0);
    const totalFee = durationHours * feePerHour * attendeeCount;

    const initialSeatCount = Number(
      selectedWindow?.remainingSeats ??
        selectedWindow?.remaining ??
        selectedWindow?.availableSeats ??
        selectedRoom?.seatingCapacity ??
        selectedRoom?.capacity ??
        1
    );

    setLastBookingDetails({
      roomName: getRoomTitle(selectedRoom),
      district: selectedRoom.district || "-",
      location: selectedRoom.location || "-",
      facilities: selectedRoom.facilities || "-",
      capacity: selectedRoom.seatingCapacity || "-",
      slotDate: formatDateOnly(bookingStartIso),
      startAt: formatDateTime(bookingStartIso),
      endAt: formatDateTime(bookingEndIso),
      timeRange: `${formatTimeOnly(bookingStartIso)} - ${formatTimeOnly(
        bookingEndIso
      )}`,
      attendeeCount,
      purpose: bookingForm.purpose?.trim() || "-",
      durationHours,
      totalFee,
      remainingSeatsAfter:
        initialSeatCount >= 0
          ? Math.max(initialSeatCount - attendeeCount, 0)
          : "-",
      status:
        selectedRoom.approvalRequired === true
          ? "Pending Approval"
          : "Booked",
      selectedWindow: selectedWindow ? formatWindowLabel(selectedWindow) : "-",
    });

    closeBookingModal();
    showSuccess("Room booked successfully.");
  } catch (err) {
    console.error("Booking error:", err);

    if (
      err?.message?.toLowerCase().includes("unauthorized") ||
      err?.message?.includes("401")
    ) {
      showError("Unauthorized. Please login again.");
    } else {
      showError(err.message || "Failed to book room.");
    }
  } finally {
    setBookingRoomId(null);
  }
};

  const normalizeImagePath = (url) => {
    if (!url || typeof url !== "string") return "";

    const cleaned = url.trim().replace(/\\/g, "/");

    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
      return cleaned;
    }

    if (cleaned.startsWith("/")) {
      return `${API_BASE_URL}${cleaned}`;
    }

    return `${API_BASE_URL}/${cleaned}`;
  };

  const extractRoomImages = (room) => {
    if (!room) return [];

    const urls = [];

    if (Array.isArray(room.images)) {
      room.images.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.roomImages)) {
      room.roomImages.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.imageUrls)) {
      room.imageUrls.forEach((img) => {
        urls.push(normalizeImagePath(img));
      });
    }

    if (room.imageUrl) {
      urls.push(normalizeImagePath(room.imageUrl));
    }

    if (room.thumbnailUrl) {
      urls.push(normalizeImagePath(room.thumbnailUrl));
    }

    return [...new Set(urls.filter(Boolean))];
  };

  const getPrimaryImage = (room) => {
    const images = extractRoomImages(room);
    return images.length > 0 ? images[0] : null;
  };

  const getRoomTitle = (room) => {
    if (!room) return "Room";
    if (room.displayName) return room.displayName;
    if (room.roomName) return room.roomName;
    return `${room.blockName || "Block"} - ${room.roomNumber || room.id}`;
  };

  const openGallery = (room, startIndex = 0) => {
    const images = extractRoomImages(room);
    if (images.length === 0) return;

    setGalleryImages(images);
    setGalleryTitle(getRoomTitle(room));
    setGalleryIndex(startIndex);
    setGalleryOpen(true);
    setZoomStyle({
      backgroundImage: `url(${images[startIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const closeGallery = () => {
    setGalleryOpen(false);
    setGalleryImages([]);
    setGalleryTitle("");
    setGalleryIndex(0);
    setZoomStyle({
      backgroundImage: "",
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const showPrevImage = () => {
    const newIndex =
      galleryIndex === 0 ? galleryImages.length - 1 : galleryIndex - 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const showNextImage = () => {
    const newIndex =
      galleryIndex === galleryImages.length - 1 ? 0 : galleryIndex + 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const handleThumbnailClick = (index) => {
    setGalleryIndex(index);
    setZoomStyle({
      backgroundImage: `url(${galleryImages[index]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const handleZoomMove = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width) * 100;
    const y = ((e.clientY - rect.top) / rect.height) * 100;

    setZoomStyle({
      backgroundImage: `url(${galleryImages[galleryIndex]})`,
      backgroundPosition: `${x}% ${y}%`,
      opacity: 1,
    });
  };

  const handleZoomLeave = () => {
    setZoomStyle((prev) => ({
      ...prev,
      opacity: 0,
      backgroundPosition: "center",
    }));
  };

  const handleImageError = (e) => {
    e.target.style.display = "none";
    const parent = e.target.parentElement;
    if (parent && !parent.querySelector(".image-error-placeholder")) {
      const placeholder = document.createElement("div");
      placeholder.className = "image-error-placeholder";
      placeholder.innerText = "Image not available";
      parent.appendChild(placeholder);
    }
  };

  const bookingSummary = useMemo(() => {
  if (!selectedRoom || !selectedCustomRange) {
    return {
      durationHours: 0,
      totalFee: 0,
    };
  }

  const start = new Date(selectedCustomRange.startAt);
  const end = new Date(selectedCustomRange.endAt);

  const durationMs = end.getTime() - start.getTime();
  const durationHours = durationMs > 0 ? durationMs / (1000 * 60 * 60) : 0;

  const feePerHour = Number(selectedRoom.feePerHour || 0);
  const totalFee = durationHours * feePerHour * attendeeCountNumber;

  return {
    durationHours,
    totalFee,
  };
}, [selectedRoom, selectedCustomRange, attendeeCountNumber]);

  

  return (
    <div className="room-page">
      <div className="room-container">
        <div className="room-topBar">
          <div>
            <h1 className="room-heading">Room Slot Booking</h1>
            <p className="room-subText">
              Filter by district, location, date, time, price, and facilities.
              If one field is filled, matching rooms are shown. If multiple
              fields are filled, only rooms matching all filled fields are shown.
            </p>
          </div>

          <button
            type="button"
            className="room-backButton"
            onClick={() => navigate("/student")}
          >
            Back to Dashboard
          </button>
        </div>

        {message && <div className="room-successBox">{message}</div>}
        {error && <div className="room-errorBox">{error}</div>}

        {lastBookingDetails && (
          <div className="room-formCard" style={{ marginBottom: "20px" }}>
            <div className="room-formHeader">
              <h2 className="room-formTitle">Last Booking Details</h2>
              <p className="room-formSubtitle">
                Full booking information including selected slot and room
                details.
              </p>
            </div>

            <div className="room-infoList">
              <p>
                <strong>Room:</strong> {lastBookingDetails.roomName}
              </p>
              <p>
                <strong>District:</strong> {lastBookingDetails.district}
              </p>
              <p>
                <strong>Location:</strong> {lastBookingDetails.location}
              </p>
              <p>
                <strong>Facilities:</strong> {lastBookingDetails.facilities}
              </p>
              <p>
                <strong>Capacity:</strong> {lastBookingDetails.capacity}
              </p>
              <p>
                <strong>Selected Window:</strong>{" "}
                {lastBookingDetails.selectedWindow}
              </p>
              <p>
                <strong>Date:</strong> {lastBookingDetails.slotDate}
              </p>
              <p>
                <strong>Start:</strong> {lastBookingDetails.startAt}
              </p>
              <p>
                <strong>End:</strong> {lastBookingDetails.endAt}
              </p>
              <p>
                <strong>Time Range:</strong> {lastBookingDetails.timeRange}
              </p>
              <p>
                <strong>Attendee Count:</strong>{" "}
                {lastBookingDetails.attendeeCount}
              </p>
              <p>
                <strong>Purpose:</strong> {lastBookingDetails.purpose}
              </p>
              <p>
                <strong>Duration:</strong> {lastBookingDetails.durationHours}{" "}
                hour(s)
              </p>
              <p>
                <strong>Total Fee:</strong> ₹
                {Number(lastBookingDetails.totalFee || 0).toFixed(2)}
              </p>
              <p>
                <strong>Status:</strong> {lastBookingDetails.status}
              </p>
              <p>
                <strong>Remaining Seats After Booking:</strong>{" "}
                {lastBookingDetails.remainingSeatsAfter}
              </p>
            </div>
          </div>
        )}

        <form onSubmit={handleSearch} className="room-formCard">
          <div className="room-formHeader">
            <h2 className="room-formTitle">Search Study Rooms</h2>
            <p className="room-formSubtitle">
              Fill any one or more filters to search rooms and view available
              slot windows for the selected day.
            </p>
          </div>

          <div className="room-formGrid">
            <div className="room-fieldGroup">
              <label className="room-label">District</label>
              <select
                name="district"
                value={filters.district}
                onChange={handleChange}
                className="room-input"
              >
                <option value="">Select Tamil Nadu District</option>
                {tamilNaduDistricts.map((district) => (
                  <option key={district} value={district}>
                    {district}
                  </option>
                ))}
              </select>
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Location</label>
              <input
                type="text"
                name="location"
                placeholder="Enter location / block / room name"
                value={filters.location}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Date</label>
              <input
                type="date"
                name="date"
                value={filters.date}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Time</label>
              <input
                type="time"
                name="time"
                value={filters.time}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Max Price / Hour</label>
              <input
                type="number"
                name="maxPrice"
                min="0"
                placeholder="Enter max price"
                value={filters.maxPrice}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Facilities</label>
              <input
                type="text"
                name="facilities"
                placeholder="e.g. AC, WiFi, Projector"
                value={filters.facilities}
                onChange={handleChange}
                className="room-input"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Room</label>
              <select
                name="roomId"
                value={filters.roomId}
                onChange={handleChange}
                className="room-input"
              >
                <option value="">Select Room</option>
                {rooms.map((room) => (
                  <option key={room.id} value={room.id}>
                    {getRoomTitle(room)}
                  </option>
                ))}
              </select>
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Available Slot Windows</label>
              <input
                type="text"
                value={
                  filters.roomId
                    ? slotLoading
                      ? "Loading windows..."
                      : `${selectedRoomSlots.length} window(s) available`
                    : filters.date || filters.time
                    ? "Search to view date/time based windows"
                    : "Select room to view windows"
                }
                className="room-input"
                disabled
              />
            </div>
          </div>

          <div className="room-actionRow">
            <button
              type="submit"
              className="room-addButton"
              disabled={loading || slotLoading || searchingAvailability}
            >
              {loading || searchingAvailability ? "Searching..." : "Search Rooms"}
            </button>

            <button
              type="button"
              onClick={handleReset}
              className="room-cancelButton"
              disabled={
                loading ||
                slotLoading ||
                searchingAvailability ||
                bookingRoomId !== null
              }
            >
              Reset
            </button>
          </div>
        </form>

        <div className="room-listHeader">
          <h2 className="room-sectionTitle">Available Rooms</h2>
          <span className="room-roomCount">{rooms.length} rooms</span>
        </div>

        {loading ? (
          <div className="room-emptyBox">Loading rooms...</div>
        ) : rooms.length === 0 ? (
          <div className="room-emptyBox">
            No rooms found for the selected filter(s).
          </div>
        ) : (
          <div className="room-roomGrid">
            {rooms.map((room) => {
              const images = extractRoomImages(room);
              const primaryImage = getPrimaryImage(room);
              const isSelected = String(filters.roomId) === String(room.id);

              const cacheKey = filters.date
                ? `${room.id}_${filters.date}`
                : `${room.id}_all`;

              const allSlotsForRoom = roomSlotsMap[cacheKey] || [];
              const roomFilteredSlots = isSelected
                ? selectedRoomSlots
                : applyDateTimeFilterToSlots(allSlotsForRoom);

              return (
                <div key={room.id} className="room-card">
                  <div
                    className="room-imageContainer"
                    onClick={() => openGallery(room, 0)}
                    title={
                      images.length > 0
                        ? "Click to view images"
                        : "No image available"
                    }
                  >
                    {primaryImage ? (
                      <>
                        <img
                          src={primaryImage}
                          alt={getRoomTitle(room)}
                          className="room-cardImage"
                          onError={handleImageError}
                        />
                        {images.length > 1 && (
                          <div className="room-imageCountBadge">
                            +{images.length - 1} more
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="room-noImageBox">No Image</div>
                    )}
                  </div>

                  <div className="room-cardTop">
                    <h3 className="room-roomTitle">{getRoomTitle(room)}</h3>

                    <span className="room-feeBadge">
                      {room.feePerHour !== undefined && room.feePerHour !== null
                        ? `₹${room.feePerHour}/hr`
                        : "Available"}
                    </span>
                  </div>

                  <div className="room-infoList">
                    <p>
                      <strong>Floor / Block:</strong>{" "}
                      {room.floorBlock ||
                        room.blockName ||
                        room.floorNumber ||
                        "-"}
                    </p>
                    <p>
                      <strong>Capacity:</strong>{" "}
                      {room.seatingCapacity
                        ? `${room.seatingCapacity} seats`
                        : "-"}
                    </p>
                    <p>
                      <strong>District:</strong> {room.district || "-"}
                    </p>
                    <p>
                      <strong>Location:</strong> {room.location || "-"}
                    </p>
                    <p>
                      <strong>Facilities:</strong> {room.facilities || "-"}
                    </p>
                    <p>
                      <strong>Approval Required:</strong>{" "}
                      {room.approvalRequired ? "Yes" : "No"}
                    </p>
                    <p>
                      <strong>Selected Date:</strong> {filters.date || "-"}
                    </p>
                    <p>
                      <strong>Selected Time:</strong> {filters.time || "-"}
                    </p>
                    <p>
                      <strong>Available Windows:</strong>{" "}
                      {roomFilteredSlots.length}
                    </p>
                  </div>

                  {isSelected && (
                    <div
                      className="room-infoList"
                      style={{
                        marginTop: "10px",
                        padding: "12px",
                        background: "#f8fafc",
                        borderRadius: "12px",
                        border: "1px solid #e2e8f0",
                      }}
                    >
                      <p style={{ marginBottom: "8px" }}>
                        <strong>
                          {filters.date
                            ? "Available Windows For Selected Day:"
                            : "Available Windows:"}
                        </strong>
                      </p>

                      {slotLoading ? (
                        <p>Loading windows...</p>
                      ) : roomFilteredSlots.length === 0 ? (
                        <p>No active slot windows available for the selected filter.</p>
                      ) : (
                        roomFilteredSlots.map((slot, index) => (
                          <p
                            key={`${slot.startAt}-${slot.endAt}-${index}`}
                            style={{ margin: "6px 0" }}
                          >
                            {formatWindowLabel(slot)} | Remaining:{" "}
                            {slot.remainingSeats ?? "-"}
                          </p>
                        ))
                      )}
                    </div>
                  )}

                  <div className="room-cardButtons">
                    <button
                      type="button"
                      className="room-viewButton"
                      onClick={() => openGallery(room, 0)}
                      disabled={images.length === 0}
                    >
                      View Images
                    </button>

                    <button
                      type="button"
                      className="room-bookButton"
                      onClick={() => openBookingModal(room)}
                      disabled={bookingRoomId === room.id}
                    >
                      {bookingRoomId === room.id ? "Booking..." : "Book Now"}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {bookingModalOpen && selectedRoom && (
        <div className="room-modalOverlay" onClick={closeBookingModal}>
          <div
            className="room-modalContent"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="room-modalClose" onClick={closeBookingModal}>
              ×
            </button>

            <h3 className="room-modalTitle">Confirm Room Booking</h3>

            <div className="room-infoList" style={{ marginBottom: "16px" }}>
              <p>
                <strong>Room:</strong> {getRoomTitle(selectedRoom)}
              </p>
              <p>
                <strong>District:</strong> {selectedRoom.district || "-"}
              </p>
              <p>
                <strong>Location:</strong> {selectedRoom.location || "-"}
              </p>
              <p>
                <strong>Facilities:</strong> {selectedRoom.facilities || "-"}
              </p>
              <p>
                <strong>Capacity:</strong>{" "}
                {selectedRoom.seatingCapacity || "-"} seats
              </p>
              <p>
                <strong>Selected Date Filter:</strong> {filters.date || "-"}
              </p>
              <p>
                <strong>Selected Time Filter:</strong> {filters.time || "-"}
              </p>
              <p>
                <strong>Approval Required:</strong>{" "}
                {selectedRoom.approvalRequired ? "Yes" : "No"}
              </p>
            </div>

            <div className="room-formGrid">
              <div className="room-fieldGroup room-fullWidth">
                <label className="room-label">Select Available Window</label>
                <select
                  name="selectedWindowId"
                  value={bookingForm.selectedWindowId}
                  onChange={handleBookingInputChange}
                  className="room-input"
                >
                  <option value="">Select Window</option>
                  {bookingModalSlots.map((slot) => (
                    <option key={slot.id} value={slot.id}>
                      {formatWindowLabel(slot)} | Remaining:{" "}
                      {slot.remainingSeats ?? "-"}
                    </option>
                  ))}
                </select>
              </div>

              <div className="room-fieldGroup">
                <label className="room-label">Booking Start</label>
                <input
                  type="datetime-local"
                  name="startAt"
                  step={BOOKING_STEP_SECONDS}
                  min={selectedWindow ? getMinBookableStartInput(selectedWindow) : ""}
                  max={selectedWindow ? getWindowEndInput(selectedWindow) : ""}
                  value={bookingForm.startAt}
                  onChange={handleBookingInputChange}
                  className="room-input"
                  disabled={!selectedWindow}
                />
              </div>

              <div className="room-fieldGroup">
                <label className="room-label">Booking End</label>
                <input
                  type="datetime-local"
                  name="endAt"
                  step={BOOKING_STEP_SECONDS}
                  min={
                    bookingForm.startAt ||
                    (selectedWindow ? getWindowStartInput(selectedWindow) : "")
                  }
                  max={selectedWindow ? getWindowEndInput(selectedWindow) : ""}
                  value={bookingForm.endAt}
                  onChange={handleBookingInputChange}
                  className="room-input"
                  disabled={!selectedWindow}
                />
              </div>

              <div className="room-fieldGroup">
                <label className="room-label">Attendee Count</label>
                <input
                  type="number"
                  name="attendeeCount"
                  min="1"
                  max={initialRemainingSeats || undefined}
                  value={bookingForm.attendeeCount || "1"}
                  onChange={handleBookingInputChange}
                  className="room-input"
                  disabled={!selectedWindow}
                />
              </div>

              <div className="room-fieldGroup">
                <label className="room-label">Remaining Seats</label>
                <input
                  type="text"
                  value={
                    selectedWindow
                      ? String(remainingSeatsAfterSelection ?? initialRemainingSeats ?? "-")
                      : "Select window first"
                  }
                  className="room-input"
                  disabled
                />
              </div>

              <div className="room-fieldGroup room-fullWidth">
                <label className="room-label">Purpose</label>
                <textarea
                  name="purpose"
                  rows="3"
                  placeholder="Enter booking purpose"
                  value={bookingForm.purpose}
                  onChange={handleBookingInputChange}
                  className="room-textarea"
                />
              </div>
            </div>

            <div className="room-infoList" style={{ marginTop: "16px" }}>
              <p>
                <strong>Selected Window:</strong>{" "}
                {selectedWindow ? formatWindowLabel(selectedWindow) : "-"}
              </p>
              <p>
                <strong>Booking Start:</strong>{" "}
                {selectedCustomRange
                  ? formatDateTime(selectedCustomRange.startAt)
                  : "-"}
              </p>
              <p>
                <strong>Booking End:</strong>{" "}
                {selectedCustomRange
                  ? formatDateTime(selectedCustomRange.endAt)
                  : "-"}
              </p>
              <p>
                <strong>Time Range:</strong>{" "}
                {selectedCustomRange
                  ? `${formatTimeOnly(selectedCustomRange.startAt)} - ${formatTimeOnly(
                      selectedCustomRange.endAt
                    )}`
                  : "-"}
              </p>
              <p>
                <strong>Attendee Count:</strong> {attendeeCountNumber}
              </p>
              <p>
                <strong>Duration:</strong>{" "}
                {Number.isFinite(bookingSummary.durationHours)
                  ? bookingSummary.durationHours
                  : 0}{" "}
                hour(s)
              </p>
              <p>
                <strong>Total Fee:</strong> ₹
                {Number.isFinite(bookingSummary.totalFee)
                  ? bookingSummary.totalFee.toFixed(2)
                  : "0.00"}
              </p>
            </div>

            <div className="room-cardButtons" style={{ marginTop: "18px" }}>
              <button
                type="button"
                className="room-viewButton"
                onClick={closeBookingModal}
              >
                Cancel
              </button>

              <button
                type="button"
                className="room-bookButton"
                onClick={handleConfirmBooking}
                disabled={
                  bookingRoomId === selectedRoom.id ||
                  bookingModalSlots.length === 0
                }
              >
                {bookingRoomId === selectedRoom.id
                  ? "Booking..."
                  : "Confirm Booking"}
              </button>
            </div>
          </div>
        </div>
      )}

      {galleryOpen && (
        <div className="room-modalOverlay" onClick={closeGallery}>
          <div
            className="room-modalContent"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="room-modalClose" onClick={closeGallery}>
              ×
            </button>

            <h3 className="room-modalTitle">{galleryTitle}</h3>

            <div
              className="room-productGalleryLayout"
              style={{
                gridTemplateColumns:
                  galleryImages.length > 1 ? "110px 1fr" : "1fr",
              }}
            >
              {galleryImages.length > 1 && (
                <div className="room-verticalThumbnailColumn">
                  {galleryImages.map((img, index) => (
                    <div
                      key={index}
                      className={`room-verticalThumbWrapper ${
                        index === galleryIndex ? "active" : ""
                      }`}
                      onClick={() => handleThumbnailClick(index)}
                    >
                      <img
                        src={img}
                        alt={`Thumbnail ${index + 1}`}
                        className="room-verticalThumbnail"
                        onError={handleImageError}
                      />
                    </div>
                  ))}
                </div>
              )}

              <div className="room-mainGalleryPanel">
                <div
                  className="room-mainImageStage"
                  onMouseMove={handleZoomMove}
                  onMouseLeave={handleZoomLeave}
                >
                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      className="room-arrowLeft"
                      onClick={showPrevImage}
                    >
                      ‹
                    </button>
                  )}

                  <img
                    src={galleryImages[galleryIndex]}
                    alt={`Room ${galleryIndex + 1}`}
                    className="room-modalImage"
                    onError={handleImageError}
                  />

                  <div
                    className="room-zoomLens"
                    style={{
                      backgroundImage: zoomStyle.backgroundImage,
                      backgroundPosition: zoomStyle.backgroundPosition,
                      opacity: zoomStyle.opacity,
                    }}
                  />

                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      className="room-arrowRight"
                      onClick={showNextImage}
                    >
                      ›
                    </button>
                  )}
                </div>

                <div className="room-imageCounter">
                  Image {galleryIndex + 1} of {galleryImages.length}
                </div>

                {galleryImages.length > 1 && (
                  <div className="room-bottomThumbnailRow">
                    {galleryImages.map((img, index) => (
                      <img
                        key={index}
                        src={img}
                        alt={`Bottom thumbnail ${index + 1}`}
                        className={`room-bottomThumbnail ${
                          index === galleryIndex ? "active" : ""
                        }`}
                        onClick={() => handleThumbnailClick(index)}
                        onError={handleImageError}
                      />
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}