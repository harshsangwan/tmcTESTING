#!/bin/bash
echo "=== Fixing Auth Service Roles ==="

# Connect to MySQL and insert the required roles
docker-compose exec mysql mysql -uroot -pletmEc0de#8 task_management_auth << 'EOF'
-- Check if we already have records to avoid duplicates
SELECT COUNT(*) FROM roles;

-- Insert roles if none exist
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_MEMBER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_MANAGER');

-- Verify roles were created
SELECT * FROM roles;
EOF

echo "Roles have been initialized in the database"