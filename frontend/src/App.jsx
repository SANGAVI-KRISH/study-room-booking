import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from "react-router-dom";
import { useEffect, useState } from "react";
import "./App.css";

import AdminDashboard from "./pages/AdminDashboard";
import StaffDashboard from "./pages/StaffDashboard";
import StudentDashboard from "./pages/StudentDashboard";
import ManageRooms from "./pages/ManageStudyRooms";
import Unauthorized from "./pages/Unauthorized";
import RoomAvailability from "./pages/RoomAvailability";
import BookingHistory from "./pages/BookingHistory";
import Notifications from "./pages/Notifications";
import MyBookings from "./pages/MyBookings";
import AdminBookingApproval from "./pages/AdminBookingApproval";
import AdminReports from "./pages/AdminReports";
import ManageTimeSlots from "./pages/ManageTimeSlots";
import RoomSlots from "./pages/RoomSlots";
import AdminFeedback from "./pages/AdminFeedback";

const API_BASE_URL = "http://localhost:8080";

/* ================= HOME PAGE ================= */

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="home-page">
      <header className="navbar">
        <div className="logo">Smart Study Room Booking System</div>

        <div className="nav-buttons">
          <button
            className="btn btn-outline"
            onClick={() => navigate("/login")}
          >
            Login
          </button>

          <button
            className="btn btn-primary"
            onClick={() => navigate("/register")}
          >
            Register
          </button>
        </div>
      </header>

      <section className="hero">
        <div className="hero-content">
          <h1>Book Study Rooms Easily</h1>
          <p>
            Find available study rooms, check facilities, and reserve your slot
            quickly.
          </p>

          <div className="hero-buttons">
            <button
              className="btn btn-primary"
              onClick={() => navigate("/register")}
            >
              Get Started
            </button>

            <button
              className="btn btn-outline"
              onClick={() => navigate("/login")}
            >
              Login
            </button>
          </div>
        </div>
      </section>

      <section className="features">
        <h2>Why Use Our System?</h2>

        <div className="feature-grid">
          <div className="feature-card">
            <h3>Easy Booking</h3>
            <p>Reserve rooms in seconds with simple booking.</p>
          </div>

          <div className="feature-card">
            <h3>Real-Time Availability</h3>
            <p>See available rooms instantly before booking.</p>
          </div>

          <div className="feature-card">
            <h3>Role Based Access</h3>
            <p>Separate dashboards for Student, Staff, and Admin.</p>
          </div>
        </div>
      </section>
    </div>
  );
}

/* ================= LOGIN / REGISTER PAGE ================= */

