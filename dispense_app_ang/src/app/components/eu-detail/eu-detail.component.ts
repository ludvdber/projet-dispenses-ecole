import {Component, effect, inject, input, signal} from '@angular/core';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {Ue} from '../../model/ue';
import {UeFullDto} from '../../model/ue-full-dto';
import {DispenseService} from '../../services/dispense.service';

/**
 * Affiche le détail d'une UE sélectionnée, incluant son programme
 * et la liste complète de ses acquis d'apprentissage chargés depuis
 * GET /api/ue/detail/{code}.
 *
 * Utilise un InputSignal Angular 21 et un effect() pour recharger
 * automatiquement le détail lorsque l'UE change.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-eu-detail',
  standalone: true,
  imports: [
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatProgressSpinner,
    MatExpansionModule,
    MatChip,
    MatChipSet,
  ],
  templateUrl: './eu-detail.component.html',
  styleUrl: './eu-detail.component.css',
})
export class EuDetailComponent {
  /** UE sélectionnée transmise par SectionComposant via liaison de propriété. */
  ue = input<Ue>();

  private dispenseService = inject(DispenseService);

  /** Détail complet de l'UE (acquis inclus), null tant que non chargé. */
  ueDetail = signal<UeFullDto | null>(null);

  /** Indique qu'un chargement HTTP est en cours. */
  isLoading = signal(false);

  constructor() {
    // Réagit automatiquement à chaque changement de l'input signal ue()
    effect(() => {
      const currentUe = this.ue();
      if (currentUe) {
        this.isLoading.set(true);
        this.ueDetail.set(null);
        this.dispenseService.getUeDetail(currentUe.code).subscribe({
          next: detail => {
            this.ueDetail.set(detail);
            this.isLoading.set(false);
          },
          error: () => {
            this.isLoading.set(false);
          },
        });
      } else {
        this.ueDetail.set(null);
      }
    });
  }
}
