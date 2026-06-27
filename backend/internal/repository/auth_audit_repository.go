package repository

import (
	"context"
	"database/sql"
	"fmt"
	"strings"
	"time"
)

type AuthAuditLog struct {
	ID        int64 `json:"id"`
	Event     string
	Username  string
	Role      string
	Success   bool
	IPAddress string
	UserAgent string
	Detail    string
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

type AuthAuditRepository interface {
	Create(ctx context.Context, log AuthAuditLog) error
	List(ctx context.Context, filter AuthAuditFilter) ([]AuthAuditLog, int, error)
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

func normalizeNullable(value string) interface{} {
	trimmed := strings.TrimSpace(value)
	if trimmed == "" {
		return nil
	}
	return trimmed
}
