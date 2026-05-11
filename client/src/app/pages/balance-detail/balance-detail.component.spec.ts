import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BalanceDetailComponent } from './balance-detail.component';

describe('BalanceDetailComponent', () => {
  let component: BalanceDetailComponent;
  let fixture: ComponentFixture<BalanceDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BalanceDetailComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BalanceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
