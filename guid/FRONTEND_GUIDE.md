# Ranaswanu - Frontend Developer Guide

For: React developers (Vite + Tailwind + React Router + axios)
Backend: Spring Boot at `http://localhost:8080/api`

This guide tells you:

1. What is already finished.
2. What is not finished, category by category, in the order you should build it.
3. Which API to call, what to send, and what you get back.
4. How to show backend errors to the user.
5. How to use the common `src/api` folder (no API calls inside pages).
6. An AI prompt to paste before you ask AI to write code.

> This guide was made by reading the code only. Nothing was run. If something does not match when you test it, tell the team and fix this file.

---

## 0. Status symbols

| Symbol | Meaning |
|---|---|
| ✅ | Works end to end (frontend + backend) |
| 🟡 | Page or API exists but is broken, fake, or half done |
| ❌ | Not started |
| 🔧 | Backend is not ready yet. Build the page, but it cannot work until backend is done |

---

## 1. The 5 golden rules

1. **Pages never write URLs.** All URLs live in `src/api/endpoints.js`.
2. **Pages never use `axios` or `fetch` directly** (except the weather page, which calls a public weather service).
3. **Every page that loads data shows 3 states:** loading, error, empty.
4. **Never fake success.** If the API fails, show the error. Do not save fake data in the page and do not use `alert()`.
5. **Field names must be exactly the same as the backend.** The backend names win. Do not rename them.

---

## 2. One-time setup (do this first, 15 minutes)

Do these 4 small steps once. After that every page follows the same pattern.

### Step 2.1 - Fix the file names

File names are case sensitive on Linux and Mac. Windows hides the problem, but builds fail.

| Wrong import | Fix |
|---|---|
| `@/api/ENDPOINTS` | `@/api/endpoints.js` |
| `@/routes/DevRouteList.jsx` (file is `Devroutelist.jsx`) | rename the file to `DevRouteList.jsx` |

Also delete these dead files:

- `src/api/authService.js` (imports a file that does not exist, uses old `/users/...` URLs)
- `src/page/auth/ResetPassword.jsx` or `Resetpasswordpage.jsx` (keep ONE). Keep the one that uses `ApiService`, which is `Resetpasswordpage.jsx`. Rename it to `ResetPasswordPage.jsx` and fix the import in `Routes.config.js`.
- `src/page/farmer/FarmerDashboard.jsx` (empty placeholder; the real layout is in `src/layouts/FarmerDashboard.jsx`)

### Step 2.2 - Add `src/api/apiError.js` (turns any error into a message)

```js
// src/api/apiError.js
// Turns any axios error into: { status, message, fieldErrors }
// message     -> one sentence to show the user
// fieldErrors -> { fieldName: "message" } to show under each input

const DEFAULT_MESSAGES = {
    401: "Please log in again.",
    403: "You do not have permission to do this.",
    404: "We could not find what you asked for.",
    409: "This already exists.",
    413: "The file is too big. The maximum size is 5 MB.",
};

export function getApiError(error) {
    // No response at all = server is off or no internet
    if (!error?.response) {
        return {
            status: 0,
            message: "Cannot reach the server. Check your internet and try again.",
            fieldErrors: {},
        };
    }

    const status = error.response.status;
    const data = error.response.data;

    // Shape A: validation errors  ->  { "email": "Invalid email format", ... }
    const isFieldMap =
        status === 400 && data && typeof data === "object" && typeof data.error !== "string";
    if (isFieldMap) {
        return { status, message: "Please fix the highlighted fields.", fieldErrors: data };
    }

    // Shape B: everything else  ->  { timestamp, status, error: "Readable message" }
    if (typeof data?.error === "string" && data.error.trim() !== "") {
        return { status, message: data.error, fieldErrors: {} };
    }

    // Backend sent something we do not know. Use a safe default.
    const fallback =
        status >= 500
            ? "Something went wrong on our side. Please try again later."
            : DEFAULT_MESSAGES[status] || `Request failed (${status}). Please try again.`;

    return { status, message: fallback, fieldErrors: {} };
}
```

### Step 2.3 - Add two small methods to `ApiService`

Open `src/api/ApiService.js` and add these inside the class (below `request`).
Also export ONE shared instance at the bottom, so pages do not do `new ApiService()` each time.

```js
	// Call an endpoint object from endpoints.js:  api.call(ENDPOINTS.FARMER_CROPS.LIST)
	call(endpoint, data = {}) {
		return this.request(endpoint.method, endpoint.url, data);
	}

	// Upload a file. Send a FormData object:  api.upload(ENDPOINTS.ME.UPLOAD_PICTURE, formData)
	async upload(endpoint, formData) {
		const response = await this.client.request({
			method: endpoint.method,
			url: endpoint.url,
			data: formData,
			headers: { "Content-Type": "multipart/form-data" },
		});
		return response.data;
	}
```

At the very bottom of the file:

```js
export const api = new ApiService();   // use this in pages
export default ApiService;             // old pages still work
```

### Step 2.4 - Add `src/component/MessageBox.jsx` (replaces every `alert()`)

```jsx
// src/component/MessageBox.jsx
// type = "error" or "success". Shows nothing when text is empty.
function MessageBox({ type = "error", text }) {
    if (!text) return null;

    const style =
        type === "success"
            ? "bg-green-50 text-green-700 border-green-200"
            : "bg-red-50 text-red-700 border-red-200";

    return (
        <div className={`rounded-lg border px-4 py-2 text-sm ${style}`} role="alert">
            {text}
        </div>
    );
}

export default MessageBox;
```

### Step 2.5 - Add `src/api/fileUrl.js` (shows uploaded images)

Uploaded images are served by the backend at `http://localhost:8080/files/...`. Today the profile picture comes as `/files/profile-pics/x.jpeg`, but a product image comes as `product-images/x.jpeg` (the backend team will make them the same). This helper works for both:

```js
// src/api/fileUrl.js
const FILE_BASE_URL = "http://localhost:8080";

// fileUrl(product.productImage)  ->  full address for <img src=...>
export function fileUrl(path) {
    if (!path) return null;                         // no image: show a placeholder in the page
    if (path.startsWith("http")) return path;
    return FILE_BASE_URL + (path.startsWith("/files/") ? path : "/files/" + path);
}
```

