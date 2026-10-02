# Ranaswanu - Backend Developer Guide

For: Spring Boot developers (Java, SQL Server, Flyway, JWT)
Frontend: React app at `http://localhost:5173`, calls `http://localhost:8080/api`

This guide tells you:

1. What data (key + value) the frontend sends to each API.
2. What the backend must send back, **including the error for every API**, so the frontend can show the right message.
3. What is finished, what is fake, and what is missing.
4. Fixes the backend still needs (error format, security, CORS, database).
5. An AI prompt to paste before you ask AI to write code.

> This guide was made by reading the code only. Nothing was run. If you test something and it behaves differently, fix this file.
> The frontend team has a matching file: `FRONTEND_GUIDE.md`. **Key names in both files must stay identical.**

---

## 0. Status symbols

| Symbol | Meaning |
|---|---|
| ✅ | Works with real database data |
| 🟡 | Works but needs a fix (error format, small missing part) |
| 🎭 | Endpoint exists but returns **hard-coded fake data** (not using the database) |
| ❌ | Does not exist |

---

## 1. How the frontend talks to you

| Topic | Rule |
|---|---|
| Base URL | `http://localhost:8080/api` |
| Login | Frontend sends header `Authorization: Bearer <token>` on every call (except the 4 auth calls and public product browse) |
| Body | JSON, keys in `camelCase`. File upload = `multipart/form-data` with the part named `file` |
| Who is the user | **Always** use `@AuthenticationPrincipal UserDetails` (the token). Never take `userId`, `buyerId` or `farmerId` from the URL or body for "my own" data |
| Dates | Frontend sends full ISO text for `Instant` fields: `"2026-10-02T14:30:00.000Z"`. Date-only fields (`LocalDate`) use `"2026-10-02"` |
| Numbers | Money and quantity as JSON numbers, not strings |
| Success codes | `200` read / update, `201` created, `204` deleted (no body) |
| Images | Uploaded files are served at `/files/**` (public) |

---

## 2. Rules for backend code

1. Layers: **Controller → Service → Repository**. Controllers only receive and return. Logic lives in the service.
2. Never return an Entity. Use `RequestDto` and `ResponseDto`. Never use `Map<String, Object>` for responses.
3. No hard-coded fake data in controllers.
4. Lombok: `@Getter` / `@Setter` (or `@Data` on DTOs). No `@Builder` on new entities.
5. Every validation annotation needs a readable `message = "..."`. The frontend shows these texts to the user.
6. A new table or column = a **new** Flyway file (`V11__...sql`). Never edit an old migration.
7. Every service method that writes to more than one table gets `@Transactional`.
8. Do not commit passwords. `application.yaml` has the database password, mail password and JWT secret in plain text. Move them to environment variables: `password: ${DB_PASSWORD}`, `secret: ${JWT_SECRET}`, mail `${MAIL_PASSWORD}`.

---

## 3. Error handling (most important part)

### 3.1 The two error shapes (the frontend depends on these)

**Shape A - validation error, status 400.** A plain map: field name → message.

```json
{ "email": "Invalid email format", "password": "Password must be at least 8 characters" }
```

**Shape B - every other error.** Always these 3 keys:

```json
{ "timestamp": "2026-10-02T10:00:00Z", "status": 404, "error": "Crop not found" }
```

`error` must be a sentence a farmer can read. Never put Java text (`NullPointerException`, SQL text, stack trace) in `error`.

### 3.2 Status codes to use

| Status | Use when | Example `error` |
|---|---|---|
| 400 | Bad input that is not a field error, or a business rule broke | "Not enough stock for Tomatoes. Available: 3 kg" |
| 401 | Not logged in, token expired, wrong login | "Invalid email or password" |
| 403 | Logged in, but this role cannot do it | "Only farmers can manage product listings" |
| 404 | Item does not exist **or belongs to someone else** | "Crop not found" |
| 409 | Duplicate or already done | "Email already registered" |
| 413 | File too big | "The file is too big. The maximum size is 5 MB." |
| 500 | Unexpected bug | "Something went wrong on our side. Please try again later." |

> Use **404, not 403**, when an item belongs to another user. It does not leak that the item exists, and the frontend shows "not found".

### 3.3 What is wrong today (found in the code)

| # | Problem | Result for the frontend |
|---|---|---|
| E1 | `BadCredentialsException` (wrong password, bad reset token) has no handler | Most likely 403 with an empty body (not tested) |
| E2 | `AccessDeniedException` is used for "not found or not owned" (4 services) and has no handler | Most likely 403 with an empty body, wrong status (not tested) |
| E3 | Missing or expired token → Spring default | Most likely 403 with an empty body (should be 401 with a message) |
| E4 | Bad JSON or a date in the wrong format (`HttpMessageNotReadableException`) has no handler | Spring default error JSON, not our shape |
| E5 | File over 5 MB (`MaxUploadSizeExceededException`) has no handler | Spring default error |
| E6 | Any other bug has no handler | Spring default 500 with `path`, `trace` info |
| E7 | `DataIntegrityViolationException` always says "Username or Email already exists", even for other constraint errors | Wrong message |
| E8 | `RuntimeException("Failed to store file...")` | Falls into E6 |
| E9 | CORS `allowedMethods` has no **PATCH** | The browser blocks product status, order status and delivery status calls |
| E10 | No `.cors(...)` in the security chain | Browser preflight (OPTIONS) to protected URLs may be rejected with 403. Check in the browser console. This fix is safe to add anyway |
| E11 | `GET /api/products` is not public | Buyers must log in just to browse. The frontend treats it as public |

### 3.4 Fix: custom exceptions (create 2 small classes)

```java
// exception/ResourceNotFoundException.java  -> becomes 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

// exception/ConflictException.java  -> becomes 409
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
```

### 3.5 Fix: replace `UserGlobalExceptionHandler` with this complete class

