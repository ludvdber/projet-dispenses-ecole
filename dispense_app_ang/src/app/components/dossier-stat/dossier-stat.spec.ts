import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DossierStat } from './dossier-stat';

describe('DossierStat', () => {
  let component: DossierStat;
  let fixture: ComponentFixture<DossierStat>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DossierStat]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DossierStat);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
