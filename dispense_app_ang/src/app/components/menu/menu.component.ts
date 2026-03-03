import {Component, inject} from '@angular/core';
import {RouterModule} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbar} from '@angular/material/toolbar';
import {DossierStat} from '../dossier-stat/dossier-stat';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatDivider} from '@angular/material/divider';

/**
 * Barre de navigation principale.
 * Liens directs dans la toolbar, plus besoin de drawer.
 * @author Ludovic
 */
@Component({
  selector: 'app-menu',
  imports: [RouterModule, MatIconModule, MatToolbar, DossierStat, MatButton, MatIconButton, MatTooltip, MatMenu, MatMenuItem, MatMenuTrigger, MatDivider],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class MenuComponent {
  private authService = inject(AuthService);

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
}
