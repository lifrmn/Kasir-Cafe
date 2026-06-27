package repository

import (
	"context"
	"database/sql"
	"fmt"
	"strings"
	"time"
)

type AuthAuditLog struct {
	ID        int64     `json:"id"`
	Event     string    `json:"event"`
	Username  string    `json:"username"`
	Role      string    `json:"role"`
	Success   bool      `json:"success"`
	IPAddress string    `json:"ip_address"`
	UserAgent string    `json:"user_agent"`
	Detail    string    `json:"detail"`
	CreatedAt time.Time `json:"created_at"`
}

type AuthAuditFilter struct {
	Event    string
	Username string
	DateFrom string
	DateTo   string
	Page     int
	Limit    int
}

type AuthAuditDailyFailedLogin struct {
	Day   time.Time `json:"day"`
	Total int       `json:"total"`
}

type AuthAuditTopIP struct {
	IPAddress string `json:"ip_address"`
	Total     int    `json:"total"`
}

type AuthAuditSummary struct {
	TotalEvents       int                         `json:"total_events"`
	TotalLogin        int                         `json:"total_login"`
	TotalFailedLogin  int                         `json:"total_failed_login"`
	TotalLogout       int                         `json:"total_logout"`
	TotalRefresh      int                         `json:"total_refresh"`
	FailedLoginPerDay []AuthAuditDailyFailedLogin `json:"failed_login_per_day"`
	TopIPAddresses    []AuthAuditTopIP            `json:"top_ip_addresses"`
}

type AuthAuditRepository interface {
	Create(ctx context.Context, log AuthAuditLog) error
	List(ctx context.Context, filter AuthAuditFilter) ([]AuthAuditLog, int, error)
	ListAll(ctx context.Context, filter AuthAuditFilter) ([]AuthAuditLog, error)
	Summary(ctx context.Context, days int) (AuthAuditSummary, error)
	DeleteOlderThan(ctx context.Context, days int) (int64, error)
}

type authAuditRepository struct {
	db *sql.DB
}

func NewAuthAuditRepository(db *sql.DB) AuthAuditRepository {
	return &authAuditRepository{db: db}
}

func (r *authAuditRepository) Create(ctx context.Context, logData AuthAuditLog) error {
	_, err := r.db.ExecContext(ctx, `
		INSERT INTO auth_audit_logs (event, username, role, success, ip_address, user_agent, detail)
		VALUES ($1, $2, $3, $4, $5, $6, $7)`,
		strings.TrimSpace(logData.Event),
		normalizeNullable(logData.Username),
		normalizeNullable(logData.Role),
		logData.Success,
		normalizeNullable(logData.IPAddress),
		normalizeNullable(logData.UserAgent),
		normalizeNullable(logData.Detail),
	)
	return err
}

