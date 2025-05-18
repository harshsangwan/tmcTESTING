#!/bin/bash
echo "=== Checking role entries in database ==="
docker-compose exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_auth; SELECT * FROM roles;"