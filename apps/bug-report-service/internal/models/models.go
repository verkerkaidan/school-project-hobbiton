package models

import "time"

// BugReportStatus enumerates the lifecycle states of a bug report.
type BugReportStatus string

const (
	StatusOpen   BugReportStatus = "OPEN"
	StatusClosed BugReportStatus = "CLOSED"
)

// BugReport is the persisted bug report record.
type BugReport struct {
	ID          string          `json:"id"`
	Title       string          `json:"title"`
	Description string          `json:"description"`
	Status      BugReportStatus `json:"status"`
	CreatedAt   time.Time       `json:"createdAt"`
	ClosedAt    *time.Time      `json:"closedAt"`
}

// CreateBugReportRequest is the inbound payload from the frontend.
type CreateBugReportRequest struct {
	Title       string `json:"title"`
	Description string `json:"description"`
}