func (r *authAuditRepository) List(ctx context.Context, filter AuthAuditFilter) ([]AuthAuditLog, int, error) {
	where := make([]string, 0)
	args := make([]interface{}, 0)
	idx := 1

	if strings.TrimSpace(filter.Event) != "" {
		where = append(where, fmt.Sprintf("event = $%d", idx))
		args = append(args, strings.TrimSpace(filter.Event))
		idx++
	}

	if strings.TrimSpace(filter.Username) != "" {
		where = append(where, fmt.Sprintf("LOWER(COALESCE(username, '')) LIKE $%d", idx))
		args = append(args, "%"+strings.ToLower(strings.TrimSpace(filter.Username))+"%")
		idx++
	}

	if strings.TrimSpace(filter.DateFrom) != "" {
		where = append(where, fmt.Sprintf("created_at >= $%d::timestamptz", idx))
		args = append(args, strings.TrimSpace(filter.DateFrom))
		idx++
	}

	if strings.TrimSpace(filter.DateTo) != "" {
		where = append(where, fmt.Sprintf("created_at <= $%d::timestamptz", idx))
		args = append(args, strings.TrimSpace(filter.DateTo))
		idx++
	}

	whereClause := ""
	if len(where) > 0 {
		whereClause = " WHERE " + strings.Join(where, " AND ")
	}

	countQuery := "SELECT COUNT(1) FROM auth_audit_logs" + whereClause
	var total int
	if err := r.db.QueryRowContext(ctx, countQuery, args...).Scan(&total); err != nil {
		return nil, 0, err
	}

	page := filter.Page
	if page < 1 {
		page = 1
	}
	limit := filter.Limit
	if limit <= 0 {
		limit = 20
	}
	offset := (page - 1) * limit

	query := "SELECT id, event, COALESCE(username, ''), COALESCE(role, ''), success, COALESCE(ip_address, ''), COALESCE(user_agent, ''), COALESCE(detail, ''), created_at FROM auth_audit_logs" + whereClause + fmt.Sprintf(" ORDER BY created_at DESC LIMIT $%d OFFSET $%d", idx, idx+1)
	queryArgs := append(args, limit, offset)

	rows, err := r.db.QueryContext(ctx, query, queryArgs...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	logs := make([]AuthAuditLog, 0)
	for rows.Next() {
		var item AuthAuditLog
		if err := rows.Scan(&item.ID, &item.Event, &item.Username, &item.Role, &item.Success, &item.IPAddress, &item.UserAgent, &item.Detail, &item.CreatedAt); err != nil {
			return nil, 0, err
		}
		logs = append(logs, item)
	}

	return logs, total, rows.Err()
}

func (r *authAuditRepository) buildFilter(filter AuthAuditFilter) (string, []interface{}, int) {
	where := make([]string, 0)
	args := make([]interface{}, 0)
	idx := 1

	if strings.TrimSpace(filter.Event) != "" {
		where = append(where, fmt.Sprintf("event = $%d", idx))
		args = append(args, strings.TrimSpace(filter.Event))
		idx++
	}

	if strings.TrimSpace(filter.Username) != "" {
		where = append(where, fmt.Sprintf("LOWER(COALESCE(username, '')) LIKE $%d", idx))
		args = append(args, "%"+strings.ToLower(strings.TrimSpace(filter.Username))+"%")
		idx++
	}

	if strings.TrimSpace(filter.DateFrom) != "" {
		where = append(where, fmt.Sprintf("created_at >= $%d::timestamptz", idx))
		args = append(args, strings.TrimSpace(filter.DateFrom))
		idx++
	}

	if strings.TrimSpace(filter.DateTo) != "" {
		where = append(where, fmt.Sprintf("created_at <= $%d::timestamptz", idx))
		args = append(args, strings.TrimSpace(filter.DateTo))
		idx++
	}

	whereClause := ""
	if len(where) > 0 {
		whereClause = " WHERE " + strings.Join(where, " AND ")
	}

	return whereClause, args, idx
}

func (r *authAuditRepository) ListAll(ctx context.Context, filter AuthAuditFilter) ([]AuthAuditLog, error) {
	whereClause, args, _ := r.buildFilter(filter)

	query := "SELECT id, event, COALESCE(username, ''), COALESCE(role, ''), success, COALESCE(ip_address, ''), COALESCE(user_agent, ''), COALESCE(detail, ''), created_at FROM auth_audit_logs" + whereClause + " ORDER BY created_at DESC"

	rows, err := r.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	logs := make([]AuthAuditLog, 0)
	for rows.Next() {
		var item AuthAuditLog
		if err := rows.Scan(&item.ID, &item.Event, &item.Username, &item.Role, &item.Success, &item.IPAddress, &item.UserAgent, &item.Detail, &item.CreatedAt); err != nil {
			return nil, err
		}
		logs = append(logs, item)
	}

	return logs, rows.Err()
}

func (r *authAuditRepository) Summary(ctx context.Context, days int) (AuthAuditSummary, error) {
	if days <= 0 {
		days = 30
	}

	var summary AuthAuditSummary
	err := r.db.QueryRowContext(ctx, `
		SELECT
			COUNT(1),
			COUNT(1) FILTER (WHERE event = 'login'),
			COUNT(1) FILTER (WHERE event = 'login' AND success = false),
			COUNT(1) FILTER (WHERE event = 'logout'),
			COUNT(1) FILTER (WHERE event = 'refresh')
		FROM auth_audit_logs
		WHERE created_at >= NOW() - make_interval(days => $1)`,
		days,
	).Scan(
		&summary.TotalEvents,
		&summary.TotalLogin,
		&summary.TotalFailedLogin,
		&summary.TotalLogout,
		&summary.TotalRefresh,
	)
	if err != nil {
		return AuthAuditSummary{}, err
	}

	failedRows, err := r.db.QueryContext(ctx, `
		SELECT date_trunc('day', created_at) AS day, COUNT(1)
		FROM auth_audit_logs
		WHERE event = 'login' AND success = false
		  AND created_at >= NOW() - make_interval(days => $1)
		GROUP BY day
		ORDER BY day DESC`,
		days,
	)
	if err != nil {
		return AuthAuditSummary{}, err
	}
	defer failedRows.Close()

	summary.FailedLoginPerDay = make([]AuthAuditDailyFailedLogin, 0)
	for failedRows.Next() {
		var item AuthAuditDailyFailedLogin
		if err := failedRows.Scan(&item.Day, &item.Total); err != nil {
			return AuthAuditSummary{}, err
		}
		summary.FailedLoginPerDay = append(summary.FailedLoginPerDay, item)
	}
	if err := failedRows.Err(); err != nil {
		return AuthAuditSummary{}, err
	}

	ipRows, err := r.db.QueryContext(ctx, `
		SELECT COALESCE(ip_address, ''), COUNT(1) AS total
		FROM auth_audit_logs
		WHERE created_at >= NOW() - make_interval(days => $1)
		  AND COALESCE(ip_address, '') <> ''
		GROUP BY ip_address
		ORDER BY total DESC
		LIMIT 5`,
		days,
	)
	if err != nil {
		return AuthAuditSummary{}, err
	}
	defer ipRows.Close()

	summary.TopIPAddresses = make([]AuthAuditTopIP, 0)
	for ipRows.Next() {
		var item AuthAuditTopIP
		if err := ipRows.Scan(&item.IPAddress, &item.Total); err != nil {
			return AuthAuditSummary{}, err
		}
		summary.TopIPAddresses = append(summary.TopIPAddresses, item)
	}

	return summary, ipRows.Err()
}

func (r *authAuditRepository) DeleteOlderThan(ctx context.Context, days int) (int64, error) {
	if days <= 0 {
		days = 180
	}

	result, err := r.db.ExecContext(ctx, `
		DELETE FROM auth_audit_logs
		WHERE created_at < NOW() - make_interval(days => $1)`,
		days,
	)
	if err != nil {
		return 0, err
	}

	return result.RowsAffected()
}

func normalizeNullable(value string) interface{} {
	trimmed := strings.TrimSpace(value)
	if trimmed == "" {
		return nil
	}
	return trimmed
}
