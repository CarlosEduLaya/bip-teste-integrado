import { Routes } from '@angular/router';
import { BeneficioListComponent } from './components/beneficio-list/beneficio-list.component';
import { BeneficioFormComponent } from './components/beneficio-form/beneficio-form.component';
import { TransferComponent } from './components/transfer/transfer.component';

export const routes: Routes = [
  { path: '', redirectTo: 'beneficios', pathMatch: 'full' },
  { path: 'beneficios', component: BeneficioListComponent },
  { path: 'beneficios/new', component: BeneficioFormComponent },
  { path: 'beneficios/:id/edit', component: BeneficioFormComponent },
  { path: 'transfer', component: TransferComponent },
  { path: '**', redirectTo: 'beneficios' }
];
