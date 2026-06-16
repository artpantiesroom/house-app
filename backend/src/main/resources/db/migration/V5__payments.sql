CREATE TABLE payments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  resident_profile_id INTEGER NOT NULL,
  apartment_id INTEGER,
  type VARCHAR(30) NOT NULL CHECK (type IN ('RENT', 'UTILITIES', 'MAINTENANCE', 'SECURITY', 'PARKING', 'OTHER')),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
  amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
  currency VARCHAR(10) NOT NULL DEFAULT 'UAH' CHECK (currency IN ('UAH')),
  period_year INTEGER NOT NULL CHECK (period_year BETWEEN 2000 AND 2100),
  period_month INTEGER NOT NULL CHECK (period_month BETWEEN 1 AND 12),
  title_uk VARCHAR(160) NOT NULL,
  title_en VARCHAR(160),
  description_uk VARCHAR(1000),
  description_en VARCHAR(1000),
  due_date DATE NOT NULL,
  paid_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  created_by_user_id INTEGER,
  CONSTRAINT fk_payments_resident_profile FOREIGN KEY (resident_profile_id) REFERENCES resident_profiles(id) ON DELETE CASCADE,
  CONSTRAINT fk_payments_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE SET NULL,
  CONSTRAINT fk_payments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_payments_resident_profile_id ON payments(resident_profile_id);
CREATE INDEX idx_payments_apartment_id ON payments(apartment_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_type ON payments(type);
CREATE INDEX idx_payments_period ON payments(period_year, period_month);
CREATE INDEX idx_payments_due_date ON payments(due_date);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);