```java
package com.rukshan.ranaswanu.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class UserGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UserGlobalExceptionHandler.class);

    // Shape A: { "fieldName": "message" }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // Shape B helper: { timestamp, status, error }
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadInput(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "The request has a missing or wrongly formatted value. Check dates and numbers.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleWrongType(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        // For login we deliberately hide whether the user exists (see section 5.1)
        String message = (ex instanceof UsernameNotFoundException) ? "Invalid email or password" : ex.getMessage();
        return error(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Duplicate username/email is checked earlier and gives a clearer message.
        return error(HttpStatus.CONFLICT, "This action conflicts with existing data.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooBig(MaxUploadSizeExceededException ex) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "The file is too big. The maximum size is 5 MB.");
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<Map<String, Object>> handleMailFailure(MailException ex) {
        log.error("Mail sending failed", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "We could not send the email. Please try again later.");
    }

    // Last safety net: never show Java details to the user
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(RuntimeException ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side. Please try again later.");
    }
}
```

Notes:
- Spring always picks the **most specific** handler, so the `RuntimeException` safety net only catches what nothing else caught.
- Keep `UsernameNotFoundException` mapped to 404 with the text "Invalid email or password" only because the old frontend login page already handles 404. After section 5.1 is done, login never throws it.

### 3.6 Fix: 401 and 403 coming from the security filter (E3, E9, E10, E11)

Create `security/ApiErrorWriter.java`:

```java
package com.rukshan.ranaswanu.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

// Writes a Shape B error for errors raised before a controller is reached
public final class ApiErrorWriter {
    private ApiErrorWriter() {}

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write(
                "{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status + ",\"error\":\"" + safe + "\"}");
    }
}
```

In `AppConfig.securityFilterChain` add these parts:

```java
http
    .cors(Customizer.withDefaults())                       // E10: lets CORS run before security
    .csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, e) ->          // E3: no / bad / expired token
            ApiErrorWriter.write(res, 401, "Please log in again."))
        .accessDeniedHandler((req, res, e) ->
            ApiErrorWriter.write(res, 403, "You do not have permission to do this.")))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/api/auth/register", "/api/auth/login",
                         "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()   // E11
        .requestMatchers("/ws/**").permitAll()
        .requestMatchers("/files/**").permitAll()
        .anyRequest().authenticated())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

Imports: `org.springframework.http.HttpMethod`, `org.springframework.security.config.Customizer`.

### 3.7 Fix: CORS (E9)

In `CorsConfig`, add `"PATCH"`:

```java
.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
```

### 3.8 Fix: replace `AccessDeniedException` and "not found" `IllegalArgumentException` (E2)

Change these lines to `throw new ResourceNotFoundException("...")`:

| File | Line (about) | New message |
|---|---|---|
| `LiveStockService` | 51 | "Livestock record not found" |
| `CropService` | 79 | "Crop not found" |
| `FieldPlotService` | 85 | "Field plot not found" |
| `FieldPlotService` | 42 (`IllegalArgumentException`) | "Crop not found" |
| `ProductListingService` | 136 | "Product listing not found" |
| `ProductListingService` | 105 (`IllegalArgumentException`) | "Product listing not found" |

**Keep** `AccessDeniedException` only for `ProductListingService` line 127 ("Only farmers can manage product listings"). That one is a real 403.

### 3.9 What the frontend does with your errors

| You send | Frontend shows |
|---|---|
| Shape A (400 map) | Each message under its input box |
| Shape B | The `error` text in a red box |
| 401 on a protected page | Clears the token, goes to login |
| Empty or unknown body | A safe default message |

So: **always send a clear `error` sentence**, and give every validation annotation a `message`.

---

## 4. Overall status board

| # | Category | Backend status |
|---|---|---|
| 1 | Auth and profile | ✅ works, 🟡 error fixes, ❌ change password |
| 2 | Farm management | ✅ crops, field plots · 🟡 activities, expenses, livestock (missing update / delete) |
| 3 | Dashboard, calendar, support | ❌ all missing |
| 4 | Products | ✅ complete · 🟡 small fixes |
| 5 | Orders and checkout | 🎭 fake (cart + orders), database needs changes |
| 6 | Ratings | 🎭 fake (2 duplicate endpoints) |
| 7 | Shared delivery | 🎭 fake |
| 8 | Notifications and chat | 🟡 WebSocket only broadcasts, nothing is saved · ❌ REST endpoints |

Suggested order: **error handling (section 3) → 1 → 2 → 4 → 3 → 5 → 6 → 7 → 8**.

---

## 5. Category by category

### 5.1 Category 1 - Authentication and profile

#### `POST /api/auth/register` ✅ (🟡 fixes)

Frontend sends:

| Key | Type | Rule |
|---|---|---|
| `username` | string | required, 3 to 50 characters |
| `email` | string | required, valid, max 100 |
| `password` | string | required, 8 to 255 characters |

Success `201`: `{ "message": "User registered successfully", "username": "saman" }`

Errors:

| Status | Body | When |
|---|---|---|
| 400 | `{ "username": "Username must be 3 to 50 characters" }` (any of the 3 fields) | Field rule broken |
| 409 | `{ ..., "status": 409, "error": "Email already registered" }` | Email exists |
| 409 | `{ ..., "error": "Username already taken" }` | Username exists |

Fixes:
- `AuthRegistrationDto`: username is `@Size(min = 1, max = 50, message = "Username must be 3–20 characters")`. The rule and the message disagree. Use `@Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")`.
- In `AuthImpl.register`, check first (the repository already has these methods):
  ```java
  if (userRepository.existsByEmail(requestData.getEmail())) throw new ConflictException("Email already registered");
  if (userRepository.existsByName(requestData.getUsername())) throw new ConflictException("Username already taken");
  ```
- The new user has `role = null`. This is expected. The frontend sends the user to a "Choose your role" page.

#### `POST /api/auth/login` ✅ (🟡 fixes)

Frontend sends: `email` **or** `username`, and `password` (required).

```json
{ "email": "saman@mail.com", "password": "12345678" }
```

Success `200`: `{ "token": "eyJ...", "message": "Login successful" }`

Errors:

| Status | Body | When |
|---|---|---|
| 400 | `{ "password": "Password is required" }` | Password blank |
| 400 | `{ ..., "error": "Username or email is required" }` | Both blank |
| 401 | `{ ..., "status": 401, "error": "Invalid email or password" }` | User not found **or** wrong password (same text for both, on purpose) |

Fixes in `AuthImpl.login`:
- Replace `throw new UsernameNotFoundException(...)` (user not found) and `throw new BadCredentialsException("Invalid password")` with `throw new BadCredentialsException("Invalid email or password")`.
- Replace the "Username or email is required" `BadCredentialsException` with `IllegalArgumentException` (it is a 400, not a login failure).
- `UserService` has a second copy of `login` and `resetPassword`. Keep only the one in `AuthImpl` and delete the duplicate.

