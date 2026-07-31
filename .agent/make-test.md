# Unit test only
## Objective

Create comprehensive unit tests for every function, method, and business rule introduced during development.

The goal is to verify correctness, prevent regressions, and document expected behavior.

---

## General Rules

- Every newly created function must have corresponding unit tests.
- Every public method must be covered by at least one test.
- Every business rule must have dedicated test cases.
- Tests must be deterministic.
- Tests must be independent.
- Tests must not rely on execution order.
- Tests must not share mutable state.
- Tests must run in isolation.

---

## What Must Be Tested

For every function, verify:

### Happy Path
- Valid input
- Expected output
- Expected side effects

### Boundary Cases
- Minimum values
- Maximum values
- Empty collections
- Single-item collections
- Null values (when allowed)
- Optional parameters
- Default values

### Invalid Input
- Invalid parameters
- Missing required values
- Malformed input
- Incorrect object state

### Exception Cases
- Expected exceptions
- Validation failures
- Business rule violations

### State Changes
- Object state before execution
- Object state after execution

### External Interactions
Verify that dependencies are called correctly.

Examples:
- Repository
- Cache
- Message Broker
- Email Service
- File Storage
- External APIs
