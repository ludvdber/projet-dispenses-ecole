import {Component, computed, inject, resource} from '@angular/core';
import Keycloak from 'keycloak-js';
import {User} from '../../model/user';
import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MatDivider} from '@angular/material/divider';
import {MatProgressBar} from '@angular/material/progress-bar';

/**
 * Affiche le profil de l'utilisateur connecté (données Keycloak).
 * Utilise resource() Angular 21 pour le chargement asynchrone du profil.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-user-profile',
  imports: [
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardSubtitle,
    MatCardContent,
    MatIcon,
    MatDivider,
    MatProgressBar,
  ],
  templateUrl: 'user-profile.component.html',
  styleUrl: 'user-profile.component.css',
})
export class UserProfileComponent {
  private readonly keycloak = inject(Keycloak);

  /** Charge le profil Keycloak de façon réactive */
  private profileResource = resource({
    loader: async (): Promise<User | undefined> => {
      if (!this.keycloak?.authenticated) return undefined;
      const profile = await this.keycloak.loadUserProfile();
      return {
        prenom: profile?.firstName,
        nom: profile?.lastName,
        email: profile?.email,
        username: profile?.username,
      };
    },
  });

  /** Profil chargé, undefined tant que non disponible */
  user = computed(() => this.profileResource.value());

  /** Vrai pendant le chargement initial */
  isLoading = computed(() => this.profileResource.isLoading());
}
