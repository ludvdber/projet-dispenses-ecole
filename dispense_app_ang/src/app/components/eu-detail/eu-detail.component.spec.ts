import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EuDetailComponent } from './eu-detail.component';

describe('EuDetailComponent', () => {
  let component: EuDetailComponent;
  let fixture: ComponentFixture<EuDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EuDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EuDetailComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
