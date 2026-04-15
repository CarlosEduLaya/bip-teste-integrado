import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BeneficioService } from '../../services/beneficio.service';
import { Beneficio } from '../../models/beneficio.model';

@Component({
  selector: 'app-beneficio-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './beneficio-list.component.html'
})
export class BeneficioListComponent implements OnInit {

  beneficios: Beneficio[] = [];
  loading = false;
  error = '';
  successMsg = '';

  constructor(private service: BeneficioService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.service.findAll().subscribe({
      next: data => { this.beneficios = data; this.loading = false; },
      error: () => { this.error = 'Erro ao carregar benefícios.'; this.loading = false; }
    });
  }

  deactivate(b: Beneficio): void {
    if (!confirm(`Desativar "${b.nome}"?`)) return;
    this.service.deactivate(b.id!).subscribe({
      next: () => { this.successMsg = 'Benefício desativado.'; this.load(); },
      error: () => { this.error = 'Erro ao desativar.'; }
    });
  }
}
