import {Injectable, signal} from '@angular/core';
import {BooleanInput} from '@angular/cdk/coercion';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SidebarService {

  readonly toggleCmd = signal(0);

  toggleMenu(): void {
    this.toggleCmd.update(v => v + 1);
  }
}
