export async function bookRoom(bookingData) {

  const token = localStorage.getItem("token");

  const response = await fetch("http://localhost:8080/api/bookings", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify(bookingData)
  });

  if (!response.ok) {
    throw new Error("Failed to book room");
  }

  return response.json();
}