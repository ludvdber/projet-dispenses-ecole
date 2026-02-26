import {Component, computed, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {of} from 'rxjs';
import {Section} from '../../model/section';
import {Ue} from '../../model/ue';
import {DispenseService} from '../../services/dispense.service';
import {MatDivider} from '@angular/material/divider';
import {MatListItem, MatNavList} from '@angular/material/list';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {MatProgressBar} from '@angular/material/progress-bar';
import {EuDetailComponent} from '../eu-detail/eu-detail.component';
import {MatIcon} from '@angular/material/icon';

/**
 * Page de consultation des sections et de leurs UE.
 * Panneau gauche : liste sections + UE ; panneau droit : détail UE.
 *
 * Utilise rxResource pour le chargement réactif des sections et des UE.
 *
 * @author Ludovic
 */
@Component({
  selector: 'app-section',
  imports: [
    MatListItem,
    MatNavList,
    MatSidenav,
    MatSidenavContainer,
    MatSidenavContent,
    MatDivider,
    MatProgressBar,
    EuDetailComponent,
    MatIcon,
  ],
  templateUrl: './section.composant.html',
  styleUrl: './section.composant.css',
})
export class SectionComposant {
  private dispenseService = inject(DispenseService);

  /** Section sélectionnée par l'utilisateur */
  selectedSection = signal<Section | undefined>(undefined);

  /** UE sélectionnée pour affichage du détail */
  selectedUe = signal<Ue | undefined>(undefined);

  /** Chargement réactif de la liste des sections */
  sectionsResource = rxResource({
    stream: () => this.dispenseService.getSections(),
  });

  /** Liste des sections chargées */
  sections = computed(() => this.sectionsResource.value() ?? []);

  /** Chargement réactif des UE — se déclenche à chaque changement de section */
  uesResource = rxResource({
    params: () => this.selectedSection(),
    stream: ({params: section}) => {
      if (!section) return of([] as Ue[]);
      return this.dispenseService.getUesBySection(section.code);
    },
  });

  /** Liste des UE de la section courante */
  ues = computed(() => this.uesResource.value() ?? []);

  /** Vrai si les UE sont en cours de chargement */
  uesLoading = computed(() => this.uesResource.isLoading());

  selectSection(section: Section): void {
    this.selectedSection.set(section);
    this.selectedUe.set(undefined);
  }

  selectUe(ue: Ue): void {
    this.selectedUe.set(ue);
  }
}
