import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAllFeedbacks } from "../api/feedbackApi";
import "./AdminFeedback.css";

export default function AdminFeedback() {
  const navigate = useNavigate();

  const [feedbacks, setFeedbacks] = useState([]);
  const [filteredFeedbacks, setFilteredFeedbacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageError, setPageError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [ratingFilter, setRatingFilter] = useState("ALL");
  const [sortOrder, setSortOrder] = useState("LATEST");

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  useEffect(() => {
    if (!token) {
      setLoading(false);
      setPageError("You are not logged in.");
      return;
    }

    if (String(role || "").toUpperCase() !== "ADMIN") {
      setLoading(false);
      setPageError("Access denied. Admin only.");
      return;
    }

    loadFeedbacks();
  }, [token, role]);

  useEffect(() => {
    applyFilters();
  }, [feedbacks, searchTerm, ratingFilter, sortOrder]);

  const loadFeedbacks = async () => {
    try {
      setLoading(true);
      setPageError("");

      const data = await getAllFeedbacks();

      const safeData = Array.isArray(data) ? data : [];
      setFeedbacks(safeData);
      setFilteredFeedbacks(safeData);
    } catch (error) {
      console.error("Failed to load feedbacks:", error);
      setPageError(error.message || "Failed to load feedbacks.");
      setFeedbacks([]);
      setFilteredFeedbacks([]);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let updated = [...feedbacks];

    const keyword = searchTerm.trim().toLowerCase();
    if (keyword) {
      updated = updated.filter((item) => {
        const studentName = String(
          item.studentName || item.userName || item.userFullName || ""
        ).toLowerCase();

        const roomName = String(
          item.roomName || item.studyRoomName || item.room || ""
        ).toLowerCase();

        const comment = String(
          item.comment || item.feedback || item.review || ""
        ).toLowerCase();

        return (
          studentName.includes(keyword) ||
          roomName.includes(keyword) ||
          comment.includes(keyword)
        );
      });
    }

    if (ratingFilter !== "ALL") {
      updated = updated.filter(
        (item) => String(item.rating || "") === String(ratingFilter)
      );
    }

    updated.sort((a, b) => {
      const dateA = new Date(a.createdAt || a.submittedAt || a.feedbackDate || 0);
      const dateB = new Date(b.createdAt || b.submittedAt || b.feedbackDate || 0);

      if (sortOrder === "LATEST") return dateB - dateA;
      if (sortOrder === "OLDEST") return dateA - dateB;
      if (sortOrder === "RATING_HIGH") return (b.rating || 0) - (a.rating || 0);
      if (sortOrder === "RATING_LOW") return (a.rating || 0) - (b.rating || 0);

      return 0;
    });

    setFilteredFeedbacks(updated);
  };

  const formatDateTime = (value) => {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "-";

    return date.toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const renderStars = (rating) => {
    const safeRating = Number(rating) || 0;
    const fullStars = "★".repeat(Math.max(0, Math.min(5, safeRating)));
    const emptyStars = "☆".repeat(5 - Math.max(0, Math.min(5, safeRating)));

    return (
      <span className="admin-feedback-stars" title={`${safeRating}/5`}>
        {fullStars}
        {emptyStars}
      </span>
    );
  };

  const getRatingBadgeClass = (rating) => {
    const value = Number(rating) || 0;

    if (value >= 4) return "rating-badge excellent";
    if (value === 3) return "rating-badge average";
    if (value > 0) return "rating-badge poor";
    return "rating-badge default";
  };

  const stats = useMemo(() => {
    const total = feedbacks.length;
    const avg =
      total > 0
        ? (
            feedbacks.reduce((sum, item) => sum + (Number(item.rating) || 0), 0) /
            total
          ).toFixed(1)
        : "0.0";

    const fiveStar = feedbacks.filter((item) => Number(item.rating) === 5).length;
    const lowRatings = feedbacks.filter((item) => Number(item.rating) <= 2).length;

    return { total, avg, fiveStar, lowRatings };
  }, [feedbacks]);

  if (loading) {
    return (
      <div className="admin-feedback-page">
        <div className="admin-feedback-header">
          <div>
            <h1>Student Feedback</h1>
            <p>View and monitor student feedback for study rooms</p>
          </div>
        </div>

        <div className="admin-feedback-loading-card">
          <div className="spinner" />
          <p>Loading feedbacks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-feedback-page">
      <div className="admin-feedback-header">
        <div>
          <h1>Student Feedback</h1>
          <p>View and monitor student feedback for study rooms</p>
        </div>

        <div className="admin-feedback-header-actions">
          <button
            className="secondary-btn"
            onClick={() => navigate("/admin")}
          >
            Back to Dashboard
          </button>

          <button className="primary-btn" onClick={loadFeedbacks}>
            Refresh
          </button>
        </div>
      </div>

      {pageError ? (
        <div className="admin-feedback-error-box">{pageError}</div>
      ) : (
        <>
          <div className="admin-feedback-stats">
            <div className="stat-card">
              <span className="stat-label">Total Feedbacks</span>
              <span className="stat-value">{stats.total}</span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Average Rating</span>
              <span className="stat-value">{stats.avg} / 5</span>
            </div>

            <div className="stat-card">
              <span className="stat-label">5 Star Reviews</span>
              <span className="stat-value">{stats.fiveStar}</span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Low Ratings</span>
              <span className="stat-value">{stats.lowRatings}</span>
            </div>
          </div>

          <div className="admin-feedback-filters">
            <div className="filter-group">
              <label htmlFor="searchTerm">Search</label>
              <input
                id="searchTerm"
                type="text"
                placeholder="Search by student, room, or comment"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <label htmlFor="ratingFilter">Rating</label>
              <select
                id="ratingFilter"
                value={ratingFilter}
                onChange={(e) => setRatingFilter(e.target.value)}
              >
                <option value="ALL">All Ratings</option>
                <option value="5">5 Star</option>
                <option value="4">4 Star</option>
                <option value="3">3 Star</option>
                <option value="2">2 Star</option>
                <option value="1">1 Star</option>
              </select>
            </div>

            <div className="filter-group">
              <label htmlFor="sortOrder">Sort By</label>
              <select
                id="sortOrder"
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value)}
              >
                <option value="LATEST">Latest First</option>
                <option value="OLDEST">Oldest First</option>
                <option value="RATING_HIGH">Highest Rating</option>
                <option value="RATING_LOW">Lowest Rating</option>
              </select>
            </div>
          </div>

          {filteredFeedbacks.length === 0 ? (
            <div className="admin-feedback-empty-box">
              No feedbacks found.
            </div>
          ) : (
            <div className="admin-feedback-grid">
              {filteredFeedbacks.map((item, index) => {
                const feedbackId =
                  item.feedbackId || item.id || item.bookingId || index;

                const studentName =
                  item.studentName || item.userName || item.userFullName || "-";

                const roomName =
                  item.roomName || item.studyRoomName || item.room || "-";

                const comment =
                  item.comment || item.feedback || item.review || "No comment";

                const rating = Number(item.rating) || 0;

                const bookingDate =
                  item.bookingDate || item.startAt || item.bookingStartAt || null;

                const createdAt =
                  item.createdAt || item.submittedAt || item.feedbackDate || null;

                return (
                  <div className="admin-feedback-card" key={feedbackId}>
                    <div className="feedback-card-top">
                      <div>
                        <h3>{studentName}</h3>
                        <p className="room-name">{roomName}</p>
                      </div>

                      <span className={getRatingBadgeClass(rating)}>
                        {rating}/5
                      </span>
                    </div>

                    <div className="feedback-rating-row">
                      {renderStars(rating)}
                    </div>

                    <div className="feedback-info">
                      <div className="info-box">
                        <span className="info-label">Booking Date</span>
                        <span className="info-value">
                          {formatDateTime(bookingDate)}
                        </span>
                      </div>

                      <div className="info-box">
                        <span className="info-label">Submitted On</span>
                        <span className="info-value">
                          {formatDateTime(createdAt)}
                        </span>
                      </div>
                    </div>

                    <div className="feedback-comment-box">
                      <span className="comment-label">Student Comment</span>
                      <p>{comment}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}