import {Component, inject, OnInit} from '@angular/core';
import {Section} from '../../model/section';
import {Ue} from '../../model/ue';
import {DispenseService} from '../../services/dispense.service';
import {MatDivider, MatList, MatListItem, MatNavList} from '@angular/material/list';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {EuDetailComponent} from '../eu-detail/eu-detail.component';
import {MatIcon} from '@angular/material/icon';


@Component({
  selector: 'app-section',
  imports: [
    MatList,
    MatListItem,
    MatSidenav,
    MatSidenavContainer,
    MatSidenavContent,
    EuDetailComponent,
    MatNavList,
    MatDivider,
    MatIcon,
  ],
  templateUrl: './section.composant.html',
  styleUrl: './section.composant.css',
})
export class SectionComposant implements OnInit {

  dispenseService = inject(DispenseService);

  sections: Section[] = [];
  selectedSection?: Section;

  private uesCache = new Map<string, Ue[]>();//cache
  ues: Ue[] = [];
  selectedUe?: Ue;


  loadUes(sectionId: string) {
    this.selectedUe = undefined;
    if (this.uesCache.has(sectionId)) {
      this.ues = this.uesCache.get(sectionId)!;
      return;
    }

    this.dispenseService.getUesBySection(sectionId).subscribe({
      next: data => {
        this.ues = data;
        this.uesCache.set(sectionId, data);//mise en cache
      },
      error: error => console.error(error)
    });
  }

  selectSection(section: Section): void {
    this.selectedSection = section;
    this.selectedUe = undefined;
    this.loadUes(section.code);
  }

  selectUe(ue: Ue): void {
    this.selectedUe = ue;
  }

  ngOnInit(): void {
    this.dispenseService.getSections().subscribe({
      next: data => {
        this.sections = data;
        //précharge le 1èr onglet
        if (this.sections.length > 0) {
          this.loadUes(this.sections[0].code);
        }
      },
      error: err => console.error(err)
    });
  }
}
