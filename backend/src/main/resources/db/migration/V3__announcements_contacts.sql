CREATE TABLE announcements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title_uk VARCHAR(160) NOT NULL,
  title_en VARCHAR(160),
  body_uk VARCHAR(5000) NOT NULL,
  body_en VARCHAR(5000),
  category VARCHAR(30) NOT NULL CHECK (category IN ('GENERAL', 'MAINTENANCE', 'PAYMENT', 'SECURITY', 'EVENT', 'OTHER')),
  priority VARCHAR(30) NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
  status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
  published_at TIMESTAMP,
  expires_at TIMESTAMP,
  created_by_user_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_announcements_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_announcements_status ON announcements(status);
CREATE INDEX idx_announcements_category ON announcements(category);
CREATE INDEX idx_announcements_published_at ON announcements(published_at);
CREATE INDEX idx_announcements_expires_at ON announcements(expires_at);

CREATE TABLE building_contacts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name_uk VARCHAR(120) NOT NULL,
  name_en VARCHAR(120),
  role_uk VARCHAR(120) NOT NULL,
  role_en VARCHAR(120),
  department_uk VARCHAR(120),
  department_en VARCHAR(120),
  phone VARCHAR(40),
  email VARCHAR(255),
  availability_uk VARCHAR(255),
  availability_en VARCHAR(255),
  sort_order INTEGER NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CHECK (phone IS NOT NULL OR email IS NOT NULL)
);

CREATE INDEX idx_building_contacts_active ON building_contacts(active);
CREATE INDEX idx_building_contacts_sort_order ON building_contacts(sort_order);
