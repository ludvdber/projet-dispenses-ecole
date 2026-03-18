import {computed, effect, inject, Injectable, signal} from '@angular/core';
import Keycloak from 'keycloak-js';
import {KEYCLOAK_EVENT_SIGNAL, KeycloakEventType, ReadyArgs, typeEventArgs} from 'keycloak-angular';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly keycloak = inject(Keycloak);
  private readonly keycloakSignal = inject(KEYCLOAK_EVENT_SIGNAL);

  /** État d'authentification réactif */
  readonly authenticated = signal(false);
  /** Nom d'utilisateur réactif */
  readonly username = signal('');

  /** Signal dérivé pour lecture dans les templates */
  readonly isAuthenticated = computed(() => this.authenticated());

  constructor() {
    effect(() => {
      const keycloakEvent = this.keycloakSignal();

      if (keycloakEvent.type === KeycloakEventType.Ready) {
        const connected = typeEventArgs<ReadyArgs>(keycloakEvent.args);

        if (connected) {
          this.keycloak.loadUserProfile().then(userProfile => {
            this.username.set(userProfile.username ?? '');
            this.authenticated.set(true);
          }).catch(() => {
            this.username.set('');
            this.authenticated.set(false);
          });
        } else {
          this.authenticated.set(false);
          this.username.set('');
        }
      }

      if (keycloakEvent.type === KeycloakEventType.AuthLogout) {
        this.authenticated.set(false);
        this.username.set('');
      }
    });
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}

