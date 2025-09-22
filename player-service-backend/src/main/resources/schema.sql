DROP TABLE IF EXISTS PLAYERS;

-- Create a table from the csv
CREATE TABLE PLAYERS AS SELECT * FROM CSVREAD('Player.csv');

-- Add email column for new player functionality
-- ALTER TABLE PLAYERS ADD COLUMN EMAIL VARCHAR(255);