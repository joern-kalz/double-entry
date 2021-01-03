import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DialogMessage } from './dialog-message.enum';
import { DialogService } from './dialog.service';

import { DialogsComponent } from './dialogs.component';

describe('DialogsComponent', () => {
  let component: DialogsComponent;
  let fixture: ComponentFixture<DialogsComponent>;

  let dialogService: jasmine.SpyObj<DialogService>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DialogsComponent ],
      providers: [
        { provide: DialogService, useValue: 
          jasmine.createSpyObj('DialogService', ['hide'], {message: DialogMessage.CONNECTION_ERROR})}
      ]
    })
    .compileComponents();

    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DialogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show error', () => {
    const dialogDiv = fixture.nativeElement.querySelector('.dialog');
    expect(dialogDiv.textContent).toContain('Fehler');
  });

  it('should close', () => {
    const button = fixture.nativeElement.querySelector('button');
    button.click();
    expect(dialogService.hide).toHaveBeenCalled();
  });
});
