# PBJT Assessment Integration - Setup Guide

## 📦 Database Setup

### 1. Create PostgreSQL Database

```bash
# Login as postgres user
psql -U postgres

# Create database
CREATE DATABASE pbjt_assessment_db;

# Exit psql
\q
```

### 2. Run Setup SQL

```bash
# Execute setup SQL file
psql -U postgres -d pbjt_assessment_db -f src/main/resources/sql/pbjt_assessment_setup.sql
```

### 3. Verify Database

```sql
-- Connect to database
\c pbjt_assessment_db

-- Check tables
\dt

-- Check sample data
SELECT COUNT(*) FROM pbjt_assessments;
SELECT COUNT(*) FROM pbjt_observation_history;

-- View sample records
SELECT business_id, business_name, annual_pbjt, confidence_level 
FROM pbjt_assessments;
```

---

## ⚙️ Backend Configuration

### Configuration sudah ditambahkan di:
- ✅ `application.properties` - Database connection for PBJT
- ✅ `MultipleDatabaseConfig.java` - Bean configuration for pbjtDataSource dan pbjtJdbcTemplate

**No additional configuration needed!** Just ensure:
1. PostgreSQL is running on `localhost:5432`
2. Username: `postgres`
3. Password: `root`
4. Database `pbjt_assessment_db` exists

---

## 🚀 Backend Testing

### Start Spring Boot Application

```bash
cd leaflet-geo
mvn spring-boot:run
```

### Test API Endpoints

```bash
# Health check
curl http://localhost:8080/api/pbjt-assessments/health

# Get all assessments
curl http://localhost:8080/api/pbjt-assessments?page=0&size=10

# Get specific assessment
curl http://localhost:8080/api/pbjt-assessments/1

# Get by business ID
curl http://localhost:8080/api/pbjt-assessments/business/BIZ-001

# Get count
curl http://localhost:8080/api/pbjt-assessments/count
```

### Create New Assessment (POST)

```bash
curl -X POST http://localhost:8080/api/pbjt-assessments \
  -H "Content-Type: application/json" \
  -d '{
    "businessId": "BIZ-TEST-001",
    "businessName": "Test Restaurant",
    "assessmentDate": "2026-01-10",
    "buildingArea": 100.00,
    "seatingCapacity": 40,
    "operatingHoursStart": "10:00:00",
    "operatingHoursEnd": "22:00:00",
    "businessType": "RESTAURANT",
    "paymentMethods": ["Cash", "QRIS"],
    "latitude": -8.125000,
    "longitude": 113.718750,
    "address": "Jl. Test No. 123",
    "kelurahan": "Test Kelurahan",
    "kecamatan": "Test Kecamatan",
    "kabupaten": "Lumajang",
    "surveyorId": "SURV-TEST",
    "observations": [
      {
        "observationDate": "2026-01-10T12:00:00",
        "dayType": "WEEKDAY_PEAK",
        "visitors": 50,
        "durationHours": 2.0,
        "sampleTransactions": [25000, 30000, 28000, 32000, 27000]
      },
      {
        "observationDate": "2026-01-10T15:00:00",
        "dayType": "WEEKDAY_OFFPEAK",
        "visitors": 15,
        "durationHours": 2.0,
        "sampleTransactions": [15000, 18000, 16000, 20000, 17000]
      }
    ]
  }'
```

---

## 📁 Files Created

### Backend Java Files:
```
leaflet-geo/src/main/java/com/example/leaflet_geo/
├── controller/
│   └── PbjtAssessmentController.java          ✅ CRUD API endpoints
├── service/
│   ├── PbjtAssessmentService.java             ✅ Business logic
│   └── PbjtCalculationService.java            ✅ Tax calculation logic
├── repository/
│   ├── PbjtAssessmentRepository.java          ✅ JPA repository
│   └── ObservationHistoryRepository.java      ✅ JPA repository
├── entity/
│   ├── PbjtAssessment.java                    ✅ Main entity
│   ├── ObservationHistory.java                ✅ Related entity
│   └── enums/
│       ├── BusinessType.java                  ✅ Enum
│       ├── DayType.java                       ✅ Enum
│       └── ConfidenceLevel.java               ✅ Enum
└── dto/
    ├── AssessmentRequestDTO.java              ✅ Request DTO
    ├── AssessmentResponseDTO.java             ✅ Response DTO
    ├── CalculationResultDTO.java              ✅ Calculation DTO
    └── ObservationDTO.java                    ✅ Observation DTO
```

### Configuration Files:
```
leaflet-geo/src/main/
├── resources/
│   ├── application.properties                 ✅ Updated
│   └── sql/
│       └── pbjt_assessment_setup.sql          ✅ Created
└── java/com/example/leaflet_geo/config/
    └── MultipleDatabaseConfig.java            ✅ Updated
```

---

## 📊 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pbjt-assessments` | Get all assessments (paginated) |
| GET | `/api/pbjt-assessments/{id}` | Get assessment by ID |
| GET | `/api/pbjt-assessments/business/{businessId}` | Get assessment by business ID |
| GET | `/api/pbjt-assessments/kabupaten/{kabupaten}` | Get assessments by kabupaten |
| GET | `/api/pbjt-assessments/kecamatan/{kecamatan}` | Get assessments by kecamatan |
| GET | `/api/pbjt-assessments/count` | Get total count |
| GET | `/api/pbjt-assessments/health` | Health check |
| POST | `/api/pbjt-assessments` | Create new assessment |
| PUT | `/api/pbjt-assessments/{id}` | Update assessment |
| DELETE | `/api/pbjt-assessments/{id}` | Delete assessment |

---

## 🔧 Troubleshooting

### Database Connection Error
```
Error: could not connect to database
```
**Solution:**
1. Check PostgreSQL is running: `systemctl status postgresql` (Linux) or check Services (Windows)
2. Verify credentials in `application.properties`
3. Ensure database exists: `psql -U postgres -l | grep pbjt`

### Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution:**
1. Kill existing process: `lsof -ti:8080 | xargs kill` (Mac/Linux)
2. Or change port in `application.properties`: `server.port=8081`

### JPA/Hibernate Errors
```
Error: org.hibernate.MappingException
```
**Solution:**
1. Ensure tables exist in database
2. Run setup SQL script again
3. Check `spring.jpa.hibernate.ddl-auto=none` in properties

---

## ✅ Verification Checklist

- [ ] PostgreSQL running
- [ ] Database `pbjt_assessment_db` created
- [ ] Setup SQL executed successfully
- [ ] Sample data exists (3 assessments)
- [ ] Spring Boot starts without errors
- [ ] Health check returns 200 OK
- [ ] Can GET all assessments
- [ ] Can GET assessment by ID
- [ ] Can POST new assessment
- [ ] Response follows leaflet-geo pattern

---

## 🎯 Next Steps

Backend is complete! Next steps:
1. **Frontend Integration** - Create Angular module for PBJT Assessment
2. **Map Integration** - Display assessment pins on leaflet map
3. **Dashboard Integration** - Add PBJT charts to dashboard-pajak
4. **Testing** - Unit tests dan integration tests
5. **Documentation** - API documentation dengan Swagger

---

## 📝 Notes

- Database terpisah dari `sig` database (better isolation)
- Menggunakan JPA Repository (cleaner code vs JdbcTemplate)
- Pattern response mengikuti leaflet-geo existing (pagination, success flag)
- Validasi menggunakan Jakarta Validation
- Transaction management dengan `@Transactional`
- Lombok untuk reduce boilerplate code
- Slf4j untuk logging

**Backend READY! 🚀**
