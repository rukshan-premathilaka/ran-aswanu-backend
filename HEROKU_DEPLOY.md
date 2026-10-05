# Deploy Ranaswanu backend to Heroku (Spring Boot + SQL Server)

## 0. Before you start
- **Heroku has NO SQL Server add-on** (its database is PostgreSQL only). Your project uses SQL Server
  (Flyway scripts are T-SQL), so the database must be hosted somewhere else, for example **Azure SQL Database**
  or any SQL Server host that is reachable from the internet.
- On that host: create an empty database, and allow connections from outside. Heroku dynos have changing IPs,
  so the SQL firewall must allow all IPs (0.0.0.0 - 255.255.255.255), or you must add a static-IP add-on to Heroku.
  Use a strong database password.
- Your JDBC URL looks like this (Azure example):
  `jdbc:sqlserver://YOURSERVER.database.windows.net:1433;database=YOURDB;encrypt=true;trustServerCertificate=false;loginTimeout=30;`
- Gmail: turn on 2-Step Verification, then create an **App password** (16 letters). Use it as MAIL_PASSWORD.

## 1. Create the app (terminal)
```bash
heroku login
cd path/to/ranaswanu            # the folder that contains pom.xml
heroku create YOUR-APP-NAME
heroku buildpacks:set heroku/java
```

## 2. Set Config Vars (Heroku web dashboard) - do this BEFORE the first push
Dashboard -> your app -> **Settings** -> **Reveal Config Vars**.

| KEY | VALUE |
|---|---|
| DB_URL | your JDBC URL (above) |
| DB_USERNAME | database user |
| DB_PASSWORD | database password |
| JWT_SECRET | random text, at least 32 characters (different from local) |
| MAIL_USERNAME | the Gmail address, e.g. ranaswanu.co@gmail.com |
| MAIL_PASSWORD | Gmail App password (spaces are ignored) |
| FRONTEND_RESET_URL | `https://YOUR-FRONTEND/reset-password` (the link inside the reset email) |
| CORS_ALLOWED_ORIGINS | `https://YOUR-FRONTEND` (several: separate with commas, no spaces). Also used for WebSocket |
| ADMIN1_USERNAME / ADMIN1_EMAIL / ADMIN1_PASSWORD | first admin |
| ADMIN2_... ADMIN3_... ADMIN4_... ADMIN5_... | admins 2 to 5, same three keys each |

Admin passwords: at least 8 characters. Optional vars: `MAIL_FROM`, `SHOW_SQL`, `DB_POOL_SIZE`, `JWT_EXPIRATION_MS`.

## 3. Deploy (terminal)
```bash
git add .
git commit -m "Heroku deployment"
git push heroku master
heroku logs --tail
```
In the logs you should see Flyway applying V1 ... V21, then `ADMIN1: account created for ...` up to ADMIN5.

## 4. The 5 admin accounts
- They are created automatically at server start from the ADMINn_* variables (never from the frontend).
- Safe on every restart: nothing is duplicated, and existing passwords are NOT changed.
- Log in with the **email or username** + password at `POST /api/auth/login`.
- After the first successful start you may delete the ADMINn_PASSWORD vars (passwords are stored hashed).
- To change an admin password later: set the new ADMINn_PASSWORD plus `ADMIN_RESET_PASSWORDS=true`,
  let the app restart, then delete `ADMIN_RESET_PASSWORDS`.
- Do NOT run `guid/seed_data.sql` on the server (it has public test accounts with weak passwords).

## 5. Forgot-password email
- Test: `curl -X POST https://YOUR-APP.herokuapp.com/api/auth/forgot-password -H "Content-Type: application/json" -d '{"email":"user@example.com"}'`
- If no email arrives, run `heroku logs --tail` and look for **"Password reset email FAILED"** - it shows the real reason
  (wrong app password, Gmail blocked the login, ...). Also check the Spam folder.

## 6. Uploaded images
Heroku erases its disk on every restart, so images are now stored in the database (table `stored_files`)
and served at `/files/...` exactly as before.

## 7. Quick check
```bash
curl -X POST https://YOUR-APP.herokuapp.com/api/auth/login -H "Content-Type: application/json" -d '{"email":"ADMIN1_EMAIL","password":"ADMIN1_PASSWORD"}'
```
Expected: `{"token":"...","message":"Login successful"}`.
