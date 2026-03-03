import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {AuthService} from '../../auth/auth.service';

/**
 * Page d'accueil avec hero et cartes d'action rapide.
 * @author Ludovic
 */
@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    MatCard,
    MatCardContent,
    MatIcon,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private authService = inject(AuthService);

  authenticated = this.authService.isAuthenticated();

  login() {
    this.authService.login();
  }
}
