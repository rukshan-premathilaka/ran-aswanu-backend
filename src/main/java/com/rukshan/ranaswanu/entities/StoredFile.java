package com.rukshan.ranaswanu.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Nationalized;
import org.hibernate.type.SqlTypes;

// An uploaded image kept in the database (Heroku disk is wiped on every restart).
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stored_files")
public class StoredFile {

    // e.g. "profile-pics/3f2c....jpg" - the same value the other tables already store
    @Id
    @Nationalized
    @Column(name = "file_path", nullable = false, length = 255)
    private String path;

    @Nationalized
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "data", nullable = false)
    private byte[] data;

    public StoredFile(String path, String contentType, byte[] data) {
        this.path = path;
        this.contentType = contentType;
        this.data = data;
    }
}
