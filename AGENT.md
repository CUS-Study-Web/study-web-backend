### WHY
Spring Boot backend for an educational e-learning platform. Supports courses, documents, and learning materials for students while presenting the institution as a professional education provider.

### Tech Stack
- Java 25, Spring Boot, Maven
- PostgreSQL + Flyway for DB migrations
- Folder layout: see FOLDER_STRUCTURE.md

### WORKFLOW
Each task must follow **plan mode** then **TDD style** (test-first). Grab context from user, write code per `.agent/coding-style.md`, and verify against `.agent/make-test.md`.

## AI Agent Rules
- Before writing new code, search for an existing implementation.
- Never create duplicate classes or methods.
- Prefer modifying existing code over rewriting it.
- Follow the project's current architecture and conventions.
- Keep changes as small as possible.
- Do not introduce new libraries unless necessary.
- If multiple solutions exist, choose the simplest maintainable one.
- If requirements are ambiguous, state assumptions clearly.
- Preserve backward compatibility unless instructed otherwise.
- Ensure generated code compiles without warnings.