export interface Doctor {
  id: string;
  name: string;
  email: string;
  crm: string;
  specialty: string;
}

export interface Patient {
  id: string;
  name: string;
  email: string;
  phone: string;
}

export interface Prescription {
  medication: string;
  dosage: string;
}

export interface SoapNote {
  id?: string;
  consultationId: string;
  subjective: string;
  objective: string;
  assessment: string;
  recommendations: string;
  followUpDays: number;
  prescriptions: string[];
  examsRequested: string[];
  confidenceScore?: number;
  rawTranscriptionLength?: number;
  status: 'DRAFT' | 'APPROVED_AND_SIGNED';
  signedByDoctorId?: string;
  signedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Consultation {
  id: string;
  roomName: string;
  doctor?: Doctor;
  patient?: Patient;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'AUDIO_PROCESSING' | 'PENDING_DOCTOR_APPROVAL' | 'FINALIZED' | 'CANCELLED';
  soapNote?: SoapNote;
  audioStorageKey?: string;
}

export interface JitsiTokenResponse {
  token: string;
  room: string;
  domain: string;
  isDoctor: boolean;
}
