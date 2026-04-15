import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { BeneficioService } from '../../services/beneficio.service';
import { Beneficio } from '../../models/beneficio.model';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxMaskDirective],
  templateUrl: './transfer.component.html'
})
export class TransferComponent implements OnInit {

  beneficios: Beneficio[] = [];
  fromId: number | null = null;
  toId: number | null = null;

  /** Valor exibido com máscara, ex: "1.500,00" */
  amountMascarado = '';
  /** Valor numérico real enviado à API */
  amountNumerico = 0;

  loading = false;
  submitting = false;
  error = '';
  success = '';

  constructor(private service: BeneficioService) {}

  ngOnInit(): void {
    this.carregarBeneficios();
  }

  carregarBeneficios(): void {
    this.loading = true;
    this.service.findAll(true).subscribe({
      next: data => { this.beneficios = data; this.loading = false; },
      error: () => { this.error = 'Erro ao carregar benefícios.'; this.loading = false; }
    });
  }

  onAmountChange(valor: string): void {
    this.amountNumerico = this.parseMoeda(valor);
  }

  submit(): void {
    if (!this.fromId || !this.toId || !this.amountNumerico) return;
    this.submitting = true;
    this.error = '';
    this.success = '';

    this.service.transfer({ fromId: this.fromId, toId: this.toId, amount: this.amountNumerico })
      .subscribe({
        next: () => {
          this.success = `Transferência de R$ ${this.formatarMoeda(this.amountNumerico)} realizada com sucesso!`;
          this.fromId = null;
          this.toId = null;
          this.amountMascarado = '';
          this.amountNumerico = 0;
          this.submitting = false;
          this.carregarBeneficios();
        },
        error: err => {
          this.error = err?.error?.message || 'Erro ao realizar transferência.';
          this.submitting = false;
        }
      });
  }

  private parseMoeda(valor: string): number {
    if (!valor) return 0;
    return parseFloat(valor.replace(/\./g, '').replace(',', '.')) || 0;
  }

  private formatarMoeda(valor: number): string {
    return valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
