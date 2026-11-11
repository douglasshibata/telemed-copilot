import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { SoapApiService } from '../../core/services/soap-api.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { JitsiService } from '../../core/services/jitsi.service';
import { Consultation, SoapNote } from '../../core/models/telemed.models';

@Component({
  selector: 'app-consultation-room',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="consultation-layout">
      <!-- Left side: 60% Jitsi Meet Telemedicine Conference -->
      <section class="video-section">
        <div class="header-bar">
          <h2>Teleconsulta Segura</h2>
          <span class="badge" [class.badge-active]="isCallActive()">
            {{ isCallActive() ? 'Em Andamento' : 'Chamada Finalizada' }}
          </span>
        </div>

        <div id="jitsi-container" class="jitsi-frame"></div>

        <div class="controls-bar">
          <button class="btn btn-secondary" (click)="endCallAndProcessAudio()" [disabled]="!isCallActive()">
            Encerrar Consulta e Processar Áudio
          </button>
        </div>
      </section>

      <!-- Right side: 40% Structured SOAP Clinical Editor -->
      <section class="soap-section">
        <div class="soap-header">
          <h3>Prontuário Estruturado (Padrão SOAP)</h3>
          <span class="status-badge" [class.signed]="soapNote().status === 'APPROVED_AND_SIGNED'">
            {{ soapNote().status === 'APPROVED_AND_SIGNED' ? 'Assinado Digitalmente' : 'Rascunho IA (Editável)' }}
          </span>
        </div>

        <div class="soap-body">
          <!-- S: Subjective -->
          <div class="soap-card">
            <label class="card-title">S - Subjetivo (Queixas e Histórico)</label>
            <textarea
              [(ngModel)]="soapNote().subjective"
              rows="3"
              [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
              placeholder="Queixa principal, duração dos sintomas e história atual..."
            ></textarea>
          </div>

          <!-- O: Objective -->
          <div class="soap-card">
            <label class="card-title">O - Objetivo (Exame Clínico e Sinais)</label>
            <textarea
              [(ngModel)]="soapNote().objective"
              rows="3"
              [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
              placeholder="Sinais vitais informados, exame ectoscópico e orientação..."
            ></textarea>
          </div>

          <!-- A: Assessment -->
          <div class="soap-card">
            <label class="card-title">A - Avaliação (Hipóteses e CID-10)</label>
            <textarea
              [(ngModel)]="soapNote().assessment"
              rows="2"
              [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
              placeholder="Diagnóstico provável, código CID-10 e diagnósticos diferenciais..."
            ></textarea>
          </div>

          <!-- P: Plan -->
          <div class="soap-card">
            <label class="card-title">P - Plano Terapêutico e Prescrições</label>
            <div class="prescriptions-list">
              <span class="sub-label">Prescrições:</span>
              <ul>
                <li *ngFor="let rx of soapNote().prescriptions; let i = index">
                  <input
                    [(ngModel)]="soapNote().prescriptions[i]"
                    [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
                  />
                </li>
              </ul>
            </div>

            <div class="plan-details">
              <label class="sub-label">Orientações e Recomendações:</label>
              <textarea
                [(ngModel)]="soapNote().recommendations"
                rows="2"
                [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
                placeholder="Orientações de estilo de vida, alertas de gravidade..."
              ></textarea>

              <div class="follow-up">
                <label>Retorno em (dias):</label>
                <input
                  type="number"
                  [(ngModel)]="soapNote().followUpDays"
                  [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="soap-footer">
          <button
            class="btn btn-primary"
            (click)="approveAndSign()"
            [disabled]="soapNote().status === 'APPROVED_AND_SIGNED'"
          >
            Aprovar e Assinar Prontuário (ICP-Brasil)
          </button>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .consultation-layout {
      display: flex;
      height: 100vh;
      width: 100%;
      background: #f8fafc;
      font-family: system-ui, -apple-system, sans-serif;
    }
    .video-section {
      width: 60%;
      height: 100%;
      display: flex;
      flex-direction: column;
      border-right: 1px solid #e2e8f0;
      background: #0f172a;
    }
    .soap-section {
      width: 40%;
      height: 100%;
      display: flex;
      flex-direction: column;
      background: #ffffff;
      padding: 1.5rem;
      box-sizing: border-box;
      overflow-y: auto;
    }
    .header-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      background: #1e293b;
      color: white;
    }
    .jitsi-frame {
      flex: 1;
      width: 100%;
      background: #000;
    }
    .controls-bar {
      padding: 0.75rem;
      background: #1e293b;
      display: flex;
      justify-content: flex-end;
    }
    .soap-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      border-bottom: 2px solid #e2e8f0;
      padding-bottom: 0.75rem;
    }
    .soap-card {
      margin-bottom: 1.25rem;
      background: #f8fafc;
      padding: 1rem;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
    }
    .card-title {
      display: block;
      font-weight: 700;
      color: #1e293b;
      margin-bottom: 0.5rem;
      font-size: 0.95rem;
    }
    textarea, input {
      width: 100%;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      padding: 0.5rem;
      font-size: 0.9rem;
      box-sizing: border-box;
    }
    .badge {
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      background: #64748b;
    }
    .badge-active { background: #22c55e; }
    .status-badge {
      padding: 0.25rem 0.6rem;
      border-radius: 4px;
      font-size: 0.8rem;
      background: #fef08a;
      color: #854d0e;
    }
    .status-badge.signed {
      background: #bbf7d0;
      color: #166534;
    }
    .btn {
      padding: 0.6rem 1.2rem;
      border-radius: 6px;
      border: none;
      font-weight: 600;
      cursor: pointer;
    }
    .btn-primary { background: #2563eb; color: white; }
    .btn-secondary { background: #ef4444; color: white; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class ConsultationRoomComponent implements OnInit, OnDestroy {
  consultationId = 'mock-consultation-1';
  isCallActive = signal<boolean>(true);

  soapNote = signal<SoapNote>({
    consultationId: this.consultationId,
    subjective: 'Aguardando encerramento da consulta para síntese do áudio via IA...',
    objective: 'Aguardando exame...',
    assessment: 'Aguardando hipóteses...',
    recommendations: '',
    followUpDays: 30,
    prescriptions: [],
    examsRequested: [],
    status: 'DRAFT'
  });

  private wsSubscription?: Subscription;

  constructor(
    private soapApi: SoapApiService,
    private wsService: WebSocketService,
    private jitsiService: JitsiService
  ) {}

  ngOnInit(): void {
    // Listen for real-time WebSocket STOMP updates when AI finishes generating SOAP
    this.wsSubscription = this.wsService.getSoapUpdates().subscribe((updatedNote: SoapNote) => {
      if (updatedNote) {
        this.soapNote.set(updatedNote);
      }
    });
  }

  endCallAndProcessAudio(): void {
    this.isCallActive.set(false);
    this.jitsiService.dispose();
    this.soapApi.notifyAudioReady(this.consultationId, `audio-${this.consultationId}.wav`).subscribe();
  }

  approveAndSign(): void {
    this.soapApi.updateSoap(this.consultationId, this.soapNote(), true).subscribe((signedNote) => {
      this.soapNote.set(signedNote);
    });
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
    this.jitsiService.dispose();
  }
}
