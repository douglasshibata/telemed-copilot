import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { SoapNote } from '../models/telemed.models';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private soapUpdates$ = new Subject<SoapNote>();

  // Observable for frontend components to subscribe to real-time generated SOAP notes
  getSoapUpdates(): Observable<SoapNote> {
    return this.soapUpdates$.asObservable();
  }

  // Emits new SOAP note received from WebSocket STOMP topic (/topic/consultation/{id}/soap)
  pushSoapUpdate(soapNote: SoapNote): void {
    this.soapUpdates$.next(soapNote);
  }
}
