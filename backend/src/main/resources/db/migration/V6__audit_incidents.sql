CREATE TABLE audit_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  actor_user_id INTEGER,
  actor_email VARCHAR(255),
  actor_role VARCHAR(30),
  action VARCHAR(60) NOT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id INTEGER,
  summary VARCHAR(500) NOT NULL,
  metadata_json TEXT,
  ip_address VARCHAR(80),
  user_agent VARCHAR(500),
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_audit_logs_actor_user FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

CREATE TABLE security_incidents (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title VARCHAR(160) NOT NULL,
  description VARCHAR(3000) NOT NULL,
  severity VARCHAR(30) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED', 'FALSE_POSITIVE')),
  category VARCHAR(40) NOT NULL CHECK (category IN ('AUTHENTICATION', 'AUTHORIZATION', 'DATA_ACCESS', 'PAYMENT', 'MAINTENANCE', 'SYSTEM', 'OTHER')),
  reported_by_user_id INTEGER,
  assigned_to_user_id INTEGER,
  related_audit_log_id INTEGER,
  resolution_notes VARCHAR(3000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP,
  CONSTRAINT fk_security_incidents_reported_by FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_security_incidents_assigned_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_security_incidents_audit_log FOREIGN KEY (related_audit_log_id) REFERENCES audit_logs(id) ON DELETE SET NULL
);

CREATE INDEX idx_security_incidents_severity ON security_incidents(severity);
CREATE INDEX idx_security_incidents_status ON security_incidents(status);
CREATE INDEX idx_security_incidents_category ON security_incidents(category);
CREATE INDEX idx_security_incidents_reported_by ON security_incidents(reported_by_user_id);
CREATE INDEX idx_security_incidents_assigned_to ON security_incidents(assigned_to_user_id);
CREATE INDEX idx_security_incidents_created_at ON security_incidents(created_at);
CREATE INDEX idx_security_incidents_resolved_at ON security_incidents(resolved_at);
