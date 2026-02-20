import {Component, computed, inject, signal} from '@angular/core';
import {MatCard, MatCardActions, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MatError, MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {DispenseService} from '../../services/dispense.service';
import {DatePipe} from '@angular/common';

/**
 * Formulaire de création d'un dossier de dispense.
 *
 * Utilise les Signal Forms Angular 21 (signal + computed, sans FormGroup).
 * Affiche un retour visuel via MatSnackBar et redirige vers /home après succès.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-dossier-create',
  imports: [
    MatCard,
    MatCardTitle,
    MatIcon,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatInput,
    ReactiveFormsModule,
    MatError,
    MatCardActions,
    MatButton,
    DatePipe,
  ],
  templateUrl: './dossier-create.html',
  styleUrl: './dossier-create.css',
})
export class DossierCreate {
  private dossierService = inject(DispenseService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  readonly dateCreation = new Date();

  /** Valeur de l'objet de la demande, liée au textarea par signal. */
  objetDemande = signal('');

  /** Vrai si le texte contient au moins 10 caractères non-blancs. */
  isValid = computed(() => this.objetDemande().trim().length >= 10);

  /** Vrai pendant la soumission HTTP pour désactiver le bouton et éviter les doublons. */
  isSubmitting = signal(false);

  createDossier() {
    if (!this.isValid() || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.dossierService.createDossier(this.objetDemande()).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open('Dossier créé avec succès !', 'Fermer', {duration: 3000});
        this.router.navigate(['/home']);
      },
      error: err => {
        this.isSubmitting.set(false);
        const msg =
          err.error?.objetDemande ||
          err.error?.message ||
          'Erreur lors de la création du dossier.';
        this.snackBar.open(msg, 'Fermer', {duration: 5000});
      },
    });
  }
}
