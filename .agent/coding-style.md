# Coding Standards

## General Principles
- Always write clean, maintainable, and production-ready code.
- Prioritize readability over clever implementations.
- Keep functions and classes focused on a single responsibility.
- Avoid duplicated logic (DRY).
- Prefer composition over inheritance.
- Never over-engineer simple solutions.

## Architecture
- Follow Clean Architecture.
- Respect the Dependency Inversion Principle.
- Separate code into:
  - Domain
  - Application
  - Infrastructure
  - Presentation (if applicable)
- Never let Infrastructure depend on Presentation.

## Object-Oriented Programming
- Apply OOP whenever it improves maintainability.
- Follow all SOLID principles.
- Use interfaces/abstractions whenever appropriate.
- Encapsulate business logic inside domain objects instead of services whenever possible.
- Avoid God Classes.
- Keep methods small and cohesive.

## SOLID
- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

Violating SOLID requires explicit justification.

## Naming Convention
- Use meaningful, self-explanatory names.
- Avoid abbreviations unless universally accepted.
- Classes → PascalCase
- Methods → camelCase 
- Variables → camelCase
- Constants → UPPER_SNAKE_CASE (or language convention)
- Interfaces → Prefix with `I` (C#)
- Booleans should read naturally:
  - isActive
  - hasPermission
  - canEdit

## API Design
- Return consistent response models.
- Never expose internal entities directly.
- Use DTOs for requests and responses.
- Include appropriate HTTP status codes.
- Return meaningful error messages.
- Validate all incoming requests.
- Keep controllers thin.
- Put business logic inside Application/Domain layers.

## Error Handling
- Never swallow exceptions.
- Throw domain-specific exceptions.
- Log unexpected errors.
- Return user-friendly messages.
- Avoid generic Exception whenever possible.

## Validation
- Validate input as early as possible.
- Do not trust client input.
- Business validation belongs in Domain/Application, not Controller.

## Dependency Injection
- Use constructor injection.
- Avoid Service Locator.
- Register dependencies with appropriate lifetimes.
- Depend on abstractions, not concrete implementations.

## Database
- Repository handles persistence only.
- Business logic should never exist inside repositories.
- Avoid N+1 queries.
- Use transactions where required.
- Prefer async database operations.

## Async Programming
- Use async/await consistently.
- Avoid blocking calls.
- Propagate async all the way.

## Performance
- Avoid premature optimization.
- Minimize unnecessary allocations.
- Cache expensive operations when appropriate.
- Optimize only after identifying bottlenecks.

## Logging
- Log important business events.
- Log exceptions with sufficient context.
- Never log sensitive information.
- Use structured logging.

## Security
- Validate every request.
- Sanitize user input.
- Never hardcode secrets.
- Use environment variables or secret managers.
- Apply authorization before business logic.
- Follow the Principle of Least Privilege.

## Testing
- Write code that is easy to test.
- Prefer dependency injection.
- Avoid static state.
- Separate business logic from infrastructure.
- Aim for high unit test coverage of business logic.

## Code Style
- Keep methods under ~30 lines when practical.
- Keep classes focused.
- Remove dead code.
- Remove unused imports.
- Avoid deeply nested conditionals.
- Prefer early returns.
- Prefer guard clauses.
- Keep cyclomatic complexity low.

## Git
- Make small, focused commits.
- Write meaningful commit messages.
- Do not commit generated files unless necessary.

## When Generating Code
- Produce complete, compilable code.
- Do not leave TODO placeholders unless explicitly requested.
- Do not generate unused code.
- Follow existing project structure.
- Reuse existing components before creating new ones.
- Explain architectural decisions when introducing new patterns.