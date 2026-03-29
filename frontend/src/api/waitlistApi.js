const WAITLIST_BASE_URL = "http://localhost:8080/api/waitlist";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
}

// Join waitlist
export async function joinWaitlist(data) {
  const res = await fetch(`${WAITLIST_BASE_URL}/join`, {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });

  if (!res.ok) throw new Error("Failed to join waitlist");
  return res.json();
}

// Get position
export async function getWaitlistPosition(userId, roomId) {
  const res = await fetch(
    `${WAITLIST_BASE_URL}/position?userId=${userId}&roomId=${roomId}`,
    { headers: getAuthHeaders() }
  );

  if (!res.ok) throw new Error("Failed to fetch position");
  return res.json();
}