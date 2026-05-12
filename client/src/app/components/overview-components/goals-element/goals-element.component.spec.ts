import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalsElementComponent } from './goals-element.component';

describe('GoalsElementComponent', () => {
  let component: GoalsElementComponent;
  let fixture: ComponentFixture<GoalsElementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GoalsElementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GoalsElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
