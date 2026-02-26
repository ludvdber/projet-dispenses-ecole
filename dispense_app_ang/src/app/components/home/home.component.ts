import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MatAnchor, MatButton} from '@angular/material/button';
import {AuthService} from '../../auth/auth.service';

/**
 * Page d'accueil présentant l'application et un accès rapide aux UE.
 * Affiche un bouton de connexion si l'utilisateur n'est pas authentifié.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardSubtitle,
    MatCardContent,
    MatIcon,
    MatAnchor,
    MatButton,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private authService = inject(AuthService);

  /** Etat d'authentification réactif */
  authenticated = this.authService.isAuthenticated();

  login() {
    this.authService.login();
  }
}
