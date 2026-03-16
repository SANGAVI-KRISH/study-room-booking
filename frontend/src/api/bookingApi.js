import axios from "axios";

const API_URL = "http://localhost:8080/api/bookings";

const getAuthHeaders = () => ({
  headers: {
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  },
});

export const createBooking = async (bookingData) => {
  const res = await axios.post(API_URL, bookingData, getAuthHeaders());
  return res.data;
};

export const getPendingBookings = async () => {
  const res = await axios.get(`${API_URL}/pending`, getAuthHeaders());
  return res.data;
};

export const approveBooking = async (id) => {
  const res = await axios.put(`${API_URL}/${id}/approve`, {}, getAuthHeaders());
  return res.data;
};

export const rejectBooking = async (id) => {
  const res = await axios.put(`${API_URL}/${id}/reject`, {}, getAuthHeaders());
  return res.data;
};

export const cancelBooking = async (id) => {
  const res = await axios.put(`${API_URL}/${id}/cancel`, {}, getAuthHeaders());
  return res.data;
};