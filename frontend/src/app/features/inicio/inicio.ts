import { Component, computed, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { FASE_ACTUAL, MODULOS } from '../../core/modulos';
import { SesionService } from '../../core/sesion/sesion.service';

/**
 * Pantalla de inicio: los 24 apartados "a golpe de vista" (pág. 3 del documento).
 * Los que aún no están construidos indican en qué fase llegan.
 */
@Component({
  selector: 'geex-inicio',
  imports: [RouterLink, MatIconModule],
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio {
  protected readonly sesion = inject(SesionService);
  protected readonly modulos = MODULOS;
  protected readonly faseActual = FASE_ACTUAL;
  protected readonly disponibles = MODULOS.filter((m) => m.ruta).length;

  protected readonly saludo = computed(() => {
    const hora = new Date().getHours();
    const momento = hora < 14 ? 'Buenos días' : hora < 21 ? 'Buenas tardes' : 'Buenas noches';
    return `${momento}, ${this.sesion.perfil()?.nombre ?? ''}`;
  });

  protected readonly hoy = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date());
}
