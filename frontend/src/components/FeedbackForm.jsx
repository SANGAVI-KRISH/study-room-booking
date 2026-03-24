import { useMemo, useState } from "react";
import { submitFeedback } from "../api/feedbackApi";

export default function FeedbackForm({ booking, onSuccess, onCancel }) {
  const [rating, setRating] = useState(5);
  const [cleanlinessRating, setCleanlinessRating] = useState(5);
  const [usefulnessRating, setUsefulnessRating] = useState(5);
  const [comments, setComments] = useState("");
  const [maintenanceIssue, setMaintenanceIssue] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("info");

  const userId = localStorage.getItem("userId");

  const bookingId = booking?.bookingId ?? booking?.id ?? null;
  const roomName = booking?.roomName ?? "Room";

  const averageRating = useMemo(() => {
    const avg =
      (Number(rating) +
        Number(cleanlinessRating) +
        Number(usefulnessRating)) /
      3;
    return avg.toFixed(1);
  }, [rating, cleanlinessRating, usefulnessRating]);

  const validateRating = (value) => {
    const n = Number(value);
    return !Number.isNaN(n) && n >= 1 && n <= 5;
  };

  const resetMessage = () => {
    setMessage("");
    setMessageType("info");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    resetMessage();

    if (!userId) {
      setMessageType("error");
      setMessage("User not found. Please login again.");
      return;
    }

    if (!bookingId) {
      setMessageType("error");
      setMessage("Booking information is missing.");
      return;
    }

    if (
      !validateRating(rating) ||
      !validateRating(cleanlinessRating) ||
      !validateRating(usefulnessRating)
    ) {
      setMessageType("error");
      setMessage("All ratings must be between 1 and 5.");
      return;
    }

    if (comments.trim().length > 1000) {
      setMessageType("error");
      setMessage("Comments cannot exceed 1000 characters.");
      return;
    }

    if (maintenanceIssue.trim().length > 1000) {
      setMessageType("error");
      setMessage("Maintenance issue cannot exceed 1000 characters.");
      return;
    }

    try {
      setLoading(true);

      await submitFeedback(
        {
          bookingId,
          rating: Number(rating),
          cleanlinessRating: Number(cleanlinessRating),
          usefulnessRating: Number(usefulnessRating),
          comments: comments.trim(),
          maintenanceIssue: maintenanceIssue.trim(),
        },
        userId
      );

      setMessageType("success");
      setMessage("Feedback submitted successfully.");

      setTimeout(() => {
        if (onSuccess) {
          onSuccess();
        }
      }, 500);
    } catch (error) {
      setMessageType("error");
      setMessage(error.message || "Failed to submit feedback.");
    } finally {
      setLoading(false);
    }
  };

  const RatingStars = ({ label, value, onChange, idPrefix }) => {
    return (
      <div style={styles.fieldGroup}>
        <label style={styles.label}>{label}</label>

        <div style={styles.starsRow}>
          {[1, 2, 3, 4, 5].map((star) => {
            const active = Number(value) >= star;

            return (
              <button
                key={`${idPrefix}-${star}`}
                type="button"
                onClick={() => onChange(star)}
                disabled={loading}
                aria-label={`${label} ${star} out of 5`}
                title={`${star} / 5`}
                style={{
                  ...styles.starButton,
                  color: active ? "#f59e0b" : "#cbd5e1",
                  cursor: loading ? "not-allowed" : "pointer",
                }}
              >
                ★
              </button>
            );
          })}

          <span style={styles.ratingValue}>{value}/5</span>
        </div>
      </div>
    );
  };

  return (
    <div style={styles.card}>
      <h3 style={styles.title}>Give Feedback</h3>

      <p style={styles.subtitle}>
        Share your experience for <strong>{roomName}</strong>
      </p>

      <div style={styles.summaryBox}>
        <span style={styles.summaryLabel}>Current Average Rating:</span>
        <span style={styles.summaryValue}>{averageRating}/5</span>
      </div>

      <form onSubmit={handleSubmit}>
        <RatingStars
          label="Overall Rating"
          value={rating}
          onChange={setRating}
          idPrefix="overall"
        />

        <RatingStars
          label="Cleanliness Rating"
          value={cleanlinessRating}
          onChange={setCleanlinessRating}
          idPrefix="cleanliness"
        />

        <RatingStars
          label="Usefulness Rating"
          value={usefulnessRating}
          onChange={setUsefulnessRating}
          idPrefix="usefulness"
        />

        <div style={styles.fieldGroup}>
          <label htmlFor="comments" style={styles.label}>
            Comments
          </label>
          <textarea
            id="comments"
            rows="4"
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder="Share your experience"
            maxLength={1000}
            disabled={loading}
            style={styles.textarea}
          />
          <div style={styles.counter}>{comments.length}/1000</div>
        </div>

        <div style={styles.fieldGroup}>
          <label htmlFor="maintenanceIssue" style={styles.label}>
            Maintenance Issue
          </label>
          <textarea
            id="maintenanceIssue"
            rows="3"
            value={maintenanceIssue}
            onChange={(e) => setMaintenanceIssue(e.target.value)}
            placeholder="Report any maintenance issue if found"
            maxLength={1000}
            disabled={loading}
            style={styles.textarea}
          />
          <div style={styles.counter}>{maintenanceIssue.length}/1000</div>
        </div>

        {message ? (
          <div
            style={{
              ...styles.messageBox,
              ...(messageType === "error"
                ? styles.errorMessage
                : styles.successMessage),
            }}
          >
            {message}
          </div>
        ) : null}

        <div style={styles.buttonRow}>
          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.submitButton,
              opacity: loading ? 0.7 : 1,
              cursor: loading ? "not-allowed" : "pointer",
            }}
          >
            {loading ? "Submitting..." : "Submit Feedback"}
          </button>

          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            style={{
              ...styles.cancelButton,
              cursor: loading ? "not-allowed" : "pointer",
            }}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

