# Deploy Ranaswanu backend to Heroku (Spring Boot + PostgreSQL)

## 0. Before you start
- The database is now **PostgreSQL** (Heroku Postgres add-on). SQL Server / Azure SQL is no longer used,
  so you can delete that database and its firewall rules.
- You do **not** set `DB_URL`, `DB_USERNAME` or `DB_PASSWORD` on Heroku. The Heroku Java buildpack creates
  `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME` and `JDBC_DATABASE_PASSWORD` automatically when the Postgres
  add-on is attached, and `application.yaml` reads them.
- Gmail: turn on 2-Step Verification, then create an **App password** (16 letters). Use it as MAIL_PASSWORD.

## 1. Create the app + database (terminal)
```bash
heroku login
cd path/to/ranaswanu            # the folder that contains pom.xml
heroku create YOUR-APP-NAME
heroku buildpacks:set heroku/java
heroku addons:create heroku-postgresql:essential-0
```
(`essential-0` is the smallest paid plan. You can also add Heroku Postgres in the dashboard under
**Resources**.)

## 2. Set Config Vars (Heroku web dashboard) - do this BEFORE the first push
Dashboard -> your app -> **Settings** -> **Reveal Config Vars**.
`DATABASE_URL` is already there (added by the add-on) - leave it alone.

| KEY | VALUE |
|---|---|
| JWT_SECRET | random text, at least 32 characters (different from local) |
| MAIL_USERNAME | the Gmail address, e.g. ranaswanu.co@gmail.com |
| MAIL_PASSWORD | Gmail App password (spaces are ignored) |
| FRONTEND_RESET_URL | `https://YOUR-FRONTEND/reset-password` (the link inside the reset email) |
| CORS_ALLOWED_ORIGINS | `https://YOUR-FRONTEND` (several: separate with commas, no spaces). Also used for WebSocket |
| ADMIN1_USERNAME / ADMIN1_EMAIL / ADMIN1_PASSWORD | first admin |
| ADMIN2_... ADMIN3_... ADMIN4_... ADMIN5_... | admins 2 to 5, same three keys each |

If you used the old SQL Server setup, **delete** `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` from Config Vars
(they would override the Heroku Postgres values).

Admin passwords: at least 8 characters. Optional vars: `MAIL_FROM`, `SHOW_SQL`, `DB_POOL_SIZE`, `JWT_EXPIRATION_MS`.

## 3. Deploy (terminal)
```bash
git add .
git commit -m "Switch to PostgreSQL for Heroku"
git push heroku master
heroku logs --tail
```
In the logs you should see Flyway applying V1 ... V21 on an empty PostgreSQL database, then
`ADMIN1: account created for ...` up to ADMIN5.

## 4. The 5 admin accounts
- They are created automatically at server start from the ADMINn_* variables (never from the frontend).
- Safe on every restart: nothing is duplicated, and existing passwords are NOT changed.
- Log in with the **email or username** + password at `POST /api/auth/login`.
- After the first successful start you may delete the ADMINn_PASSWORD vars (passwords are stored hashed).
- To change an admin password later: set the new ADMINn_PASSWORD plus `ADMIN_RESET_PASSWORDS=true`,
  let the app restart, then delete `ADMIN_RESET_PASSWORDS`.
- Do NOT run `guid/seed_data.sql` on the server (it is SQL Server syntax and has public test accounts).

## 5. Forgot-password email
- Test: `curl -X POST https://YOUR-APP.herokuapp.com/api/auth/forgot-password -H "Content-Type: application/json" -d '{"email":"user@example.com"}'`
- If no email arrives, run `heroku logs --tail` and look for **"Password reset email FAILED"** - it shows the real reason
  (wrong app password, Gmail blocked the login, ...). Also check the Spam folder.

## 6. Uploaded images
Heroku erases its disk on every restart, so images are stored in the database (table `stored_files`)
and served at `/files/...` exactly as before.

## 7. Quick check
```bash
curl -X POST https://YOUR-APP.herokuapp.com/api/auth/login -H "Content-Type: application/json" -d '{"email":"ADMIN1_EMAIL","password":"ADMIN1_PASSWORD"}'
```
Expected: `{"token":"...","message":"Login successful"}`.

## 8. Look inside the database (optional)
```bash
heroku pg:psql
\dt
select * from flyway_schema_history order by installed_rank;
```

## Running locally with PostgreSQL
1. Install PostgreSQL and create an empty database: `createdb ranaswanu`
2. In `src/main/resources/application-local.yaml` set `spring.datasource.url`
   (`jdbc:postgresql://localhost:5432/ranaswanu`), `username` and `password`.
3. Start the app - Flyway creates all tables.
