# Error Codes

## System (`SystemErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| INTERNAL_ERROR | SYS_001 | An unexpected error occurred |
| VALIDATION_ERROR | SYS_002 | Validation failed |
| RESOURCE_NOT_FOUND | SYS_003 | Resource not found |
| METHOD_NOT_ALLOWED | SYS_004 | HTTP method not supported |
| DATABASE_ERROR | SYS_005 | Database error occurred |
| INVALID_PARAMETER | SYS_006 | Invalid parameter provided |
| INVALID_MULTIPART | SYS_007 | Invalid multipart request. Please check your file upload. |
| UNAUTHORIZED | SYS_008 | Unauthorized access |
| FORBIDDEN | SYS_009 | Forbidden access by user role |

## Auth (`AuthErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| INVALID_GMAIL | AUTH_000 | Invalid gmail |
| EMAIL_ALREADY_EXISTS | AUTH_001 | Email already exists |
| INVALID_PASSWORD | AUTH_002 | Password must contain 8 characters |
| INVALID_CREDENTIALS | AUTH_003 | Invalid email or password |
| REFRESH_TOKEN_REQUIRED | AUTH_004 | Refresh token is required |
| INVALID_REFRESH_TOKEN | AUTH_005 | Invalid refresh token |
| REFRESH_TOKEN_EXPIRED | AUTH_006 | Refresh token has expired or been revoked |
| OTP_REQUEST_TOO_FREQUENT | AUTH_007 | Please wait before requesting a new OTP |
| OTP_EXPIRED | AUTH_008 | OTP has expired |
| OTP_INVALID | AUTH_009 | Invalid OTP |
| OTP_MAX_ATTEMPTS_EXCEEDED | AUTH_010 | Maximum OTP verification attempts exceeded |
| EMAIL_SEND_FAILED | AUTH_011 | Failed to send email |
| USER_NOT_FOUND | AUTH_012 | User not found |

## User (`UserErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| USER_NOT_FOUND | USER_001 | User not found |
| USER_NOT_AUTHENTICATED | USER_002 | User is not authenticated |
| INVALID_USER_INPUT | USER_003 | User input wrong format request |

## Course (`CourseErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| COURSE_NOT_FOUND | COURSE_001 | Course not found |
| SUBJECT_NOT_FOUND | COURSE_002 | Subject not found |
| LESSON_NOT_FOUND | COURSE_003 | Lesson not found |
| COURSE_TITLE_EXISTS | COURSE_004 | A course with this title already exists |
| SUBJECT_TITLE_EXISTS | COURSE_005 | A subject with this title already exists |
| CREATED_COURSE_THUMBNAIL_CANNOT_BE_NULL | COURSE_006 | Thumbnal of new course can not be empty |
| COURSE_TITLE_EMPTY | COURSE_007 | Course title must not be empty |

## Assessment (`AssessmentErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| ASSESSMENT_NOT_FOUND | ASSESSMENT_001 | Assessment not found |
| HOMEWORK_REQUIRES_SUBJECT | ASSESSMENT_002 | Homework requires a subject ID |
| EXAM_REQUIRES_COURSE | ASSESSMENT_003 | Exam must belong to a course |
| INVALID_ANSWER_KEYS | ASSESSMENT_004 | Invalid answer keys format |
| ATTEMPT_NOT_FOUND | ASSESSMENT_005 | Assessment attempt not found or access denied |
| VIP_ONLY | ASSESSMENT_006 | This assessment is for VIP members only |
| DUPLICATE_ANSWER | ASSESSMENT_007 | Duplicate answer submitted for the same question |

## File (`FileErrorCode`)

| Enum Name | Code | Message |
|---|---|---|
| FILE_EMPTY | FILE_001 | File is empty |
| FILE_EXTENSION_NOT_ALLOWED | FILE_002 | File extension is not allowed |
| UPLOAD_FAILED | FILE_003 | File upload failed |
| DELETE_FAILED | FILE_004 | File delete failed |
