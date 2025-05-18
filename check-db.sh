#!/bin/bash
echo "=== Checking Auth Service Database ==="

# Check and create default roles if they don't exist
docker-compose exec mysql mysql -uroot -pletmEc0de#8 task_management_auth << 'EOF'
-- Check roles table
SELECT * FROM roles;

-- Create default roles if needed
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_MANAGER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_MEMBER');

-- Check roles after insert
SELECT * FROM roles;

-- Check users table structure and constraints
DESCRIBE users;

-- Check if any users exist
SELECT COUNT(*) FROM users;
EOF

echo "Database check complete."