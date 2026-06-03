@echo off
setlocal

echo ============================================
echo  DDL Hotel Accommodations - PBJT Assessment
echo  Database: pbjt_assessment_db @ localhost
echo ============================================
echo.

set /p PGPORT=Port PostgreSQL (default 5432, tekan Enter untuk skip): 
if "%PGPORT%"=="" set PGPORT=5432

set /p PGPASSWORD=Password user postgres: 

set PSQL=D:\App\postgresql\bin\psql.exe
set DDL_FILE=D:\BPRD\leaflet-geo\leaflet-geo\src\main\resources\sql\hotel_accommodation_ddl.sql

echo.
echo Connecting ke pbjt_assessment_db...
echo.

"%PSQL%" -U postgres -h localhost -p %PGPORT% -d pbjt_assessment_db -f "%DDL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  SUKSES! Tabel hotel berhasil dibuat.
    echo ============================================
) else (
    echo.
    echo ============================================
    echo  ERROR! Cek pesan di atas.
    echo  Pastikan database pbjt_assessment_db sudah ada.
    echo ============================================
    echo.
    echo Jika database belum ada, jalankan dulu:
    echo   CREATE DATABASE pbjt_assessment_db;
)

echo.
pause
