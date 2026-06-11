CREATE TABLE apartments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  building_section VARCHAR(50) NOT NULL,
  floor INTEGER NOT NULL CHECK (floor BETWEEN 0 AND 200),
  apartment_number VARCHAR(30) NOT NULL UNIQUE,
  area_sq_m NUMERIC(8,2) NOT NULL CHECK (area_sq_m > 0),
  rooms INTEGER NOT NULL CHECK (rooms BETWEEN 1 AND 20),
  status VARCHAR(30) NOT NULL CHECK (status IN ('OCCUPIED', 'VACANT', 'MAINTENANCE')),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_apartments_apartment_number ON apartments(apartment_number);

CREATE TABLE resident_profiles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL UNIQUE,
  apartment_id INTEGER UNIQUE,
  phone VARCHAR(40),
  emergency_contact_name VARCHAR(120),
  emergency_contact_phone VARCHAR(40),
  avatar_path VARCHAR(255),
  notes VARCHAR(1000),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_resident_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_resident_profiles_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE SET NULL
);

CREATE INDEX idx_resident_profiles_user_id ON resident_profiles(user_id);
CREATE INDEX idx_resident_profiles_apartment_id ON resident_profiles(apartment_id);