---

## 3. The page pattern (copy this for every page)

### 3.1 Load a list (GET)

```jsx
import { useEffect, useState } from "react";
import { api } from "@/api/ApiService.js";
import ENDPOINTS from "@/api/endpoints.js";
import { getApiError } from "@/api/apiError.js";
import MessageBox from "@/component/MessageBox.jsx";

function ExampleListPage() {
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [errorText, setErrorText] = useState("");

    useEffect(() => {
        const loadItems = async () => {
            setIsLoading(true);
            setErrorText("");
            try {
                const data = await api.call(ENDPOINTS.FARMER_CROPS.LIST);
                setItems(data);
            } catch (error) {
                setErrorText(getApiError(error).message);
            } finally {
                setIsLoading(false);
            }
        };
        loadItems();
    }, []);

    if (isLoading) return <p className="text-sm text-gray-500">Loading...</p>;
    if (errorText) return <MessageBox type="error" text={errorText} />;
    if (items.length === 0) return <p className="text-sm text-gray-500">Nothing here yet.</p>;

    return <ul>{items.map((item) => <li key={item.cropId}>{item.cropName}</li>)}</ul>;
}

export default ExampleListPage;
```

### 3.2 Save a form (POST / PUT)

```jsx
const [form, setForm] = useState({ cropName: "", category: "", unit: "kg" });
const [fieldErrors, setFieldErrors] = useState({});
const [formError, setFormError] = useState("");
const [successText, setSuccessText] = useState("");
const [isSaving, setIsSaving] = useState(false);

const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setFormError("");
    setSuccessText("");
    setIsSaving(true);
    try {
        await api.call(ENDPOINTS.FARMER_CROPS.CREATE, form);
        setSuccessText("Crop saved.");
    } catch (error) {
        const err = getApiError(error);
        setFieldErrors(err.fieldErrors);   // red text under each input
        setFormError(err.message);         // message box at the top of the form
    } finally {
        setIsSaving(false);
    }
};

// In the JSX:
// <MessageBox type="error" text={formError} />
// <MessageBox type="success" text={successText} />
// <FormInput ... error={fieldErrors.cropName} />
```

### 3.3 Endpoints with an id in the URL

`endpoints.js` uses small functions for these. Call them like this:

```js
await api.call(ENDPOINTS.FARMER_CROPS.UPDATE(cropId), form);   // PUT /farmer/crops/5
await api.call(ENDPOINTS.FARMER_CROPS.DELETE(cropId));         // DELETE /farmer/crops/5
```

### 3.4 Upload a file

```js
const formData = new FormData();
formData.append("file", selectedFile);            // the key MUST be "file"
await api.upload(ENDPOINTS.FARMER_PRODUCTS.UPLOAD_IMAGE(listId), formData);
```

Allowed: JPEG, PNG, WEBP. Maximum 5 MB.

---

## 4. How errors look and what to show

The backend sends **two shapes** of error. `getApiError` already understands both, so pages only use `err.message` and `err.fieldErrors`.

**Shape A - validation error (status 400):** a plain map of field name to message

```json
{ "email": "Invalid email format", "password": "Password must be at least 8 characters" }
```
Show each message under its input.

**Shape B - everything else:** has an `error` text

```json
{ "timestamp": "2026-10-02T10:00:00.000+00:00", "status": 409, "error": "Username or Email already exists" }
```
Show `error` in a `MessageBox`.

### What each status means

| Status | Meaning | What the page should do |
|---|---|---|
| 0 (no response) | Server off / no internet | Show "Cannot reach the server" |
| 400 + field map | A field is wrong | Show under each input |
| 400 + `error` | Bad request (example: "Not enough stock") | Show `error` in the message box |
| 401 | Not logged in, wrong password, or token expired | On a protected page: clear token and go to `/login`. On the login form: show the message |
| 403 | Logged in but not allowed (example: not a farmer) | Show the message. Do not log out |
| 404 | Item not found | Show the message |
| 409 | Already exists | Show the message |
| 413 | File too big | Show "maximum 5 MB" |
| 500 | Server bug | Show the safe default message |

> **Heads up (found by reading backend code):** today the backend may answer a wrong password or an expired token with **403 and an empty body** instead of 401 with a message. `getApiError` handles that safely (shows a default message). The backend team has this fix in their guide. After they fix it, nothing changes in your code.

### Protect pages and handle expired login

Create `src/routes/ProtectedRoute.jsx` and wrap farmer and buyer pages with it:

```jsx
import { Navigate } from "react-router-dom";

// If there is no token, go to login
function ProtectedRoute({ children }) {
    const token = localStorage.getItem("my_app_token");
    if (!token) return <Navigate to="/login" replace />;
    return children;
}

export default ProtectedRoute;
```

In `FarmerRoutes.jsx` wrap the layout: `<Route element={<ProtectedRoute><FarmerDashboard /></ProtectedRoute>}>`.

When any call returns **401** on a protected page, run `localStorage.removeItem("my_app_token")` and `navigate("/login")`.

---

## 5. Complete `src/api/endpoints.js` (target)

Replace the file with this. Lines marked `// 🔧` have no working backend yet.

