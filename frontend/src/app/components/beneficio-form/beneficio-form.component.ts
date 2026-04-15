import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { BeneficioService } from '../../services/beneficio.service';
import { Beneficio } from '../../models/beneficio.model';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxMaskDirective],
  templateUrl: './beneficio-form.component.html'
})
export class BeneficioFormComponent implements OnInit {

  beneficio: Beneficio = { nome: '', descricao: '', valor: 0, ativo: true };
  isEdit = false;
  loading = false;
  saving = false;
  error = '';

  /** Valor exibido no input com máscara (string), ex: "1.500,00" */
  valorMascarado = '';

  constructor(
    private service: BeneficioService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.loading = true;
      this.service.findById(+id).subscribe({
        next: b => {
          this.beneficio = b;
          // Formata o valor numérico para o campo mascarado (ex: 1500 → "1500,00")
          this.valorMascarado = b.valor.toFixed(2).replace('.', ',');
          this.loading = false;
        },
        error: () => { this.error = 'Benefício não encontrado.'; this.loading = false; }
      });
    }
  }

  onValorChange(valor: string): void {
    // Remove separadores de milhar e converte vírgula decimal para ponto
    this.beneficio.valor = this.parseMoeda(valor);
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const op = this.isEdit
      ? this.service.update(this.beneficio.id!, this.beneficio)
      : this.service.create(this.beneficio);

    op.subscribe({
      next: () => this.router.navigate(['/beneficios']),
      error: err => {
        this.error = err?.error?.message || 'Erro ao salvar.';
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/beneficios']);
  }

  private parseMoeda(valor: string): number {
    if (!valor) return 0;
    // "1.500,75" → 1500.75
    return parseFloat(valor.replace(/\./g, '').replace(',', '.')) || 0;
  }
}
