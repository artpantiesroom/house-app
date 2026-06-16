CREATE TABLE maintenance_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  resident_profile_id INTEGER NOT NULL,
  apartment_id INTEGER,
  title VARCHAR(160) NOT NULL,
  description VARCHAR(3000) NOT NULL,
  category VARCHAR(30) NOT NULL CHECK (category IN ('PLUMBING', 'ELECTRICITY', 'HEATING', 'INTERNET', 'ELEVATOR', 'CLEANING', 'SECURITY', 'OTHER')),
  priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
  status VARCHAR(30) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'IN_PROGRESS', 'WAITING_RESIDENT', 'RESOLVED', 'CANCELLED')),
  admin_response VARCHAR(3000),
  internal_notes VARCHAR(3000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP,
  CONSTRAINT fk_maintenance_requests_resident_profile FOREIGN KEY (resident_profile_id) REFERENCES resident_profiles(id) ON DELETE CASCADE,
  CONSTRAINT fk_maintenance_requests_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE SET NULL
);

CREATE INDEX idx_maintenance_requests_resident_profile_id ON maintenance_requests(resident_profile_id);
CREATE INDEX idx_maintenance_requests_apartment_id ON maintenance_requests(apartment_id);
CREATE INDEX idx_maintenance_requests_status ON maintenance_requests(status);
CREATE INDEX idx_maintenance_requests_category ON maintenance_requests(category);
CREATE INDEX idx_maintenance_requests_priority ON maintenance_requests(priority);
CREATE INDEX idx_maintenance_requests_created_at ON maintenance_requests(created_at);
