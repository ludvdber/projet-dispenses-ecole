import {Component, computed, inject} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {DispenseService} from '../../services/dispense.service';
import {MatBadge} from '@angular/material/badge';
import {MatButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {Router} from '@angular/router';

/**
 * Badges affichant le nombre de dossiers en cours / traités dans la toolbar.
 * Permet de naviguer vers la création de dossier si aucun n'est en cours.
 *
 * Utilise rxResource pour charger les statistiques de façon réactive.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-dossier-stat',
  imports: [
    MatBadge,
    MatButton,
    MatTooltip,
  ],
  templateUrl: './dossier-stat.html',
  styleUrl: './dossier-stat.css',
})
export class DossierStat {
  private readonly router = inject(Router);
  private readonly dispenseService = inject(DispenseService);

  /** Charge les stats dossiers au montage du composant */
  private statsResource = rxResource({
    stream: () => this.dispenseService.getNbdossier(),
  });

  /** Nombre de dossiers en cours */
  enCours = computed(() => this.statsResource.value()?.enCours ?? 0);

  /** Nombre de dossiers traités */
  traite = computed(() => this.statsResource.value()?.traite ?? 0);

  /** On ne peut créer un dossier que s'il n'y en a pas en cours */
  canCreate = computed(() => this.enCours() === 0);

  protected creeDossier() {
    if (this.canCreate()) {
      this.router.navigate(['/createDossier']);
    }
  }
}
