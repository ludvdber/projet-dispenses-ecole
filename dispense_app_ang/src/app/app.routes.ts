import {Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {UserProfileComponent} from './components/user-profile/user-profile.component';
import {canActivateAuthRole} from './guards/auth-role.guard';
import {SectionComposant} from './components/section/section.composant';
import {DossierCreate} from './components/dossier-create/dossier-create';
import {ForbiddenComponent} from './components/forbidden/forbidden.component';

export const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
  },
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'forbidden', component: ForbiddenComponent},
  {
    path: 'profile',
    component: UserProfileComponent,
    canActivate: [canActivateAuthRole],
    data: {role: 'view-profile'}
  },
  {
    path: "ues",
    component: SectionComposant,
    canActivate: [canActivateAuthRole],
    data: {role: 'ETUDIANT'}
  },
  {
    path: "createDossier",
    component: DossierCreate,
    canActivate: [canActivateAuthRole],
    data: {role: 'ETUDIANT'}
  }
];