```js
// ALL backend URLs live here. Pages never type a URL.
// Base URL (http://localhost:8080/api) is set once in ApiService.js
const ENDPOINTS = {
    // ---------- 1. AUTH ----------
    AUTH: {
        REGISTER: { url: "/auth/register", method: "POST" },
        LOGIN: { url: "/auth/login", method: "POST" },
        FORGOT_PASSWORD: { url: "/auth/forgot-password", method: "POST" },
        RESET_PASSWORD: { url: "/auth/reset-password", method: "POST" },
    },

    // ---------- 1. MY PROFILE ----------
    ME: {
        GET_PROFILE: { url: "/me", method: "GET" },
        UPDATE_PROFILE: { url: "/me", method: "PUT" },
        UPDATE_ROLE: { url: "/me/role", method: "PUT" },
        UPLOAD_PICTURE: { url: "/me/picture", method: "POST" },
        CHANGE_PASSWORD: { url: "/me/password", method: "PUT" }, // 🔧
    },

    // ---------- 2. FARM MANAGEMENT ----------
    FARMER_CROPS: {
        LIST: { url: "/farmer/crops", method: "GET" },
        CREATE: { url: "/farmer/crops", method: "POST" },
        GET_BY_ID: (cropId) => ({ url: `/farmer/crops/${cropId}`, method: "GET" }),
        UPDATE: (cropId) => ({ url: `/farmer/crops/${cropId}`, method: "PUT" }),
        DELETE: (cropId) => ({ url: `/farmer/crops/${cropId}`, method: "DELETE" }),
    },
    FARMER_FIELD_PLOTS: {
        LIST: { url: "/farmer/field-plots", method: "GET" },
        CREATE: { url: "/farmer/field-plots", method: "POST" },
        UPDATE: (id) => ({ url: `/farmer/field-plots/${id}`, method: "PUT" }),
        DELETE: (id) => ({ url: `/farmer/field-plots/${id}`, method: "DELETE" }),
    },
    FARMER_ACTIVITIES: {
        // The dashboard "tasks" list uses these (an activity = a task)
        LIST: { url: "/farmer/activities", method: "GET" },
        CREATE: { url: "/farmer/activities", method: "POST" },
        SET_DONE: (id) => ({ url: `/farmer/activities/${id}/status`, method: "PATCH" }), // 🔧
        DELETE: (id) => ({ url: `/farmer/activities/${id}`, method: "DELETE" }),          // 🔧
    },
    FARMER_EXPENSES: {
        LIST: { url: "/farmer/expenses", method: "GET" },
        CREATE: { url: "/farmer/expenses", method: "POST" },
    },
    FARMER_LIVESTOCK: {
        LIST: { url: "/farmer/livestock", method: "GET" },
        CREATE: { url: "/farmer/livestock", method: "POST" },
        DELETE: (id) => ({ url: `/farmer/livestock/${id}`, method: "DELETE" }),
    },

    // ---------- 3. DASHBOARD, CALENDAR, SUPPORT ----------
    FARMER_DASHBOARD: {
        GET_SUMMARY: { url: "/farmer/dashboard/summary", method: "GET" }, // 🔧
    },
    FARMER_CALENDAR: {
        GET_MONTH: (year, month) => ({ url: `/farmer/calendar?year=${year}&month=${month}`, method: "GET" }), // 🔧
        GET_DATE: (date) => ({ url: `/farmer/calendar/${date}`, method: "GET" }),    // 🔧
        SAVE_DATE: (date) => ({ url: `/farmer/calendar/${date}`, method: "PUT" }),   // 🔧
        DELETE_DATE: (date) => ({ url: `/farmer/calendar/${date}`, method: "DELETE" }), // 🔧
    },
    SUPPORT: {
        SEND_MESSAGE: { url: "/support/messages", method: "POST" },       // 🔧
        LIST_MY_MESSAGES: { url: "/support/messages", method: "GET" },    // 🔧
    },

    // ---------- 4. PRODUCTS (MARKETPLACE) ----------
    PRODUCTS: {
        // Public browse. Query: ?category=Vegetables&keyword=tomato
        LIST_ALL: { url: "/products", method: "GET" },
        GET_BY_ID: (listId) => ({ url: `/products/${listId}`, method: "GET" }),
    },
    FARMER_PRODUCTS: {
        CREATE: { url: "/farmer/products", method: "POST" },
        LIST_MINE: { url: "/farmer/products", method: "GET" },
        UPDATE: (listId) => ({ url: `/farmer/products/${listId}`, method: "PUT" }),
        SET_PUBLISHED: (listId) => ({ url: `/farmer/products/${listId}/status`, method: "PATCH" }),
        DELETE: (listId) => ({ url: `/farmer/products/${listId}`, method: "DELETE" }),
        UPLOAD_IMAGE: (listId) => ({ url: `/farmer/products/${listId}/image`, method: "POST" }),
    },

    // ---------- 5. ORDERS (cart lives in the browser, see section 7.5) ----------
    BUYER_ORDERS: {
        PLACE_ORDER: { url: "/buyer/orders", method: "POST" }, // 🔧
        LIST_MINE: { url: "/buyer/orders", method: "GET" },    // 🔧
    },
    FARMER_ORDERS: {
        LIST_MINE: { url: "/farmer/orders", method: "GET" },   // 🔧
        SET_STATUS: (orderId) => ({ url: `/farmer/orders/${orderId}/status`, method: "PATCH" }), // 🔧
    },
    ORDERS: {
        GET_BY_ID: (orderId) => ({ url: `/orders/${orderId}`, method: "GET" }), // 🔧
    },

    // ---------- 6. RATINGS ----------
    RATINGS: {
        SUBMIT: (orderId) => ({ url: `/orders/${orderId}/rating`, method: "POST" }), // 🔧
        LIST_FOR_USER: (userId) => ({ url: `/users/${userId}/reviews`, method: "GET" }), // 🔧
    },

    // ---------- 7. SHARED DELIVERY ----------
    DELIVERY: {
        CREATE_REQUEST: { url: "/delivery-requests", method: "POST" },    // 🔧
        LIST_MY_REQUESTS: { url: "/delivery-requests", method: "GET" },   // 🔧
        GET_MATCHES: (requestId) => ({ url: `/delivery-requests/${requestId}/matches`, method: "GET" }), // 🔧
        JOIN: (requestId) => ({ url: `/delivery-requests/${requestId}/join`, method: "POST" }),          // 🔧
        GET_STATUS: (deliveryId) => ({ url: `/deliveries/${deliveryId}/status`, method: "GET" }),        // 🔧
    },

    // ---------- 8. NOTIFICATIONS & CHAT (REST part) ----------
    NOTIFICATIONS: {
        LIST_MINE: { url: "/notifications", method: "GET" },                                   // 🔧
        MARK_READ: (id) => ({ url: `/notifications/${id}/read`, method: "PATCH" }),            // 🔧
    },
    CHAT: {
        START: { url: "/chats", method: "POST" },                                              // 🔧
        LIST_CHATS: { url: "/chats", method: "GET" },                                          // 🔧
        LIST_MESSAGES: (chatId) => ({ url: `/chats/${chatId}/messages`, method: "GET" }),      // 🔧
    },
};

export default ENDPOINTS;
```

