import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {NbDossier} from '../../model/nb-dossier';
import {DispenseService} from '../../services/dispense.service';
import {MatBadge} from '@angular/material/badge';
import {MatButton} from '@angular/material/button';
import {Router} from '@angular/router';

@Component({
  selector: 'app-dossier-stat',
  imports: [
    MatBadge,
    MatButton
  ],
  templateUrl: './dossier-stat.html',
  styleUrl: './dossier-stat.css',
})
export class DossierStat implements OnInit {
  private readonly router = inject(Router);
  private readonly dispenseService = inject(DispenseService);

  nbDossier = signal<NbDossier>({
    traite: 0,
    enCours: 0
  });


  canCreate = computed(() => this.nbDossier().enCours === 0);


  getNbDossier() {
    this.dispenseService.getNbdossier().subscribe(
      {
        next: (nbDossier: NbDossier) => {
          this.nbDossier.set(nbDossier);
        },
        error: err => {
          console.log(err);
        }
      }
    )
  }

  ngOnInit() {
    this.getNbDossier()

  }

  protected creeDossier() {
    if (this.canCreate()) {
      this.router.navigate(['/createDossier']);
    }
  }
}
