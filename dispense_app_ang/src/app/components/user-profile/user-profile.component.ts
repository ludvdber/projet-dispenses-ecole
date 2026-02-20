import { Component, OnInit, inject } from '@angular/core';

import Keycloak from 'keycloak-js';
import {User} from '../../model/user';

@Component({
  selector: 'app-user-profile',
  templateUrl: 'user-profile.component.html',
  styleUrls: [`user-profile.component.css`]
})
export class UserProfileComponent implements OnInit {
  private readonly keycloak = inject(Keycloak);

  user: User | undefined;

  async ngOnInit() {
    if (this.keycloak?.authenticated) {
      const profile = await this.keycloak.loadUserProfile();

      this.user = {
        prenom: `${profile?.firstName}`,
        nom: `${profile?.lastName}`,
        email: profile?.email,
        username: profile?.username
      };
    }
  }
}
