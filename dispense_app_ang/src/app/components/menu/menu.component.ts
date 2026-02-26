import {Component, inject} from '@angular/core';
import {RouterModule} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbar} from '@angular/material/toolbar';
import {SidebarService} from '../sidebar/sidebar.service';
import {DossierStat} from '../dossier-stat/dossier-stat';
import {MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';

/**
 * Barre d'outils principale de l'application.
 * Contient le branding, le bouton drawer et les actions d'authentification.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-menu',
  imports: [RouterModule, MatIconModule, MatToolbar, DossierStat, MatIconButton, MatTooltip],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class MenuComponent {
  private authService = inject(AuthService);
  private sidebarService = inject(SidebarService);

  protected authenticated = this.authService.isAuthenticated();

  login() {
    this.authService.login();
  }

  logout() {
    this.authService.logout();
  }

  getUsername() {
    return this.authService.username;
  }

  toggleMenu() {
    this.sidebarService.toggleMenu();
  }

}
