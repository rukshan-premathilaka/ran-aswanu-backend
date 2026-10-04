# Backend on Heroku (Spring Boot + Heroku Postgres + Cloudinary)

## 0. Before you start
- DELETE `src/main/resources/application-local.yaml` and `application-local.example.yaml` (old SQL Server settings, and the first one contains real passwords).
- Change the passwords that were inside it: Gmail app password, JWT secret, old DB password.
- Create a free Cloudinary account -> Dashboard -> copy the **API Environment variable** (`cloudinary://KEY:SECRET@CLOUD`).

## 1. Run locally first (optional but recommended)
1. Install PostgreSQL, create a database: `createdb ranaswanu`
2. Put your DB password and Gmail app password in `.env`
3. `./mvnw spring-boot:run`  -> Flyway creates all 19 tables automatically.

## 2. Create the Heroku app
```
heroku login
heroku create your-api-name
heroku buildpacks:set heroku/java
heroku addons:create heroku-postgresql:essential-0
heroku ps:type web=basic
```
- If a plan name is rejected, add the cheapest (about $5) **Heroku Postgres** plan and the **Basic** dyno from the dashboard (Resources tab). Your student credit covers both (max $13 per month).
- Basic does not sleep; Eco sleeps after 30 minutes and cuts live chat.

## 3. Config Vars (Settings -> Config Vars)
```
heroku config:set JWT_SECRET="$(openssl rand -base64 48)"
heroku config:set MAIL_USERNAME=ranaswanu.co@gmail.com MAIL_PASSWORD="your gmail app password"
heroku config:set CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
heroku config:set FRONTEND_RESET_URL=https://your-app.vercel.app/reset-password
heroku config:set CLOUDINARY_URL="cloudinary://KEY:SECRET@CLOUD"
```
`DATABASE_URL` and `JDBC_DATABASE_*` are created by Heroku itself. `PORT` too.
If you use several domains: `CORS_ALLOWED_ORIGINS=https://a.vercel.app,https://b.com` (no trailing slash).

## 4. Deploy
- Dashboard -> Deploy -> connect your GitHub repo -> Deploy Branch, **or** `git push heroku master`.
- Watch: `heroku logs --tail`  (look for "Started RanaswanuApplication" and "Image uploads use Cloudinary").

## 5. Connect the frontend (Vercel -> Settings -> Environment Variables -> Redeploy)
```
VITE_API_BASE_URL=https://your-api-name-xxxx.herokuapp.com/api
VITE_FILES_BASE_URL=https://your-api-name-xxxx.herokuapp.com
VITE_WS_URL=https://your-api-name-xxxx.herokuapp.com/ws/chat
```

## 6. First admin user
The new database is empty. Register a user on the site, then:
```
heroku pg:psql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Troubleshooting
| Problem | Fix |
|---|---|
| `Web process failed to bind to $PORT within 60 seconds` | `heroku config:set SPRING_MAIN_LAZY_INITIALIZATION=true` |
| `R14 Memory quota exceeded` | `heroku config:set JAVA_TOOL_OPTIONS="-Xmx256m -Xss512k"` |
| `Schema-validation: missing column` | The database was changed by hand. `heroku pg:reset` then redeploy (deletes all data) |
| Browser says CORS error | `CORS_ALLOWED_ORIGINS` must equal your Vercel address exactly (https, no `/` at the end) |
| Images disappear after restart | `CLOUDINARY_URL` is missing or wrong (check the logs) |
| Old images show a broken link | Images saved before the move live only on your old server; upload them again |
