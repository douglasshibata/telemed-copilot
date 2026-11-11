import { Component } from '@angular/core';
import { ConsultationRoomComponent } from './features/consultation-room/consultation-room.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ConsultationRoomComponent],
  template: `<app-consultation-room></app-consultation-room>`
})
export class AppComponent {}
