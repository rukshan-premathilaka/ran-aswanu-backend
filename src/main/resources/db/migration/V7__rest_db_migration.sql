CREATE TABLE Farm_Activities(
    activity_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    activity NVARCHAR(500) NOT NULL,
    activity_status BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT FK_Farm_Activity
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE Expenses(
    expense_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    expense_date DATETIME2 DEFAULT GETDATE(),
    title NVARCHAR(50) NOT NULL,
    category NVARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL,-- Change ti decimal if needed
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT FK_Expenses
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE Customer_Reviews(
    review_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    review_date DATETIME2 DEFAULT GETDATE(),
    description NVARCHAR(500) NOT NULL,
    rating INT NOT NULL CHECK(rating BETWEEN 1 AND 5),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT FK_Customer_Review
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE Reminders(
    reminder_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reminder_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    description NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    user_id BIGINT NOT NULL,
    CONSTRAINT FK_Reminder
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);