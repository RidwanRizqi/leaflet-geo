import re

with open('d:/BPRD/leaflet-geo/leaflet-geo-FE/src/app/pages/pbjt-assessment/components/assessment-list/assessment-list.component.html', 'r', encoding='utf-8') as f:
    text = f.read()

btn_str = '''                      <button class="btn btn-outline-info" (click)="openObservationModal(assessment)" title="Observasi">
                        <i class="ri-survey-line"></i>
                      </button>
                      <button class="btn btn-outline-danger"'''

t2 = text.replace('                      <button class="btn btn-outline-danger"', btn_str)

modal_str = '''

<!-- Observation Modal -->
<div class="modal" tabindex="-1" [class.d-block]="showObservationModal" [style.backgroundColor]="showObservationModal ? 'rgba(0,0,0,0.5)' : 'transparent'">
  <div class="modal-dialog modal-xl modal-dialog-scrollable">
    <div class="modal-content" *ngIf="selectedAssessment">
      <div class="modal-header">
        <h5 class="modal-title">Observasi - {{ selectedAssessment.businessName }}</h5>
        <button type="button" class="btn-close" aria-label="Close" (click)="closeObservationModal()"></button>
      </div>
      <div class="modal-body bg-light">

        <!-- Existing Observations -->
        <div class="card mb-4">
          <div class="card-header bg-white">
            <h6 class="mb-0">Daftar Observasi</h6>
          </div>
          <div class="card-body">
            <div *ngIf="isLoadingObservations" class="text-center py-3">
              <div class="spinner-border spinner-border-sm text-primary" role="status"></div> Loading...
            </div>
            
            <div *ngIf="!isLoadingObservations && observationsList.length === 0" class="alert alert-info">
              Belum ada data observasi untuk assessment ini.
            </div>

            <div class="table-responsive" *ngIf="!isLoadingObservations && observationsList.length > 0">
              <table class="table table-sm table-bordered">
                <thead class="table-light">
                  <tr>
                    <th>Tanggal</th>
                    <th>Tipe Hari</th>
                    <th>Pengunjung</th>
                    <th>Durasi (Jam)</th>
                    <th>Catatan</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let obs of observationsList">
                    <td>{{ obs.observationDate | date:'dd/MM/yyyy' }}</td>
                    <td>{{ obs.dayType }}</td>
                    <td>{{ obs.visitors }}</td>
                    <td>{{ obs.durationHours }}</td>
                    <td>{{ obs.notes }}</td>
                    <td>
                      <button class="btn btn-sm btn-danger" (click)="deleteObservation(obs.id)" title="Hapus"><i class="ri-delete-bin-line"></i></button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Add New Observation Form -->
        <div class="card">
          <div class="card-header bg-white">
            <h6 class="mb-0">Tambah Observasi Baru</h6>
          </div>
          <div class="card-body">
            <form [formGroup]="observationForm" (ngSubmit)="submitObservation()">
              <div class="row mb-3">
                <div class="col-md-3">
                  <label class="form-label required">Tanggal Observasi</label>
                  <input type="date" class="form-control" formControlName="observationDate"
                    [ngClass]="{ 'is-invalid': observationForm.get('observationDate')?.touched && observationForm.get('observationDate')?.invalid }">
                </div>
                <div class="col-md-3">
                  <label class="form-label required">Tipe Hari</label>
                  <select class="form-select" formControlName="dayType"
                    [ngClass]="{ 'is-invalid': observationForm.get('dayType')?.touched && observationForm.get('dayType')?.invalid }">
                    <option value="WEEKDAY_PEAK">Hari Kerja - Ramai</option>
                    <option value="WEEKDAY_OFFPEAK">Hari Kerja - Sepi</option>
                    <option value="WEEKEND_PEAK">Akhir Pekan - Ramai</option>
                  </select>
                </div>
                <div class="col-md-3">
                  <label class="form-label required">Jumlah Pengunjung</label>
                  <input type="number" class="form-control" formControlName="visitors"
                    [ngClass]="{ 'is-invalid': observationForm.get('visitors')?.touched && observationForm.get('visitors')?.invalid }">
                </div>
                <div class="col-md-3">
                  <label class="form-label required">Durasi (Jam)</label>
                  <input type="number" step="0.5" class="form-control" formControlName="durationHours"
                    [ngClass]="{ 'is-invalid': observationForm.get('durationHours')?.touched && observationForm.get('durationHours')?.invalid }">
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Catatan</label>
                <textarea class="form-control" rows="2" formControlName="notes"></textarea>
              </div>

              <h6 class="mt-4 mb-3 d-flex justify-content-between align-items-center">
                <span>Sample Transaksi (Minimal 5)</span>
                <button type="button" class="btn btn-sm btn-outline-primary" (click)="addSampleTransaction()">
                  <i class="ri-add-line"></i> Tambah Sample
                </button>
              </h6>

              <div formArrayName="sampleTransactions">
                <div class="row" *ngFor="let sample of sampleTransactions.controls; let i=index" [formGroupName]="i">
                  <div class="col-md-5 mb-2">
                    <div class="input-group input-group-sm">
                      <span class="input-group-text">Rp</span>
                      <input type="number" class="form-control" formControlName="amount" placeholder="Nilai Transaksi"
                        [ngClass]="{ 'is-invalid': sample.get('amount')?.touched && sample.get('amount')?.invalid }">
                    </div>
                  </div>
                  <div class="col-md-6 mb-2">
                    <input type="text" class="form-control form-control-sm" formControlName="notes" placeholder="Catatan/Menu (Opsional)">
                  </div>
                  <div class="col-md-1 mb-2">
                    <button type="button" class="btn btn-sm btn-outline-danger w-100" (click)="removeSampleTransaction(i)" [disabled]="sampleTransactions.length <= 5">
                      <i class="ri-delete-bin-line"></i>
                    </button>
                  </div>
                </div>
              </div>

              <div class="d-flex justify-content-end mt-4">
                <button type="button" class="btn btn-light me-2" (click)="closeObservationModal()">Batal</button>
                <button type="submit" class="btn btn-primary" [disabled]="isSubmittingObservation || observationForm.invalid">
                  <span *ngIf="isSubmittingObservation" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Simpan Observasi
                </button>
              </div>
            </form>
          </div>
        </div>

      </div>
    </div>
  </div>
</div>
'''

if '<!-- Observation Modal -->' not in t2:
    t2 += modal_str

with open('d:/BPRD/leaflet-geo/leaflet-geo-FE/src/app/pages/pbjt-assessment/components/assessment-list/assessment-list.component.html', 'w', encoding='utf-8') as f:
    f.write(t2)