# PBJT Assessment - Image Upload Feature

## Overview
Fitur upload gambar untuk dokumentasi usaha pada form assessment PBJT. User dapat upload maksimal 4 gambar (foto menu, suasana usaha, dll) dengan drag & drop atau file picker.

## Backend Implementation

### 1. File Upload Configuration
**File**: `src/main/resources/application.properties`
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=20MB
file.upload.dir=uploads/pbjt-images
```

### 2. Upload Endpoint
**File**: `PbjtAssessmentController.java`

**Endpoint**: `POST /api/pbjt-assessments/upload-images`

**Request**: `multipart/form-data` dengan parameter `files` (array of files)

**Validasi**:
- Max 4 gambar
- Max 5MB per file
- Tipe file: image/jpeg, image/png, image/gif, image/webp

**Response**:
```json
{
  "success": true,
  "message": "Gambar berhasil diupload",
  "urls": [
    "/uploads/pbjt-images/uuid-1.jpg",
    "/uploads/pbjt-images/uuid-2.png"
  ]
}
```

### 3. Static Resource Handler
**File**: `WebConfig.java`
- Serve uploaded images dari `/uploads/pbjt-images/**`
- File disimpan dengan UUID untuk menghindari konflik nama

### 4. Database Schema
**Column**: `photo_urls` (text array) di tabel `pbjt_assessments`
- Menyimpan array URL gambar yang telah di-upload

## Frontend Implementation

### 1. Component Updates
**File**: `assessment-form.component.ts`

**Properties**:
- `uploadedImages: ImagePreview[]` - Array gambar yang di-upload
- `maxImages: number = 4` - Limit maksimal gambar
- `isDragOver: boolean` - Status drag over untuk styling
- `allowedTypes: string[]` - Tipe file yang diperbolehkan

**Methods**:
- `onDragOver()` - Handle drag over event
- `onDragLeave()` - Handle drag leave event
- `onDrop()` - Handle drop event
- `onFileSelect()` - Handle file input change
- `handleFiles()` - Process dan validasi files
- `removeImage()` - Hapus gambar dari preview
- `getRemainingSlots()` - Hitung slot tersisa

**Submit Flow**:
1. Validasi form
2. Upload gambar ke backend (jika ada)
3. Ambil URL gambar dari response
4. Submit assessment dengan photoUrls

### 2. Service Updates
**File**: `pbjt-assessment.service.ts`

**Method baru**:
```typescript
uploadImages(files: File[]): Observable<any> {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  return this.http.post(`${this.apiUrl}/upload-images`, formData);
}
```

### 3. Model Updates
**File**: `assessment.model.ts`

**AssessmentRequest interface**:
```typescript
photoUrls?: string[];  // Array URL gambar yang di-upload
supportingDocUrl?: string;
```

### 4. Template Features
**File**: `assessment-form.component.html`

**Lokasi**: Step 1 - Profil Usaha (setelah field Tanggal Assessment)

**Fitur**:
- Drag & drop area dengan visual feedback
- Preview grid responsive (4 kolom desktop, 2 mobile)
- Button remove untuk setiap gambar
- Counter slot tersisa
- Validasi real-time

**Styling**: `assessment-form.component.scss`
- Drop zone dengan hover & drag-over effects
- Image preview grid dengan overlay
- Remove button dengan hover animation
- Responsive untuk mobile

## Usage Flow

### User Flow
1. User membuka form assessment
2. Di Step 1 (Profil Usaha), scroll ke bagian "Foto Usaha"
3. User bisa:
   - Drag & drop gambar ke drop zone
   - Klik drop zone untuk file picker
4. Preview gambar langsung muncul dalam grid
5. User bisa hapus gambar dengan klik tombol X
6. Maksimal 4 gambar, masing-masing max 5MB
7. Saat submit form:
   - Gambar di-upload dulu ke server
   - Server return URL gambar
   - Assessment disimpan dengan photoUrls

### Technical Flow
```
Frontend                    Backend
   |                           |
   |-- Upload Images --------->| POST /upload-images
   |                           | - Validate files
   |                           | - Save to uploads/pbjt-images/
   |                           | - Generate UUIDs
   |<-- Return URLs -----------| { urls: [...] }
   |                           |
   |-- Submit Assessment ----->| POST /pbjt-assessments
   |   (with photoUrls)        | - Save to database
   |<-- Success Response ------| { success: true }
```

## Directory Structure
```
leaflet-geo/
├── uploads/
│   └── pbjt-images/        # Upload directory
│       ├── uuid-1.jpg
│       ├── uuid-2.png
│       └── ...
├── src/main/
│   ├── java/
│   │   └── com/example/leaflet_geo/
│   │       ├── config/
│   │       │   └── WebConfig.java
│   │       └── controller/
│   │           └── PbjtAssessmentController.java
│   └── resources/
│       └── application.properties
```

## Testing

### Manual Test
1. Start backend: `.\mvnw.cmd spring-boot:run`
2. Start frontend: `ng serve --port 4300`
3. Open form: `http://localhost:4300/pbjt-assessment/new`
4. Test drag & drop gambar
5. Test file picker
6. Test validasi (max 4, max 5MB, tipe file)
7. Test remove gambar
8. Test submit dengan gambar

### Validation Tests
- ✅ Max 4 gambar
- ✅ Max 5MB per file
- ✅ Tipe file: JPG, PNG, GIF, WebP only
- ✅ Drag & drop berfungsi
- ✅ File picker berfungsi
- ✅ Preview muncul setelah upload
- ✅ Remove gambar berfungsi
- ✅ Submit dengan gambar berhasil
- ✅ URL gambar tersimpan di database
- ✅ Gambar bisa diakses via URL

## Notes
- Gambar disimpan dengan UUID untuk menghindari konflik
- Gambar tidak otomatis dihapus saat assessment dihapus (perlu cleanup service)
- Frontend hanya preview, belum save ke backend sampai user submit form
- CORS sudah dikonfigurasi untuk allow upload
- Upload directory otomatis dibuat jika belum ada

## Future Enhancements
1. Image compression sebelum upload
2. Thumbnail generation
3. Delete image endpoint untuk cleanup
4. Image gallery modal untuk view fullsize
5. Reorder images functionality
6. Progress indicator untuk upload besar
