import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAllRooms, getAvailableSlotsByRoomAndDate } from "../api/roomApi";

export default function RoomSlots() {
  const navigate = useNavigate();

  const [rooms, setRooms] = useState([]);
  const [district, setDistrict] = useState("");
  const [location, setLocation] = useState("");
  const [roomId, setRoomId] = useState("");
  const [date, setDate] = useState("");
  const [slots, setSlots] = useState([]);
  const [roomInfo, setRoomInfo] = useState(null);
  const [message, setMessage] = useState("");
  const [loadingRooms, setLoadingRooms] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);

  useEffect(() => {
    loadRooms();
  }, []);

  async function loadRooms() {
    try {
      setLoadingRooms(true);
      setMessage("");
      setRooms([]);

      const data = await getAllRooms();
      setRooms(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Failed to load rooms:", error);
      setMessage(error?.message || "Failed to load rooms.");
      setRooms([]);
    } finally {
      setLoadingRooms(false);
    }
  }

  const districts = useMemo(() => {
    const uniqueDistricts = [
      ...new Set(
        rooms.map((room) => (room?.district || "").trim()).filter(Boolean)
      ),
    ];
    return uniqueDistricts.sort((a, b) => a.localeCompare(b));
  }, [rooms]);

  const locations = useMemo(() => {
    const filtered = rooms.filter((room) => {
      if (!district) return true;
      return (room?.district || "").trim() === district;
    });

    const uniqueLocations = [
      ...new Set(
        filtered.map((room) => (room?.location || "").trim()).filter(Boolean)
      ),
    ];

    return uniqueLocations.sort((a, b) => a.localeCompare(b));
  }, [rooms, district]);

  const filteredRooms = useMemo(() => {
    return rooms.filter((room) => {
      const roomDistrict = (room?.district || "").trim();
      const roomLocation = (room?.location || "").trim();

      const matchesDistrict = !district || roomDistrict === district;
      const matchesLocation = !location || roomLocation === location;

      return matchesDistrict && matchesLocation;
    });
  }, [rooms, district, location]);

  const selectedRoom = useMemo(() => {
    return rooms.find((room) => room.id === roomId) || null;
  }, [rooms, roomId]);

  function getRoomLabel(room) {
    const districtText = room?.district || "-";
    const locationText = room?.location || "-";
    const blockText = room?.blockName || "-";
    const roomNumberText = room?.roomNumber || "-";

    return `${districtText} - ${locationText} - ${blockText} - Room ${roomNumberText}`;
  }

  function getSelectedRoomName(room) {
    if (!room) return "-";

    const districtText = room?.district || "-";
    const locationText = room?.location || "-";
    const blockText = room?.blockName || "-";
    const roomNumberText = room?.roomNumber || "-";

    return `${districtText} - ${locationText} - ${blockText} - Room ${roomNumberText}`;
  }

  function formatDateOnly(value) {
    if (!value) return "-";

    if (/^\d{4}-\d{2}-\d{2}$/.test(String(value))) {
      const [year, month, day] = String(value).split("-");
      return `${day}/${month}/${year}`;
    }

    const parsed = new Date(value);
    if (!Number.isNaN(parsed.getTime())) {
      return parsed.toLocaleDateString("en-GB");
    }

    return String(value);
  }

  function formatTime(value) {
    if (!value) return "-";

    const text = String(value).trim();

    if (/^\d{2}:\d{2}:\d{2}$/.test(text)) {
      return text;
    }

    if (/^\d{2}:\d{2}$/.test(text)) {
      return `${text}:00`;
    }

    const parsed = new Date(text);
    if (!Number.isNaN(parsed.getTime())) {
      return parsed.toLocaleTimeString("en-GB", { hour12: false });
    }

    return text;
  }

  function formatPrice(value) {
    if (value === null || value === undefined || value === "") return "0.00";

    const num = Number(value);
    if (Number.isNaN(num)) return String(value);

    return num.toFixed(2);
  }

  function normalizeTimeText(value) {
    if (!value) return "";

    const text = String(value).trim();

    if (/^\d{2}:\d{2}:\d{2}$/.test(text)) {
      return text;
    }

    if (/^\d{2}:\d{2}$/.test(text)) {
      return `${text}:00`;
    }

    const parsed = new Date(text);
    if (!Number.isNaN(parsed.getTime())) {
      return parsed.toLocaleTimeString("en-GB", { hour12: false });
    }

    return text;
  }

  function timeToMinutes(timeValue) {
    if (!timeValue) return null;

    const text = normalizeTimeText(timeValue);
    const match = text.match(/^(\d{2}):(\d{2})(?::(\d{2}))?$/);

    if (!match) return null;

    const hours = Number(match[1]);
    const minutes = Number(match[2]);

    if (Number.isNaN(hours) || Number.isNaN(minutes)) return null;

    return hours * 60 + minutes;
  }

  function addDaysToDateString(dateStr, days) {
    const base = new Date(`${dateStr}T00:00:00`);
    if (Number.isNaN(base.getTime())) return dateStr;

    base.setDate(base.getDate() + days);

    const year = base.getFullYear();
    const month = String(base.getMonth() + 1).padStart(2, "0");
    const day = String(base.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
  }

  function normalizeSlot(slot, index, sourceDate, isSpillover = false) {
    const startAt =
      slot?.startTime ||
      slot?.startAt ||
      slot?.fromTime ||
      slot?.start ||
      "";

    const endAt =
      slot?.endTime ||
      slot?.endAt ||
      slot?.toTime ||
      slot?.end ||
      "";

    return {
      id:
        slot?.id ||
        slot?.slotId ||
        `${sourceDate}-${index}-${startAt}-${endAt}-${isSpillover ? "spill" : "main"}`,

      roomName: slot?.roomName || getSelectedRoomName(selectedRoom),

      startAt: normalizeTimeText(startAt),
      endAt: normalizeTimeText(endAt),

      availabilityStatus:
        slot?.availabilityStatus ||
        slot?.status ||
        slot?.slotStatus ||
        (slot?.isAvailable === true
          ? "available"
          : slot?.isAvailable === false
          ? "unavailable"
          : ""),

      bookedCount:
        slot?.bookedCount ??
        slot?.booked ??
        slot?.reservedCount ??
        0,

      sourceDate,
      isSpillover,
    };
  }

  function buildDisplayRange(slot, selectedDate) {
    const startMinutes = timeToMinutes(slot.startAt);
    const endMinutes = timeToMinutes(slot.endAt);

    let displayStartDate = selectedDate;
    let displayEndDate = selectedDate;

    if (slot.isSpillover) {
      displayStartDate = addDaysToDateString(selectedDate, 1);
    }

    if (startMinutes !== null && endMinutes !== null) {
      const crossesMidnight = endMinutes <= startMinutes;
      if (crossesMidnight) {
        displayEndDate = addDaysToDateString(displayStartDate, 1);
      } else {
        displayEndDate = displayStartDate;
      }
    }

    return {
      ...slot,
      displayStartDate,
      displayEndDate,
      startSortMinutes:
        (slot.isSpillover ? 24 * 60 : 0) + (startMinutes ?? 0),
      endSortMinutes:
        (slot.isSpillover ? 24 * 60 : 0) +
        (endMinutes !== null && endMinutes <= (startMinutes ?? -1)
          ? endMinutes + 24 * 60
          : endMinutes ?? 0),
    };
  }

  function canMergeSlots(current, next) {
    if (!current || !next) return false;

    return (
      current.availabilityStatus === next.availabilityStatus &&
      (current.bookedCount ?? 0) === (next.bookedCount ?? 0) &&
      current.endSortMinutes === next.startSortMinutes
    );
  }

  function mergeContinuousSlots(slotList, room, selectedDate) {
    if (!Array.isArray(slotList) || slotList.length === 0) return [];

    const prepared = slotList
      .map((slot) => buildDisplayRange(slot, selectedDate))
      .sort((a, b) => a.startSortMinutes - b.startSortMinutes);

    const merged = [];
    let current = {
      ...prepared[0],
      capacity: room?.seatingCapacity ?? 0,
      price: room?.feePerHour ?? 0,
    };

    for (let i = 1; i < prepared.length; i += 1) {
      const next = {
        ...prepared[i],
        capacity: room?.seatingCapacity ?? 0,
        price: room?.feePerHour ?? 0,
      };

      if (canMergeSlots(current, next)) {
        current = {
          ...current,
          endAt: next.endAt,
          displayEndDate: next.displayEndDate,
          endSortMinutes: next.endSortMinutes,
        };
      } else {
        merged.push({
          ...current,
          remainingSeats: Math.max(
            0,
            (room?.seatingCapacity ?? 0) - (current.bookedCount ?? 0)
          ),
        });
        current = next;
      }
    }

    merged.push({
      ...current,
      remainingSeats: Math.max(
        0,
        (room?.seatingCapacity ?? 0) - (current.bookedCount ?? 0)
      ),
    });

    return merged;
  }

  function filterNextDaySpillover(nextDaySlots) {
    if (!Array.isArray(nextDaySlots) || nextDaySlots.length === 0) {
      return [];
    }

    const normalized = nextDaySlots
      .map((slot, index) => normalizeSlot(slot, index, "next", true))
      .filter((slot) => {
        const startMinutes = timeToMinutes(slot.startAt);
        const endMinutes = timeToMinutes(slot.endAt);

        if (startMinutes === null || endMinutes === null) return false;

        // take only early-morning continuation like 00:00 -> 01:00, 01:00 -> 02:00
        return startMinutes < 12 * 60;
      })
      .sort((a, b) => (timeToMinutes(a.startAt) ?? 0) - (timeToMinutes(b.startAt) ?? 0));

    const spillover = [];
    let expectedStart = 0;

    for (const slot of normalized) {
      const startMinutes = timeToMinutes(slot.startAt);
      const endMinutes = timeToMinutes(slot.endAt);

      if (startMinutes === expectedStart) {
        spillover.push(slot);
        expectedStart = endMinutes ?? expectedStart;
      } else {
        break;
      }
    }

    return spillover;
  }

  async function handleSearchSlots() {
    if (!roomId) {
      setMessage("Please select a room.");
      setSlots([]);
      setRoomInfo(null);
      return;
    }

    if (!date) {
      setMessage("Please select a date.");
      setSlots([]);
      setRoomInfo(null);
      return;
    }

    try {
      setLoadingSlots(true);
      setMessage("");
      setSlots([]);
      setRoomInfo(null);

      const nextDate = addDaysToDateString(date, 1);

      const [data, nextDayData] = await Promise.all([
        getAvailableSlotsByRoomAndDate(roomId, date),
        getAvailableSlotsByRoomAndDate(roomId, nextDate).catch(() => null),
      ]);

      console.log("Available slots response:", data);
      console.log("Next day slots response:", nextDayData);

      const mainRawSlots = Array.isArray(data?.slots) ? data.slots : [];
      const nextRawSlots = Array.isArray(nextDayData?.slots) ? nextDayData.slots : [];

      const normalizedMain = mainRawSlots.map((slot, index) =>
        normalizeSlot(slot, index, date, false)
      );

      const spilloverSlots = filterNextDaySpillover(nextRawSlots);

      const mergedSlots = mergeContinuousSlots(
        [...normalizedMain, ...spilloverSlots],
        selectedRoom,
        date
      );

      setSlots(mergedSlots);

      setRoomInfo({
        roomId: data?.roomId || roomId,
        roomName: getSelectedRoomName(selectedRoom),
        date: data?.date || date,
        dayOfWeek: data?.dayOfWeek ?? "",
        slotDurationMins: data?.slotDurationMins ?? "",
        capacity: selectedRoom?.seatingCapacity ?? 0,
        pricePerHour: selectedRoom?.feePerHour ?? 0,
      });

      if (mergedSlots.length === 0) {
        setMessage("No slots available for the selected date.");
      }
    } catch (error) {
      console.error("Failed to load slots:", error);
      setMessage(error?.message || "Failed to load room slots.");
      setSlots([]);
      setRoomInfo(null);
    } finally {
      setLoadingSlots(false);
    }
  }

  return (
    <div style={{ padding: "20px" }}>
      <h2>Available Room Slots</h2>

      <div
        style={{
          display: "grid",
          gap: "12px",
          maxWidth: "520px",
          marginBottom: "20px",
        }}
      >
        <div>
          <label>District</label>
          <select
            value={district}
            disabled={loadingRooms}
            onChange={(e) => {
              setDistrict(e.target.value);
              setLocation("");
              setRoomId("");
              setDate("");
              setSlots([]);
              setRoomInfo(null);
              setMessage("");
            }}
            style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          >
            <option value="">Select District</option>
            {districts.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label>Location</label>
          <select
            value={location}
            disabled={loadingRooms || !district}
            onChange={(e) => {
              setLocation(e.target.value);
              setRoomId("");
              setDate("");
              setSlots([]);
              setRoomInfo(null);
              setMessage("");
            }}
            style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          >
            <option value="">Select Location</option>
            {locations.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label>Room</label>
          <select
            value={roomId}
            disabled={loadingRooms || filteredRooms.length === 0}
            onChange={(e) => {
              setRoomId(e.target.value);
              setSlots([]);
              setRoomInfo(null);
              setMessage("");
            }}
            style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          >
            <option value="">Select Room</option>
            {filteredRooms.map((room) => (
              <option key={room.id} value={room.id}>
                {getRoomLabel(room)}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label>Date</label>
          <input
            type="date"
            value={date}
            onChange={(e) => {
              setDate(e.target.value);
              setSlots([]);
              setRoomInfo(null);
              setMessage("");
            }}
            style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          />
        </div>

        <div style={{ display: "flex", gap: "10px" }}>
          <button onClick={handleSearchSlots} disabled={loadingSlots || loadingRooms}>
            {loadingSlots ? "Loading..." : "Show Slots"}
          </button>

          <button onClick={() => navigate(-1)} disabled={loadingSlots}>
            Back
          </button>
        </div>
      </div>

      {loadingRooms && <p>Loading rooms...</p>}
      {message && <p>{message}</p>}
      {!loadingRooms && rooms.length === 0 && !message && <p>No rooms found.</p>}

      {roomInfo && (
        <div
          style={{
            marginBottom: "16px",
            padding: "12px",
            border: "1px solid #ddd",
            borderRadius: "8px",
            maxWidth: "700px",
          }}
        >
          <p>
            <strong>Room:</strong> {roomInfo.roomName || "-"}
          </p>
          <p>
            <strong>Date:</strong> {formatDateOnly(roomInfo.date || date)}
          </p>
          <p>
            <strong>Capacity:</strong> {roomInfo.capacity ?? 0}
          </p>
          <p>
            <strong>Price / Hour:</strong> {formatPrice(roomInfo.pricePerHour)}
          </p>
        </div>
      )}

      {slots.length > 0 && (
        <div>
          <h3>Available Slots</h3>

          <div style={{ display: "grid", gap: "12px" }}>
            {slots.map((slot, index) => (
              <div
                key={slot.id || index}
                style={{
                  border: "1px solid #ccc",
                  borderRadius: "8px",
                  padding: "12px",
                  maxWidth: "700px",
                }}
              >
                <p>
                  <strong>Room:</strong> {slot.roomName || roomInfo?.roomName || "-"}
                </p>
                <p>
                  <strong>Start:</strong> {formatTime(slot.startAt)}
                </p>
                <p>
                  <strong>End:</strong> {formatTime(slot.endAt)}
                </p>
                <p>
                  <strong>Status:</strong> {slot.availabilityStatus || "-"}
                </p>
                <p>
                  <strong>Capacity:</strong> {selectedRoom?.seatingCapacity ?? 0}
                </p>
                <p>
                  <strong>Booked:</strong> {slot.bookedCount ?? 0}
                </p>
                <p>
                  <strong>Remaining:</strong>{" "}
                  {Math.max(
                    0,
                    (selectedRoom?.seatingCapacity ?? 0) - (slot.bookedCount ?? 0)
                  )}
                </p>
                <p>
                  <strong>Price:</strong> {formatPrice(selectedRoom?.feePerHour ?? 0)}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}