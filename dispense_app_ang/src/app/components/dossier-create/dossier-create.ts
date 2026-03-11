import {Component, inject} from '@angular/core';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {DossierService} from '../../services/dossier.service';

/**
 * Création automatique d'un dossier de dispense.
 *
 * Dès l'initialisation, appelle le backend pour créer le dossier
 * avec un objet auto-généré. Redirige vers le détail en cas de succès,
 * ou vers la liste avec un message d'erreur sinon.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-dossier-create',
  imports: [MatProgressSpinner],
  templateUrl: './dossier-create.html',
  styleUrl: './dossier-create.css',
})
export class DossierCreate {
  private dossierService = inject(DossierService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  constructor() {
    const now = new Date();
    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const yyyy = now.getFullYear();
    const objetDemande = `Demande de dispense du ${dd}/${mm}/${yyyy}`;

    this.dossierService.createDossier(objetDemande).subscribe({
      next: (dossier) => {
        this.router.navigate(['/dossier', dossier.id]);
      },
      error: (err) => {
        const msg = err.error?.error || err.error?.message || 'Erreur lors de la création du dossier.';
        this.snackBar.open(msg, 'Fermer', {duration: 5000});
        this.router.navigate(['/dossiers']);
      },
    });
  }
}
