## Authentication Module

### Roles

The application has three authenticated roles:

- **Admin**
- **Learner**
- **Assistant**

Additionally, **Guest** users can access public endpoints (implemented in a later phase) and do not require authentication.

### Authentication Endpoints

Implement the following endpoints:

#### Register
- Only **Learners** can self-register.
- **Assistants** are created by an **Admin**.
- A successful registration returns both an **Access Token** and a **Refresh Token**.

#### Login
- Authenticate using **email** and **password**.
- Return an **Access Token** and a **Refresh Token** upon successful authentication.

#### Forgot Password
- Allow users to request a password reset.
- Implement a secure password reset flow.

#### Refresh Token
- Validate the refresh token.
- Generate and return a new access token (and refresh token if using rotation).

#### Change Password
- Authenticated users can change their password.
- Verify the current password before updating to the new password.