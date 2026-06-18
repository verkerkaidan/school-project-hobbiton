package handlers

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"regexp"
	"testing"
	"time"

	"bug-report-service/internal/config"
	"bug-report-service/internal/models"

	sqlmock "github.com/DATA-DOG/go-sqlmock"
	"github.com/golang-jwt/jwt/v5"
)

func newTestHandler(t *testing.T) (*Handler, sqlmock.Sqlmock) {
	t.Helper()
	db, mock, err := sqlmock.New()
	if err != nil {
		t.Fatalf("sqlmock.New: %v", err)
	}
	t.Cleanup(func() { db.Close() })
	cfg := &config.Config{JWTSecret: "test-secret", AllowedOrigins: "http://localhost"}
	return New(db, cfg), mock
}

func makeTestToken(secret string) string {
	claims := jwt.MapClaims{"sub": "u1", "exp": time.Now().Add(time.Hour).Unix()}
	token, _ := jwt.NewWithClaims(jwt.SigningMethodHS256, claims).SignedString([]byte(secret))
	return token
}

// ---------- createBugReport ----------

func TestCreateBugReport_Success(t *testing.T) {
	h, mock := newTestHandler(t)

	mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO bug_reports`)).
		WillReturnResult(sqlmock.NewResult(1, 1))

	body, _ := json.Marshal(map[string]string{"title": "Crash", "description": "App crashes on launch"})
	req := httptest.NewRequest(http.MethodPost, "/bug-reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer "+makeTestToken("test-secret"))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	h.createBugReport(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d: %s", w.Code, w.Body.String())
	}
	var report models.BugReport
	if err := json.NewDecoder(w.Body).Decode(&report); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if report.Title != "Crash" || report.Description != "App crashes on launch" {
		t.Errorf("unexpected report content: %+v", report)
	}
	if report.Status != models.StatusOpen {
		t.Errorf("expected status OPEN, got %s", report.Status)
	}
	if report.ID == "" {
		t.Error("expected non-empty ID")
	}
	if err := mock.ExpectationsWereMet(); err != nil {
		t.Error(err)
	}
}

func TestCreateBugReport_MissingTitle(t *testing.T) {
	h, _ := newTestHandler(t)

	body, _ := json.Marshal(map[string]string{"title": "", "description": "desc"})
	req := httptest.NewRequest(http.MethodPost, "/bug-reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer "+makeTestToken("test-secret"))
	w := httptest.NewRecorder()

	h.createBugReport(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

func TestCreateBugReport_TrimsWhitespace(t *testing.T) {
	h, mock := newTestHandler(t)

	mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO bug_reports`)).
		WillReturnResult(sqlmock.NewResult(1, 1))

	body, _ := json.Marshal(map[string]string{"title": "  Title  ", "description": "  Desc  "})
	req := httptest.NewRequest(http.MethodPost, "/bug-reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer "+makeTestToken("test-secret"))
	w := httptest.NewRecorder()

	h.createBugReport(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", w.Code)
	}
	var report models.BugReport
	json.NewDecoder(w.Body).Decode(&report)
	if report.Title != "Title" || report.Description != "Desc" {
		t.Errorf("whitespace not trimmed: title=%q desc=%q", report.Title, report.Description)
	}
}

func TestCreateBugReport_Unauthorized(t *testing.T) {
	h, _ := newTestHandler(t)

	body, _ := json.Marshal(map[string]string{"title": "T", "description": "D"})
	req := httptest.NewRequest(http.MethodPost, "/bug-reports", bytes.NewReader(body))
	// No Authorization header
	w := httptest.NewRecorder()

	h.createBugReport(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", w.Code)
	}
}