> Removed from the old file: `/farmer/tasks` (use activities instead), `/farmer/orders/recent`, `/farmer/activities/recent`, `/users/{id}/ratings` (use `/users/{id}/reviews`).

---

## 6. Overall status board

| # | Category | Frontend | Backend |
|---|---|---|---|
| 1 | Authentication and profile | 🟡 | ✅ (small gaps) |
| 2 | Farm management | 🟡 | 🟡 |
| 3 | Dashboard, calendar, support | 🟡 | ❌ |
| 4 | Products (marketplace) | 🟡 | ✅ |
| 5 | Orders and checkout | ❌ | 🟡 fake data |
| 6 | Ratings and reviews | 🟡 UI only | 🟡 fake data |
| 7 | Shared delivery | 🟡 UI only | 🟡 fake data |
| 8 | Chat and notifications | 🟡 UI only | 🟡 not saved |
| 9 | Mobile app and offline | ❌ | n/a |

Build in this order: **1 → 2 → 4 → 3 → 5 → 6 → 7 → 8**.

---

## 7. Category by category

### 7.1 Category 1 - Authentication and profile

**Already done ✅:** Register, Login, Forgot password and Reset password pages call the real API and show errors well. (They are the best examples to copy.)

**To do, in order:**

1. 🟡 **Login redirect is wrong.** `LoginPage.jsx` does `navigate("/dashboard")`, which does not exist. The login response only has `token` and `message`, so after login call `GET /me` and use `role`:
   - `FARMER` → `/farmer/home`
   - `BUYER` → `/home`
   - `TRANSPORT` → `/DeliveryRequest`
   - `null` (new user) → `/choose-role`
2. ❌ **Create `ChooseRolePage`** (route `/choose-role`). New users have **no role**. Show 3 buttons (Farmer, Buyer, Transport). On click call `PUT /me/role` with `{ "role": "FARMER" }`, then redirect as above.
3. ❌ **Add `ProtectedRoute`** (section 4) to every farmer and buyer route.
4. 🟡 **Welcome page** navigates to `/signin`, which does not exist. Change to `/login`. Remove the fake "saving language" code unless you really build language saving.
5. 🟡 **Fix the 2 settings pages** (`FarmerSettingsPage`, `BuyerProfilePage`). They send wrong field names and use fake fallback data. See the table below.
6. ❌ **Profile picture upload** in settings (`POST /me/picture`, file key `file`).
7. 🔧 **Change password** (`PUT /me/password`). The settings page sends `password` to `/me`, but the backend ignores it.
8. 🟡 **Switch the 4 auth pages to `getApiError`** (Login, Register, Forgot, Reset). They do `if (status === 400 && typeof data === "object") setFieldErrors(data)`. That is wrong when the backend sends a 400 with an `error` text, because the text is put in `fieldErrors` and never shown. Replace each catch block with the pattern in section 3.2 (`const err = getApiError(error); setFieldErrors(err.fieldErrors); setFormError(err.message);`). Keep the success flow as it is.

#### API: Register - `POST /auth/register`

Send:

| Key | Type | Rule |
|---|---|---|
| `username` | string | required, max 50 |
| `email` | string | required, valid email, max 100 |
| `password` | string | required, 8 to 255 characters |

Success `201`: `{ "message": "...", "username": "saman" }`

#### API: Login - `POST /auth/login`

Send: `username` OR `email` (one is enough) and `password`

```json
{ "email": "saman@mail.com", "password": "12345678" }
```
Success `200`: `{ "token": "eyJ...", "message": "..." }`. Save as `localStorage "my_app_token"`.
`ApiService` adds the `Authorization: Bearer <token>` header automatically.

#### API: Forgot password - `POST /auth/forgot-password`

Send `{ "email": "saman@mail.com" }`. Success `200`: `{ "message": "..." }`.
The backend answers 200 even if the email does not exist (this is on purpose, for safety). So always show: "If this email is registered, we sent a reset link."

#### API: Reset password - `POST /auth/reset-password`

Send `{ "token": "<from the link ?token=...>", "newPassword": "newpass123" }`. Success `200`: `{ "message": "..." }`.

#### API: My profile - `GET /me`

Response:

| Key | Type | Example |
|---|---|---|
| `userId` | number | 7 |
| `username` | string | "saman" |
| `email` | string | "saman@mail.com" |
| `role` | string or null | "FARMER" |
| `active` | boolean | true |
| `phoneNumber` | string or null | "0712345678" |
| `address` | string or null | "No 45, Farm Road, Kandy" |
| `profilePictureUrl` | string or null | "/files/profile-pics/abc.jpeg" |
| `createdAt` | date | "2026-07-01T10:00:00.000+00:00" |

#### API: Update profile - `PUT /me`

Send only these keys (all optional):

| Key | Type | Rule |
|---|---|---|
| `username` | string | 3 to 50 characters |
| `email` | string | valid email |
| `phoneNumber` | string | 7 to 15 characters, digits, `+`, `-`, spaces |
| `address` | string | max 255 |

Response: same as `GET /me`.

**Fix these mismatches in the two settings pages:**

| Page sends now | Must send | Page reads now | Must read |
|---|---|---|---|
| `fullName` | `username` | `data.name` | `data.username` |
| `phone` | `phoneNumber` | `data.phone` | `data.phoneNumber` |
| `deliveryAddress` | `address` | `data.address` | ok |
| `city`, `district` | **No backend fields.** Remove them from the form, or ask backend to add them (decision D3) | - | - |
| `password` | Remove from this form. Use `PUT /me/password` | - | - |

#### API: Choose role - `PUT /me/role`

Send `{ "role": "FARMER" }`. Allowed: `FARMER`, `BUYER`, `TRANSPORT`.
Response: `{ "userId": 7, "role": "FARMER", "message": "..." }`.
Error `400`: `{ "error": "Invalid role. Allowed values: [FARMER, BUYER, TRANSPORT]" }`.

#### API: Upload picture - `POST /me/picture`

