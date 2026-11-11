import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Consultation, SoapNote, JitsiTokenResponse } from '../models/telemed.models';

@Injectable({
  providedIn: 'root'
})
export class SoapApiService {
  private apiUrl = '/api/consultations';

  constructor(private http: HttpClient) {}

  getConsultation(id: string): Observable<Consultation> {
    return this.http.get<Consultation>(`${this.apiUrl}/${id}`);
  }

  getJitsiToken(consultationId: string, isDoctor: boolean = true): Observable<JitsiTokenResponse> {
    return this.http.post<JitsiTokenResponse>(`${this.apiUrl}/${consultationId}/token?isDoctor=${isDoctor}`, {});
  }

  updateSoap(consultationId: string, soap: Partial<SoapNote>, approveAndSign: boolean = false): Observable<SoapNote> {
    return this.http.put<SoapNote>(`${this.apiUrl}/${consultationId}/soap`, {
      ...soap,
      approveAndSign
    });
  }

  notifyAudioReady(consultationId: string, storageKey: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(`${this.apiUrl}/${consultationId}/audio-ready`, {
      audio_storage_key: storageKey
    });
  }
}
