import {Component, computed, DestroyRef, effect, inject, signal, viewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {of} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {CoursEcole} from '../../model/cours-ecole';
import {DatePipe} from '@angular/common';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {MatChip} from '@angular/material/chips';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatSelect, MatOption} from '@angular/material/select';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTooltip} from '@angular/material/tooltip';
import {MatStepper, MatStep, MatStepLabel, MatStepperNext, MatStepperPrevious} from '@angular/material/stepper';
import {DossierService} from '../../services/dossier.service';
import {CoursService} from '../../services/cours.service';
import {DocumentService} from '../../services/document.service';
import {EtatDossier, etatLabel, etatColor, etatMessage} from '../../model/etat-dossier';
import {CoursEtudiant} from '../../model/cours-etudiant';
import {Dispense} from '../../model/dispense';

/**
 * Détail d'un dossier de dispense : stepper 4 étapes (cours, dispenses, documents, soumission).
 * @author Ludovic
 */
@Component({
  selector: 'app-dossier-detail',
  imports: [
    DatePipe,
    MatCard, MatCardHeader, MatCardTitle, MatCardSubtitle, MatCardContent, MatCardActions,
    MatChip, MatButton, MatIconButton, MatIcon,
    MatFormField, MatLabel, MatInput, MatSelect, MatOption,
    MatProgressSpinner, MatTooltip,
    MatStepper, MatStep, MatStepLabel, MatStepperNext, MatStepperPrevious,
  ],
  templateUrl: './dossier-detail.component.html',
  styleUrl: './dossier-detail.component.css',
})
export class DossierDetailComponent {

  // ======================== Injections ========================

  private readonly dossierService = inject(DossierService);
  private readonly coursService = inject(CoursService);
  private readonly documentService = inject(DocumentService);
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  // ======================== Identifiant et navigation ========================

  readonly dossierId = signal(Number(this.route.snapshot.paramMap.get('id')));
  stepper = viewChild(MatStepper);

  etatLabel = etatLabel;
  etatColor = etatColor;
  etatMessage = etatMessage;

  // Redirection si le dossier n'existe pas ou n'appartient pas a l'utilisateur
  private errorRedirect = effect(() => {
    const err = this.dossierResource.error();
    if (err) {
      this.snackBar.open('Dossier introuvable ou accès interdit', 'Fermer', {duration: 4000});
      this.router.navigate(['/dossiers']);
    }
  });

  // ======================== Ressources HTTP ========================

  dossierResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.dossierService.getDossier(id),
  });

  coursResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.coursService.getCoursParDossier(id),
  });

  dispenseResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.dossierService.getDispensesParDossier(id),
  });

  documentsResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.documentService.getDocumentsParDossier(id),
  });

  completudeResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.dossierService.getCompletude(id),
  });

  analyseResource = rxResource({
    params: () => this.dossierId(),
    stream: ({params: id}) => this.dossierService.getAnalyse(id),
  });

  ecolesResource = rxResource({
    stream: () => this.dossierService.getEcoles(),
  });

  uesResource = rxResource({
    stream: () => this.dossierService.getUes(),
  });

  // ======================== Computed : donnees principales ========================

  dossier = computed(() => this.dossierResource.value());
  cours = computed(() => this.coursResource.value() ?? []);
  dispenses = computed(() => this.dispenseResource.value() ?? []);
  documents = computed(() => this.documentsResource.value() ?? []);
  completude = computed(() => this.completudeResource.value());
  suggestions = computed(() => this.analyseResource.value() ?? []);
  ecoles = computed(() => this.ecolesResource.value() ?? []);
  ues = computed(() => this.uesResource.value() ?? []);
  isLoading = computed(() => this.dossierResource.isLoading());

  isModifiable = computed(() => {
    const etat = this.dossier()?.etat;
    return etat === EtatDossier.DEMANDE_EN_COURS || etat === EtatDossier.ATTENTE_COMPLEMENT;
  });

  canSubmit = computed(() =>
    this.completude()?.complet === true && this.isModifiable()
  );

  // ======================== Computed : documents filtres ========================

  bulletinDocs = computed(() => this.documents().filter(d => d.typeDoc === 'BULLETIN'));
  motivationDocs = computed(() => this.documents().filter(d => d.typeDoc === 'MOTIVATION'));
  programmeDocs = computed(() => this.documents().filter(d => d.typeDoc === 'PROGRAMME_COURS'));

  // ======================== Computed : validite des steps ========================

  step1Valid = computed(() => {
    const c = this.cours();
    if (c.length === 0) return false;
    const inconnus = c.filter(x => x.statutSaisie === 'INCONNU');
    return inconnus.every(inc => inc.urlFiche || this.hasProgDoc(inc.id!));
  });

  step2Valid = computed(() => {
    const d = this.dispenses();
    if (d.length === 0) return false;
    return d.every(disp => disp.coursJustificatifs && disp.coursJustificatifs.length > 0);
  });

  step3Valid = computed(() =>
    this.bulletinDocs().length > 0 && this.motivationDocs().length > 0
  );

  // ======================== Signaux formulaire cours ========================

  selectedEcoleId = signal<string | null>(null);
  nomEcoleLibre = signal('');
  selectedCoursCode = signal<string | null>(null);
  coursCodeCours = signal('');
  coursIntitule = signal('');
  coursEcts = signal<number | null>(null);
  coursUrlFiche = signal('');

  coursEcoleResource = rxResource({
    params: () => this.selectedEcoleId(),
    stream: ({params: ecoleId}) => {
      if (!ecoleId || ecoleId === 'AUTRE') return of([] as CoursEcole[]);
      return this.dossierService.getCoursEcole(ecoleId);
    },
  });

  coursEcole = computed(() => this.coursEcoleResource.value() ?? []);

  isEcoleConnue = computed(() => {
    const id = this.selectedEcoleId();
    return id != null && id !== 'AUTRE';
  });

  hasCoursConnus = computed(() => this.coursEcole().length > 0);

  selectedCoursConnu = computed(() => {
    const code = this.selectedCoursCode();
    if (!code || code === 'AUTRE') return null;
    return this.coursEcole().find(c => c.codeCours === code) ?? null;
  });

  isCoursLibre = computed(() => {
    if (!this.isEcoleConnue()) return true;
    if (!this.hasCoursConnus()) return true;
    return this.selectedCoursCode() === 'AUTRE';
  });

  coursFormValid = computed(() => {
    const ecoleId = this.selectedEcoleId();
    if (ecoleId == null) return false;
    if (ecoleId === 'AUTRE' && !this.nomEcoleLibre().trim()) return false;

    if (this.selectedCoursConnu()) return true;

    const ects = this.coursEcts();
    if (ects != null && ects < 1) return false;
    return this.coursCodeCours().trim().length > 0 && this.coursIntitule().trim().length > 0;
  });

  // ======================== Signaux formulaire dispense ========================

  selectedUeCode = signal<string | null>(null);
  selectedCoursIdMap = signal<Record<number, number | null>>({});

  // ======================== Signaux inline URL/upload pour cours INCONNU ========================

  inlineUrlFiche = signal<Record<number, string>>({});

  // ======================== Helpers ========================

  setCoursForDispense(dispenseId: number, coursId: number | null): void {
    this.selectedCoursIdMap.update(m => ({...m, [dispenseId]: coursId}));
  }

  hasProgDoc(coursId: number): boolean {
    return this.programmeDocs().some(d => d.coursEtudiantId === coursId);
  }

  dispenseBadge(disp: Dispense): 'reconnu' | 'inconnu' | null {
    const cours = disp.coursJustificatifs;
    if (!cours || cours.length === 0) return null;
    return disp.correspondanceReconnue ? 'reconnu' : 'inconnu';
  }

  retour(): void {
    this.router.navigate(['/dossiers']);
  }

  goToStep(index: number): void {
    const s = this.stepper();
    if (s) s.selectedIndex = index;
  }

  // ======================== Actions cours ========================

  ajouterCours(): void {
    if (!this.coursFormValid()) return;

    const cours = this.buildCoursFromForm();
    this.coursService.addCours(cours).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.reloadCoursData();
        this.resetCoursForm();
        this.snackBar.open('Cours ajouté', 'OK', {duration: 2000});
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  supprimerCours(id: number): void {
    this.coursService.deleteCours(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.coursResource.reload();
        this.completudeResource.reload();
        this.snackBar.open('Cours supprimé', 'OK', {duration: 2000});
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  onProgFileSelected(event: Event, coursId: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.documentService.uploadDocument(this.dossierId(), 'PROGRAMME_COURS', file, coursId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.documentsResource.reload();
          this.completudeResource.reload();
          input.value = '';
          this.snackBar.open('Programme uploadé', 'OK', {duration: 2000});
        },
        error: (err: HttpErrorResponse) => {
          input.value = '';
          this.showError(err);
        },
      });
  }

  // ======================== Actions dispenses ========================

  creerDispense(codeUe?: string): void {
    const code = codeUe ?? this.selectedUeCode();
    if (!code || !code.trim()) return;
    this.dossierService.createDispense(this.dossierId(), code)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.reloadDispenseData();
          this.selectedUeCode.set(null);
          this.snackBar.open('Dispense créée', 'OK', {duration: 2000});
        },
        error: (err: HttpErrorResponse) => this.showError(err),
      });
  }

  supprimerDispense(id: number): void {
    this.dossierService.deleteDispense(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.dispenseResource.reload();
        this.completudeResource.reload();
        this.snackBar.open('Dispense supprimée', 'OK', {duration: 2000});
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  lierCours(dispenseId: number): void {
    const coursId = this.selectedCoursIdMap()[dispenseId];
    if (coursId == null) return;
    this.dossierService.lierCours(dispenseId, coursId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.dispenseResource.reload();
          this.completudeResource.reload();
          this.selectedCoursIdMap.update(m => ({...m, [dispenseId]: null}));
          this.snackBar.open('Cours lié', 'OK', {duration: 2000});
        },
        error: (err: HttpErrorResponse) => this.showError(err),
      });
  }

  delierCours(dispenseId: number, coursId: number): void {
    this.dossierService.delierCours(dispenseId, coursId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.dispenseResource.reload();
          this.completudeResource.reload();
        },
        error: (err: HttpErrorResponse) => this.showError(err),
      });
  }

  // ======================== Actions documents ========================

  onFileSelected(event: Event, typeDoc: string): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.documentService.uploadDocument(this.dossierId(), typeDoc, file)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.documentsResource.reload();
          this.completudeResource.reload();
          input.value = '';
          this.snackBar.open('Document uploadé', 'OK', {duration: 2000});
        },
        error: (err: HttpErrorResponse) => {
          input.value = '';
          this.showError(err);
        },
      });
  }

  supprimerDocument(id: number): void {
    this.documentService.softDeleteDocument(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.documentsResource.reload();
        this.completudeResource.reload();
        this.snackBar.open('Document supprimé', 'OK', {duration: 2000});
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  telecharger(id: number, filename: string): void {
    const url = this.documentService.getDownloadUrl(id);
    this.http.get(url, {responseType: 'blob'}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (blob: Blob) => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = filename;
        a.click();
        URL.revokeObjectURL(a.href);
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  // ======================== Soumission ========================

  soumettre(): void {
    this.dossierService.submitDossier(this.dossierId()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.snackBar.open('Dossier soumis avec succès !', 'OK', {duration: 3000});
        this.router.navigate(['/dossiers']);
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  // ======================== Methodes privees ========================

  private buildCoursFromForm(): CoursEtudiant {
    const ecoleId = this.selectedEcoleId();
    const connu = this.selectedCoursConnu();
    return {
      dossierId: this.dossierId(),
      codeEcole: ecoleId !== 'AUTRE' ? ecoleId! : undefined,
      nomEcole: ecoleId === 'AUTRE' ? this.nomEcoleLibre() : undefined,
      codeCours: connu ? connu.codeCours : this.coursCodeCours(),
      intitule: connu ? connu.intitule : this.coursIntitule(),
      ects: connu ? (connu.ects ?? undefined) : (this.coursEcts() ?? undefined),
      urlFiche: connu ? undefined : (this.coursUrlFiche() || undefined),
    };
  }

  private resetCoursForm(): void {
    this.selectedEcoleId.set(null);
    this.nomEcoleLibre.set('');
    this.selectedCoursCode.set(null);
    this.coursCodeCours.set('');
    this.coursIntitule.set('');
    this.coursEcts.set(null);
    this.coursUrlFiche.set('');
  }

  private reloadCoursData(): void {
    this.coursResource.reload();
    this.analyseResource.reload();
    this.completudeResource.reload();
  }

  private reloadDispenseData(): void {
    this.dispenseResource.reload();
    this.analyseResource.reload();
    this.completudeResource.reload();
  }

  private showError(err: HttpErrorResponse): void {
    const msg = err.error?.error || err.error?.message || 'Erreur inattendue';
    this.snackBar.open(msg, 'Fermer', {duration: 5000});
  }
}