FormData with key `file`. Response: `{ "userId": 7, "profilePictureUrl": "/files/...", "message": "..." }`.
To show the image: `<img src={fileUrl(profile.profilePictureUrl)} />` (helper from step 2.5).

---

### 7.2 Category 2 - Farm management

**Backend ✅ for crops, field plots, activities, expenses, livestock (some only list + add).**
**Frontend:** only the Crop Management page exists, and it is 🟡.

> **Important design problem (Decision D1).**
> The Crop Management page shows `name, variety, planted date, stage, health, notes`. In the backend these belong to **Field Plots** (`currentCrop, cropVariety, growthStage, healthCondition, fieldLogs, inspectionDate`), not to **Crops**. A Crop in the backend is a *harvest record* (`cropName, category, unit, harvestQuantity, harvestDate`).
> And a field plot requires a `cropId`. **Recommended:** the backend makes `cropId` optional (see backend guide), and the Crop Management page uses `/farmer/field-plots`.

**To do, in order:**

1. 🟡 **Rebuild `FarmerCropManagementPage`** on `ENDPOINTS.FARMER_FIELD_PLOTS`. Remove the "add a fake crop when API fails" code.
2. ❌ **Livestock page** (proposal says crops and livestock are equal). List, add, delete.
3. ❌ **Expenses page.** List, add. Show a total at the top (add the `amount` values in the page).
4. ❌ **Farm activities list** (also used as dashboard tasks, see 7.3).
5. ❌ Add the 3 new pages to `FarmerRoutes.jsx` and to the sidebar in `layouts/FarmerDashboard.jsx`.

#### Field plots

`GET /farmer/field-plots` → list. `POST /farmer/field-plots`, `PUT /farmer/field-plots/{id}`, `DELETE /farmer/field-plots/{id}`.

Send:

| Key | Type | Rule |
|---|---|---|
| `currentCrop` | string | required, example "Carrot" |
| `cropVariety` | string | required, example "New Kuroda" |
| `areaUnit` | number | optional, example 2.5 |
| `growthStage` | string | required: `Seedling`, `Vegetative`, `Flowering`, `Harvest` |
| `healthCondition` | string | required: `Excellent`, `Good`, `Alert` |
| `fieldLogs` | string | optional notes |
| `inspectionDate` | string | optional, `"2026-10-02"` (date only) |
| `cropId` | number | required today. Becomes optional after decision D1 |

Response (each item): `fieldPlotId, cropId, cropName, currentCrop, cropVariety, areaUnit, growthStage, healthCondition, fieldLogs, inspectionDate, createdAt, updatedAt`.

#### Crops (harvest records)

`GET/POST /farmer/crops`, `GET/PUT/DELETE /farmer/crops/{cropId}`.

Send:

| Key | Type | Rule |
|---|---|---|
| `cropName` | string | required |
| `category` | string | required, example "Vegetables" |
| `unit` | string | required, example "kg" |
| `harvestQuantity` | number | required |
| `harvestDate` | string | required, **full ISO date-time**: `new Date(inputValue).toISOString()` |
| `notes` | string | optional |

Response: `cropId, cropName, category, unit, harvestQuantity, harvestDate, notes, createdAt, updatedAt`.

#### Expenses

`GET /farmer/expenses`, `POST /farmer/expenses`.

Send: `title` (required), `category` (required), `amount` (required, whole number in LKR), `expenseDate` (optional, ISO date-time).
Response: `expenseId, title, category, amount, expenseDate`.

#### Livestock

`GET /farmer/livestock`, `POST /farmer/livestock`, `DELETE /farmer/livestock/{id}`.

Send: `category` (required, example "Cow"), `breed` (optional), `amount` (required, whole number).
Response: `liveStockId, category, breed, amount`.

#### Activities

`GET /farmer/activities`, `POST /farmer/activities`.
Send: `{ "activity": "Apply fertilizer to plot 2" }`.
Response: `activityId, activity, activityStatus (true = done), createdAt`.
🔧 Toggle done: `PATCH /farmer/activities/{id}/status` with `{ "done": true }`. 🔧 Delete: `DELETE /farmer/activities/{id}`.

**Common errors for all farm endpoints**

| Status | `error` text | When |
|---|---|---|
| 400 | field map | A required field is blank or a number is wrong |
| 400 | "Unparseable date" style message or a default Spring message | You sent `"2026-10-02"` where an ISO date-time is needed |
| 401 | - | Token missing or expired |
| 404 | "Crop not found" (after backend fix; today it may be 403) | Item does not exist or belongs to another farmer |

---

### 7.3 Category 3 - Dashboard, calendar, support (backend not ready 🔧)

#### Dashboard (`FarmerHomePage`)

Today only the crop and product counts are real. Tasks and the orders table are hard-coded.

1. 🔧 Replace the counts with `GET /farmer/dashboard/summary`.
2. Replace hard-coded tasks with farm activities (7.2): list, add, tick (PATCH), delete.
3. Replace the hard-coded orders table with `GET /farmer/orders` (7.5). Show the first 5.
4. **Delete the catch-block that sets fake counts (2 and 3).**

Response of `GET /farmer/dashboard/summary`:

```json
{
  "totalFieldPlots": 3,
  "totalCrops": 5,
  "activeListings": 2,
  "pendingOrders": 1,
  "totalLivestock": 12,
  "monthExpenses": 45000
}
```

#### Calendar (`FarmerCalenderPage`)

The page already calls `GET/PUT /farmer/calendar/{date}`. The backend does not have it yet.

| Call | Send | Response |
|---|---|---|
| `GET /farmer/calendar/2026-10-02` | - | `{ "date": "2026-10-02", "note": "Spray plot 2" }`. If no note: `{ "date": "2026-10-02", "note": "" }` |
| `PUT /farmer/calendar/2026-10-02` | `{ "note": "Spray plot 2" }` | same as above |
| `DELETE /farmer/calendar/2026-10-02` | - | `204` no body |
| `GET /farmer/calendar?year=2026&month=10` | - | `[ { "date": "2026-10-02", "note": "..." } ]` (use it to show dots on the calendar) |

To do in the page: remove the "Saved locally!" fallback, show errors with `MessageBox`, remove `alert()`. Note: `note` max length 500.

#### Help and support (`FarmerHelpSupportPage`)

