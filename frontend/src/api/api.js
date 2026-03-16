const BASE_URL = "http://localhost:8080/api";

// -------------------
// Parse response safely
// -------------------
async function parseResponse(response) {
  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    return await response.json();
  }

  return await response.text();
}

// -------------------
// Login API
// -------------------
export async function loginUser(email, password) {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email: email.trim(),
      password,
    }),
  });

  const data = await parseResponse(response);

  if (!response.ok) {
    if (typeof data === "string") {
      throw new Error(data || "Login failed");
    }
    throw new Error(data?.message || "Login failed");
  }

  console.log("Login API response:", data);
  return data;
}

// -------------------
// Get token from localStorage
// -------------------
export function getToken() {
  return localStorage.getItem("token");
}

// -------------------
// Get user ID from localStorage
// -------------------
export function getUserId() {
  return localStorage.getItem("userId");
}

// -------------------
// Logout function
// -------------------
export function logoutUser() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("email");
  localStorage.removeItem("name");
  localStorage.removeItem("userId");
  localStorage.removeItem("user");
  window.location.href = "/";
}

// -------------------
// Common API request function
// -------------------
export async function apiRequest(endpoint, method = "GET", body = null) {
  const token = getToken();

  const options = {
    method,
    headers: {
      "Content-Type": "application/json",
    },
  };

  if (token) {
    options.headers.Authorization = `Bearer ${token}`;
  }

  if (body !== null) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, options);
  const data = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error(
        typeof data === "string"
          ? data || "Unauthorized. Please login again."
          : data?.message || "Unauthorized. Please login again."
      );
    }

    throw new Error(
      typeof data === "string"
        ? data || `Request failed with status ${response.status}`
        : data?.message || `Request failed with status ${response.status}`
    );
  }

  return data;
}

// -------------------
// Example protected GET request
// -------------------
export async function getStudentProfile() {
  return apiRequest("/student/profile", "GET");
}

// -------------------
// Example protected POST request
// -------------------
export async function createBooking(bookingData) {
  return apiRequest("/bookings", "POST", bookingData);
}

// -------------------
// Example protected PUT request
// -------------------
export async function updateProfile(profileData) {
  return apiRequest("/student/profile", "PUT", profileData);
}

// -------------------
// Example protected DELETE request
// -------------------
export async function deleteBooking(id) {
  return apiRequest(`/bookings/${id}`, "DELETE");
}