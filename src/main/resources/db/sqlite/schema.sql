DROP TABLE IF EXISTS vet_specialties;
DROP TABLE IF EXISTS vets;
DROP TABLE IF EXISTS specialties;
DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS types;
DROP TABLE IF EXISTS owners;


CREATE TABLE vets (
  id         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  first_name VARCHAR(30),
  last_name  VARCHAR(30)
);
CREATE INDEX vets_last_name ON vets (last_name);

CREATE TABLE specialties (
  id   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  name VARCHAR(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties (
  vet_id       INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL
);

CREATE TABLE types (
  id   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  name VARCHAR(80)
);
CREATE INDEX types_name ON types (name);

CREATE TABLE owners (
  id         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  first_name VARCHAR(30),
  last_name  VARCHAR_IGNORECASE(30),
  address    VARCHAR(255),
  city       VARCHAR(80),
  telephone  VARCHAR(20)
);
CREATE INDEX owners_last_name ON owners (last_name);

CREATE TABLE pets (
  id         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  name       VARCHAR(30),
  birth_date DATE,
  type_id    INTEGER NOT NULL,
  owner_id   INTEGER NOT NULL
);
CREATE INDEX pets_name ON pets (name);

CREATE TABLE visits (
  id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  pet_id      INTEGER NOT NULL,
  visit_date  DATE,
  description VARCHAR(255)
);
CREATE INDEX visits_pet_id ON visits (pet_id);