#### `POST /api/auth/forgot-password` ✅

Frontend sends `{ "email": "saman@mail.com" }` (required, valid email).
Success `200`: `{ "message": "If this email is registered, a reset link has been sent." }` (**always 200**, even when the email is unknown. Keep this, it protects privacy).

Errors: `400` `{ "email": "Enter a valid email address" }` · `500` "We could not send the email. Please try again later." (mail server problem, handled by `MailException`).
Add `message =` to `AuthForgotPasswordDto` annotations.

#### `POST /api/auth/reset-password` ✅ (🟡 fixes)

Frontend sends:

| Key | Type | Rule |
|---|---|---|
| `token` | string | required (from the email link) |
| `newPassword` | string | required, at least 8 characters |

Success `200`: `{ "message": "Password reset successful" }`

Errors:

| Status | `error` | When |
|---|---|---|
| 400 | field map `{ "newPassword": "Password must be at least 8 characters" }` | Too short |
| 404 | "Invalid or expired reset token" | Token not found |
| 404 | "This reset token has already been used" | Used |
| 404 | "This reset token has expired" | Expired |

Fixes: in `AuthImpl.resetPassword` replace the 3 `BadCredentialsException` with `ResourceNotFoundException` (the existing reset page already treats 404 and 410 as "link invalid"). Add `@Size(min = 8, message = ...)` and `message =` to `AuthResetPasswordDto`.

#### `GET /api/me` ✅

No body. Success `200`:

```json
{
  "userId": 7, "username": "saman", "email": "saman@mail.com", "role": "FARMER",
  "active": true, "phoneNumber": "0712345678", "address": "No 45, Farm Road, Kandy",
  "profilePictureUrl": "/files/profile-pics/abc.jpeg", "createdAt": "2026-07-01T10:00:00.000+00:00"
}
```
`role`, `phoneNumber`, `address`, `profilePictureUrl` can be `null`. Errors: 401 (no token).

#### `PUT /api/me` ✅

Frontend sends (all optional): `username` (3 to 50), `email` (valid), `phoneNumber` (`^[0-9+\-\s]{7,15}$`), `address` (max 255).
Success `200`: same body as `GET /me`.

Errors: `400` field map (example `{ "phoneNumber": "Invalid phone number format" }`) · `409` "Email already registered" / "Username already taken" (add the same `exists...` checks when username or email changes).

#### `PUT /api/me/role` ✅

Frontend sends `{ "role": "FARMER" }`. Allowed: `FARMER`, `BUYER`, `TRANSPORT`.
Success `200`: `{ "userId": 7, "role": "FARMER", "message": "Role updated" }`
Errors: `400` `{ "role": "Role is required" }` · `400` `{ "error": "Invalid role. Allowed values: [FARMER, BUYER, TRANSPORT]" }`.

#### `POST /api/me/picture` ✅

`multipart/form-data`, part `file`. Success `200`: `{ "userId": 7, "profilePictureUrl": "/files/profile-pics/abc.jpeg", "message": "..." }`
Errors: `400` "File is empty" / "Only JPEG, PNG, and WEBP images are allowed" · `413` too big. (These come from `FileStorageService`, they already use the right shape.)
Change `RuntimeException("Failed to store file...")` to a clear message the user can read: "We could not save the file. Please try again."

#### `PUT /api/me/password` ❌ new

The settings page has a "new password" field. No endpoint exists.

Frontend sends: `{ "currentPassword": "old12345", "newPassword": "new12345" }` (both required, new at least 8 characters).
Success `200`: `{ "message": "Password changed successfully" }`
Errors: `400` field map · `400` `{ "error": "Current password is wrong" }` (use `IllegalArgumentException`, **not** 401, because 401 logs the user out on the frontend).

---

### 5.2 Category 2 - Farm management

All of these use `@AuthenticationPrincipal` and are ✅ unless noted. A farmer only sees and changes their own records.

**Common errors for all endpoints in this category**

| Status | Body | When |
|---|---|---|
| 400 | field map | Required field blank, wrong number |
| 400 | `error`: "The request has a missing or wrongly formatted value. Check dates and numbers." | A date was sent in the wrong format |
| 401 | `error`: "Please log in again." | No / expired token |
| 404 | `error`: "Crop not found" (or the record name) | Id does not exist **or belongs to another farmer** |

#### Crops (harvest records) - `GET/POST /api/farmer/crops`, `GET/PUT/DELETE /api/farmer/crops/{cropId}`

| Key | Type | Rule |
|---|---|---|
| `cropName` | string | required |
| `category` | string | required |
| `unit` | string | required |
| `harvestQuantity` | number | required |
| `harvestDate` | ISO date-time | required |
| `notes` | string | optional |

Response keys: `cropId, cropName, category, unit, harvestQuantity, harvestDate, notes, createdAt, updatedAt`.
Status: `POST` 201, `DELETE` 204. Add `message =` to all `@NotBlank` / `@NotNull` in `CropRequestDto`.

#### Field plots - `GET/POST /api/farmer/field-plots`, `PUT/DELETE /api/farmer/field-plots/{fieldPlotId}`

| Key | Type | Rule |
|---|---|---|
| `currentCrop` | string | required |
| `cropVariety` | string | required |
| `areaUnit` | number | optional |
| `growthStage` | string | required: `Seedling`, `Vegetative`, `Flowering`, `Harvest` |
| `healthCondition` | string | required: `Excellent`, `Good`, `Alert` |
| `fieldLogs` | string | optional |
| `inspectionDate` | `"2026-10-02"` | optional |
| `cropId` | number | **required today → make optional (decision D1)** |

Response keys: `fieldPlotId, cropId, cropName, currentCrop, cropVariety, areaUnit, growthStage, healthCondition, fieldLogs, inspectionDate, createdAt, updatedAt` (`cropId` and `cropName` can be `null`).

**Decision D1 (recommended): `cropId` optional.** The frontend "Crop Management" page creates plots with no harvest record. Changes: (1) remove `@NotNull` from `FieldPlotRequestDto.cropId`, (2) the migration in section 6 makes `field_plots.crop_id` nullable, (3) in `FieldPlotService` only load the crop when `cropId != null`, (4) in the response map `cropName` only when a crop exists.
Also validate `growthStage` and `healthCondition` against the allowed lists. Wrong value → `400` field map `{ "growthStage": "Must be one of: Seedling, Vegetative, Flowering, Harvest" }` (use `@Pattern` or `@Schema`).

