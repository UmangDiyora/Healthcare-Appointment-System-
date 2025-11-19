-- Healthcare Database Initialization Script
-- This script creates the database schema if it doesn't exist

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE healthcare TO healthcare;

-- Database is created by Docker environment variable
-- Tables will be created by Hibernate/JPA on first run
