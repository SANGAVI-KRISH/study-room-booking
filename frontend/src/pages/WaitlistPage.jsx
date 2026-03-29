import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRoomFeedback } from "../api/feedbackApi";
import FeedbackList from "../components/FeedbackList";
import "./RoomAvailability.css";

const API_BASE_URL = "http://localhost:8080";
const APP_TIMEZONE_OFFSET = "+05:30";
const BOOKING_STEP_SECONDS = 1800;
const WAITLIST_BASE_URL = `${API_BASE_URL}/api/waitlist`;

export default function RoomWaitlist() {
  const navigate = useNavigate();

  const [roomFeedbackSummary, setRoomFeedbackSummary] = useState(null);

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
    startTime: "",
    endTime: "",
    maxPrice: "",
    facilities: "",
  };

  const [filters, setFilters] = useState(initialFilters);
  const [allRooms, setAllRooms] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [roomSlotsMap, setRoomSlotsMap] = useState({});
  const [selectedRoomSlots, setSelectedRoomSlots] = useState([]);
  const [myWaitlistEntries, setMyWaitlistEntries] = useState([]);

  const [loading, setLoading] = useState(false);
  const [slotLoading, setSlotLoading] = useState(false);
  const [searchingAvailability, setSearchingAvailability] = useState(false);
  const [joiningRoomId, setJoiningRoomId] = useState(null);
  const [waitlistAutoAssign, setWaitlistAutoAssign] = useState(true);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [waitlistModalOpen, setWaitlistModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedStartTime, setSelectedStartTime] = useState("");
  const [selectedEndTime, setSelectedEndTime] = useState("");
  const [timeError, setTimeError] = useState("");

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
    setTimeError("");
  };

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (galleryOpen) {
        if (e.key === "Escape") closeGallery();
        if (e.key === "ArrowLeft" && galleryImages.length > 1) showPrevImage();
        if (e.key === "ArrowRight" && galleryImages.length > 1) showNextImage();
      }

      if (waitlistModalOpen && e.key === "Escape") {
        closeWaitlistModal();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [galleryOpen, galleryImages.length, waitlistModalOpen]);

  useEffect(() => {
    fetchRooms();
    fetchMyWaitlistEntries();
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
    if (!selectedRoom?.id) {
      setSelectedRoomSlots([]);
      return;
    }
    loadSlotsForSelectedRoom(selectedRoom.id);
  }, [selectedRoom?.id, filters.date]);

  useEffect(() => {
    async function loadRoomFeedback() {
      if (!selectedRoom?.id) {
        setRoomFeedbackSummary(null);
        return;
      }

      try {
        const data = await getRoomFeedback(selectedRoom.id);
        setRoomFeedbackSummary(data);
      } catch (fetchError) {
        console.error("Failed to load room feedback:", fetchError);
        setRoomFeedbackSummary(null);
      }
    }

    loadRoomFeedback();
  }, [selectedRoom]);

  const hasAnyFilter = useMemo(() => {
    return Boolean(
      filters.district ||
        filters.location.trim() ||
        filters.roomId ||
        filters.date ||
        filters.startTime ||
        filters.endTime ||
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

  const fetchMyWaitlistEntries = async () => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");
    
    if (!userId || !token) return;
    
    try {
      const res = await fetch(`${WAITLIST_BASE_URL}/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) return;
      const data = await res.json();
      setMyWaitlistEntries(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to fetch waitlist entries:", err);
    }
  };

  const pad2 = (n) => String(n).padStart(2, "0");

  const buildDateTimeFromDateAndTime = (dateStr, timeValue) => {
    if (!dateStr || !timeValue) return null;
    const parts = timeValue.split(":");
    if (parts.length < 2) return null;
    const hh = pad2(Number(parts[0]));
    const mm = pad2(Number(parts[1]));
    return `${dateStr}T${hh}:${mm}:00${APP_TIMEZONE_OFFSET}`;
  };

  const validateTimeRange = (startTime, endTime) => {
    if (!startTime || !endTime) return true;
    const start = new Date(`2000-01-01T${startTime}`);
    const end = new Date(`2000-01-01T${endTime}`);
    return end > start;
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

  const normalizeStoredSlots = (slotList) => {
    const list = Array.isArray(slotList) ? slotList : [];

    return list
      .filter(
        (slot) => slot && slot.startAt && slot.endAt && slot.isActive !== false
      )
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

  const addOneDay = (dateStr) => {
    const d = new Date(`${dateStr}T00:00:00`);
    d.setDate(d.getDate() + 1);
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(
      d.getDate()
    )}`;
  };

  const needsNextDay = (startTime, endTime) => {
    const s = parseTimeString(startTime);
    const e = parseTimeString(endTime);
    if (!s || !e) return false;
    return e <= s;
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

  const applyDateTimeFilterToSlots = (slotList, date = "", time = "") => {
    return (slotList || []).filter((slot) => {
      const dateMatch = date ? isSameDate(slot.startAt, date) : true;
      const timeMatch = time ? isTimeWithinSlot(slot, time) : true;
      return dateMatch && timeMatch;
    });
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
    } catch (fetchError) {
      console.warn(
        "Date-based slot fetch failed, falling back to stored slots:",
        fetchError
      );
      const storedSlots = await fetchStoredRoomSlots(roomId);
      return applyDateTimeFilterToSlots(storedSlots);
    }
  };

  const loadSlotsForSelectedRoom = async (roomId) => {
    try {
      setSlotLoading(true);
      clearAlerts();

      const cacheKey = filters.date ? `${roomId}_${filters.date}` : `${roomId}_all`;

      let slotsForRoom = roomSlotsMap[cacheKey];

      if (!slotsForRoom) {
        slotsForRoom = await fetchRoomSlots(roomId, filters.date);
        setRoomSlotsMap((prev) => ({
          ...prev,
          [cacheKey]: slotsForRoom,
        }));
      }

      setSelectedRoomSlots(applyDateTimeFilterToSlots(slotsForRoom, filters.date));
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
        setSelectedRoomSlots([]);
      }

      return updated;
    });

    if (name === "date") {
      setSelectedRoomSlots([]);
    }

    if (name === "startTime" || name === "endTime") {
      setTimeError("");
    }
  };

  const handleReset = () => {
    setFilters(initialFilters);
    setRooms(allRooms);
    setSelectedRoomSlots([]);
    setRoomFeedbackSummary(null);
    clearAlerts();
    closeWaitlistModal();
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    clearAlerts();

    // Validate time range
    if (filters.startTime && filters.endTime && !validateTimeRange(filters.startTime, filters.endTime)) {
      setTimeError("End time must be after start time");
      return;
    }

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

    if (!filters.date) {
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
            const cacheKey = filters.date ? `${room.id}_${filters.date}` : `${room.id}_all`;

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
              cacheKey: filters.date ? `${room.id}_${filters.date}` : `${room.id}_all`,
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
          filteredSlots: applyDateTimeFilterToSlots(item.slots, filters.date),
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

  const buildWaitlistSlot = (room, startTime, endTime) => {
    if (!room?.id || !filters.date || !startTime || !endTime) return null;
    const startAt = buildDateTimeFromDateAndTime(filters.date, startTime);
    const endAt = buildDateTimeFromDateAndTime(filters.date, endTime);
    if (!startAt || !endAt) return null;
    return { roomId: room.id, startAt, endAt };
  };

  const canJoinWaitlist = (room) => {
    if (!room?.id || !filters.date || !filters.startTime || !filters.endTime) return false;
    if (!validateTimeRange(filters.startTime, filters.endTime)) return false;

    const slot = buildWaitlistSlot(room, filters.startTime, filters.endTime);
    if (!slot) return false;

    // Check if user is already on waitlist for this slot
    const alreadyJoined = myWaitlistEntries.some(
      (e) =>
        String(e.roomId) === String(room.id) &&
        e.status !== "CANCELLED" &&
        e.startAt === slot.startAt
    );

    return !alreadyJoined;
  };

  const joinWaitlist = async ({ userId, roomId, startAt, endAt, autoAssign }) => {
    const token = localStorage.getItem("token");
    const query = new URLSearchParams({
      userId: String(userId),
      roomId: String(roomId),
      startAt,
      endAt,
      autoAssign: String(Boolean(autoAssign)),
    });

    const res = await fetch(`${WAITLIST_BASE_URL}/join?${query.toString()}`, {
      method: "POST",
      headers: token
        ? {
            Authorization: `Bearer ${token}`,
          }
        : {},
    });

    const contentType = res.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
      ? await res.json()
      : await res.text();

    if (!res.ok) {
      const message =
        (typeof data === "string" && data) ||
        data?.message ||
        "Failed to join waitlist";
      throw new Error(message);
    }

    return data;
  };

  const openWaitlistModal = (room) => {
    clearAlerts();
    
    const token = localStorage.getItem("token");
    if (!token) {
      showError("Please login first to join waitlist.");
      return;
    }

    if (!validateTimeRange(filters.startTime, filters.endTime)) {
      setTimeError("End time must be after start time");
      return;
    }

    setSelectedRoom(room);
    setSelectedStartTime(filters.startTime);
    setSelectedEndTime(filters.endTime);
    setWaitlistModalOpen(true);
  };

  const closeWaitlistModal = () => {
    setWaitlistModalOpen(false);
    setSelectedRoom(null);
    setRoomFeedbackSummary(null);
  };

  const handleConfirmJoinWaitlist = async () => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");

    clearAlerts();

    if (!token) {
      showError("Please login first to join waitlist.");
      return;
    }

    if (!userId) {
      showError("User ID not found. Please login again.");
      return;
    }

    const slot = buildWaitlistSlot(selectedRoom, selectedStartTime, selectedEndTime);

    if (!slot) {
      showError("Please select valid start and end times.");
      return;
    }

    try {
      setJoiningRoomId(selectedRoom.id);

      const result = await joinWaitlist({
        userId,
        roomId: selectedRoom.id,
        startAt: slot.startAt,
        endAt: slot.endAt,
        autoAssign: waitlistAutoAssign,
      });

      const waitlistPosition =
        result?.position ??
        result?.waitlist?.positionNumber ??
        result?.waitlist?.position ??
        null;

      showSuccess(
        waitlistPosition
          ? `Joined waitlist successfully! Your position is #${waitlistPosition}.`
          : "Joined waitlist successfully!"
      );

      closeWaitlistModal();
      fetchMyWaitlistEntries();
    } catch (err) {
      console.error("Join waitlist error:", err);
      showError(err.message || "Failed to join waitlist.");
    } finally {
      setJoiningRoomId(null);
    }
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

  const getRoomTitle = (room) => {
    if (!room) return "Room";
    if (room.displayName) return room.displayName;
    if (room.roomName) return room.roomName;
    return `${room.blockName || "Block"} - ${room.roomNumber || room.id}`;
  };

  const slotPreview = useMemo(() => {
    if (!filters.date || !filters.startTime || !filters.endTime) return null;
    if (!validateTimeRange(filters.startTime, filters.endTime)) return null;

    const startAt = buildDateTimeFromDateAndTime(filters.date, filters.startTime);
    const endAt = buildDateTimeFromDateAndTime(filters.date, filters.endTime);
    if (!startAt || !endAt) return null;

    return { startAt, endAt };
  }, [filters.date, filters.startTime, filters.endTime]);

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

  return (
    <div className="room-page">
      <div className="room-container">
        <div className="room-topBar">
          <div>
            <h1 className="room-heading">Join Waitlist</h1>
            <p className="room-subText">
              Select date and time range to join waitlist for fully booked rooms.
              You'll be notified when a slot becomes available.
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
        {timeError && <div className="room-errorBox">{timeError}</div>}

        {/* My Waitlist Entries Section */}
        {myWaitlistEntries.length > 0 && (
          <div className="room-formCard" style={{ marginBottom: "20px" }}>
            <div className="room-formHeader">
              <h2 className="room-formTitle">My Waitlist Entries</h2>
              <p className="room-formSubtitle">
                You will be notified when a slot becomes available.
              </p>
            </div>

            <div className="room-infoList">
              {myWaitlistEntries.map((entry) => (
                <div key={entry.id} style={{ marginBottom: "15px", padding: "10px", borderBottom: "1px solid #e2e8f0" }}>
                  <p><strong>Room:</strong> {entry.roomName || entry.roomId}</p>
                  <p><strong>Date:</strong> {formatDateOnly(entry.startAt)}</p>
                  <p><strong>Time:</strong> {formatTimeOnly(entry.startAt)} - {formatTimeOnly(entry.endAt)}</p>
                  <p><strong>Position:</strong> #{entry.positionNumber ?? entry.position ?? "-"}</p>
                  <p><strong>Status:</strong> <span style={{ color: entry.status === "ASSIGNED" ? "#10b981" : "#f59e0b" }}>{entry.status || "PENDING"}</span></p>
                </div>
              ))}
            </div>
          </div>
        )}

        <form onSubmit={handleSearch} className="room-formCard">
          <div className="room-formHeader">
            <h2 className="room-formTitle">Find Rooms for Waitlist</h2>
            <p className="room-formSubtitle">
              Select date and time range to join waitlist for rooms that are fully booked.
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
              <label className="room-label">Date *</label>
              <input
                type="date"
                name="date"
                value={filters.date}
                onChange={handleChange}
                className="room-input"
                required
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Start Time *</label>
              <input
                type="time"
                name="startTime"
                value={filters.startTime}
                onChange={handleChange}
                className="room-input"
                required
                step="1800"
              />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">End Time *</label>
              <input
                type="time"
                name="endTime"
                value={filters.endTime}
                onChange={handleChange}
                className="room-input"
                required
                step="1800"
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
              <label className="room-label">Waitlist Slot Preview</label>
              <input
                type="text"
                value={
                  slotPreview
                    ? `${formatDateTime(slotPreview.startAt)} → ${formatDateTime(slotPreview.endAt)}`
                    : timeError || "Select date & time range to preview"
                }
                className="room-input"
                disabled
              />
            </div>

            <div className="room-fieldGroup room-fullWidth">
              <label className="room-label">
                Auto Assign When Available
              </label>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  padding: "10px 12px",
                  border: "1px solid #d1d5db",
                  borderRadius: "10px",
                  background: "#fff",
                }}
              >
                <input
                  id="waitlistAutoAssign"
                  type="checkbox"
                  checked={waitlistAutoAssign}
                  onChange={(e) => setWaitlistAutoAssign(e.target.checked)}
                  disabled={joiningRoomId !== null}
                />
                <label
                  htmlFor="waitlistAutoAssign"
                  style={{ margin: 0, cursor: "pointer" }}
                >
                  Automatically book when slot becomes available
                </label>
              </div>
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
                joiningRoomId !== null
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
              const canJoin = canJoinWaitlist(room);
              const isJoining = joiningRoomId === room.id;

              const cacheKey = filters.date ? `${room.id}_${filters.date}` : `${room.id}_all`;
              const cachedSlots = roomSlotsMap[cacheKey] || [];
              const roomFilteredSlots = isSelected
                ? selectedRoomSlots
                : applyDateTimeFilterToSlots(cachedSlots, filters.date);

              // Check if already on waitlist for this slot
              const slot = buildWaitlistSlot(room, filters.startTime, filters.endTime);
              const alreadyOnWaitlist = slot && myWaitlistEntries.some(
                (e) => String(e.roomId) === String(room.id) &&
                  e.status !== "CANCELLED" &&
                  e.startAt === slot.startAt
              );

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
                      <strong>Time Range:</strong> {filters.startTime || "-"} - {filters.endTime || "-"}
                    </p>
                  </div>

                  {alreadyOnWaitlist && (
                    <div
                      style={{
                        marginTop: "10px",
                        padding: "8px 12px",
                        background: "#fef9c3",
                        borderRadius: "8px",
                        fontSize: "13px",
                        color: "#854d0e",
                        fontWeight: 500,
                      }}
                    >
                      ✓ You are already on the waitlist for this slot.
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

                    {canJoin && !alreadyOnWaitlist ? (
                      <button
                        type="button"
                        className="room-bookButton"
                        style={{ backgroundColor: "#6f42c1" }}
                        onClick={() => openWaitlistModal(room)}
                        disabled={isJoining}
                      >
                        {isJoining ? "Joining..." : "Join Waitlist"}
                      </button>
                    ) : (!filters.date || !filters.startTime || !filters.endTime) ? (
                      <span style={{ fontSize: "12px", color: "#94a3b8", paddingTop: "6px" }}>
                        Select date & time range
                      </span>
                    ) : alreadyOnWaitlist ? (
                      <span style={{ fontSize: "12px", color: "#15803d", paddingTop: "6px" }}>
                        Already in waitlist
                      </span>
                    ) : (
                      <span style={{ fontSize: "12px", color: "#94a3b8", paddingTop: "6px" }}>
                        Slot has availability
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Waitlist Confirmation Modal */}
      {waitlistModalOpen && selectedRoom && (
        <div className="room-modalOverlay" onClick={closeWaitlistModal}>
          <div
            className="room-modalContent"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="room-modalClose" onClick={closeWaitlistModal}>
              ×
            </button>

            <h3 className="room-modalTitle">Confirm Waitlist Join</h3>

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
                <strong>Capacity:</strong> {selectedRoom.seatingCapacity || "-"} seats
              </p>
              <p>
                <strong>Selected Date:</strong> {filters.date || "-"}
              </p>
              <p>
                <strong>Time Range:</strong> {selectedStartTime} - {selectedEndTime}
              </p>
              <p>
                <strong>Approval Required:</strong>{" "}
                {selectedRoom.approvalRequired ? "Yes" : "No"}
              </p>
            </div>

            <div
              style={{
                marginBottom: "16px",
                padding: "14px",
                background: "#f8fafc",
                border: "1px solid #e2e8f0",
                borderRadius: "12px",
              }}
            >
              <h4 style={{ margin: "0 0 10px 0", color: "#1e293b" }}>
                Room Feedback
              </h4>
              <FeedbackList summary={roomFeedbackSummary} />
            </div>

            <div className="room-fieldGroup">
              <label className="room-label">Auto-Assign</label>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  padding: "10px 12px",
                  border: "1px solid #d1d5db",
                  borderRadius: "10px",
                  background: "#fff",
                  marginBottom: "20px",
                }}
              >
                <input
                  id="modalAutoAssign"
                  type="checkbox"
                  checked={waitlistAutoAssign}
                  onChange={(e) => setWaitlistAutoAssign(e.target.checked)}
                />
                <label
                  htmlFor="modalAutoAssign"
                  style={{ margin: 0, cursor: "pointer", fontSize: "14px" }}
                >
                  Automatically book when slot becomes available
                </label>
              </div>
            </div>

            <div className="room-cardButtons" style={{ marginTop: "18px" }}>
              <button
                type="button"
                className="room-viewButton"
                onClick={closeWaitlistModal}
              >
                Cancel
              </button>

              <button
                type="button"
                className="room-bookButton"
                style={{ backgroundColor: "#6f42c1" }}
                onClick={handleConfirmJoinWaitlist}
                disabled={joiningRoomId === selectedRoom.id}
              >
                {joiningRoomId === selectedRoom.id
                  ? "Joining..."
                  : "Confirm & Join Waitlist"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Gallery Modal */}
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