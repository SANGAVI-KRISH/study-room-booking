import { useEffect, useState } from "react";
import { getAllUsers } from "../api/roomApi";
import "./ManageUser.css";

export default function ManageUser() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Role check
  const role = (localStorage.getItem("role") || "").trim().toUpperCase();
  if (role !== "ADMIN") {
    return (
      <div className="access-denied-container">
        <h2>Access Denied</h2>
        <p>You are not authorized to view this page.</p>
      </div>
    );
  }

  // Fetch users
  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await getAllUsers();

        if (!Array.isArray(data)) {
          throw new Error("Invalid response format");
        }

        // Sort: ADMIN → STAFF → STUDENT
        const roleOrder = { ADMIN: 1, STAFF: 2, STUDENT: 3 };

        const sortedData = [...data].sort((a, b) => {
          return (
            (roleOrder[a.role?.toUpperCase()] || 99) -
            (roleOrder[b.role?.toUpperCase()] || 99)
          );
        });

        setUsers(sortedData);
      } catch (err) {
        console.error("Failed to fetch users:", err);
        setError("Failed to fetch users. Please check backend.");
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  // Filter users
  const filteredUsers = users.filter((user) =>
    (user.name || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (user.email || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (user.role || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return <p className="loading-text">Loading users...</p>;
  if (error) return <p className="error-text">{error}</p>;

  return (
    <div className="manage-user-container">
      <h2>Manage Users</h2>

      <input
        type="text"
        placeholder="Search by name, email, or role..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="search-input"
      />

      <table className="user-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
          </tr>
        </thead>

        <tbody>
          {filteredUsers.length > 0 ? (
            filteredUsers.map((user, index) => (
              <tr key={user.id || index}>
                <td>{user.name || "N/A"}</td>
                <td>{user.email || "N/A"}</td>
                <td>{user.role || "N/A"}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="3">No users found</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}