func TestCreateBugReport_InvalidBody(t *testing.T) {
	h, _ := newTestHandler(t)

	req := httptest.NewRequest(http.MethodPost, "/bug-reports", bytes.NewReader([]byte("not json")))
	req.Header.Set("Authorization", "Bearer "+makeTestToken("test-secret"))
	w := httptest.NewRecorder()

	h.createBugReport(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}

// ---------- listBugReports ----------

func TestListBugReports_All(t *testing.T) {
	h, mock := newTestHandler(t)

	now := time.Now().UTC()
	rows := sqlmock.NewRows([]string{"id", "title", "description", "status", "created_at", "closed_at"}).
		AddRow("id-1", "Bug A", "desc a", "OPEN", now, nil).
		AddRow("id-2", "Bug B", "desc b", "CLOSED", now, &now)

	mock.ExpectQuery(regexp.QuoteMeta(`SELECT id, title, description, status, created_at, closed_at`)).
		WillReturnRows(rows)

	req := httptest.NewRequest(http.MethodGet, "/bug-reports", nil)
	w := httptest.NewRecorder()

	h.listBugReports(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", w.Code, w.Body.String())
	}
	var reports []models.BugReport
	json.NewDecoder(w.Body).Decode(&reports)
	if len(reports) != 2 {
		t.Fatalf("expected 2 reports, got %d", len(reports))
	}
	if err := mock.ExpectationsWereMet(); err != nil {
		t.Error(err)
	}
}

func TestListBugReports_FilterByStatus(t *testing.T) {
	h, mock := newTestHandler(t)

	now := time.Now().UTC()
	rows := sqlmock.NewRows([]string{"id", "title", "description", "status", "created_at", "closed_at"}).
		AddRow("id-1", "Bug A", "desc a", "OPEN", now, nil)

	mock.ExpectQuery(regexp.QuoteMeta(`WHERE status = $1`)).
		WithArgs("OPEN").
		WillReturnRows(rows)

	req := httptest.NewRequest(http.MethodGet, "/bug-reports?status=OPEN", nil)
	w := httptest.NewRecorder()

	h.listBugReports(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
	var reports []models.BugReport
	json.NewDecoder(w.Body).Decode(&reports)
	if len(reports) != 1 {
		t.Fatalf("expected 1 report, got %d", len(reports))
	}
	if err := mock.ExpectationsWereMet(); err != nil {
		t.Error(err)
	}
}

func TestListBugReports_EmptyResult(t *testing.T) {
	h, mock := newTestHandler(t)

	rows := sqlmock.NewRows([]string{"id", "title", "description", "status", "created_at", "closed_at"})
	mock.ExpectQuery(regexp.QuoteMeta(`SELECT id, title, description, status, created_at, closed_at`)).
		WillReturnRows(rows)

	req := httptest.NewRequest(http.MethodGet, "/bug-reports", nil)
	w := httptest.NewRecorder()

	h.listBugReports(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
	var reports []models.BugReport
	json.NewDecoder(w.Body).Decode(&reports)
	if reports == nil || len(reports) != 0 {
		t.Fatalf("expected empty slice, got %v", reports)
	}
}

// ---------- closeBugReport ----------

func TestCloseBugReport_Success(t *testing.T) {
	h, mock := newTestHandler(t)

	now := time.Now().UTC()
	mock.ExpectExec(regexp.QuoteMeta(`UPDATE bug_reports`)).
		WithArgs(sqlmock.AnyArg(), "report-1").
		WillReturnResult(sqlmock.NewResult(0, 1))

	mock.ExpectQuery(regexp.QuoteMeta(`SELECT id, title, description, status, created_at, closed_at`)).
		WithArgs("report-1").
		WillReturnRows(sqlmock.NewRows([]string{"id", "title", "description", "status", "created_at", "closed_at"}).
			AddRow("report-1", "Bug A", "desc", "CLOSED", now, &now))

	req := httptest.NewRequest(http.MethodPut, "/bug-reports/report-1/close", nil)
	req.SetPathValue("id", "report-1")
	w := httptest.NewRecorder()

	h.closeBugReport(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", w.Code, w.Body.String())
	}
	var report models.BugReport
	json.NewDecoder(w.Body).Decode(&report)
	if report.Status != models.StatusClosed {
		t.Errorf("expected CLOSED, got %s", report.Status)
	}
	if report.ClosedAt == nil {
		t.Error("expected non-nil closedAt")
	}
	if err := mock.ExpectationsWereMet(); err != nil {
		t.Error(err)
	}
}

func TestCloseBugReport_NotFound(t *testing.T) {
	h, mock := newTestHandler(t)

	mock.ExpectExec(regexp.QuoteMeta(`UPDATE bug_reports`)).
		WithArgs(sqlmock.AnyArg(), "missing-id").
		WillReturnResult(sqlmock.NewResult(0, 0)) // 0 rows affected

	req := httptest.NewRequest(http.MethodPut, "/bug-reports/missing-id/close", nil)
	req.SetPathValue("id", "missing-id")
	w := httptest.NewRecorder()

	h.closeBugReport(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
	if err := mock.ExpectationsWereMet(); err != nil {
		t.Error(err)
	}
}

func TestCloseBugReport_MissingID(t *testing.T) {
	h, _ := newTestHandler(t)

	req := httptest.NewRequest(http.MethodPut, "/bug-reports//close", nil)
	// PathValue "id" not set — simulates missing id
	w := httptest.NewRecorder()

	h.closeBugReport(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", w.Code)
	}
}