#### Expenses - `GET/POST /api/farmer/expenses` 🟡

Frontend sends: `title` (required, max 50), `category` (required, max 50), `amount` (required, whole number above 0), `expenseDate` (optional ISO date-time).
Response keys: `expenseId, title, category, amount, expenseDate`.
Fix: add `@Size(max = 50)` on `title` and `category` (database column is `NVARCHAR(50)`, a longer text today gives a 409/500 instead of a clear message), `@Positive` on `amount`.

#### Livestock - `GET/POST /api/farmer/livestock`, `DELETE /api/farmer/livestock/{liveStockId}` 🟡

Frontend sends: `category` (required, max 100), `breed` (optional, max 100), `amount` (required, 0 or more).
Response keys: `liveStockId, category, breed, amount`. 

#### Activities (also the dashboard task list) - 🟡

| Call | Frontend sends | Success |
|---|---|---|
| `GET /api/farmer/activities` ✅ | - | `[ { "activityId": 1, "activity": "...", "activityStatus": false, "createdAt": "..." } ]` |
| `POST /api/farmer/activities` ✅ | `{ "activity": "Apply fertilizer" }` (required, max 500) | 201, one item |
| `PATCH /api/farmer/activities/{id}/status` ❌ | `{ "done": true }` (required boolean) | 200, one item |
| `DELETE /api/farmer/activities/{id}` ❌ | - | 204 |

Errors: `404` "Activity not found". Build the two missing ones in `FarmActivityService` the same way `LiveStockService.delete` is written.

---

### 5.3 Category 3 - Dashboard, calendar, support (all ❌)

> The frontend already calls the calendar and support URLs. These controllers/services were **not in the uploaded code**. If they exist on another branch, merge them and compare with this spec.

#### `GET /api/farmer/dashboard/summary` ❌

No body. Success `200`:

```json
{
  "totalFieldPlots": 3, "totalCrops": 5, "activeListings": 2,
  "pendingOrders": 1, "totalLivestock": 12, "monthExpenses": 45000
}
```

How: count rows of the logged-in farmer. `activeListings` = listings with `listingStatus = true`. `totalLivestock` = sum of `amount`. `monthExpenses` = sum of expenses with `expenseDate` in the current month. `pendingOrders` = orders with status `PENDING` that contain this farmer's products (0 until orders are real).
Errors: 401. Create `DashboardController` + `DashboardService` + repository count methods.

#### Calendar (uses the existing `reminders` table) ❌

One note per day per farmer. Store `reminder_date` as the start of that day. `description` = the note.

| Call | Frontend sends | Success |
|---|---|---|
| `GET /api/farmer/calendar/{date}` | - (date like `2026-10-02`) | `{ "date": "2026-10-02", "note": "Spray plot 2" }` or `{ "date": "2026-10-02", "note": "" }` when no note (**200, not 404**) |
| `PUT /api/farmer/calendar/{date}` | `{ "note": "Spray plot 2" }` (required, max 500). Create if missing, otherwise update | `200` same body |
| `DELETE /api/farmer/calendar/{date}` | - | `204` (also `204` if nothing to delete) |
| `GET /api/farmer/calendar?year=2026&month=10` | - | `[ { "date": "2026-10-02", "note": "..." } ]` (only days that have a note) |

Errors: `400` `{ "error": "Invalid value for 'date'." }` (wrong date format, from `MethodArgumentTypeMismatchException`; use `@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date`) · `400` `{ "note": "Note must be at most 500 characters" }` · `400` "Month must be between 1 and 12".

#### Support messages ❌ (new table)

| Call | Frontend sends | Success |
|---|---|---|
| `POST /api/support/messages` | `{ "subject": "Cannot add crop", "message": "..." }` (subject required max 100, message required max 1000) | `201` `{ "messageId": 1, "subject": "...", "message": "...", "createdAt": "..." }` |
| `GET /api/support/messages` | - | `[ same objects, newest first ]` (only my messages) |

Errors: `400` field map `{ "subject": "Subject is required", "message": "Message is required" }`.
Needs the `support_messages` table (section 6).

---

### 5.4 Category 4 - Products ✅ (small fixes)

All of this works with the database. Controller: `ProductController`.

#### `POST /api/farmer/products` and `PUT /api/farmer/products/{listId}`

| Key | Type | Rule |
|---|---|---|
| `productName` | string | required |
| `category` | string | required |
| `description` | string | optional, max 500 |
| `unitOfMeasurement` | string | required, max 10 (example "kg") |
| `pricePerUnit` | number | required, above 0 |
| `availableStock` | number | required, 0 or more |
| `minimumOrderQuantity` | number | required, 0 or more |
| `harvestedDate` | ISO date-time | optional |
| `deliveryOption` | string | required: `Pickup`, `Delivery`, `Both` |

Success: `POST` 201, `PUT` 200. New listings start **unpublished** (`listingStatus = false`).

Response keys: `listId, farmerId, farmerName, productName, category, description, unitOfMeasurement, pricePerUnit, availableStock, minimumOrderQuantity, harvestedDate, deliveryOption, productImage, listingStatus, createdAt, updatedAt`.

Errors:

| Status | `error` / body | When |
|---|---|---|
| 400 | field map `{ "pricePerUnit": "Price must be above 0" }` | Field rule broken |
| 403 | "Only farmers can manage product listings" | User role is not FARMER |
| 404 | "Product listing not found" | Wrong id or not yours |

#### `PATCH /api/farmer/products/{listId}/status`

Frontend sends `{ "published": true }`. Success `200`: the product. Errors: 404, 403. **Needs CORS PATCH (E9).**
Fix: if `published` is missing, return `400` `{ "error": "published is required (true or false)" }`. Today a missing key silently becomes `false`.

#### `DELETE /api/farmer/products/{listId}` - `204`. Errors: 403, 404.

#### `POST /api/farmer/products/{listId}/image`

