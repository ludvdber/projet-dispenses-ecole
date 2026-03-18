import {Component, effect, inject, signal, viewChild, computed} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {of} from 'rxjs';
import {Section} from '../../model/section';
import {Ue} from '../../model/ue';
import {DispenseService} from '../../services/dispense.service';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatChipListbox, MatChipOption} from '@angular/material/chips';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatButton, MatIconButton} from '@angular/material/button';
import {EuDetailComponent} from '../eu-detail/eu-detail.component';
import {MatIcon} from '@angular/material/icon';

/**
 * Page de consultation des UE par section.
 * Chips sections + table triable, détail pleine largeur au clic.
 * @author Ludovic
 */
@Component({
  selector: 'app-section',
  imports: [
    MatTableModule,
    MatSortModule,
    MatChipListbox,
    MatChipOption,
    MatFormField,
    MatLabel,
    MatInput,
    MatProgressBar,
    MatButton,
    MatIconButton,
    EuDetailComponent,
    MatIcon,
  ],
  templateUrl: './section.composant.html',
  styleUrl: './section.composant.css',
})
export class SectionComposant {
  private readonly dispenseService = inject(DispenseService);

  // --- Signaux d'état ---
  selectedSection = signal<Section | undefined>(undefined);
  selectedUe = signal<Ue | undefined>(undefined);

  // Colonnes affichées dans la table
  displayedColumns = ['code', 'nom', 'ects', 'nbPeriodes'];

  // DataSource Material pour le tri et le filtre
  dataSource = new MatTableDataSource<Ue>([]);

  // Référence au MatSort du template (pour connecter le tri)
  private sort = viewChild(MatSort);

  // --- Chargement des données ---
  sectionsResource = rxResource({
    stream: () => this.dispenseService.getSections(),
  });
  sections = computed(() => this.sectionsResource.value() ?? []);

  uesResource = rxResource({
    params: () => this.selectedSection(),
    stream: ({params: section}) => {
      if (!section) return of([] as Ue[]);
      return this.dispenseService.getUesBySection(section.code);
    },
  });
  ues = computed(() => this.uesResource.value() ?? []);
  uesLoading = computed(() => this.uesResource.isLoading());

  constructor() {
    // Quand les UEs changent, on met à jour la dataSource
    effect(() => {
      this.dataSource.data = this.ues();
    });

    // Quand le MatSort est disponible dans le DOM, on le connecte
    effect(() => {
      const s = this.sort();
      if (s) this.dataSource.sort = s;
    });
  }

  selectSection(section: Section): void {
    this.selectedSection.set(section);
    this.selectedUe.set(undefined);
    this.dataSource.filter = '';
  }

  selectUe(ue: Ue): void {
    this.selectedUe.set(ue);
  }

  closeDetail(): void {
    this.selectedUe.set(undefined);
  }

  applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
