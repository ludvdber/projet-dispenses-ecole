import {Component, effect, inject, untracked, ViewChild} from '@angular/core';
import {MatSidenav, MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatListModule} from '@angular/material/list';
import {MatDivider} from '@angular/material/divider';
import {RouterModule} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {SidebarService} from './sidebar.service';
import {MenuComponent} from '../menu/menu.component';

/**
 * Layout principal : drawer latéral de navigation + zone de contenu.
 * Le drawer est piloté par le signal toggleCmd du SidebarService.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-sidebar-cmp',
  imports: [
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatDivider,
    MenuComponent,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  @ViewChild(MatSidenav) drawer!: MatSidenav;

  constructor(private sidebarService: SidebarService) {
    effect(() => {
      this.sidebarService.toggleCmd();
      untracked(() => this.drawer?.toggle());
    });
  }
}