`multipart/form-data`, part `file`. JPEG, PNG or WEBP, max 5 MB. Success `200`: the product with `productImage` set.
Errors: `400` "File is empty" / "Only JPEG, PNG, and WEBP images are allowed" / "File exceeds maximum size of 5MB" · `413` too big · 404 · 403.

**Fix (image path):** `productImage` is saved as `product-images/uuid.jpeg` but the profile picture is returned as `/files/profile-pics/...`. In `toResponseDto` return `"/files/" + listing.getProductImage()` when not null, so both are the same.

#### Public: `GET /api/products?category=Vegetables&keyword=tomato` and `GET /api/products/{listId}`

No login needed (after fix E11). Returns **only published** products (single get: 404 if unpublished or missing).
Fixes:
- `browsePublished` uses `keyword` OR `category`, never both. Make it work with both together (add a repository method or use `Specification`).
- `getById` should return 404 "Product not found" when `listingStatus` is false (today a farmer's draft is visible to anyone who knows the id).
- Errors: `404` "Product not found".

---

### 5.5 Category 5 - Cart, checkout and orders 🎭

> **Decision D2 (recommended): no cart on the server.** The frontend keeps the cart in the browser and sends it once at checkout. **Delete the 4 fake cart endpoints** in `CartController` (`GET/POST/PUT/DELETE /buyers/{id}/cart...`) and the fake `/buyers/{id}/checkout`. No cart table is needed.
> Also delete the fake `OrderController` methods and write the real ones below. URLs no longer contain `buyerId` or `farmerId`: the user comes from the token.

#### Database changes needed first (section 6)

The `orders` table was created with `order_status BIT`, `payment_status BIT` and `delivery_id NOT NULL`. We need text statuses (`PENDING`, `ACCEPTED`...) and an order must be able to exist before a delivery is assigned. Update `Order` entity: `String orderStatus`, `String paymentStatus`, `delivery` optional.

#### `POST /api/buyer/orders` (checkout) 🎭

Frontend sends:

```json
{
  "items": [ { "listId": 12, "quantity": 5 }, { "listId": 14, "quantity": 2 } ],
  "deliveryAddress": "No 45, Temple Road, Badulla",
  "contactNumber": "0771234567",
  "paymentMethod": "CASH_ON_DELIVERY",
  "notes": "Call before delivery"
}
```

| Key | Type | Rule |
|---|---|---|
| `items` | array | required, at least 1 (`@NotEmpty`, `@Valid`) |
| `items[].listId` | number | required |
| `items[].quantity` | number | required, above 0 |
| `deliveryAddress` | string | required, max 255 |
| `contactNumber` | string | required, same pattern as phone number |
| `paymentMethod` | string | required: `CASH_ON_DELIVERY` or `BANK_TRANSFER`. **Do not accept or store card numbers.** |
| `notes` | string | optional, max 255 |

What the service does (`@Transactional`):
1. Load every listing. Each must exist and be published.
2. For each item: `quantity >= minimumOrderQuantity` and `quantity <= availableStock`.
3. **Group items by farmer. Create one `Order` per farmer** (status `PENDING`, payment status `UNPAID`, buyer = token user).
4. For each item create an `OrderItem` with `unit_price` = the listing price **now** and `subtotal = quantity * unit_price`. Set the order `total_amount`.
5. Reduce `available_stock` of each listing.
6. Add a row to `notifications` for each farmer ("New order #501").

Success `201`:

```json
{
  "orders": [
    {
      "orderId": 501, "farmerId": 7, "farmerName": "Saman Perera",
      "orderStatus": "PENDING", "paymentStatus": "UNPAID", "totalAmount": 1530.00,
      "orderDate": "2026-10-02T10:00:00Z",
      "deliveryAddress": "No 45, Temple Road, Badulla", "contactNumber": "0771234567",
      "paymentMethod": "CASH_ON_DELIVERY",
      "items": [ { "itemId": 1, "listId": 12, "productName": "Tomatoes", "quantity": 5, "unitPrice": 120.00, "subtotal": 600.00 } ]
    }
  ]
}
```

Errors:

| Status | Body | When |
|---|---|---|
| 400 | field map `{ "items": "Add at least one item", "deliveryAddress": "Delivery address is required" }` | Validation |
| 400 | `error`: "Not enough stock for Tomatoes. Available: 3 kg" | Quantity above stock |
| 400 | `error`: "Minimum order for Tomatoes is 2 kg" | Below minimum |
| 404 | `error`: "Product not found" | Listing missing or unpublished |
| 403 | `error`: "Only buyers can place orders" | Role is not BUYER (optional rule) |

If one item fails, **nothing** is saved (the transaction rolls back).

#### `GET /api/buyer/orders` 🎭

Success `200`: `[ { "orderId": 501, "farmerId": 7, "farmerName": "Saman", "orderStatus": "PENDING", "paymentStatus": "UNPAID", "totalAmount": 1530.00, "orderDate": "...", "itemCount": 2, "firstItemName": "Tomatoes" } ]` (newest first, only my orders).

#### `GET /api/farmer/orders` 🎭

Success `200`: `[ { "orderId": 501, "buyerName": "Kasun", "contactNumber": "077...", "orderStatus": "PENDING", "paymentStatus": "UNPAID", "totalAmount": 1530.00, "orderDate": "...", "itemCount": 2, "firstItemName": "Tomatoes" } ]`.
How: orders that contain at least one of this farmer's products. Errors: 403 "Only farmers can view farmer orders".

#### `GET /api/orders/{orderId}` 🎭

Success `200`: one full order (same shape as one entry of `orders` above) plus `buyerName`.
Allowed: the buyer of the order, or the farmer of the order. Anyone else gets `404` "Order not found".

#### `PATCH /api/farmer/orders/{orderId}/status` 🎭 (needs CORS PATCH)

Frontend sends `{ "status": "ACCEPTED" }`. Allowed: `ACCEPTED`, `REJECTED`, `SHIPPED`, `COMPLETED`.
Success `200`: `{ "orderId": 501, "orderStatus": "ACCEPTED" }`

Allowed changes only: `PENDING → ACCEPTED`, `PENDING → REJECTED`, `ACCEPTED → SHIPPED`, `SHIPPED → COMPLETED`.

| Status | `error` | When |
|---|---|---|
| 400 | `{ "status": "Status is required" }` | Missing |
| 400 | "Invalid status. Allowed values: [ACCEPTED, REJECTED, SHIPPED, COMPLETED]" | Unknown text |
| 400 | "Cannot change status from COMPLETED to ACCEPTED" | Not an allowed change |
| 403 | "Only farmers can update orders" | Wrong role |
| 404 | "Order not found" | Not this farmer's order |

When `REJECTED`: add the stock back to the listings. After any status change, add a notification for the buyer.

**Delete:** `GET /orders/{id}/timeline`. The frontend draws the timeline from `orderStatus`. (A real timeline needs a history table. Skip it for now.)

---

### 5.6 Category 6 - Ratings and reviews 🎭

The `customer_reviews` table has only `user_id`, `rating`, `description`. It has no order or reviewer. Migration in section 6 adds `reviewer_id` and `order_id`. Meaning: `user_id` = **the person being rated** (the farmer), `reviewer_id` = the buyer.

There are **two** fake endpoints for the same thing: `GET /users/{id}/ratings` (in `UserController`) and `GET /users/{id}/reviews` (in `RatingController`). **Keep `/reviews`, delete `/ratings`.**

#### `POST /api/orders/{orderId}/rating` 🎭

Frontend sends `{ "score": 5, "comment": "Fresh and on time" }` (`score` required 1 to 5, `comment` optional max 500).
The farmer being rated is the farmer of the order (the frontend does **not** send it).

Success `201`: `{ "ratingId": 9, "orderId": 501, "revieweeId": 7, "score": 5, "comment": "...", "submittedAt": "..." }`

| Status | Body | When |
|---|---|---|
| 400 | `{ "score": "Score must be between 1 and 5" }` | Out of range (`@Min(1) @Max(5)`) |
| 400 | `error`: "You can only rate a completed order" | Order is not `COMPLETED` |
| 403 | `error`: "Only the buyer of this order can rate it" | Other user |
| 404 | `error`: "Order not found" | Missing |
| 409 | `error`: "You already rated this order" | Second rating |

DTO key mapping: `score` ↔ column `rating`, `comment` ↔ column `description`. Add a notification for the farmer.

#### `GET /api/users/{userId}/reviews` 🎭

Frontend sends nothing. Success `200`:

```json
{
  "userId": 7, "averageRating": 4.6, "totalReviews": 23,
  "reviews": [ { "ratingId": 9, "orderId": 501, "reviewerName": "Kasun", "score": 5, "comment": "...", "date": "2026-10-01" } ]
}
```
Rules: `averageRating` rounded to 1 decimal (`0.0` when there are no reviews, not an error). Newest first. Error: `404` "User not found".

---

### 5.7 Category 7 - Shared delivery 🎭

Idea (from the proposal): users sending goods to similar places share one vehicle to save cost.

Model with the existing tables:
- `transportation_requests` = one user's request.
- `deliveries` = the shared trip. Several requests point to the same `delivery_id`.
- A request has no delivery until it joins or is assigned. Migration (section 6) makes `transportation_requests.delivery_id` nullable and changes the two `BIT` status columns to text.

Name mapping (API key → column): `pickupLocation` → `pickup_location`, `destination` → `delivery_location`, `preferredDateTime` → `requested_date_time`, `vehicleType` → `vehicle_type`, `estimatedWeight` → `estimated_weight`, `size` → `size`, `description` → `description`, `specialInstructions` → `special_instructions`.

#### `POST /api/delivery-requests` 🎭

Frontend sends:

| Key | Type | Rule |
|---|---|---|
| `pickupLocation` | string | required, max 255 |
| `destination` | string | required, max 255 |
| `preferredDateTime` | ISO date-time | required, must be in the future |
| `vehicleType` | string | required, max 50 |
| `estimatedWeight` | number | required, whole number above 0 (kg) |
| `size` | string | required, max 50 |
| `description` | string | optional, max 500 |
| `specialInstructions` | string | optional, max 500 |

Success `201`: `{ "requestId": 701, "status": "OPEN", "pickupLocation": "...", "destination": "...", "preferredDateTime": "...", "createdAt": "..." }`
Errors: `400` field map · `400` `{ "preferredDateTime": "Date must be in the future" }`.

#### `GET /api/delivery-requests` 🎭 (my requests)

Success `200`: `[ { "requestId": 701, "pickupLocation": "...", "destination": "...", "preferredDateTime": "...", "status": "OPEN" } ]`. (The old URL `/delivery-requests/{userId}` is removed.)

#### `GET /api/delivery-requests/{requestId}/matches` 🎭

`requestId` is **my** request. Return other users' `OPEN` requests that could share a trip.
Success `200`:

```json
{
  "requestId": 701, "totalMatches": 2,
  "matches": [ { "requestId": 702, "userName": "Nimal", "pickupLocation": "Bandarawela", "destination": "Colombo",
                 "preferredDateTime": "2026-10-10T08:00:00Z", "matchScore": 0.85, "estimatedSavingPercent": 50 } ]
}
```
A simple match rule is enough for the project: score = 0.5 if the destination text is equal (ignore case) + 0.3 if the pickup text is equal + 0.2 if the dates are on the same day. Only return matches with a score of 0.5 or more, best first. `estimatedSavingPercent = 100 - 100 / (number of requests in the trip)` (this is only an estimate).
Errors: `404` "Delivery request not found" (also when it is not mine).

#### `POST /api/delivery-requests/{requestId}/join` 🎭

Frontend sends `{ "withRequestId": 702 }` (required). `requestId` in the URL is my request.
Success `200`: `{ "requestId": 701, "deliveryId": 801, "status": "MATCHED", "estimatedSavingPercent": 50 }`
How: if the other request has no delivery, create one. Set my request's `delivery_id` to the same delivery. Both requests become `MATCHED`.

| Status | `error` | When |
|---|---|---|
| 400 | `{ "withRequestId": "withRequestId is required" }` | Missing |
| 400 | "You cannot join your own request" | Same user |
| 404 | "Delivery request not found" | Either id missing / not allowed |
| 409 | "You already joined this delivery" | Already matched |
| 409 | "This request is no longer open" | Already `COMPLETED` / `CANCELLED` |

#### `GET /api/deliveries/{deliveryId}/status` 🎭

Success `200`: `{ "deliveryId": 801, "status": "IN_TRANSIT", "estimatedArrival": "2026-10-10T14:00:00Z", "lastUpdated": "..." }`
Allowed for users who have a request in that delivery. Others: `404` "Delivery not found". (Remove the fake `driverName`, `vehicleNumber`, `currentLocation` unless you store them.)

#### `PATCH /api/deliveries/{deliveryId}/status` 🎭 (for transport users; no frontend page yet)

Send `{ "status": "PICKED_UP" }`. Allowed: `PENDING`, `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`. Success `200`: `{ "deliveryId": 801, "status": "PICKED_UP", "lastUpdated": "..." }`.
Errors: `400` "Invalid status. Allowed values: [...]" · `403` "Only transport users can update deliveries" · `404`. Needs CORS PATCH.

---

### 5.8 Category 8 - Notifications and chat

#### Notifications (table `notifications` exists, no endpoints) ❌

| Call | Frontend sends | Success |
|---|---|---|
| `GET /api/notifications` | - | `[ { "notificationId": 1, "title": "Order accepted", "message": "...", "isRead": false, "createdAt": "..." } ]` newest first, only mine |
| `PATCH /api/notifications/{id}/read` | - | `200` `{ "notificationId": 1, "isRead": true }` |

Errors: `404` "Notification not found" (not mine). Needs CORS PATCH.

> **Careful with the key name `isRead`.** A Java field `boolean isRead` is written by Jackson as `"read"`. Use `@JsonProperty("isRead") private boolean read;` in the response DTO so the key is really `isRead`. Same for messages.

Create a small `NotificationService.create(userId, title, message)` that saves the row **and** calls `ChatWebSocketController.pushNotification(...)`. The order, rating and delivery services call it.

#### Chat 🟡 / ❌

Today `ChatWebSocketController` only broadcasts. It **does not save** anything, and it trusts `senderId` from the message body (anyone can pretend to be anyone). `/ws/**` is open to everyone.

| Call | Frontend sends | Success |
|---|---|---|
| `POST /api/chats` ❌ | `{ "otherUserId": 9 }` (required) | `200` (or `201` if new) `{ "chatId": 3, "otherUserId": 9, "otherUserName": "Kasun" }`. Returns the existing chat if there is one |
| `GET /api/chats` ❌ | - | `[ { "chatId": 3, "otherUserId": 9, "otherUserName": "Kasun", "lastMessage": "...", "updatedAt": "..." } ]` |
| `GET /api/chats/{chatId}/messages` ❌ | - | `[ { "messageId": 1, "senderId": 7, "content": "Hello", "sentAt": "...", "isRead": true } ]` oldest first |

Errors: `400` `{ "otherUserId": "otherUserId is required" }` · `400` "You cannot chat with yourself" · `404` "User not found" / "Chat not found" (chat where I am not one of the two users).

Chat table rule: `UNIQUE (user_one_id, user_two_id)`. Always save the **smaller** id as `user_one_id` so (3, 9) and (9, 3) are the same chat.

WebSocket changes:
1. Frontend connects to `/ws/chat` (SockJS) and sends `Authorization: Bearer <token>` in the STOMP CONNECT headers. Add a `ChannelInterceptor` that reads the header on `CONNECT`, checks the token with `JwtUtil`, and sets the user. Reject the connection if the token is bad.
2. In `@MessageMapping("/chat/{chatId}")` read the user from `Principal`, **not** from the payload. Frontend now sends only `{ "content": "Hi" }`.
3. Check the sender belongs to the chat, check `content` is not blank and max 1000 characters.
4. **Save** the message in `messages`, update `chats.updated_at`, then broadcast to `/topic/chat/{chatId}`: `{ "messageId": 1, "chatId": 3, "senderId": 7, "content": "Hi", "sentAt": "..." }`.
5. Sending to `/user/queue/errors` is the WebSocket way to report an error (example `{ "error": "Message is too long" }`).
6. Change `.setAllowedOriginPatterns("*")` to `"http://localhost:5173"` before the demo.

---

## 6. Database migration - one new file

Create `src/main/resources/db/migration/V11__api_alignment.sql`. SQL Server will not change a column that has a default constraint, so each change drops the default first.

```sql
-- V11: changes needed so the real APIs can be built

-- 1. Field plots can exist without a harvest record (decision D1)
ALTER TABLE field_plots ALTER COLUMN crop_id BIGINT NULL;
GO

-- 2. Support messages table
CREATE TABLE support_messages (
    message_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    subject    NVARCHAR(100)  NOT NULL,
    message    NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    user_id    BIGINT NOT NULL,
    CONSTRAINT FK_Support_Messages_users FOREIGN KEY (user_id) REFERENCES users (user_id)
);
GO

-- 3. Orders: text statuses, delivery optional
DECLARE @sql NVARCHAR(MAX);

SELECT @sql = 'ALTER TABLE orders DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'order_status';
IF @sql IS NOT NULL EXEC sp_executesql @sql;
SET @sql = NULL;

SELECT @sql = 'ALTER TABLE orders DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'payment_status';
IF @sql IS NOT NULL EXEC sp_executesql @sql;
SET @sql = NULL;

ALTER TABLE orders ALTER COLUMN order_status   NVARCHAR(20) NOT NULL;
ALTER TABLE orders ALTER COLUMN payment_status NVARCHAR(20) NOT NULL;
ALTER TABLE orders ADD CONSTRAINT DF_orders_order_status   DEFAULT 'PENDING' FOR order_status;
ALTER TABLE orders ADD CONSTRAINT DF_orders_payment_status DEFAULT 'UNPAID'  FOR payment_status;
ALTER TABLE orders ALTER COLUMN delivery_id BIGINT NULL;
UPDATE orders SET order_status = 'PENDING' WHERE order_status IN ('0', '1');
UPDATE orders SET payment_status = 'UNPAID' WHERE payment_status IN ('0', '1');
GO

-- 4. Deliveries and transportation requests: text statuses, delivery optional on a request
DECLARE @sql2 NVARCHAR(MAX);

SELECT @sql2 = 'ALTER TABLE deliveries DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('deliveries') AND c.name = 'delivery_status';
IF @sql2 IS NOT NULL EXEC sp_executesql @sql2;
SET @sql2 = NULL;

SELECT @sql2 = 'ALTER TABLE transportation_requests DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('transportation_requests') AND c.name = 'request_status';
IF @sql2 IS NOT NULL EXEC sp_executesql @sql2;
SET @sql2 = NULL;

ALTER TABLE deliveries ALTER COLUMN delivery_status NVARCHAR(20) NOT NULL;
ALTER TABLE deliveries ADD CONSTRAINT DF_deliveries_status DEFAULT 'PENDING' FOR delivery_status;
ALTER TABLE transportation_requests ALTER COLUMN request_status NVARCHAR(20) NOT NULL;
ALTER TABLE transportation_requests ADD CONSTRAINT DF_transport_status DEFAULT 'OPEN' FOR request_status;
ALTER TABLE transportation_requests ALTER COLUMN delivery_id BIGINT NULL;
UPDATE deliveries SET delivery_status = 'PENDING' WHERE delivery_status IN ('0', '1');
UPDATE transportation_requests SET request_status = 'OPEN' WHERE request_status IN ('0', '1');
GO

-- 5. Reviews: who wrote it and for which order (user_id stays = the person being rated)
ALTER TABLE customer_reviews ADD reviewer_id BIGINT NULL;
ALTER TABLE customer_reviews ADD order_id    BIGINT NULL;
ALTER TABLE customer_reviews ADD CONSTRAINT FK_Reviews_Reviewer FOREIGN KEY (reviewer_id) REFERENCES users (user_id);
ALTER TABLE customer_reviews ADD CONSTRAINT FK_Reviews_Order    FOREIGN KEY (order_id)    REFERENCES orders (order_id);
GO
```

Then update the entities to match (`String` statuses, nullable relations, new columns). `ddl-auto: validate` will stop the app on start if an entity does not match the table, which is useful: read the message and fix the entity.

> The old migrations used `BIT` for statuses. If your teammates already have data they care about, back up first. For a student project with test data only, this is safe.

---

## 7. Clean-up list (do after the features work)

- [ ] `ProductListingResponseDto` is inside `dto/request/farmer/`. Move it to `dto/response/farmer/`.
- [ ] Entity `Expens` is a spelling mistake. Rename to `Expense` (and its repository).
- [ ] Duplicate code: `UserService` and `AuthImpl` both have login / reset-password logic. Keep one.
- [ ] Empty folders `impl/`, `mapper/`, `model/`, `util/`: delete or use.
- [ ] Add `message =` to every validation annotation (the frontend shows them).
- [ ] Move secrets to environment variables (rule 8).
- [ ] Do not keep `.idea/`, `uploads/` and database dump files inside the zip or in Git. Add them to `.gitignore`.
- [ ] Role checks: only products check `FARMER`. Add the same check to farmer-only and buyer-only endpoints (403 with a clear message).
- [ ] Update the Postman collection and the OpenAPI file after each change, so frontend and backend always agree.

---

## 8. Test checklist (Postman or the frontend)

For every endpoint, test these 4 cases and look at the JSON:

1. Good request → expected status and keys.
2. Missing or wrong field → 400 with a field map.
3. No token → 401 with `{ "error": "Please log in again." }`.
4. Another user's id → 404 (not 403, not 500).

Also test once: PATCH from the browser (CORS), a 6 MB image (413), a date sent as `"2026-10-02"` to an `Instant` field (400 with a readable message, not a stack trace).

---

## 9. AI prompt for backend work

Paste this at the start of a new AI chat. Then add your task.

````text
You are helping me build the Spring Boot backend of "Ranaswanu" (smart farming + crop selling).
Stack: Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA, SQL Server, Flyway, Lombok.
Base package: com.rukshan.ranaswanu. I am a student. Write simple code that is easy to read.

STRUCTURE
- Controller -> Service -> Repository. Controllers only receive and return. Logic is in the service.
- Packages: controller, service (service/farmer for farmer features), repository, entities, dto/request/<area>, dto/response/<area>, exception, security, config.
- Names: XxxController, XxxService, XxxRepository, XxxRequestDto, XxxResponseDto.
- NEVER return an Entity. NEVER use Map<String,Object> responses. NEVER return fake hard-coded data.
- Lombok: @Getter/@Setter (or @Data on DTOs). No @Builder on new entities.
- The logged-in user always comes from @AuthenticationPrincipal UserDetails (email). Never take userId / buyerId / farmerId from the URL or body for "my own" data.
- A record that belongs to another user must give 404, not 403.
- Methods that write more than one table use @Transactional. Keep methods under ~25 lines.
- A new table or column = a NEW Flyway migration (V12__..., next number). Never edit an old migration.
- Every method has a one-line plain English comment that says WHY.

DATA RULES
- Request DTO validation: every annotation has a readable message, example @NotBlank(message = "Crop name is required").
- JSON keys are camelCase and MUST be exactly the names in the contract I give you. If a name is missing, ASK me. Do not guess.
- Instant fields are ISO date-time text. LocalDate fields are "yyyy-MM-dd".
- A boolean property that starts with "is" needs @JsonProperty("isRead") so the JSON key is correct.
- Success codes: 200 read/update, 201 created, 204 deleted.

ERRORS (very important, the frontend shows them to the user)
- Only two error shapes are allowed.
  Shape A (status 400 validation): { "fieldName": "message" }  (this comes from MethodArgumentNotValidException)
  Shape B (everything else): { "timestamp": "...", "status": 404, "error": "Readable sentence" }
- Throw: IllegalArgumentException -> 400, BadCredentialsException -> 401, AccessDeniedException -> 403,
  ResourceNotFoundException -> 404, ConflictException -> 409. Add new handlers in the existing @RestControllerAdvice class when needed.
- The "error" text must be a clear sentence for a farmer. Never put Java, SQL or stack-trace text in it.
- For every endpoint I ask for, list the errors it can return (status + error text + when) in a table.

SECURITY
- Public only: /api/auth/**, GET /api/products, GET /api/products/*, /files/**, /ws/**. Everything else needs a token.
- Do not store card numbers. Do not put secrets in code (use environment variables).

HOW TO ANSWER
1. First list the files you will create or change (one line each) and any new migration.
2. Then give COMPLETE files, each starting with its full path. No "rest of code here".
3. Change only what I asked.
4. End with max 3 lines: what changed, what I must run or test, and what the frontend must know (URL, keys, errors).

THE API CONTRACT FOR MY TASK (I will paste the section from BACKEND_GUIDE.md here):
<paste the API section here>

MY TASK:
<write your task here>
````
