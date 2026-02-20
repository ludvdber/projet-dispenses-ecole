import {Component, effect, inject, untracked, ViewChild} from '@angular/core';
import {MatSidenav, MatSidenavContainer, MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbar, MatToolbarModule} from '@angular/material/toolbar';
import {MatListItem, MatListModule, MatNavList} from '@angular/material/list';
import {RouterLink, RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {BooleanInput} from '@angular/cdk/coercion';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map, shareReplay} from 'rxjs';
import {SidebarService} from './sidebar.service';
import {MenuComponent} from '../menu/menu.component';
import {AuthService} from '../../auth/auth.service';

@Component({
  selector: 'app-sidebar-cmp',
  imports: [
    RouterModule,
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MenuComponent,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  private authService: AuthService = inject(AuthService);
  protected authenticated = this.authService.isAuthenticated();

  @ViewChild(MatSidenav) drawer!: MatSidenav;

  private breakpointObserver = inject(BreakpointObserver);
  isHandset$ = this.breakpointObserver.observe([Breakpoints.Small]).pipe(
    map((result) => result.matches),
    //shareReplay()
  )

  constructor(public sidebarService: SidebarService) {
    effect(() => {
      this.sidebarService.toggleCmd();
      untracked(() => {
        this.drawer?.toggle();
      })
    });
  }
}
