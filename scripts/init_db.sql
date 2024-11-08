CREATE TABLE inventory(
  id serial PRIMARY KEY,
  name varchar(255) NOT NULL,
  quantity int NOT NULL,
  pricePerPound int NOT NULL
);

