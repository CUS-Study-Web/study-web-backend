.PHONY: up infra dev
up:
	@echo "Building Docker image..."
	docker build -t study-web-backend .

infra:
	@echo "Starting infra..."
	docker compose up -d

dev:
	@echo "Running at local..."
	./mvnw spring-boot:run
