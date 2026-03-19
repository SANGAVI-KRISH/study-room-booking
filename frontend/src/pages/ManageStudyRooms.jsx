import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080";

export default function ManageStudyRooms() {
  const navigate = useNavigate();

  const tamilNaduDistricts = [
    "Ariyalur",
    "Chengalpattu",
    "Chennai",
    "Coimbatore",
    "Cuddalore",
    "Dharmapuri",
    "Dindigul",
    "Erode",
    "Kallakurichi",
    "Kancheepuram",
    "Kanniyakumari",
    "Karur",
    "Krishnagiri",
    "Madurai",
    "Mayiladuthurai",
    "Nagapattinam",
    "Namakkal",
    "Nilgiris",
    "Perambalur",
    "Pudukkottai",
    "Ramanathapuram",
    "Ranipet",
    "Salem",
    "Sivaganga",
    "Tenkasi",
    "Thanjavur",
    "Theni",
    "Thoothukudi",
    "Tiruchirappalli",
    "Tirunelveli",
    "Tirupathur",
    "Tiruppur",
    "Tiruvallur",
    "Tiruvannamalai",
    "Tiruvarur",
    "Vellore",
    "Viluppuram",
    "Virudhunagar",
  ];

  const emptyForm = {
    blockName: "",
    roomNumber: "",
    floorNumber: "",
    seatingCapacity: "",
    availabilityTimings: "",
    facilities: "",
    district: "",
    location: "",
    feePerHour: "",
    approvalRequired: false,
  };

  const [rooms, setRooms] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingRoomId, setEditingRoomId] = useState(null);

  const [selectedFiles, setSelectedFiles] = useState([]);
  const [localPreviewUrls, setLocalPreviewUrls] = useState([]);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [galleryOpen, setGalleryOpen] = useState(false);
  const [galleryImages, setGalleryImages] = useState([]);
  const [galleryTitle, setGalleryTitle] = useState("");
  const [galleryIndex, setGalleryIndex] = useState(0);
  const [zoomStyle, setZoomStyle] = useState({
    backgroundImage: "",
    backgroundPosition: "center",
    opacity: 0,
  });

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    if (role !== "ADMIN") {
      alert("Access denied. Admin only.");
      navigate("/login");
      return;
    }

    fetchRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate, token, role]);

  useEffect(() => {
    const previewUrls = selectedFiles.map((file) => URL.createObjectURL(file));
    setLocalPreviewUrls(previewUrls);

    return () => {
      previewUrls.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [selectedFiles]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (!galleryOpen) return;

      if (e.key === "Escape") closeGallery();
      if (e.key === "ArrowLeft" && galleryImages.length > 1) showPrevImage();
      if (e.key === "ArrowRight" && galleryImages.length > 1) showNextImage();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [galleryOpen, galleryImages.length, galleryIndex]);

  const getErrorMessage = async (res, fallbackMessage) => {
    try {
      const text = await res.text();
      if (!text) return fallbackMessage;

      try {
        const data = JSON.parse(text);
        return data.message || data.error || text || fallbackMessage;
      } catch {
        return text;
      }
    } catch {
      return fallbackMessage;
    }
  };

  const normalizeImagePath = (url) => {
    if (!url || typeof url !== "string") return "";

    const cleaned = url.trim().replace(/\\/g, "/");

    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
      return cleaned;
    }

    if (cleaned.startsWith("/")) {
      return `${API_BASE_URL}${cleaned}`;
    }

    return `${API_BASE_URL}/${cleaned}`;
  };

  const fetchRooms = async () => {
    try {
      setLoading(true);
      setError("");

      const res = await fetch(`${API_BASE_URL}/api/rooms`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        throw new Error(await getErrorMessage(res, "Failed to fetch rooms"));
      }

      const data = await res.json();
      setRooms(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Error fetching rooms");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files || []);
    setSelectedFiles(files);
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingRoomId(null);
    setSelectedFiles([]);
    setLocalPreviewUrls([]);
  };

  const validateForm = () => {
    if (
      !form.blockName.trim() ||
      !form.roomNumber.trim() ||
      !form.floorNumber.trim() ||
      !form.seatingCapacity ||
      !form.availabilityTimings.trim() ||
      !form.facilities.trim() ||
      !form.district.trim() ||
      !form.location.trim() ||
      form.feePerHour === ""
    ) {
      return "Please fill all fields including fee per hour.";
    }

    if (Number(form.seatingCapacity) <= 0) {
      return "Seating capacity must be greater than 0.";
    }

    if (Number(form.feePerHour) < 0) {
      return "Fee per hour cannot be negative.";
    }

    return null;
  };

  const buildFormData = () => {
    const formData = new FormData();

    formData.append("blockName", form.blockName.trim());
    formData.append("roomNumber", form.roomNumber.trim());
    formData.append("floorNumber", form.floorNumber.trim());
    formData.append("seatingCapacity", String(Number(form.seatingCapacity)));
    formData.append("availabilityTimings", form.availabilityTimings.trim());
    formData.append("facilities", form.facilities.trim());
    formData.append("district", form.district.trim());
    formData.append("location", form.location.trim());
    formData.append("feePerHour", String(Number(form.feePerHour)));
    formData.append("approvalRequired", String(Boolean(form.approvalRequired)));

    selectedFiles.forEach((file) => {
      formData.append("images", file);
    });

    return formData;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSubmitting(true);

      const formData = buildFormData();
      const isEditMode = Boolean(editingRoomId);

      const res = await fetch(
        isEditMode
          ? `${API_BASE_URL}/api/rooms/${editingRoomId}`
          : `${API_BASE_URL}/api/rooms`,
        {
          method: isEditMode ? "PUT" : "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        }
      );

      if (!res.ok) {
        throw new Error(
          await getErrorMessage(
            res,
            isEditMode ? "Failed to update room" : "Failed to add room"
          )
        );
      }

      const savedRoom = await res.json();

      if (isEditMode) {
        setRooms((prev) =>
          prev.map((room) => (room.id === editingRoomId ? savedRoom : room))
        );
        setMessage("Room updated successfully.");
      } else {
        setRooms((prev) => [savedRoom, ...prev]);
        setMessage("Room added successfully.");
      }

      resetForm();
    } catch (err) {
      setError(err.message || "Something went wrong");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (room) => {
    setMessage("");
    setError("");
    setEditingRoomId(room.id);

    setForm({
      blockName: room.blockName || "",
      roomNumber: room.roomNumber || "",
      floorNumber: room.floorNumber || "",
      seatingCapacity: room.seatingCapacity ?? "",
      availabilityTimings: room.availabilityTimings || "",
      facilities: room.facilities || "",
      district: room.district || "",
      location: room.location || "",
      feePerHour: room.feePerHour ?? "",
      approvalRequired: Boolean(room.approvalRequired),
    });

    setSelectedFiles([]);
    setLocalPreviewUrls([]);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleCancelEdit = () => {
    resetForm();
    setMessage("Edit cancelled.");
    setError("");
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this room?")) return;

    try {
      setMessage("");
      setError("");

      const res = await fetch(`${API_BASE_URL}/api/rooms/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        throw new Error(await getErrorMessage(res, "Failed to delete room"));
      }

      setRooms((prev) => prev.filter((room) => room.id !== id));

      if (editingRoomId === id) {
        resetForm();
      }

      setMessage("Room deleted successfully.");
    } catch (err) {
      setError(err.message || "Error deleting room");
    }
  };

  const extractRoomImages = (room) => {
    if (!room) return [];

    const urls = [];

    if (Array.isArray(room.images)) {
      room.images.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.roomImages)) {
      room.roomImages.forEach((img) => {
        if (typeof img === "string") {
          urls.push(normalizeImagePath(img));
        } else if (img) {
          urls.push(
            normalizeImagePath(
              img.imageUrl || img.url || img.path || img.image || ""
            )
          );
        }
      });
    }

    if (Array.isArray(room.imageUrls)) {
      room.imageUrls.forEach((img) => {
        urls.push(normalizeImagePath(img));
      });
    }

    if (room.imageUrl) {
      urls.push(normalizeImagePath(room.imageUrl));
    }

    if (room.thumbnailUrl) {
      urls.push(normalizeImagePath(room.thumbnailUrl));
    }

    return [...new Set(urls.filter(Boolean))];
  };

  const getPrimaryImage = (room) => {
    const images = extractRoomImages(room);
    return images.length > 0 ? images[0] : null;
  };

  const openGallery = (room, startIndex = 0) => {
    const images = extractRoomImages(room);
    if (images.length === 0) return;

    setGalleryImages(images);
    setGalleryTitle(`${room.blockName} - ${room.roomNumber}`);
    setGalleryIndex(startIndex);
    setGalleryOpen(true);
    setZoomStyle({
      backgroundImage: `url(${images[startIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const closeGallery = () => {
    setGalleryOpen(false);
    setGalleryImages([]);
    setGalleryTitle("");
    setGalleryIndex(0);
    setZoomStyle({
      backgroundImage: "",
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const showPrevImage = () => {
    const newIndex =
      galleryIndex === 0 ? galleryImages.length - 1 : galleryIndex - 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const showNextImage = () => {
    const newIndex =
      galleryIndex === galleryImages.length - 1 ? 0 : galleryIndex + 1;

    setGalleryIndex(newIndex);
    setZoomStyle((prev) => ({
      ...prev,
      backgroundImage: `url(${galleryImages[newIndex]})`,
      backgroundPosition: "center",
      opacity: 0,
    }));
  };

  const handleThumbnailClick = (index) => {
    setGalleryIndex(index);
    setZoomStyle({
      backgroundImage: `url(${galleryImages[index]})`,
      backgroundPosition: "center",
      opacity: 0,
    });
  };

  const handleZoomMove = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width) * 100;
    const y = ((e.clientY - rect.top) / rect.height) * 100;

    setZoomStyle({
      backgroundImage: `url(${galleryImages[galleryIndex]})`,
      backgroundPosition: `${x}% ${y}%`,
      opacity: 1,
    });
  };

  const handleZoomLeave = () => {
    setZoomStyle((prev) => ({
      ...prev,
      opacity: 0,
      backgroundPosition: "center",
    }));
  };

  const handleImageError = (e) => {
    e.target.style.display = "none";
    const parent = e.target.parentElement;
    if (parent && !parent.querySelector(".image-error-placeholder")) {
      const placeholder = document.createElement("div");
      placeholder.className = "image-error-placeholder";
      placeholder.innerText = "Image not available";
      Object.assign(placeholder.style, {
        width: "100%",
        height: "100%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#64748b",
        fontWeight: "700",
        fontSize: "15px",
        background: "#f1f5f9",
      });
      parent.appendChild(placeholder);
    }
  };

  const previewCountText = useMemo(() => {
    if (selectedFiles.length === 0) return "No images selected";
    if (selectedFiles.length === 1) return "1 image selected";
    return `${selectedFiles.length} images selected`;
  }, [selectedFiles]);

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <div style={styles.topBar}>
          <div>
            <h1 style={styles.heading}>Manage Study Rooms</h1>
            <p style={styles.subText}>
              Add, edit, view and manage all available study rooms.
            </p>
          </div>

          <button style={styles.backButton} onClick={() => navigate("/admin")}>
            Back to Dashboard
          </button>
        </div>

        {message && <div style={styles.successBox}>{message}</div>}
        {error && <div style={styles.errorBox}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.formCard}>
          <div style={styles.formHeader}>
            <h2 style={styles.formTitle}>
              {editingRoomId ? "Update Study Room" : "Add New Study Room"}
            </h2>
            <p style={styles.formSubtitle}>
              {editingRoomId
                ? "Modify room details and save changes."
                : "Fill in the room details carefully."}
            </p>
          </div>

          <div style={styles.formGrid}>
            <div style={styles.fieldGroup}>
              <label style={styles.label}>Block Name</label>
              <input
                name="blockName"
                placeholder="Enter block name"
                value={form.blockName}
                onChange={handleChange}
                style={styles.input}
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Room Number</label>
              <input
                name="roomNumber"
                placeholder="Enter room number"
                value={form.roomNumber}
                onChange={handleChange}
                style={styles.input}
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Floor Number</label>
              <input
                name="floorNumber"
                placeholder="Enter floor number"
                value={form.floorNumber}
                onChange={handleChange}
                style={styles.input}
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Seating Capacity</label>
              <input
                type="number"
                name="seatingCapacity"
                placeholder="Enter capacity"
                value={form.seatingCapacity}
                onChange={handleChange}
                style={styles.input}
                min="1"
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Availability Timings</label>
              <input
                name="availabilityTimings"
                placeholder="Example: 9 AM - 6 PM"
                value={form.availabilityTimings}
                onChange={handleChange}
                style={styles.input}
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Fee Per Hour</label>
              <input
                type="number"
                name="feePerHour"
                placeholder="Enter hourly fee"
                value={form.feePerHour}
                onChange={handleChange}
                style={styles.input}
                min="0"
                step="1"
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>District</label>
              <select
                name="district"
                value={form.district}
                onChange={handleChange}
                style={styles.input}
              >
                <option value="">Select Tamil Nadu District</option>
                {tamilNaduDistricts.map((district) => (
                  <option key={district} value={district}>
                    {district}
                  </option>
                ))}
              </select>
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.label}>Exact Location</label>
              <input
                name="location"
                placeholder="Enter exact location"
                value={form.location}
                onChange={handleChange}
                style={styles.input}
              />
            </div>

            <div style={{ ...styles.fieldGroup, gridColumn: "1 / -1" }}>
              <label style={styles.label}>Facilities</label>
              <textarea
                name="facilities"
                rows="3"
                placeholder="Example: Wi-Fi, AC, Whiteboard, Charging Ports"
                value={form.facilities}
                onChange={handleChange}
                style={styles.textarea}
              />
            </div>

            <div style={styles.fieldGroup}>
              <label style={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  name="approvalRequired"
                  checked={form.approvalRequired}
                  onChange={handleChange}
                />
                <span>Approval Required</span>
              </label>
            </div>

            <div style={{ ...styles.fieldGroup, gridColumn: "1 / -1" }}>
              <label style={styles.label}>
                {editingRoomId
                  ? "Replace Study Room Images"
                  : "Study Room Images"}
              </label>
              <div style={styles.uploadBox}>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  style={styles.fileInput}
                />
                <p style={styles.uploadHint}>
                  {editingRoomId
                    ? `${previewCountText}. If you select images during update, old images will be replaced.`
                    : previewCountText}
                </p>
              </div>
            </div>

            {localPreviewUrls.length > 0 && (
              <div style={styles.previewSection}>
                <div style={styles.previewTitle}>Selected Image Preview</div>
                <div style={styles.previewGrid}>
                  {localPreviewUrls.map((url, index) => (
                    <img
                      key={index}
                      src={url}
                      alt={`Preview ${index + 1}`}
                      style={styles.previewImage}
                    />
                  ))}
                </div>
              </div>
            )}
          </div>

          <div style={styles.actionRow}>
            <button
              type="submit"
              style={{
                ...styles.addButton,
                opacity: submitting ? 0.75 : 1,
                cursor: submitting ? "not-allowed" : "pointer",
              }}
              disabled={submitting}
            >
              {submitting
                ? editingRoomId
                  ? "Updating Room..."
                  : "Adding Room..."
                : editingRoomId
                ? "Update Room"
                : "Add Room"}
            </button>

            {editingRoomId && (
              <button
                type="button"
                onClick={handleCancelEdit}
                style={styles.cancelButton}
              >
                Cancel Edit
              </button>
            )}
          </div>
        </form>

        <div style={styles.listHeader}>
          <h2 style={styles.sectionTitle}>Room List</h2>
          <span style={styles.roomCount}>{rooms.length} rooms</span>
        </div>

        {loading ? (
          <div style={styles.emptyBox}>Loading rooms...</div>
        ) : rooms.length === 0 ? (
          <div style={styles.emptyBox}>No rooms added yet.</div>
        ) : (
          <div style={styles.roomGrid}>
            {rooms.map((room) => {
              const images = extractRoomImages(room);
              const primaryImage = getPrimaryImage(room);

              return (
                <div key={room.id} style={styles.card}>
                  <div
                    style={styles.imageContainer}
                    onClick={() => openGallery(room, 0)}
                    title={
                      images.length > 0
                        ? "Click to view images"
                        : "No image available"
                    }
                  >
                    {primaryImage ? (
                      <>
                        <img
                          src={primaryImage}
                          alt={`${room.blockName} ${room.roomNumber}`}
                          style={styles.cardImage}
                          onError={handleImageError}
                        />
                        {images.length > 1 && (
                          <div style={styles.imageCountBadge}>
                            +{images.length - 1} more
                          </div>
                        )}
                      </>
                    ) : (
                      <div style={styles.noImageBox}>No Image</div>
                    )}
                  </div>

                  <div style={styles.cardTop}>
                    <h3 style={styles.roomTitle}>
                      {room.blockName} - {room.roomNumber}
                    </h3>
                    <span style={styles.feeBadge}>₹{room.feePerHour ?? 0}/hr</span>
                  </div>

                  <div style={styles.infoList}>
                    <p>
                      <strong>Floor:</strong> {room.floorNumber || "-"}
                    </p>
                    <p>
                      <strong>Capacity:</strong> {room.seatingCapacity || "-"}
                    </p>
                    <p>
                      <strong>Availability:</strong> {room.availabilityTimings || "-"}
                    </p>
                    <p>
                      <strong>District:</strong> {room.district || "-"}
                    </p>
                    <p>
                      <strong>Location:</strong> {room.location || "-"}
                    </p>
                    <p>
                      <strong>Facilities:</strong> {room.facilities || "-"}
                    </p>
                    <p>
                      <strong>Approval Required:</strong>{" "}
                      {room.approvalRequired ? "Yes" : "No"}
                    </p>
                  </div>

                  <div style={styles.cardButtons}>
                    <button
                      type="button"
                      style={{
                        ...styles.viewButton,
                        opacity: images.length === 0 ? 0.6 : 1,
                        cursor: images.length === 0 ? "not-allowed" : "pointer",
                      }}
                      onClick={() => openGallery(room, 0)}
                      disabled={images.length === 0}
                    >
                      View Images
                    </button>

                    <button
                      type="button"
                      style={styles.editButton}
                      onClick={() => handleEdit(room)}
                    >
                      Edit
                    </button>

                    <button
                      type="button"
                      style={styles.deleteButton}
                      onClick={() => handleDelete(room.id)}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {galleryOpen && (
        <div style={styles.modalOverlay} onClick={closeGallery}>
          <div style={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <button style={styles.modalClose} onClick={closeGallery}>
              ×
            </button>

            <h3 style={styles.modalTitle}>{galleryTitle}</h3>

            <div
              style={{
                ...styles.productGalleryLayout,
                gridTemplateColumns:
                  galleryImages.length > 1 ? "110px 1fr" : "1fr",
              }}
            >
              {galleryImages.length > 1 && (
                <div style={styles.verticalThumbnailColumn}>
                  {galleryImages.map((img, index) => (
                    <div
                      key={index}
                      style={{
                        ...styles.verticalThumbWrapper,
                        border:
                          index === galleryIndex
                            ? "2px solid #2563eb"
                            : "1px solid #d9d9d9",
                      }}
                      onClick={() => handleThumbnailClick(index)}
                    >
                      <img
                        src={img}
                        alt={`Thumbnail ${index + 1}`}
                        style={styles.verticalThumbnail}
                        onError={handleImageError}
                      />
                    </div>
                  ))}
                </div>
              )}

              <div style={styles.mainGalleryPanel}>
                <div
                  style={styles.mainImageStage}
                  onMouseMove={handleZoomMove}
                  onMouseLeave={handleZoomLeave}
                >
                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      style={styles.arrowLeft}
                      onClick={showPrevImage}
                    >
                      ‹
                    </button>
                  )}

                  <img
                    src={galleryImages[galleryIndex]}
                    alt={`Room ${galleryIndex + 1}`}
                    style={styles.modalImage}
                    onError={handleImageError}
                  />

                  <div
                    style={{
                      ...styles.zoomLens,
                      backgroundImage: zoomStyle.backgroundImage,
                      backgroundPosition: zoomStyle.backgroundPosition,
                      opacity: zoomStyle.opacity,
                    }}
                  />

                  {galleryImages.length > 1 && (
                    <button
                      type="button"
                      style={styles.arrowRight}
                      onClick={showNextImage}
                    >
                      ›
                    </button>
                  )}
                </div>

                <div style={styles.imageCounter}>
                  Image {galleryIndex + 1} of {galleryImages.length}
                </div>

                {galleryImages.length > 1 && (
                  <div style={styles.bottomThumbnailRow}>
                    {galleryImages.map((img, index) => (
                      <img
                        key={index}
                        src={img}
                        alt={`Bottom thumbnail ${index + 1}`}
                        style={{
                          ...styles.bottomThumbnail,
                          border:
                            index === galleryIndex
                              ? "2px solid #2563eb"
                              : "1px solid #ddd",
                        }}
                        onClick={() => handleThumbnailClick(index)}
                        onError={handleImageError}
                      />
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    background: "#f1f5f9",
    padding: "32px 20px",
  },
  container: {
    maxWidth: "1250px",
    margin: "0 auto",
  },
  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "16px",
    marginBottom: "24px",
    flexWrap: "wrap",
  },
  heading: {
    margin: 0,
    fontSize: "48px",
    fontWeight: 800,
    color: "#0f172a",
    lineHeight: 1.1,
  },
  subText: {
    margin: "8px 0 0",
    color: "#64748b",
    fontSize: "16px",
  },
  backButton: {
    background: "#64748b",
    color: "#fff",
    border: "none",
    borderRadius: "12px",
    padding: "14px 18px",
    fontSize: "15px",
    fontWeight: 600,
    cursor: "pointer",
    boxShadow: "0 6px 16px rgba(100,116,139,0.25)",
  },
  successBox: {
    background: "#dcfce7",
    color: "#166534",
    border: "1px solid #86efac",
    padding: "14px 16px",
    borderRadius: "12px",
    marginBottom: "16px",
    fontWeight: 600,
  },
  errorBox: {
    background: "#fee2e2",
    color: "#991b1b",
    border: "1px solid #fca5a5",
    padding: "14px 16px",
    borderRadius: "12px",
    marginBottom: "16px",
    fontWeight: 600,
    whiteSpace: "pre-wrap",
  },
  formCard: {
    background: "#ffffff",
    borderRadius: "20px",
    padding: "28px",
    boxShadow: "0 10px 30px rgba(15,23,42,0.08)",
    marginBottom: "32px",
    border: "1px solid #e2e8f0",
  },
  formHeader: {
    marginBottom: "22px",
  },
  formTitle: {
    margin: 0,
    fontSize: "24px",
    fontWeight: 700,
    color: "#0f172a",
  },
  formSubtitle: {
    margin: "6px 0 0",
    color: "#64748b",
    fontSize: "14px",
  },
  formGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
    gap: "18px",
  },
  fieldGroup: {
    display: "flex",
    flexDirection: "column",
    gap: "8px",
  },
  label: {
    fontSize: "14px",
    fontWeight: 700,
    color: "#334155",
  },
  checkboxLabel: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    fontSize: "14px",
    fontWeight: 700,
    color: "#334155",
    marginTop: "28px",
  },
  input: {
    height: "50px",
    borderRadius: "12px",
    border: "1px solid #cbd5e1",
    padding: "0 14px",
    fontSize: "15px",
    color: "#0f172a",
    background: "#fff",
    outline: "none",
    boxSizing: "border-box",
    width: "100%",
  },
  textarea: {
    minHeight: "90px",
    borderRadius: "12px",
    border: "1px solid #cbd5e1",
    padding: "14px",
    fontSize: "15px",
    color: "#0f172a",
    background: "#fff",
    outline: "none",
    resize: "vertical",
    boxSizing: "border-box",
    width: "100%",
    fontFamily: "inherit",
    lineHeight: 1.5,
  },
  uploadBox: {
    border: "1px dashed #94a3b8",
    borderRadius: "14px",
    padding: "18px",
    background: "#f8fafc",
  },
  fileInput: {
    width: "100%",
    fontSize: "14px",
  },
  uploadHint: {
    margin: "10px 0 0",
    fontSize: "13px",
    color: "#64748b",
  },
  previewSection: {
    gridColumn: "1 / -1",
    background: "#f8fafc",
    border: "1px solid #e2e8f0",
    borderRadius: "16px",
    padding: "18px",
  },
  previewTitle: {
    fontWeight: 700,
    marginBottom: "12px",
    color: "#0f172a",
  },
  previewGrid: {
    display: "flex",
    flexWrap: "wrap",
    gap: "12px",
  },
  previewImage: {
    width: "96px",
    height: "96px",
    objectFit: "cover",
    borderRadius: "12px",
    border: "1px solid #cbd5e1",
  },
  actionRow: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
    marginTop: "22px",
  },
  addButton: {
    flex: 1,
    minWidth: "180px",
    height: "54px",
    border: "none",
    borderRadius: "14px",
    background: "linear-gradient(135deg, #2563eb, #1d4ed8)",
    color: "#fff",
    fontSize: "17px",
    fontWeight: 700,
    boxShadow: "0 10px 22px rgba(37,99,235,0.22)",
  },
  cancelButton: {
    minWidth: "150px",
    height: "54px",
    border: "1px solid #cbd5e1",
    borderRadius: "14px",
    background: "#fff",
    color: "#0f172a",
    fontSize: "15px",
    fontWeight: 700,
    cursor: "pointer",
  },
  listHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "12px",
    marginBottom: "18px",
    flexWrap: "wrap",
  },
  sectionTitle: {
    margin: 0,
    fontSize: "34px",
    fontWeight: 800,
    color: "#0f172a",
  },
  roomCount: {
    background: "#e2e8f0",
    color: "#334155",
    padding: "8px 14px",
    borderRadius: "999px",
    fontSize: "14px",
    fontWeight: 700,
  },
  emptyBox: {
    background: "#fff",
    border: "1px solid #e2e8f0",
    borderRadius: "16px",
    padding: "30px",
    textAlign: "center",
    color: "#64748b",
    fontSize: "16px",
  },
  roomGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
    gap: "22px",
  },
  card: {
    background: "#fff",
    borderRadius: "18px",
    padding: "18px",
    boxShadow: "0 10px 24px rgba(15,23,42,0.08)",
    border: "1px solid #e2e8f0",
  },
  imageContainer: {
    position: "relative",
    width: "100%",
    height: "230px",
    marginBottom: "16px",
    borderRadius: "16px",
    overflow: "hidden",
    cursor: "pointer",
    backgroundColor: "#f1f5f9",
  },
  cardImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
    display: "block",
  },
  imageCountBadge: {
    position: "absolute",
    right: "12px",
    bottom: "12px",
    backgroundColor: "rgba(15,23,42,0.85)",
    color: "#fff",
    padding: "6px 10px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: 700,
  },
  noImageBox: {
    width: "100%",
    height: "100%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#64748b",
    fontWeight: 700,
    fontSize: "15px",
  },
  cardTop: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "12px",
    marginBottom: "12px",
  },
  roomTitle: {
    margin: 0,
    fontSize: "20px",
    fontWeight: 800,
    color: "#0f172a",
  },
  feeBadge: {
    background: "#dbeafe",
    color: "#1d4ed8",
    padding: "8px 12px",
    borderRadius: "999px",
    fontSize: "13px",
    fontWeight: 800,
    whiteSpace: "nowrap",
  },
  infoList: {
    color: "#334155",
    fontSize: "14px",
    lineHeight: 1.7,
    marginBottom: "16px",
    wordBreak: "break-word",
  },
  cardButtons: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },
  viewButton: {
    flex: 1,
    minWidth: "90px",
    height: "46px",
    backgroundColor: "#16a34a",
    color: "#fff",
    border: "none",
    borderRadius: "12px",
    fontWeight: 700,
    fontSize: "14px",
    cursor: "pointer",
  },
  editButton: {
    flex: 1,
    minWidth: "90px",
    height: "46px",
    backgroundColor: "#f59e0b",
    color: "#fff",
    border: "none",
    borderRadius: "12px",
    fontWeight: 700,
    fontSize: "14px",
    cursor: "pointer",
  },
  deleteButton: {
    flex: 1,
    minWidth: "90px",
    height: "46px",
    backgroundColor: "#dc2626",
    color: "#fff",
    border: "none",
    borderRadius: "12px",
    cursor: "pointer",
    fontWeight: 700,
    fontSize: "14px",
  },
  modalOverlay: {
    position: "fixed",
    inset: 0,
    backgroundColor: "rgba(15,23,42,0.78)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 9999,
    padding: "20px",
  },
  modalContent: {
    position: "relative",
    width: "100%",
    maxWidth: "1180px",
    backgroundColor: "#fff",
    borderRadius: "20px",
    padding: "24px",
    maxHeight: "92vh",
    overflowY: "auto",
  },
  modalClose: {
    position: "absolute",
    top: "12px",
    right: "16px",
    background: "transparent",
    border: "none",
    fontSize: "30px",
    cursor: "pointer",
    zIndex: 3,
    color: "#0f172a",
  },
  modalTitle: {
    marginTop: 0,
    marginBottom: "18px",
    paddingRight: "34px",
    fontSize: "24px",
    color: "#0f172a",
  },
  productGalleryLayout: {
    display: "grid",
    gap: "18px",
    alignItems: "start",
  },
  verticalThumbnailColumn: {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
    maxHeight: "560px",
    overflowY: "auto",
    paddingRight: "4px",
  },
  verticalThumbWrapper: {
    width: "88px",
    height: "88px",
    borderRadius: "10px",
    overflow: "hidden",
    background: "#fff",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  verticalThumbnail: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },
  mainGalleryPanel: {
    width: "100%",
  },
  mainImageStage: {
    position: "relative",
    minHeight: "540px",
    backgroundColor: "#f8fafc",
    border: "1px solid #e2e8f0",
    borderRadius: "16px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    overflow: "hidden",
  },
  modalImage: {
    maxWidth: "100%",
    maxHeight: "78vh",
    objectFit: "contain",
    zIndex: 1,
  },
  zoomLens: {
    position: "absolute",
    inset: 0,
    backgroundRepeat: "no-repeat",
    backgroundSize: "180%",
    pointerEvents: "none",
    transition: "opacity 0.15s ease",
    zIndex: 2,
  },
  arrowLeft: {
    position: "absolute",
    left: "16px",
    top: "50%",
    transform: "translateY(-50%)",
    border: "none",
    backgroundColor: "rgba(15,23,42,0.65)",
    color: "#fff",
    width: "46px",
    height: "46px",
    borderRadius: "50%",
    fontSize: "30px",
    cursor: "pointer",
    zIndex: 3,
  },
  arrowRight: {
    position: "absolute",
    right: "16px",
    top: "50%",
    transform: "translateY(-50%)",
    border: "none",
    backgroundColor: "rgba(15,23,42,0.65)",
    color: "#fff",
    width: "46px",
    height: "46px",
    borderRadius: "50%",
    fontSize: "30px",
    cursor: "pointer",
    zIndex: 3,
  },
  imageCounter: {
    textAlign: "center",
    marginTop: "14px",
    fontWeight: 700,
    color: "#334155",
  },
  bottomThumbnailRow: {
    display: "flex",
    gap: "10px",
    marginTop: "16px",
    overflowX: "auto",
    paddingBottom: "4px",
  },
  bottomThumbnail: {
    width: "82px",
    height: "82px",
    objectFit: "cover",
    borderRadius: "10px",
    cursor: "pointer",
    flexShrink: 0,
    background: "#fff",
  },
};