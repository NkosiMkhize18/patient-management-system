terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  required_version = ">= 1.4.0"
}

provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  s3_force_path_style         = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  endpoints {
    ec2             = "http://localhost:4566"
    ecs             = "http://localhost:4566"
    rds             = "http://localhost:4566"
    logs            = "http://localhost:4566"
    secretsmanager  = "http://localhost:4566"
    iam             = "http://localhost:4566"
    cloudwatch      = "http://localhost:4566"
  }
}

# VPC
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  tags = {
    Name = "LocalVPC"
  }
}

resource "aws_subnet" "subnet_a" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
  availability_zone = "us-east-1a"
}

resource "aws_subnet" "subnet_b" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.2.0/24"
  availability_zone = "us-east-1b"
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "auth-cluster"
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "auth" {
  name              = "/ecs/auth-service"
  retention_in_days = 1
}

# IAM Roles for ECS
resource "aws_iam_role" "ecs_task_execution" {
  name = "ecsTaskExecutionRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_attach" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Secrets Manager
resource "aws_secretsmanager_secret" "db_password" {
  name = "admin_user"
}

resource "aws_secretsmanager_secret_version" "db_password_value" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = jsonencode({ password = "securepassword123" })
}

# RDS for auth-service
resource "aws_db_instance" "auth" {
  identifier         = "auth-service-db"
  engine             = "postgres"
  engine_version     = "13.4"
  instance_class     = "db.t3.micro"
  allocated_storage  = 20
  username           = "admin_user"
  password           = "securepassword123"
  db_name            = "auth-service-db"
  vpc_security_group_ids = []
  skip_final_snapshot = true
}

# ECS Task Definition
resource "aws_ecs_task_definition" "auth_task" {
  family                   = "auth-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn

  container_definitions = jsonencode([
    {
      name      = "auth-service",
      image     = "auth-service",
      essential = true,
      environment = [
        { name = "JWT_SECRET", value = "Y2hhVEc3aHJnb0hYTzMyZ2ZqVkpiZ1RkZG93YWxrUkM=" },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.auth.address}:${aws_db_instance.auth.port}/auth-service-db" },
        { name = "SPRING_DATASOURCE_USERNAME", value = "admin_user" },
        { name = "SPRING_DATASOURCE_PASSWORD", value = "securepassword123" },
        { name = "SPRING_JPA_HIBERNATE_DDL_AUTO", value = "update" },
        { name = "SPRING_SQL_INIT_MODE", value = "always" }
      ],
      portMappings = [
        {
          containerPort = 4005,
          hostPort      = 4005,
          protocol      = "tcp"
        }
      ],
      logConfiguration = {
        logDriver = "awslogs",
        options = {
          awslogs-group         = "/ecs/auth-service",
          awslogs-region        = "us-east-1",
          awslogs-stream-prefix = "auth"
        }
      }
    }
  ])
}

# ECS Service
resource "aws_ecs_service" "auth" {
  name            = "auth-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.auth_task.arn
  launch_type     = "FARGATE"
  desired_count   = 1

  network_configuration {
    subnets         = [aws_subnet.subnet_a.id, aws_subnet.subnet_b.id]
    assign_public_ip = true
    security_groups = []
  }

  depends_on = [aws_db_instance.auth]
}