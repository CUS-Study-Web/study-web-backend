.PHONY: up infra dev
up: infra
	@echo "Running at local..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

infra:
	@echo "Starting infra..."
	@if command -v docker >/dev/null 2>&1; then docker compose up -d; else podman compose up -d; fi

dev:
	@echo "Running at local..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
