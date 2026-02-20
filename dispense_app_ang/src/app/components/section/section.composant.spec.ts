import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionComposant } from './section.composant';

describe('SectionComposant', () => {
  let component: SectionComposant;
  let fixture: ComponentFixture<SectionComposant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SectionComposant]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SectionComposant);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
