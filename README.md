# MockServer User Guide

Welcome! This guide will walk you through running the mock server and configuring it to respond to any HTTP request you need. No deep Java knowledge required — most of the work is just editing YAML files.

---

## Table of Contents

1. [What is this?](#1-what-is-this)
2. [Prerequisites](#2-prerequisites)
3. [Running the Application](#3-running-the-application)
4. [How It Works](#4-how-it-works)
5. [Configuring Your First Mock](#5-configuring-your-first-mock)
6. [HTTP Method Examples](#6-http-method-examples)
   - [GET](#get)
   - [POST](#post)
   - [PUT](#put)
   - [PATCH](#patch)
   - [DELETE](#delete)
7. [Request Matching Options](#7-request-matching-options)
   - [Path Parameters](#path-parameters)
   - [Query Parameters](#query-parameters)
   - [Headers](#headers)
   - [Request Body Matching](#request-body-matching)
8. [Advanced Response Options](#8-advanced-response-options)
   - [Response Delays](#response-delays)
   - [Response Sequencing](#response-sequencing)
9. [Server Configuration](#9-server-configuration)
10. [Tips and Common Mistakes](#10-tips-and-common-mistakes)

---

## 1. What is this?

This application starts a local HTTP server (default port **8000**) that responds to requests with pre-configured fake (mock) responses. It's useful when you're building or testing a frontend/mobile app and the real backend isn't ready yet — you just define what you want the server to say, and it says it.

---

## 2. Prerequisites

Make sure you have the following installed:

- **Java 21** or higher
- **Maven** (comes bundled via `./mvnw` so you don't need to install it separately)

To check:
```bash
java -version   # should show 21+
```

---

## 3. Running the Application

Open a terminal in the project root folder and run:

```bash
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

You should see output like:
```
-------------------------------------------------------
Starting MockServer
Loaded 2 folder(s): get/, other/
Loaded 12 mock expectation(s) total
Server started at port:           8000
Server started at remote address: localhost
Server started at status:         true
-------------------------------------------------------
```

The server is now running at `http://localhost:8000`. Leave this terminal open while you use the mock server.

To stop the server, press `Ctrl + C` in the terminal.

> **Hot reload is enabled.** When you save a change to any YAML file, the server automatically detects it and reloads all expectations — no restart needed.

---

## 4. How It Works

All mock configurations live in YAML files inside `src/main/resources/`:

```
src/main/resources/
├── get/        ← PUT YOUR GET REQUEST MOCKS HERE
├── other/      ← PUT POST, PUT, PATCH, DELETE MOCKS HERE
└── sample/     ← Reference examples (read-only, not loaded by the server)
```

- Every `.yaml` file inside the configured folders is automatically loaded when the server starts.
- You can have **multiple files** in each folder (e.g. one file per service or feature).
- A single file can contain **multiple mocks**, separated by `---`.
- If a YAML entry is missing a required field (e.g. `method`, `path`, `statusCode`), it is **skipped with a warning** rather than crashing the server.

### Configuring which folders are loaded

The folders the server scans are controlled by `src/main/resources/application.properties`:

```properties
mockserver.yaml.folders=get/,other/
```

You can add, remove, or rename folders here without touching any code. For example, to organise mocks by team or service:

```properties
mockserver.yaml.folders=get/,other/,payments/,notifications/
```

**To add your own folder:**
1. Add the folder name (with trailing `/`) to `mockserver.yaml.folders`
2. Create the folder: `src/main/resources/payments/`
3. Add your `.yaml` files inside it
4. The server picks them up automatically on the next file save (hot reload) or restart

---

## 5. Configuring Your First Mock

Every mock follows this two-part structure:

```yaml
---
request:
  method: "GET"
  path: "/hello"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    {
      "message": "Hello, world!"
    }
```

**Step-by-step:**

1. Create a new `.yaml` file inside `src/main/resources/get/` (for GET requests) or `src/main/resources/other/` (for POST, PUT, PATCH, DELETE).
   - Example: `src/main/resources/get/my-service.yaml`
2. Paste your mock configuration into the file.
3. Save the file — the server reloads automatically.
4. Test it:
   ```bash
   curl http://localhost:8000/hello
   ```
   You should see: `{"message": "Hello, world!"}`

---

## 6. HTTP Method Examples

### GET

GET mocks go in `src/main/resources/get/`.

**Simple GET — no conditions:**
```yaml
---
request:
  method: "GET"
  path: "/api/users"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    [
      { "id": 1, "name": "Alice" },
      { "id": 2, "name": "Bob" }
    ]
```

**GET with a query parameter** (`/api/users?role=admin`):
```yaml
---
request:
  method: "GET"
  path: "/api/users"
  queryParameters:
    - name: "role"
      value: "admin"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    [
      { "id": 1, "name": "Alice", "role": "admin" }
    ]
```

**GET with a path parameter** (`/api/users/42`):
```yaml
---
request:
  method: "GET"
  path: "/api/users/{id}"
  pathParameters:
    - name: "id"
      value: "42"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 42, "name": "Alice" }
```

**GET returning a 404 error:**
```yaml
---
request:
  method: "GET"
  path: "/api/users/999"
response:
  statusCode: 404
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "error": "User not found" }
```

---

### POST

POST mocks go in `src/main/resources/other/`.

**Simple POST — no body matching (matches any body):**
```yaml
---
request:
  method: "POST"
  path: "/api/users"
response:
  statusCode: 201
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 99, "message": "User created" }
```

**POST matching a specific JSON body:**
```yaml
---
request:
  method: "POST"
  path: "/api/login"
  requestBody:
    type: "JSON"
    matcher: |
      {"username": "alice", "password": "secret123"}
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "token": "abc-123-xyz" }
```

> **Note:** `JSON` type does strict matching — all fields in `matcher` must match exactly.

**POST matching a form-encoded body** (when `Content-Type: application/x-www-form-urlencoded`):
```yaml
---
request:
  method: "POST"
  path: "/api/login"
  headers:
    - name: "Content-Type"
      value: "application/x-www-form-urlencoded"
  requestBody:
    type: "PARAMETERS"
    parameters:
      - name: "username"
        value: "alice"
      - name: "password"
        value: "secret123"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "token": "abc-123-xyz" }
```

---

### PUT

PUT mocks go in `src/main/resources/other/`.

**PUT to update a resource:**
```yaml
---
request:
  method: "PUT"
  path: "/api/users/42"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 42, "name": "Alice Updated", "message": "User updated successfully" }
```

**PUT matching a specific JSON body:**
```yaml
---
request:
  method: "PUT"
  path: "/api/users/42"
  requestBody:
    type: "JSON"
    matcher: |
      {"name": "Alice Updated", "email": "alice@example.com"}
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 42, "name": "Alice Updated" }
```

---

### PATCH

PATCH mocks go in `src/main/resources/other/`.

**PATCH to partially update a resource:**
```yaml
---
request:
  method: "PATCH"
  path: "/api/users/42"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 42, "message": "User partially updated" }
```

**PATCH matching on a specific field using JSON_PATH** (matches any body where `email` field is present):
```yaml
---
request:
  method: "PATCH"
  path: "/api/users/42"
  requestBody:
    type: "JSON_PATH"
    matcher: |
      $.email
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "id": 42, "message": "Email updated" }
```

---

### DELETE

DELETE mocks go in `src/main/resources/other/`.

**Simple DELETE:**
```yaml
---
request:
  method: "DELETE"
  path: "/api/users/42"
response:
  statusCode: 204
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    {}
```

**DELETE returning an error if not authorised:**
```yaml
---
request:
  method: "DELETE"
  path: "/api/users/42"
  headers:
    - name: "Authorization"
      value: "invalid-token"
response:
  statusCode: 403
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "error": "Forbidden" }
```

> **Ordering matters:** If two mocks match the same path, the one that appears **first** in the file (or is loaded first alphabetically) wins. Put more specific mocks (with headers or body matching) **before** less specific ones.

---

## 7. Request Matching Options

You can mix and match any of the options below to make your mock as specific or as broad as you need.

### Path Parameters

Use `{paramName}` in the path and then list expected values under `pathParameters`. The mock only fires when the URL segment matches that specific value.

```yaml
request:
  method: "GET"
  path: "/api/orders/{orderId}"
  pathParameters:
    - name: "orderId"
      value: "101"
```

This matches `/api/orders/101` but NOT `/api/orders/202`.

To match **any** value for a path segment, use a **regex wildcard** in the path instead and omit `pathParameters`:

```yaml
request:
  method: "GET"
  path: "/api/orders/.*"
```

This matches `/api/orders/101`, `/api/orders/abc`, anything.

---

### Query Parameters

List the query parameters you want to match. If a request includes the listed parameters with the listed values, the mock fires.

```yaml
request:
  method: "GET"
  path: "/api/products"
  queryParameters:
    - name: "category"
      value: "electronics"
    - name: "inStock"
      value: "true"
```

This matches `/api/products?category=electronics&inStock=true`.

> Extra query parameters in the request are **ignored** — only the ones you list need to match.

---

### Headers

You can require specific request headers to be present with specific values.

```yaml
request:
  method: "GET"
  path: "/api/secure-data"
  headers:
    - name: "Authorization"
      value: "Bearer my-secret-token"
```

This mock only fires when the request includes that exact `Authorization` header.

---

### Request Body Matching

Available for all HTTP methods. There are six matching types:

#### `JSON` — Exact JSON match

The request body must contain exactly these fields and values.

```yaml
requestBody:
  type: "JSON"
  matcher: |
    {"username": "alice", "role": "admin"}
```

#### `JSON_PATH` — Match when a JSONPath expression returns results

The body must contain at least one element matching the JSONPath expression. Useful for partial matching.

```yaml
requestBody:
  type: "JSON_PATH"
  matcher: |
    $.items[?(@.price > 100)]
```

#### `REGEX` — Regular expression match on the raw body string

```yaml
requestBody:
  type: "REGEX"
  matcher: |
    .*"status"\s*:\s*"active".*
```

#### `XML` — Exact XML match

```yaml
requestBody:
  type: "XML"
  matcher: |
    <user><name>Alice</name></user>
```

#### `XPATH` — Match when an XPath expression returns results (for XML bodies)

```yaml
requestBody:
  type: "XPATH"
  matcher: |
    /users/user[name='Alice']
```

#### `PARAMETERS` — URL-encoded form fields (`application/x-www-form-urlencoded`)

```yaml
requestBody:
  type: "PARAMETERS"
  parameters:
    - name: "email"
      value: "alice@example.com"
    - name: "password"
      value: "secret"
```

---

## 8. Advanced Response Options

### Response Delays

You can simulate a slow API by adding a `delay` to any response. This is useful for testing loading states or timeout handling in your app.

```yaml
---
request:
  method: "GET"
  path: "/api/slow-endpoint"
response:
  statusCode: 200
  delay:
    unit: MILLISECONDS    # MILLISECONDS or SECONDS
    value: 1500
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "message": "This response took 1.5 seconds" }
```

| `unit` value | Example `value` | Actual delay |
|---|---|---|
| `MILLISECONDS` | `500` | 0.5 seconds |
| `MILLISECONDS` | `2000` | 2 seconds |
| `SECONDS` | `3` | 3 seconds |

---

### Response Sequencing

Sometimes you need the same endpoint to return **different responses on successive calls** — for example, a job status endpoint that first returns `202 Accepted` and then `200 OK` once done.

Use the optional `times` field to limit how many times a mock matches, then add a fallback entry below it:

```yaml
---
# First call returns 202
request:
  method: "GET"
  path: "/api/job/status"
times: 1
response:
  statusCode: 202
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "status": "processing" }
---
# All subsequent calls return 200
request:
  method: "GET"
  path: "/api/job/status"
response:
  statusCode: 200
  headers:
    - name: "Content-Type"
      value: "application/json"
  body: |
    { "status": "done" }
```

> Omitting `times` means the mock matches **unlimited** times (the default).

---

## 9. Server Configuration

All server settings live in `src/main/resources/application.properties`:

```properties
# Port the mock server listens on
mockserver.port=8000

# Internal thread pool sizes (increase for high load)
mockserver.nio.threads=10
mockserver.action.handler.threads=10

# Comma-separated list of resource folders to scan for YAML mocks
mockserver.yaml.folders=get/,other/
```

**Changing the port:** Update `mockserver.port` and restart. Then use the new port in your app and curl commands, e.g. `http://localhost:9000`.

**Adding a new folder:**
1. Add it to `mockserver.yaml.folders`, e.g. `get/,other/,payments/`
2. Create `src/main/resources/payments/`
3. Add your `.yaml` files — the server hot-reloads them automatically

---

## 10. Tips and Common Mistakes

**My mock isn't matching even though the path looks right**
Check that:
- The file is in a folder listed in `mockserver.yaml.folders`
- The `method` field exactly matches what you're sending (e.g. `"POST"`, not `"post"`)
- Any `pathParameters` values match exactly — they are not wildcards

**The server skipped my mock on startup**
Look for a `[WARN] Skipping invalid mock` line in the server output. It will tell you exactly which file and which required field is missing (`method`, `path`, or `statusCode`).

**I want one path to behave differently based on the body**
Add two mock entries for the same path — one with a specific `requestBody` matcher, and one without (as a fallback). Put the specific one first:

```yaml
---
# Matches only when body is {"type": "premium"}
request:
  method: "POST"
  path: "/api/subscribe"
  requestBody:
    type: "JSON"
    matcher: |
      {"type": "premium"}
response:
  statusCode: 200
  body: |
    {"plan": "premium", "price": 99}
---
# Fallback — matches any other body
request:
  method: "POST"
  path: "/api/subscribe"
response:
  statusCode: 200
  body: |
    {"plan": "basic", "price": 9}
```

**Using regex in the path**
You can use any Java regex pattern directly in `path`. Some useful examples:

| Pattern | Matches |
|---|---|
| `/api/users/.*` | Any segment after `/api/users/` |
| `/api/v[0-9]/users` | `/api/v1/users`, `/api/v2/users`, etc. |
| `/api/users/.*/orders` | `/api/users/42/orders`, `/api/users/abc/orders`, etc. |

**Formatting the `body` field**
Use the YAML block scalar `|` to write multi-line JSON bodies. Make sure the JSON is indented consistently under `body:`:

```yaml
  body: |
    {
      "key": "value"
    }
```

**Adding multiple mocks to the same file**
Separate each mock with `---` (three dashes on their own line):

```yaml
---
request:
  method: "GET"
  path: "/api/ping"
response:
  statusCode: 200
  body: |
    {"status": "ok"}
---
request:
  method: "GET"
  path: "/api/version"
response:
  statusCode: 200
  body: |
    {"version": "1.0.0"}
```
