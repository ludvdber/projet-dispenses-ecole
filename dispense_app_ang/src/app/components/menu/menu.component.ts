import {Component, effect, inject} from '@angular/core';

import {RouterModule} from '@angular/router';
import {HasRolesDirective} from 'keycloak-angular';
import {AuthService} from '../../auth/auth.service';
import {MatIconModule} from '@angular/material/icon';
import {SidebarService} from '../sidebar/sidebar.service';
import {DispenseService} from '../../services/dispense.service';
import {DossierStat} from '../dossier-stat/dossier-stat';
import {MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';

@Component({
  selector: 'app-menu',
  imports: [RouterModule, MatIconModule, DossierStat, MatIconButton, MatTooltip],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class MenuComponent {
  private authService: AuthService = inject(AuthService);
  protected authenticated = this.authService.isAuthenticated();

  constructor(private sidebarService: SidebarService, private dispenseService: DispenseService) {
  }

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
