# Multiple Database Configuration

Aplikasi Spring Boot ini sekarang mendukung koneksi ke dua database sekaligus:

## Database yang Dikonfigurasi

### 1. PostgreSQL (Primary Database)
- **URL**: `jdbc:postgresql://localhost:5432/sig`
- **Username**: `postgres`
- **Password**: `root`
- **Driver**: `org.postgresql.Driver`

### 2. Oracle Database
- **URL**: `jdbc:oracle:thin:@//localhost:1521/free`
- **Username**: `system`
- **Password**: `1234`
- **Driver**: `oracle.jdbc.OracleDriver`

## Konfigurasi File

### 1. `application.properties`
```properties
# PostgreSQL Database Configuration (Primary)
spring.datasource.url=jdbc:postgresql://localhost:5432/sig
spring.datasource.username=postgres
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver

# Oracle Database Configuration
spring.datasource.oracle.url=jdbc:oracle:thin:@//localhost:1521/free
spring.datasource.oracle.username=system
spring.datasource.oracle.password=1234
spring.datasource.oracle.driver-class-name=oracle.jdbc.OracleDriver
```

### 2. `MultipleDatabaseConfig.java`
- Mengelola dua DataSource terpisah
- PostgreSQL sebagai primary database
- Oracle sebagai secondary database
- Masing-masing memiliki JdbcTemplate sendiri

### 3. `DatabaseConfig.java`
- Test koneksi otomatis saat aplikasi start
- Menampilkan status kedua database
- Informasi detail tentang schema dan tabel

## Cara Menggunakan

### 1. Test Koneksi via API
```bash
# Test PostgreSQL
GET http://localhost:8080/api/database-test/postgres

# Test Oracle
GET http://localhost:8080/api/database-test/oracle

# Test Kedua Database
GET http://localhost:8080/api/database-test/both
```

### 2. Menggunakan JdbcTemplate di Service/Controller
```java
@Autowired
@Qualifier("postgresJdbcTemplate")
private JdbcTemplate postgresJdbcTemplate;

@Autowired
@Qualifier("oracleJdbcTemplate")
private JdbcTemplate oracleJdbcTemplate;
```

## Dependencies yang Ditambahkan

```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Catatan Penting

1. **PostgreSQL** tetap sebagai primary database untuk JPA/Hibernate
2. **Oracle** digunakan untuk operasi khusus yang memerlukan JDBC langsung
3. Pastikan kedua database server berjalan sebelum menjalankan aplikasi
4. Kredensial database dapat diubah di `application.properties`