const styles = {
  card: {
    background: "#fff",
    border: "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "20px",
    boxShadow: "0 4px 14px rgba(0,0,0,0.08)",
    maxWidth: "700px",
    width: "100%",
  },
  title: {
    marginTop: 0,
    marginBottom: "8px",
    color: "#111827",
  },
  subtitle: {
    marginTop: 0,
    marginBottom: "16px",
    color: "#4b5563",
  },
  summaryBox: {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    backgroundColor: "#eff6ff",
    color: "#1d4ed8",
    border: "1px solid #bfdbfe",
    borderRadius: "999px",
    padding: "8px 14px",
    marginBottom: "18px",
    fontWeight: 600,
  },
  summaryLabel: {
    fontSize: "14px",
  },
  summaryValue: {
    fontSize: "14px",
  },
  fieldGroup: {
    marginBottom: "16px",
  },
  label: {
    display: "block",
    marginBottom: "8px",
    fontWeight: 600,
    color: "#111827",
  },
  starsRow: {
    display: "flex",
    alignItems: "center",
    gap: "6px",
    flexWrap: "wrap",
  },
  starButton: {
    border: "none",
    background: "transparent",
    fontSize: "30px",
    lineHeight: 1,
    padding: "2px 4px",
  },
  ratingValue: {
    marginLeft: "8px",
    fontWeight: 600,
    color: "#374151",
  },
  textarea: {
    width: "100%",
    padding: "10px",
    borderRadius: "8px",
    border: "1px solid #d1d5db",
    resize: "vertical",
    boxSizing: "border-box",
    fontFamily: "inherit",
  },
  counter: {
    marginTop: "6px",
    fontSize: "12px",
    color: "#6b7280",
    textAlign: "right",
  },
  messageBox: {
    marginBottom: "14px",
    padding: "10px 12px",
    borderRadius: "8px",
  },
  errorMessage: {
    backgroundColor: "#fef2f2",
    color: "#b91c1c",
    border: "1px solid #fecaca",
  },
  successMessage: {
    backgroundColor: "#ecfdf5",
    color: "#047857",
    border: "1px solid #a7f3d0",
  },
  buttonRow: {
    display: "flex",
    gap: "10px",
    flexWrap: "wrap",
  },
  submitButton: {
    padding: "10px 16px",
    borderRadius: "8px",
    border: "none",
    backgroundColor: "#2563eb",
    color: "#fff",
    fontWeight: 600,
  },
  cancelButton: {
    padding: "10px 16px",
    borderRadius: "8px",
    border: "1px solid #d1d5db",
    backgroundColor: "#fff",
    color: "#111827",
    fontWeight: 600,
  },
};