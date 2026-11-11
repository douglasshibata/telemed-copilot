import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsultationRoomComponent } from './consultation-room.component';
import { SoapApiService } from '../../core/services/soap-api.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { JitsiService } from '../../core/services/jitsi.service';
import { of } from 'rxjs';
import { SoapNote } from '../../core/models/telemed.models';

describe('ConsultationRoomComponent', () => {
  let component: ConsultationRoomComponent;
  let fixture: ComponentFixture<ConsultationRoomComponent>;
  let wsService: WebSocketService;
  let soapApiSpy: jasmine.SpyObj<SoapApiService>;
  let jitsiServiceSpy: jasmine.SpyObj<JitsiService>;

  beforeEach(async () => {
    soapApiSpy = jasmine.createSpyObj('SoapApiService', ['updateSoap', 'notifyAudioReady', 'getConsultation']);
    jitsiServiceSpy = jasmine.createSpyObj('JitsiService', ['initializeJitsi', 'dispose']);
    wsService = new WebSocketService();

    await TestBed.configureTestingModule({
      imports: [ConsultationRoomComponent],
      providers: [
        { provide: SoapApiService, useValue: soapApiSpy },
        { provide: WebSocketService, useValue: wsService },
        { provide: JitsiService, useValue: jitsiServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultationRoomComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize with active call and default draft status', () => {
    expect(component.isCallActive()).toBeTrue();
    expect(component.soapNote().status).toBe('DRAFT');
  });

  it('should update SOAP note reactively upon WebSocket message arrival', () => {
    const mockNote: SoapNote = {
      consultationId: 'mock-consultation-1',
      subjective: 'Paciente relata cefaleia constante.',
      objective: 'Pressão 120/80',
      assessment: 'Cefaleia tensional (G44.2)',
      recommendations: 'Repouso',
      followUpDays: 15,
      prescriptions: ['Dipirona 500mg'],
      examsRequested: [],
      status: 'DRAFT'
    };

    wsService.pushSoapUpdate(mockNote);
    expect(component.soapNote().subjective).toBe('Paciente relata cefaleia constante.');
    expect(component.soapNote().assessment).toBe('Cefaleia tensional (G44.2)');
  });

  it('should end call and notify audio ready', () => {
    soapApiSpy.notifyAudioReady.and.returnValue(of({ status: 'AUDIO_PROCESSING_INITIATED' }));
    component.endCallAndProcessAudio();

    expect(component.isCallActive()).toBeFalse();
    expect(jitsiServiceSpy.dispose).toHaveBeenCalled();
    expect(soapApiSpy.notifyAudioReady).toHaveBeenCalledWith('mock-consultation-1', 'audio-mock-consultation-1.wav');
  });
});
