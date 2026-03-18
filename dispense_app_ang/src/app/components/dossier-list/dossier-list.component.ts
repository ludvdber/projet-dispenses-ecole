import {Component, computed, inject} from '@angular/core';
import {Router} from '@angular/router';
import {rxResource} from '@angular/core/rxjs-interop';
import {DatePipe} from '@angular/common';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {MatChip} from '@angular/material/chips';
import {MatButton, MatFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatTooltip} from '@angular/material/tooltip';
import {DossierService} from '../../services/dossier.service';
import {EtatDossier, etatLabel, etatColor} from '../../model/etat-dossier';

/**
 * Liste des dossiers de dispense de l'étudiant connecté.
 * @author Ludovic
 */
@Component({
  selector: 'app-dossier-list',
  imports: [
    DatePipe,
    MatCard, MatCardHeader, MatCardTitle, MatCardSubtitle, MatCardContent, MatCardActions,
    MatChip, MatButton, MatFabButton, MatIcon, MatProgressSpinner, MatTooltip,
  ],
  templateUrl: './dossier-list.component.html',
  styleUrl: './dossier-list.component.css',
})
export class DossierListComponent {
  private readonly dossierService = inject(DossierService);
  private readonly router = inject(Router);

  dossiersResource = rxResource({
    stream: () => this.dossierService.getDossiers(),
  });

  dossiers = computed(() => this.dossiersResource.value() ?? []);
  isLoading = computed(() => this.dossiersResource.isLoading());

  etatLabel = etatLabel;
  etatColor = etatColor;

  voirDossier(id: number) {
    this.router.navigate(['/dossier', id]);
  }

  nouveauDossier() {
    this.router.navigate(['/createDossier']);
  }
}