Page sends `{ subject, message }` and, on failure, pretends it was "logged locally". **Remove that fake success.**

| Call | Send | Response |
|---|---|---|
| `POST /support/messages` | `{ "subject": "Cannot add crop", "message": "..." }` (subject max 100, message max 1000) | `201` `{ "messageId": 1, "subject": "...", "message": "...", "createdAt": "..." }` |
| `GET /support/messages` | - | list of the same objects |

---

### 7.4 Category 4 - Products (marketplace)

**Backend ✅ complete.**

**To do, in order:**

1. 🟡 **Fix `FarmerAddHarvestPage` payload** (names do not match, image not uploaded):

| Page sends now | Must send |
|---|---|
| `name` | `productName` |
| `category` | `category` |
| `unit` | `unitOfMeasurement` |
| `price` | `pricePerUnit` |
| `totalAvailable` | `availableStock` |
| `minOrder` | `minimumOrderQuantity` |
| `harvestDate` (date only) | `harvestedDate` (full ISO: `new Date(value).toISOString()`) |
| `deliveryMethod` | `deliveryOption` (`Pickup`, `Delivery` or `Both`) |
| `description` | `description` |
| `image` (blob URL) | **Do not send.** Upload the file in a second call (below) |

2. 🟡 **Upload the image in 2 steps:** (a) `POST /farmer/products` → you get `listId`. (b) `POST /farmer/products/{listId}/image` with FormData key `file`.
3. 🟡 **Fix `FarmerManageHarvestPage`:** it reads `item.name, item.price, item.stock, item.totalAvailable, item.minOrder, item.id`. It must read `productName, pricePerUnit, availableStock, minimumOrderQuantity, listId`. Edit sends the same keys as create.
4. ❌ **Add a Publish / Unpublish button.** New products start **unpublished**, so buyers cannot see them until the farmer publishes. Call `PATCH /farmer/products/{listId}/status` with `{ "published": true }`.
5. ❌ **Build the buyer Home page** (`Home.jsx` is only a "TODO"): product cards, search box (`keyword`), category filter. Use `GET /products?category=...&keyword=...`.
6. 🟡 **Connect `ProductClick`** (product details) to `GET /products/{listId}`. Remove all dummy data (Green Valley Farms, fake reviews). Read the route id from the URL (change route to `/product/:listId`).
7. ❌ **Add to cart button** (section 7.5).

Product response (list and single):

| Key | Type |
|---|---|
| `listId` | number |
| `farmerId`, `farmerName` | number, string |
| `productName`, `category`, `description` | string |
| `unitOfMeasurement` | string (example "kg") |
| `pricePerUnit`, `availableStock`, `minimumOrderQuantity` | number |
| `harvestedDate`, `createdAt`, `updatedAt` | ISO date-time |
| `deliveryOption` | `Pickup`, `Delivery`, `Both` |
| `productImage` | string or null. Show it with `fileUrl(productImage)` (step 2.5) |
| `listingStatus` | boolean (`true` = published) |

Errors: `400` field map (blank name, price must be above 0, stock cannot be negative). `400` "Product listing not found: 12" (wrong id; will become 404). `403` "Only farmers can manage product listings" (user role is not FARMER). `400` "File exceeds maximum size of 5MB" / "Only JPEG, PNG, and WEBP images are allowed".

---

### 7.5 Category 5 - Orders and checkout (backend 🔧)

> **Decision D2 (recommended):** keep the **cart in the browser** (React state + `localStorage`), not in the database. There is no cart table. The cart is sent once at checkout. This removes 4 backend endpoints. The old fake `/buyers/{id}/cart` endpoints will be deleted.

**To do, in order:**

1. ❌ Create `src/context/CartContext.jsx` (add item, change quantity, remove item, clear, total). Save to `localStorage` key `ranaswanu_cart`.
2. ❌ Add "Add to cart" on the product details page. Check `quantity >= minimumOrderQuantity` and `<= availableStock` before adding.
3. ❌ **Cart page** and **Checkout page** (delivery address, phone, payment method, notes).
4. 🔧 `POST /buyer/orders` on "Place order". On success: clear the cart and go to the order page.
5. 🟡 **Buyer orders list** in `BuyerProfilePage`: replace the sample list with `GET /buyer/orders`.
6. ❌ **Farmer orders page** (`/farmer/orders`): list, accept / reject / ship / complete buttons.
7. ❌ **Order details page** (`GET /orders/{orderId}`).

#### Place order - `POST /buyer/orders`

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
| `items` | array | required, at least 1 |
| `items[].listId` | number | required |
| `items[].quantity` | number | required, above 0 |
| `deliveryAddress` | string | required, max 255 |
| `contactNumber` | string | required, 7 to 15 characters |
| `paymentMethod` | string | `CASH_ON_DELIVERY` or `BANK_TRANSFER`. **Never send card numbers.** |
| `notes` | string | optional, max 255 |

Success `201`. The backend creates **one order for each farmer**. If the cart has items from 2 farmers, you get 2 orders:

```json
{
  "orders": [
    {
      "orderId": 501,
      "farmerId": 7,
      "farmerName": "Saman Perera",
      "orderStatus": "PENDING",
      "paymentStatus": "UNPAID",
      "totalAmount": 1530.00,
      "orderDate": "2026-10-02T10:00:00Z",
      "deliveryAddress": "No 45, Temple Road, Badulla",
      "contactNumber": "0771234567",
      "paymentMethod": "CASH_ON_DELIVERY",
      "items": [
        { "itemId": 1, "listId": 12, "productName": "Tomatoes", "quantity": 5, "unitPrice": 120.00, "subtotal": 600.00 }
      ]
    }
  ]
}
```

After success: clear the cart and go to the buyer orders page.

Errors to show:

| Status | `error` text (example) | Meaning |
|---|---|---|
| 400 | field map | Missing address, phone, or empty `items` |
| 400 | "Not enough stock for Tomatoes. Available: 3 kg" | Stock changed. Update the cart |
| 400 | "Minimum order for Tomatoes is 2 kg" | Quantity too small |
| 404 | "Product not found" or "Product is not available" | Product was unpublished or deleted. Remove it from the cart |

