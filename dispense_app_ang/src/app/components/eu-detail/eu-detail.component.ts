import {Component, computed, inject, input, SecurityContext} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {DomSanitizer} from '@angular/platform-browser';
import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {Ue} from '../../model/ue';
import {DispenseService} from '../../services/dispense.service';
import {of} from 'rxjs';

/**
 * Détail d'une UE : programme et acquis d'apprentissage.
 * @author Ludovic
 */
@Component({
  selector: 'app-eu-detail',
  standalone: true,
  imports: [
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardSubtitle,
    MatCardContent,
    MatIcon,
    MatProgressBar,
    MatExpansionModule,
    MatChip,
    MatChipSet,
  ],
  templateUrl: './eu-detail.component.html',
  styleUrl: './eu-detail.component.css',
})
export class EuDetailComponent {
  /** UE sélectionnée (input du composant parent) */
  ue = input<Ue>();

  private readonly dispenseService = inject(DispenseService);
  private readonly sanitizer = inject(DomSanitizer);

  /** Programme HTML sanitisé pour éviter les injections XSS */
  safePrgm = computed(() => {
    const prgm = this.ue()?.prgm;
    return prgm ? this.sanitizer.sanitize(SecurityContext.HTML, prgm) ?? '' : '';
  });

  /** Charge le détail complet de l'UE dès que l'input change */
  private detailResource = rxResource({
    params: () => this.ue(),
    stream: ({params: currentUe}) => {
      if (!currentUe) return of(undefined);
      return this.dispenseService.getUeDetail(currentUe.code);
    },
  });

  /** Détail complet de l'UE (acquis inclus), undefined tant que non chargé */
  ueDetail = computed(() => this.detailResource.value());

  /** Vrai pendant le chargement HTTP */
  isLoading = computed(() => this.detailResource.isLoading());
}
