# Test login accounts

All accounts are fake. You can log in with the **username or the email**.

| Role | Username | Email | Password | Notes |
|---|---|---|---|---|
| ADMIN | `admin_ranaswanu` | `admin@ranaswanu.test` | `Admin@1234` | Admin pages and stats |
| FARMER | `farmer_nimal` | `nimal@ranaswanu.test` | `Farmer@1234` | Farmer, Nuwara Eliya. Has a PENDING order, 4 products, rating 5/5 |
| FARMER | `farmer_kamala` | `kamala@ranaswanu.test` | `Farmer@1234` | Farmer, Dambulla. Has ACCEPTED and SHIPPED orders, 1 draft product |
| FARMER | `farmer_sunil` | `sunil@ranaswanu.test` | `Farmer@1234` | Farmer, Kandy. Has 1 product disabled by admin, in-transit delivery |
| BUYER | `buyer_saman` | `saman@ranaswanu.test` | `Buyer@1234` | Buyer. Has order o7 COMPLETED and not rated yet (test rating) |
| BUYER | `buyer_dilini` | `dilini@ranaswanu.test` | `Buyer@1234` | Buyer. Has a PENDING order |
| BUYER | `buyer_ruwan` | `ruwan@ranaswanu.test` | `Buyer@1234` | Buyer. Has a SHIPPED and a REJECTED order |
| TRANSPORT | `transport_kasun` | `kasun@ranaswanu.test` | `Transport@1234` | Transport. Has an open vehicle offer and an IN_TRANSIT delivery |
| TRANSPORT | `transport_mala` | `mala@ranaswanu.test` | `Transport@1234` | Transport. Has an open vehicle offer and a PENDING delivery |

## How to load it
1. Back up your database structure first (see the earlier steps).
2. Copy the **uploads/** folder from this package into the backend project's **uploads/** folder (same folder the app uses; `app.upload.dir: uploads`). Images are then served at `/files/...`.
3. In IntelliJ open **seed_data.sql** in a query console on your database, select all and **Execute** (run the whole file in one go).
4. Start the backend and log in with any account above.
5. To remove the fake data later, run **seed_cleanup.sql**. It only deletes rows that belong to the `@ranaswanu.test` accounts.

Do NOT put these files in `db/migration`. They are not Flyway migrations.
