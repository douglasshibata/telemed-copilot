import { Injectable } from '@angular/core';

declare global {
  interface Window {
    JitsiMeetExternalAPI: any;
  }
}

@Injectable({
  providedIn: 'root'
})
export class JitsiService {
  private api: any = null;

  initializeJitsi(containerId: string, domain: string, room: string, jwtToken: string, options: any = {}): any {
    if (typeof window !== 'undefined' && window.JitsiMeetExternalAPI) {
      const config = {
        roomName: room,
        parentNode: document.getElementById(containerId),
        jwt: jwtToken,
        width: '100%',
        height: '100%',
        configOverwrite: {
          startWithAudioMuted: false,
          startWithVideoMuted: false,
          disableDeepLinking: true
        },
        interfaceConfigOverwrite: {
          TOOLBAR_BUTTONS: [
            'microphone', 'camera', 'desktop', 'hangup', 'tileview'
          ]
        },
        ...options
      };

      this.api = new window.JitsiMeetExternalAPI(domain, config);
      return this.api;
    }
    return null;
  }

  dispose(): void {
    if (this.api) {
      this.api.dispose();
      this.api = null;
    }
  }
}
