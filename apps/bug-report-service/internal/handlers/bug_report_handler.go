package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strings"
	"time"

	"bug-report-service/internal/models"

	"github.com/google/uuid"
)

// createBugReport handles POST /bug-reports.
// Requires a valid JWT (user must be logged in), but no user identity is stored.
func (h *Handler) createBugReport(w http.ResponseWriter, r *http.Request) {
	if !h.requireAuth(w, r) {
		return
	}

	var req models.CreateBugReportRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	req.Title = strings.TrimSpace(req.Title)
	req.Description = strings.TrimSpace(req.Description)

	if req.Title == "" || req.Description == "" {
		http.Error(w, "title and description are required", http.StatusBadRequest)
		return
	}

	id := uuid.New().String()
	createdAt := time.Now().UTC()

	_, err := h.db.Exec(
		`INSERT INTO bug_reports (id, title, description, status, created_at)
		 VALUES ($1, $2, $3, 'OPEN', $4)`,
		id, req.Title, req.Description, createdAt,
	)
	if err != nil {
		http.Error(w, "failed to save bug report", http.StatusInternalServerError)
		return
	}

	report := models.BugReport{
		ID:          id,
		Title:       req.Title,
		Description: req.Description,
		Status:      models.StatusOpen,
		CreatedAt:   createdAt,
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(report)
}

// listBugReports handles GET /bug-reports.
// Optional query param: ?status=OPEN|CLOSED  (omit for all reports)
func (h *Handler) listBugReports(w http.ResponseWriter, r *http.Request) {
	statusFilter := strings.ToUpper(strings.TrimSpace(r.URL.Query().Get("status")))

	var (
		rows *sql.Rows
		err  error
	)

	switch statusFilter {
	case "OPEN", "CLOSED":
		rows, err = h.db.Query(
			`SELECT id, title, description, status, created_at, closed_at
			 FROM bug_reports
			 WHERE status = $1
			 ORDER BY created_at DESC`,
			statusFilter,
		)
	default:
		rows, err = h.db.Query(
			`SELECT id, title, description, status, created_at, closed_at
			 FROM bug_reports
			 ORDER BY created_at DESC`,
		)
	}

	if err != nil {
		http.Error(w, "failed to fetch bug reports", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	reports := []models.BugReport{}
	for rows.Next() {
		var rep models.BugReport
		var closedAt *time.Time
		if err := rows.Scan(&rep.ID, &rep.Title, &rep.Description, &rep.Status, &rep.CreatedAt, &closedAt); err != nil {
			http.Error(w, "failed to read bug reports", http.StatusInternalServerError)
			return
		}
		rep.ClosedAt = closedAt
		reports = append(reports, rep)
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(reports)
}

// closeBugReport handles PUT /bug-reports/{id}/close.
// Sets the report status to CLOSED and records closed_at timestamp.
func (h *Handler) closeBugReport(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	if id == "" {
		http.Error(w, "missing report id", http.StatusBadRequest)
		return
	}

	closedAt := time.Now().UTC()
	result, err := h.db.Exec(
		`UPDATE bug_reports
		 SET status = 'CLOSED', closed_at = $1
		 WHERE id = $2 AND status = 'OPEN'`,
		closedAt, id,
	)
	if err != nil {
		http.Error(w, "failed to close bug report", http.StatusInternalServerError)
		return
	}

	rows, _ := result.RowsAffected()
	if rows == 0 {
		http.Error(w, "report not found or already closed", http.StatusNotFound)
		return
	}

	var report models.BugReport
	err = h.db.QueryRow(
		`SELECT id, title, description, status, created_at, closed_at
		 FROM bug_reports WHERE id = $1`,
		id,
	).Scan(&report.ID, &report.Title, &report.Description, &report.Status, &report.CreatedAt, &report.ClosedAt)
	if err != nil {
		http.Error(w, "failed to fetch updated report", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(report)
}
