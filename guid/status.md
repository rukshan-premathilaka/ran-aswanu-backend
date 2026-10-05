

Status · MD
# Ranaswanu Backend: Progress and Handoff

**Role:** you are the backend developer (Spring Boot 4.0.6, SQL Server, Flyway, JWT).
**Guides:** `BACKEND_GUIDE.md` (what to build) and `FRONTEND_GUIDE.md` (key names must stay identical).
**To continue in a new chat:** upload `ranaswanu.zip` (latest code), both guide files, and this file. Then say: **"Continue Ranaswanu backend from Step 10."**

---

## 1. Completed steps

| Step | Part | Status | Main changes |
|---|---|---|---|
| Rule 8 | Secrets | Done | Passwords moved to gitignored `application-local.yaml` (with an example file), new `.gitignore`, fixed `pom.xml` |
| 1 | Error handling (§3) | Done | `ResourceNotFoundException`, `ConflictException`, full `UserGlobalExceptionHandler`, `ApiErrorWriter` (401/403 JSON), CORS with PATCH, public `GET /api/products` |
| 2 | Migration V11 | Done | `V11__field_plot_crop_optional.sql` (crop_id nullable) |
| 3 | Auth and profile (§5.1) | Done | Duplicate checks (409), login 401, reset-token 404, `PUT /api/me/password`, DTO messages |
| 4 | Farm management (§5.2) | Done, **test still needed** | Optional `cropId`, activity PATCH/DELETE, expense and livestock validation |
| 5 | Products (§5.4) | Done and tested | `/files/` image prefix, browse with category + keyword, 404 for drafts, 400 when `published` is missing, validation messages |
| 6 | Dashboard, calendar, support (§5.3) | Delivered (test result not confirmed) | `V12__support_messages.sql`; `DashboardController/Service`, `CalendarController/Service` (reminders table, one note per day, 00:00 UTC), `SupportController/Service` |
| 8 | Notifications (§5.8) | Delivered and compile error fixed | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `NotificationService.create(userId, title, message)` (never throws, pushes after commit), `@JsonProperty("isRead")` |
| 7 | Orders (§5.5) | Delivered, **not tested yet** | `V13__orders_text_status.sql`; `OrderService` and `OrderController`: checkout in one transaction (one order per farmer, stock locked and reduced), buyer and farmer lists, order detail, status PATCH with transition rules, stock restored on REJECTED, notifications; dashboard `pendingOrders` is now real |
| 9 | Ratings (§5.6) | Delivered, **not tested yet** | `V14__reviews_reviewer_order.sql` (reviewer_id, order_id, FKs, unique index on order_id); `CustomerReview` entity; `RatingService`, `RatingController` (`POST /api/orders/{orderId}/rating`, `GET /api/users/{userId}/reviews`); DTOs in `dto/request/rating` and `dto/response/rating`; fake `/users/{id}/ratings` removed from `UserController` |

(Step 8 was done before Step 7 on purpose, so Step 7 can send notifications.)

### Pending to-dos from finished steps
1. ~~Delete `CartController.java`~~ Done in the Step 9 zip (the file is no longer in it). If your project folder still has it, delete it by hand.
2. Test Step 4 (farm management), Step 7 (orders) and Step 9 (ratings: mark an order COMPLETED, then POST rating gives 201; second POST gives 409; score 6 gives 400; non-completed order gives 400; other buyer gives 403; unknown order gives 404; GET reviews of the farmer shows average and list; unknown user gives 404; farmer gets a notification). Step 7 test list:
   - checkout with 2 farmers gives 2 orders
   - stock too high gives 400
   - below the minimum gives 400
   - draft product gives 404
   - farmer checkout gives 403
   - invalid `paymentMethod` gives 400
   - status PATCH happy path
   - invalid transition gives 400
   - REJECTED restores stock
   - another buyer's `GET /orders/{id}` gives 404
   - dashboard `pendingOrders` is correct
3. Test Step 8: insert a notification in SQL (`INSERT INTO notifications (message, title, user_id) VALUES (N'Test', N'Test', <id>)`), then GET and PATCH it.

---

## 2. Still to do (in order)

### Step 10: Shared delivery (§5.7)
- **Migration V15:** deliveries and transportation_requests get text statuses (guide migration section 4: `delivery_status`, `request_status`; `delivery_id` nullable on a request). Update the `Delivery` and `TransportationRequest` entities.
- Replace the fake `DeliveryController` with 6 endpoints: create request, list my requests, matches (use the match score rule in the guide), join, delivery status GET and PATCH.
- Send notifications through `NotificationService`.

### Step 11: Chat and WebSocket (§5.8)
- `POST/GET /api/chats` and `GET /api/chats/{chatId}/messages` (store the smaller user id as `user_one_id`).
- JWT `ChannelInterceptor` on STOMP CONNECT.
- Take the sender from `Principal`, not from the payload.
- Save messages before broadcasting.
- Restrict allowed origins to `http://localhost:5173`.
- Close the open `/topic/notifications/{userId}` topic, which anyone can subscribe to until this step.
- Fix `Optional.of(...)` in the chat broadcast.

### Step 12: Clean-up (§7)
- Move `ProductListingResponseDto.java` from `dto/request/farmer/` to `dto/response/farmer/` (its package line already says response).
- Rename `Expens` to `Expense` (entity and `ExpensRepository`).
- Delete the empty folders.
- Add role checks (403) to the endpoints that still lack them: crops, field plots, expenses, livestock, activities, dashboard and calendar (farmer-only); and any buyer-only endpoints.

### Step 13: Test (§8)
For every endpoint, test: a good request, a bad field, no token (401), and another user's id (404). Also test: PATCH from the browser, a 6 MB image (413) and a wrong date format. Then update Postman.

---

## 3. Project facts to remember (to avoid repeat errors)

- **Package:** `com.rukshan.ranaswanu`. Folders: `controller`, `service`, `service/farmer`, `repository`, `entities`, `dto/request[/farmer|/order]`, `dto/response[/farmer|/order]`, `exception`, `config`, `security`.
- **Migrations:** V11 (field plot crop optional), V12 (support messages), V13 (orders text status), V14 (review reviewer and order). Next free number is **V15**. SQL Server needs `GO` between batches when a column changes and the same file uses it.
- **`UserRepository` extends `CrudRepository`**, so `getReferenceById` does not exist. Use `findById` or `findByEmail`.
- **Files use CRLF line endings.** Keep them when editing.
- **Entity rule:** `String` columns need `@Nationalized` (NVARCHAR) or `ddl-auto: validate` fails.
- **Roles:** `FARMER`, `BUYER`, `TRANSPORT` (string in `User.role`).
- **Errors:** validation gives a field map (shape A). Everything else gives `{ timestamp, status, error }` (shape B). Throw `IllegalArgumentException` for 400, `ResourceNotFoundException` for 404, `AccessDeniedException` for 403, `ConflictException` for 409.
- **User identity:** always from `@AuthenticationPrincipal UserDetails` (email), never from the URL or body.
- **Notifications:** call `notificationService.create(userId, title, message)` (title max 50, message max 255).
- **I could not compile or run any step here.** After each zip, run `./mvnw clean compile`, start the app, and paste any error back.
