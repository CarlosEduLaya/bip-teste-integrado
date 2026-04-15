import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Beneficio, TransferRequest } from '../models/beneficio.model';

@Injectable({ providedIn: 'root' })
export class BeneficioService {

  private readonly base = '/api/v1/beneficios';

  constructor(private http: HttpClient) {}

  findAll(apenasAtivos = false): Observable<Beneficio[]> {
    const params = apenasAtivos ? new HttpParams().set('ativo', 'true') : new HttpParams();
    return this.http.get<Beneficio[]>(this.base, { params });
  }

  findById(id: number): Observable<Beneficio> {
    return this.http.get<Beneficio>(`${this.base}/${id}`);
  }

  create(b: Beneficio): Observable<Beneficio> {
    return this.http.post<Beneficio>(this.base, b);
  }

  update(id: number, b: Beneficio): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${this.base}/${id}`, b);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  transfer(req: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/transfer`, req);
  }
}
