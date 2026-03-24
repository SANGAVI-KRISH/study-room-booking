export default function FeedbackList({ summary }) {
  if (!summary) return null;

  const reviews = Array.isArray(summary.reviews) ? summary.reviews : [];

  const averageRating = Number(summary.averageRating || 0).toFixed(1);
  const averageCleanliness = Number(summary.averageCleanlinessRating || 0).toFixed(1);
  const averageUsefulness = Number(summary.averageUsefulnessRating || 0).toFixed(1);
  const totalReviews = Number(summary.totalReviews || reviews.length || 0);

  const renderStars = (value) => {
    const rating = Number(value || 0);

    return (
      <div style={styles.starsRow}>
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            style={{
              ...styles.star,
              color: rating >= star ? "#f59e0b" : "#d1d5db",
            }}
          >
            ★
          </span>
        ))}
        <span style={styles.starText}>{rating ? `${rating}/5` : "0/5"}</span>
      </div>
    );
  };

  const formatDateTime = (value) => {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "-";

    return date.toLocaleString("en-IN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  return (
    <div style={styles.card}>
      <h3 style={styles.heading}>Room Reviews</h3>

      <div style={styles.summaryGrid}>
        <div style={styles.summaryBox}>
          <div style={styles.summaryLabel}>Average Rating</div>
          <div style={styles.summaryValue}>{averageRating} / 5</div>
          {renderStars(averageRating)}
        </div>

        <div style={styles.summaryBox}>
          <div style={styles.summaryLabel}>Average Cleanliness</div>
          <div style={styles.summaryValue}>{averageCleanliness} / 5</div>
          {renderStars(averageCleanliness)}
        </div>

        <div style={styles.summaryBox}>
          <div style={styles.summaryLabel}>Average Usefulness</div>
          <div style={styles.summaryValue}>{averageUsefulness} / 5</div>
          {renderStars(averageUsefulness)}
        </div>

        <div style={styles.summaryBox}>
          <div style={styles.summaryLabel}>Total Reviews</div>
          <div style={styles.summaryValue}>{totalReviews}</div>
        </div>
      </div>

      {reviews.length === 0 ? (
        <div style={styles.emptyBox}>No reviews available yet.</div>
      ) : (
        <div style={styles.reviewList}>
          {reviews.map((review, index) => (
            <div
              key={review.id || review.feedbackId || `${review.userName || "review"}-${index}`}
              style={styles.reviewCard}
            >
              <div style={styles.reviewHeader}>
                <div>
                  <div style={styles.userName}>{review.userName || "User"}</div>
                  <div style={styles.reviewDate}>
                    {formatDateTime(review.createdAt)}
                  </div>
                </div>

                <div style={styles.badge}>
                  Booking Feedback
                </div>
              </div>

              <div style={styles.metricsGrid}>
                <div style={styles.metricBox}>
                  <div style={styles.metricLabel}>Overall</div>
                  {renderStars(review.rating)}
                </div>

                <div style={styles.metricBox}>
                  <div style={styles.metricLabel}>Cleanliness</div>
                  {renderStars(review.cleanlinessRating)}
                </div>

                <div style={styles.metricBox}>
                  <div style={styles.metricLabel}>Usefulness</div>
                  {renderStars(review.usefulnessRating)}
                </div>
              </div>

              <div style={styles.section}>
                <div style={styles.sectionTitle}>Comments</div>
                <div style={styles.sectionBody}>
                  {review.comments?.trim()
                    ? review.comments
                    : "No comments provided."}
                </div>
              </div>

              <div style={styles.section}>
                <div style={styles.sectionTitle}>Maintenance Issue</div>
                <div style={styles.sectionBody}>
                  {review.maintenanceIssue?.trim()
                    ? review.maintenanceIssue
                    : "No maintenance issue reported."}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
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
  },
  heading: {
    marginTop: 0,
    marginBottom: "16px",
    color: "#111827",
  },
  summaryGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "14px",
    marginBottom: "20px",
  },
  summaryBox: {
    padding: "14px 16px",
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
    borderRadius: "10px",
  },
  summaryLabel: {
    fontSize: "13px",
    color: "#6b7280",
    marginBottom: "6px",
  },
  summaryValue: {
    fontSize: "22px",
    fontWeight: 700,
    color: "#111827",
    marginBottom: "6px",
  },
  emptyBox: {
    padding: "14px",
    borderRadius: "10px",
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
    color: "#6b7280",
  },
  reviewList: {
    display: "grid",
    gap: "14px",
  },
  reviewCard: {
    border: "1px solid #e5e7eb",
    padding: "16px",
    borderRadius: "10px",
    background: "#ffffff",
  },
  reviewHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "12px",
    flexWrap: "wrap",
    marginBottom: "14px",
  },
  userName: {
    fontWeight: 700,
    color: "#111827",
    marginBottom: "4px",
  },
  reviewDate: {
    color: "#6b7280",
    fontSize: "13px",
  },
  badge: {
    padding: "6px 10px",
    borderRadius: "999px",
    background: "#eff6ff",
    color: "#1d4ed8",
    fontSize: "12px",
    fontWeight: 700,
  },
  metricsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
    gap: "10px",
    marginBottom: "14px",
  },
  metricBox: {
    padding: "12px",
    borderRadius: "8px",
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
  },
  metricLabel: {
    fontWeight: 600,
    color: "#374151",
    marginBottom: "6px",
  },
  starsRow: {
    display: "flex",
    alignItems: "center",
    gap: "3px",
    flexWrap: "wrap",
  },
  star: {
    fontSize: "18px",
    lineHeight: 1,
  },
  starText: {
    marginLeft: "6px",
    fontSize: "13px",
    fontWeight: 600,
    color: "#374151",
  },
  section: {
    marginTop: "12px",
  },
  sectionTitle: {
    fontWeight: 700,
    color: "#111827",
    marginBottom: "4px",
  },
  sectionBody: {
    color: "#374151",
    lineHeight: 1.5,
    whiteSpace: "pre-wrap",
    wordBreak: "break-word",
  },
};