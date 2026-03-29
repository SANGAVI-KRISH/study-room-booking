import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getUserProfile, updateUserPassword } from "../api/roomApi";

export default function Profile() {
  const navigate = useNavigate();

  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role");

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  // For editable profile fields
  const [editData, setEditData] = useState({
    department: "",
    phone: "",
  });
  const [editing, setEditing] = useState(false); // toggle edit mode

  // For password change
  const [formData, setFormData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [updating, setUpdating] = useState(false);

  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (!email) {
      alert("User not logged in");
      navigate("/login");
      return;
    }
    loadProfile();
  }, [email]);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setErrorMessage("");
      const data = await getUserProfile(email);
      setProfile(data);
      setEditData({
        department: data.department || "",
        phone: data.phone || "",
      });
    } catch (error) {
      console.error("Failed to load profile:", error);
      setErrorMessage(error.response?.data?.message || "Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  const handleEditChange = (e) => {
    setEditData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const toggleEdit = () => {
    setEditing((prev) => !prev);
  };

  const handleProfileUpdate = (e) => {
    e.preventDefault();
    setSuccessMessage("");
    setErrorMessage("");

    // Update only local profile
    setProfile((prev) => ({
      ...prev,
      department: editData.department,
      phone: editData.phone,
    }));

    setSuccessMessage("Profile updated");
    setEditing(false);
  };

  // Handle password change
  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handlePasswordUpdate = async (e) => {
    e.preventDefault();
    setSuccessMessage("");
    setErrorMessage("");

    if (!formData.currentPassword || !formData.newPassword || !formData.confirmPassword) {
      setErrorMessage("Please fill all password fields");
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setErrorMessage("New password and confirm password do not match");
      return;
    }

    if (formData.newPassword.length < 6) {
      setErrorMessage("Password must be at least 6 characters long");
      return;
    }

    try {
      setUpdating(true);
      const response = await updateUserPassword(email, formData);
      setSuccessMessage(response.message || "Password updated successfully");
      setFormData({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (error) {
      console.error("Password update failed:", error);
      setErrorMessage(error.response?.data?.message || "Failed to update password");
    } finally {
      setUpdating(false);
    }
  };

  // Auto-clear success message after 5s
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(""), 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const handleBack = () => {
    if (role === "ADMIN") navigate("/admin");
    else if (role === "STAFF") navigate("/staff");
    else navigate("/student");
  };

  if (loading) return <div className="page-container"><p>Loading profile...</p></div>;

  return (
    <div className="page-container" style={{ maxWidth: "900px", margin: "0 auto", padding: "20px" }}>
      <button
        onClick={handleBack}
        style={{
          marginBottom: "20px",
          padding: "10px 16px",
          border: "none",
          borderRadius: "8px",
          background: "#444",
          color: "#fff",
          cursor: "pointer",
        }}
      >
        Back to Dashboard
      </button>

      <h1 style={{ marginBottom: "20px" }}>My Profile</h1>

      {errorMessage && (
        <div style={{ marginBottom: "16px", padding: "12px", borderRadius: "8px", background: "#ffe5e5", color: "#b00020" }}>
          {errorMessage}
        </div>
      )}
      {successMessage && (
        <div style={{ marginBottom: "16px", padding: "12px", borderRadius: "8px", background: "#e7f8ea", color: "#1b5e20" }}>
          {successMessage}
        </div>
      )}

      {profile && (
        <div style={{ background: "#fff", borderRadius: "14px", padding: "24px", boxShadow: "0 4px 14px rgba(0,0,0,0.08)", marginBottom: "24px" }}>
          <h2 style={{ marginBottom: "18px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            Profile Details
            <span
              style={{ cursor: "pointer", fontSize: "20px" }}
              onClick={toggleEdit}
              title={editing ? "Cancel Edit" : "Edit Profile"}
            >
              ✏️
            </span>
          </h2>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
            <div><strong>Full Name</strong><p>{profile.name || "-"}</p></div>
            <div><strong>Email</strong><p>{profile.email || "-"}</p></div>
            <div><strong>Role</strong><p>{profile.role || "-"}</p></div>

            <form onSubmit={handleProfileUpdate} style={{ gridColumn: "1 / -1", display: "grid", gap: "16px", marginTop: "16px" }}>
              <div>
                <label><strong>Department</strong></label>
                <input
                  type="text"
                  name="department"
                  value={editData.department}
                  onChange={handleEditChange}
                  style={inputStyle}
                  readOnly={!editing}
                />
              </div>
              <div>
                <label><strong>Phone</strong></label>
                <input
                  type="text"
                  name="phone"
                  value={editData.phone}
                  onChange={handleEditChange}
                  style={inputStyle}
                  readOnly={!editing}
                />
              </div>
              {editing && (
                <button
                  type="submit"
                  style={{
                    padding: "12px 18px",
                    border: "none",
                    borderRadius: "8px",
                    background: "#1976d2",
                    color: "#fff",
                    cursor: "pointer",
                    width: "120px",
                  }}
                >
                  Save
                </button>
              )}
            </form>

            <div><strong>Status</strong><p>{profile.isActive ? "Active" : "Inactive"}</p></div>
          </div>
        </div>
      )}

      <div style={{ background: "#fff", borderRadius: "14px", padding: "24px", boxShadow: "0 4px 14px rgba(0,0,0,0.08)" }}>
        <h2 style={{ marginBottom: "18px" }}>Change Password</h2>
        <form onSubmit={handlePasswordUpdate}>
          {["currentPassword","newPassword","confirmPassword"].map((field) => (
            <div key={field} style={{ marginBottom: field==="confirmPassword"?"18px":"14px" }}>
              <label><strong>{field==="currentPassword"?"Current Password":field==="newPassword"?"New Password":"Confirm New Password"}</strong></label>
              <input type="password" name={field} value={formData[field]} onChange={handleChange} style={inputStyle} />
            </div>
          ))}
          <button
            type="submit"
            disabled={updating}
            style={{
              padding: "12px 18px",
              border: "none",
              borderRadius: "8px",
              background: updating ? "#999" : "#1976d2",
              color: "#fff",
              cursor: updating ? "not-allowed" : "pointer",
            }}
          >
            {updating ? "Updating..." : "Update Password"}
          </button>
        </form>
      </div>
    </div>
  );
}

const inputStyle = {
  width: "100%",
  marginTop: "8px",
  padding: "12px",
  borderRadius: "8px",
  border: "1px solid #ccc",
  outline: "none",
};