#### Lists and details

| Call | Response |
|---|---|
| `GET /buyer/orders` | `[ { orderId, farmerId, farmerName, orderStatus, paymentStatus, totalAmount, orderDate, itemCount, firstItemName } ]` |
| `GET /farmer/orders` | `[ { orderId, buyerName, contactNumber, orderStatus, paymentStatus, totalAmount, orderDate, itemCount, firstItemName } ]` |
| `GET /orders/{orderId}` | one full order (same shape as one entry of `orders` above) + `buyerName` |
| `PATCH /farmer/orders/{orderId}/status` | send `{ "status": "ACCEPTED" }` (allowed: `ACCEPTED`, `REJECTED`, `SHIPPED`, `COMPLETED`). Response `{ "orderId": 501, "orderStatus": "ACCEPTED" }` |

Order statuses in order: `PENDING → ACCEPTED → SHIPPED → COMPLETED` (or `REJECTED`). Draw the timeline in the page from `orderStatus`. No timeline API is needed.

Errors: `404` "Order not found" (also used when the order is not yours). `400` "Invalid status. Allowed values: [...]". `403` "Only farmers can update orders".

---

### 7.6 Category 6 - Ratings and reviews (backend 🔧)

Already built (UI only): `StarRating.jsx`, `ReviewsList.jsx`.

1. ❌ **"Rate this order" form** on the order details page. Show it only when `orderStatus` is `COMPLETED`. Stars 1 to 5 + comment.
2. 🟡 Connect `ReviewsList` and the star average on the product / seller page to `GET /users/{userId}/reviews`.

#### Submit - `POST /orders/{orderId}/rating`

```json
{ "score": 5, "comment": "Fresh and on time" }
```
Rules: `score` 1 to 5 (required), `comment` max 500 (optional). The person being rated is the farmer of that order (the backend knows it, you do not send it).

Success `201`: `{ "ratingId": 9, "orderId": 501, "revieweeId": 7, "score": 5, "comment": "...", "submittedAt": "..." }`

Errors: `400` "Score must be between 1 and 5". `400` "You can only rate a completed order". `409` "You already rated this order". `404` "Order not found".

#### List - `GET /users/{userId}/reviews`

```json
{
  "userId": 7, "averageRating": 4.6, "totalReviews": 23,
  "reviews": [ { "ratingId": 9, "orderId": 501, "reviewerName": "Kasun", "score": 5, "comment": "...", "date": "2026-10-01" } ]
}
```

---

### 7.7 Category 7 - Shared delivery (backend 🔧)

Pages exist but use hard-coded `MATCHES`, `STEPS`, `UPDATES`: `DeliveryRequestPage`, `MatchingDeliveriesPage`, `DeliveryTrackingPage`.

1. 🟡 `DeliveryRequestPage`: remove default text values ("No. 25, Galle Road..." and the date 2025-05-24). Add vehicle type. Submit with `POST /delivery-requests`. After success go to `/MatchineDeliveries?requestId=<id>` (the route name has a spelling mistake, fix to `/matching-deliveries`).
2. 🟡 `MatchingDeliveriesPage`: load `GET /delivery-requests/{id}/matches`. Show a Join button.
3. 🟡 `DeliveryTrackingPage`: load `GET /deliveries/{id}/status`. Build the step list from `status`.

#### Create request - `POST /delivery-requests`

```json
{
  "pickupLocation": "No 25, Galle Road, Colombo 03",
  "destination": "Katunayake",
  "preferredDateTime": "2026-10-10T14:30:00Z",
  "vehicleType": "Lorry",
  "estimatedWeight": 50,
  "size": "30 x 20 x 15",
  "description": "Vegetables",
  "specialInstructions": "Keep cool"
}
```

Required: `pickupLocation`, `destination`, `preferredDateTime`, `vehicleType`, `estimatedWeight` (kg, whole number above 0), `size`. Optional: `description`, `specialInstructions`.
The page has separate `date` and `time` inputs. Join them: `new Date(`${date}T${time}`).toISOString()`.

Success `201`: `{ "requestId": 701, "status": "OPEN", "pickupLocation": "...", "destination": "...", "preferredDateTime": "...", "createdAt": "..." }`

#### Other calls

| Call | Response |
|---|---|
| `GET /delivery-requests` (my requests) | list of `{ requestId, pickupLocation, destination, preferredDateTime, status }` |
| `GET /delivery-requests/{id}/matches` | `{ "requestId": 701, "totalMatches": 2, "matches": [ { "requestId": 702, "userName": "Nimal", "pickupLocation": "...", "destination": "...", "preferredDateTime": "...", "matchScore": 0.85, "estimatedSavingPercent": 30 } ] }` (show `matchScore * 100` as "match %") |
| `POST /delivery-requests/{id}/join` | `{id}` is **your own** request. Send the request you want to share with: `{ "withRequestId": 702 }`. Response `{ "requestId": 701, "deliveryId": 801, "status": "MATCHED", "estimatedSavingPercent": 50 }` |
| `GET /deliveries/{id}/status` | `{ "deliveryId": 801, "status": "IN_TRANSIT", "estimatedArrival": "...", "lastUpdated": "..." }` |

