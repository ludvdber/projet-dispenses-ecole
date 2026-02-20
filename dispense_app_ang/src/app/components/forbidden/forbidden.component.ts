import {Component} from '@angular/core';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {MatButton} from '@angular/material/button';

/**
 * Composant affiché lorsqu'un utilisateur tente d'accéder à une route
 * pour laquelle il ne possède pas les rôles requis.
 * Redirigé depuis le guard {@link canActivateAuthRole}.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatIcon,
    RouterLink,
    MatButton,
  ],
  templateUrl: './forbidden.component.html',
  styleUrl: './forbidden.component.css',
})
export class ForbiddenComponent {}
