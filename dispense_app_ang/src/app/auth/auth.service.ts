import {effect, inject, Injectable, signal} from '@angular/core';
import Keycloak from 'keycloak-js';
import {KEYCLOAK_EVENT_SIGNAL, KeycloakEventType, ReadyArgs, typeEventArgs} from 'keycloak-angular';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  public authenticated = signal(false);
  username: string | undefined = "";
  private readonly keycloak = inject(Keycloak);
  private readonly keycloakSignal = inject(KEYCLOAK_EVENT_SIGNAL);

  constructor() {
    effect(() => {
      const keycloakEvent = this.keycloakSignal();

      if (keycloakEvent.type === KeycloakEventType.Ready) {
        let connected = typeEventArgs<ReadyArgs>(keycloakEvent.args)

        if (connected) {
          this.keycloak.loadUserProfile().then(userProfile => {
              this.username = userProfile.username;
              this.authenticated.set(connected);
            }
          ).catch(
            error => {
              this.username = "";
            }
          )

        }
      }

      if (keycloakEvent.type === KeycloakEventType.AuthLogout) {
        this.username = ""
      }

    });
  }

  isAuthenticated() {
    return this.authenticated;
  }

  getUsername() {
    return this.username
  }


  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}