Statuses: `OPEN`, `MATCHED`, `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.
Errors: `400` field map. `404` "Delivery request not found". `409` "You already joined this delivery". `403` "Only transport or farmer users can create requests" (if backend adds a role rule).

---

### 7.8 Category 8 - Chat and notifications (backend 🔧)

Existing: `ChatPage` (sample contacts), `NotificationBell` + `NotificationPanel` (sample list passed as a prop).

**Part A - Notifications (do this first, it is simple)**

1. 🔧 `GET /notifications` → `[ { "notificationId": 1, "title": "Order accepted", "message": "...", "isRead": false, "createdAt": "..." } ]`.
2. `PATCH /notifications/{id}/read` → `{ "notificationId": 1, "isRead": true }`.
3. Load them inside `NotificationBell` (not hard-coded in `ChatPage`). Show the unread count.

**Part B - Chat**

0. 🔧 Start a chat (for example, "Chat with seller" on the product page): `POST /chats` with `{ "otherUserId": 9 }` → returns `{ "chatId": 3, "otherUserId": 9, "otherUserName": "Kasun" }`. If the chat already exists you get the same chat back.
1. 🔧 Load the chat list: `GET /chats` → `[ { "chatId": 3, "otherUserId": 9, "otherUserName": "Kasun", "lastMessage": "...", "updatedAt": "..." } ]`.
2. 🔧 Load history: `GET /chats/{chatId}/messages` → `[ { "messageId": 1, "senderId": 7, "content": "Hello", "sentAt": "...", "isRead": true } ]`.
3. Real time: add packages `@stomp/stompjs` and `sockjs-client`. Connect to `http://localhost:8080/ws/chat` and send your token in the connect headers: `connectHeaders: { Authorization: "Bearer " + token }`. Send to `/app/chat/{chatId}` with `{ "content": "Hi" }` (the backend knows who you are from the token, so do not send `senderId`). Listen on `/topic/chat/{chatId}`. Notifications: `/topic/notifications/{userId}`.
4. Put the WebSocket code in ONE file, `src/api/chatSocket.js`. Pages must not create sockets.

---

### 7.9 Category 9 - Mobile app and offline

The proposal says Android app (Java) with SQLite offline sync. There is no Android project in the files. `package.json` has **Capacitor Android**, which wraps this React app. Ask your supervisor whether Capacitor is accepted. Until then, nothing to build here.

---

## 8. Known bugs checklist (tick when fixed)

- [ ] `@/api/ENDPOINTS` import case
- [ ] `DevRouteList` file name case
- [ ] Delete duplicate reset-password page and `authService.js`
- [ ] Login redirect `/dashboard`, welcome redirect `/signin`
- [ ] Settings pages send wrong keys (`fullName`, `phone`)
- [ ] Add Harvest and Manage Harvest wrong keys
- [ ] Add Harvest does not upload the image
- [ ] No publish button (buyers see nothing)
- [ ] Every `alert()` and "saved locally" / "using defaults" fake fallback removed
- [ ] `ProductClick`, `ChatPage`, delivery pages still use dummy data
- [ ] Auth pages use lime, farmer pages use green. Choose green-600 everywhere

---

## 9. Decisions the team must make

| # | Question | Recommended |
|---|---|---|
| D1 | Crop Management page uses Crops or Field Plots? | Field Plots, with `cropId` optional |
| D2 | Cart in browser or in database? | Browser (less work) |
| D3 | Keep `city` and `district` on the buyer profile? | Remove (put everything in `address`) |

Write the final answer here after you decide: ______________________

---

## 10. AI prompt for frontend work

Paste this at the start of a new AI chat. Then add your task.

````text
You are helping me build the React frontend of "Ranaswanu" (smart farming + crop selling).
Stack: React 19 (Vite), Tailwind CSS v4, React Router, axios. Backend: Spring Boot at http://localhost:8080/api.
I am a student. Write simple code that is easy to read.

HOW THIS PROJECT CALLS THE BACKEND (follow exactly)
- All URLs are in src/api/endpoints.js. NEVER type a URL inside a page.
- Pages import:  import { api } from "@/api/ApiService.js";  import ENDPOINTS from "@/api/endpoints.js";
- Normal call:   const data = await api.call(ENDPOINTS.FARMER_CROPS.LIST);
- With data:     await api.call(ENDPOINTS.FARMER_CROPS.CREATE, form);
- With an id:    await api.call(ENDPOINTS.FARMER_CROPS.UPDATE(cropId), form);
- File upload:   await api.upload(ENDPOINTS.FARMER_PRODUCTS.UPLOAD_IMAGE(listId), formData);  (FormData key must be "file")
- Never use axios or fetch inside a page. Never create new ApiService instances.
- If I need an endpoint that is not in endpoints.js, add it to endpoints.js first and tell me.

ERRORS (follow exactly)
- import { getApiError } from "@/api/apiError.js";
- In every catch:  const err = getApiError(error);   err.message = text for a message box, err.fieldErrors = { fieldName: "text" } for under inputs.
- Show messages with <MessageBox type="error" text={...} /> and <MessageBox type="success" text={...} /> from "@/component/MessageBox.jsx".
- NEVER use alert(). NEVER fake success. NEVER save fake data when the API fails.
- A 401 on a protected page = remove token "my_app_token" and go to /login.

PAGE RULES
- Every page that loads data has 3 states: loading, error, empty.
- Form field names MUST be exactly the backend names (example: productName, pricePerUnit, availableStock, minimumOrderQuantity, unitOfMeasurement, deliveryOption). If I did not give you the exact names, ASK me. Do not guess.
- Dates: send full ISO text for Instant fields:  new Date(value).toISOString().  Date-only fields (inspectionDate) stay "2026-10-02".
- Use the "@" import alias. File names are PascalCase and must match the import exactly. One component per file, under about 150 lines.
- Pages go in src/page/<role>/, shared parts in src/component/.
- Comments: plain English, explain WHY. No hard-coded sample data. If something is not connected yet, write // TODO: connect to <METHOD> <URL> and tell me.

LOOK (Tailwind only, do not invent new styles)
- Primary green-600, hover green-700. Page bg-gray-50. Card: bg-white rounded-2xl border border-gray-100 shadow-sm p-6.
- Page title: text-2xl font-bold text-gray-800. Body: text-sm text-gray-700. Secondary: text-sm text-gray-500.
- Primary button: bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl px-5 py-2.5.
- Secondary button: bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 rounded-xl.
- Danger button: bg-red-600 hover:bg-red-700 text-white rounded-xl.
- Input: w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:border-green-600 focus:ring-2 focus:ring-green-100 outline-none.
- Page content: max-w-6xl mx-auto. Spacing only gap-4 / gap-6 and p-4 / p-6. Reuse CustomButton and FormInput.

HOW TO ANSWER
1. First list the files you will create or change (one line each).
2. Then give COMPLETE files, each starting with its full path. No "rest of code here".
3. Change only what I asked.
4. End with max 3 lines: what changed, what I must run or check, and anything the backend must do first.

THE API CONTRACT FOR MY TASK (I will paste the section from FRONTEND_GUIDE.md here):
<paste the API section here>

MY TASK:
<write your task here>
````
