import { useEffect, useState } from "react";
import { getMyFeedback } from "../api/feedbackApi";

export default function AdminFeedbackPage() {
  const [feedbackList, setFeedbackList] = useState([]);
  const [message, setMessage] = useState("");

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    async function loadData() {
      try {
        const data = await getMyFeedback(userId);
        setFeedbackList(Array.isArray(data) ? data : []);
      } catch (error) {
        setMessage(error.message || "Failed to load feedback");
      }
    }

    if (userId) {
      loadData();
    }
  }, [userId]);

  return (
    <div>
      <h2>Feedback</h2>
      {message ? <p style={{ color: "red" }}>{message}</p> : null}

      {feedbackList.length === 0 ? (
        <p>No feedback found.</p>
      ) : (
        feedbackList.map((item) => (
          <div
            key={item.id}
            style={{
              border: "1px solid #ccc",
              padding: "12px",
              marginBottom: "12px",
              borderRadius: "8px",
            }}
          >
            <p><strong>Room:</strong> {item.roomName || "-"}</p>
            <p><strong>Overall Rating:</strong> {item.rating ?? "-"} / 5</p>
            <p><strong>Cleanliness Rating:</strong> {item.cleanlinessRating ?? "-"} / 5</p>
            <p><strong>Usefulness Rating:</strong> {item.usefulnessRating ?? "-"} / 5</p>
            <p><strong>Comments:</strong> {item.comments?.trim() ? item.comments : "No comments"}</p>
            <p>
              <strong>Maintenance Issue:</strong>{" "}
              {item.maintenanceIssue?.trim()
                ? item.maintenanceIssue
                : "No maintenance issue reported"}
            </p>
            <p>
              <strong>Date:</strong>{" "}
              {item.createdAt ? new Date(item.createdAt).toLocaleString() : "-"}
            </p>
          </div>
        ))
      )}
    </div>
  );
}