function LoginPage({ initialMode = "login" }) {
  const navigate = useNavigate();

  const [mode, setMode] = useState(initialMode);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    newPassword: "",
    role: "STUDENT",
  });

  useEffect(() => {
    setMode(initialMode);
    setMessage("");
  }, [initialMode]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    if (token && role) {
      if (role === "ADMIN") {
        navigate("/admin");
      } else if (role === "STAFF") {
        navigate("/staff");
      } else if (role === "STUDENT") {
        navigate("/student");
      }
    }
  }, [navigate, initialMode]);

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const clearForm = () => {
    setFormData({
      name: "",
      email: "",
      password: "",
      newPassword: "",
      role: "STUDENT",
    });
  };

  const switchMode = (newMode) => {
    setMode(newMode);
    setMessage("");
    clearForm();

    if (newMode === "login") {
      navigate("/login");
    } else if (newMode === "register") {
      navigate("/register");
    } else if (newMode === "forgot") {
      navigate("/forgot-password");
    }
  };

  const clearStoredAuth = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    localStorage.removeItem("name");
    localStorage.removeItem("userId");
    localStorage.removeItem("user");
  };

  const getUrlAndPayload = () => {
    if (mode === "login") {
      return {
        url: `${API_BASE_URL}/api/auth/login`,
        payload: {
          email: formData.email.trim(),
          password: formData.password,
        },
      };
    }

    if (mode === "register") {
      return {
        url: `${API_BASE_URL}/api/auth/register`,
        payload: {
          name: formData.name.trim(),
          fullName: formData.name.trim(),
          email: formData.email.trim(),
          password: formData.password,
          role: formData.role,
        },
      };
    }

    return {
      url: `${API_BASE_URL}/api/auth/forgot-password`,
      payload: {
        email: formData.email.trim(),
        newPassword: formData.newPassword,
      },
    };
  };

  const extractErrorMessage = (data, response) => {
    if (typeof data === "string" && data.trim()) {
      return data;
    }

    if (data?.message) {
      return data.message;
    }

    if (data?.error) {
      return data.error;
    }

    if (response.status === 401) {
      return "Invalid email or password";
    }

    if (response.status === 403) {
      return "Access denied";
    }

    if (response.status === 404) {
      return "Requested API endpoint not found";
    }

    if (response.status >= 500) {
      return "Server error occurred";
    }

    return "Something went wrong";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (loading) return;

    setMessage("");

    const { url, payload } = getUrlAndPayload();

    try {
      setLoading(true);

      console.log("Submitting to:", url);
      console.log("Payload:", payload);

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const contentType = response.headers.get("content-type");
      let data = null;

      if (contentType && contentType.includes("application/json")) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      console.log("Response status:", response.status);
      console.log("Response data:", data);

      if (response.ok) {
        if (mode === "login") {
          clearStoredAuth();

          const token = data?.token ?? data?.jwt ?? "";
          const role = data?.role ?? data?.user?.role ?? "";
          const email =
            data?.email ??
            data?.user?.email ??
            formData.email.trim().toLowerCase();
          const name =
            data?.name ??
            data?.fullName ??
            data?.user?.name ??
            data?.user?.fullName ??
            "User";
          const userId =
            data?.userId ?? data?.id ?? data?.user?.id ?? data?.user?.userId;

          if (!token) {
            setMessage("Login failed: token not returned from backend.");
            return;
          }

          if (!role) {
            setMessage("Login failed: role not returned from backend.");
            return;
          }

          if (userId === null || userId === undefined) {
            setMessage(
              "Login successful, but user ID was not returned from backend."
            );
            return;
          }

          const loggedInUser = {
            id: userId,
            name,
            email,
            role,
            token,
          };

          localStorage.setItem("token", token);
          localStorage.setItem("role", role);
          localStorage.setItem("email", email);
          localStorage.setItem("name", name);
          localStorage.setItem("userId", String(userId));
          localStorage.setItem("user", JSON.stringify(loggedInUser));

          clearForm();

          if (role === "ADMIN") {
            navigate("/admin");
          } else if (role === "STAFF") {
            navigate("/staff");
          } else if (role === "STUDENT") {
            navigate("/student");
          } else {
            setMessage("Login successful, but role is invalid.");
          }
        } else if (mode === "register") {
          setMessage(
            typeof data === "string"
              ? data
              : data?.message || "Registration successful. Please login."
          );
          clearForm();
          navigate("/login");
          setMode("login");
        } else {
          setMessage(
            typeof data === "string"
              ? data
              : data?.message || "Password reset successful. Please login."
          );
          clearForm();
          navigate("/login");
          setMode("login");
        }
      } else {
        if (response.status === 401) {
          clearStoredAuth();
        }
        setMessage(extractErrorMessage(data, response));
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("Backend connection failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page-wrapper">
      <div className="login-topbar">
        <div className="login-topbar-title">
          Smart Study Room Booking System
        </div>
        <button className="btn btn-outline" onClick={() => navigate("/")}>
          Home
        </button>
      </div>

      <div className="container">
        <div className="form-box">
          <h1>
            {mode === "login"
              ? "Please sign in"
              : mode === "register"
              ? "Create account"
              : "Forgot Password"}
          </h1>

          <p className="subtitle">Smart Study Room Booking System</p>

          {message && <div className="message">{message}</div>}

          <form onSubmit={handleSubmit}>
            {mode === "register" && (
              <input
                type="text"
                name="name"
                placeholder="Full Name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            )}

            <input
              type="email"
              name="email"
              placeholder="Email"
              value={formData.email}
              onChange={handleChange}
              required
            />

            {mode !== "forgot" && (
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            )}

            {mode === "forgot" && (
              <input
                type="password"
                name="newPassword"
                placeholder="Enter New Password"
                value={formData.newPassword}
                onChange={handleChange}
                required
              />
            )}

            {mode === "register" && (
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
              >
                <option value="STUDENT">STUDENT</option>
                <option value="STAFF">STAFF</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            )}

            <button type="submit" disabled={loading} className="submit-btn">
              {loading
                ? mode === "login"
                  ? "Signing in..."
                  : mode === "register"
                  ? "Registering..."
                  : "Resetting..."
                : mode === "login"
                ? "Sign in"
                : mode === "register"
                ? "Register"
                : "Reset Password"}
            </button>
          </form>

          {mode === "login" && (
            <>
              <p className="toggle-text">
                Don't have an account?{" "}
                <span
                  onClick={() => switchMode("register")}
                  className="toggle-link"
                >
                  Register
                </span>
              </p>

              <p className="toggle-text">
                <span
                  onClick={() => switchMode("forgot")}
                  className="toggle-link"
                >
                  Forgot Password?
                </span>
              </p>
            </>
          )}

          {mode === "register" && (
            <p className="toggle-text">
              Already have an account?{" "}
              <span
                onClick={() => switchMode("login")}
                className="toggle-link"
              >
                Login
              </span>
            </p>
          )}

          {mode === "forgot" && (
            <p className="toggle-text">
              Back to{" "}
              <span
                onClick={() => switchMode("login")}
                className="toggle-link"
              >
                Login
              </span>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

/* ================= PROTECTED ROUTE ================= */

function ProtectedRoute({ children, allowedRoles }) {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}

/* ================= APP ================= */

export default function App() {
  useEffect(() => {
    const savedUser = localStorage.getItem("user");
    const savedToken = localStorage.getItem("token");

    if (savedUser && savedToken) {
      try {
        JSON.parse(savedUser);
      } catch (error) {
        console.error("Invalid user data in localStorage", error);
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("email");
        localStorage.removeItem("name");
        localStorage.removeItem("userId");
      }
    }
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage initialMode="login" />} />
        <Route
          path="/register"
          element={<LoginPage initialMode="register" />}
        />
        <Route
          path="/forgot-password"
          element={<LoginPage initialMode="forgot" />}
        />

        <Route path="/manage-time-slots" element={<ManageTimeSlots />} />
        <Route path="/room-slots" element={<RoomSlots />} />

        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/staff"
          element={
            <ProtectedRoute allowedRoles={["STAFF", "ADMIN"]}>
              <StaffDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/student"
          element={
            <ProtectedRoute allowedRoles={["STUDENT", "ADMIN"]}>
              <StudentDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/rooms"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <ManageRooms />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/reports"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminReports />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/booking-approval"
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
              <AdminBookingApproval />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/feedback"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminFeedback />
            </ProtectedRoute>
          }
        />

        <Route
          path="/rooms/availability"
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "STUDENT"]}>
              <RoomAvailability />
            </ProtectedRoute>
          }
        />

        <Route
          path="/my-bookings"
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "STUDENT"]}>
              <MyBookings />
            </ProtectedRoute>
          }
        />

        <Route
          path="/booking-history"
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "STUDENT"]}>
              <BookingHistory />
            </ProtectedRoute>
          }
        />

        <Route
          path="/notifications"
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "STUDENT"]}>
              <Notifications />
            </ProtectedRoute>
          }
        />

        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}