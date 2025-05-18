#!/bin/bash
echo "=== Examining auth database schema ==="
docker-compose exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_auth; SHOW CREATE TABLE roles; SHOW CREATE TABLE users; SHOW CREATE TABLE user_roles;"