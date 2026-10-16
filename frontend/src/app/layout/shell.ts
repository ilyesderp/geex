import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { moduloPorClave } from '../core/modulos';
import { SesionService } from '../core/sesion/sesion.service';
import { Logo } from './logo';

interface ModuloActual {
  titulo: string;
  icono: string;
}

/**
 * Estructura común de la aplicación. La barra superior muestra siempre el
 * apartado en el que se está trabajando ("SIEMPRE ARRIBA DEL TODO QUE SE VEA EN
 * EL CAMPO EN EL QUE SE ESTÁ TRABAJANDO", pág. 4 del documento).
 */
@Component({
  selector: 'geex-shell',
  imports: [RouterOutlet, RouterLink, MatMenuModule, MatIconModule, Logo],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  protected readonly modulo = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => this.moduloDe(this.router.routerState.snapshot.root)),
    ),
    { initialValue: { titulo: 'Inicio', icono: 'home' } },
  );

  protected readonly iniciales = computed(() => {
    const p = this.sesion.perfil();
    return p ? (p.nombre.charAt(0) + p.apellidos.charAt(0)).toUpperCase() : '';
  });

  protected readonly otrosDespachos = computed(() => {
    const p = this.sesion.perfil();
    return p ? p.despachos.filter((d) => d.id !== p.despachoActual.id) : [];
  });

  private moduloDe(raiz: ActivatedRouteSnapshot): ModuloActual {
    let actual: ModuloActual = { titulo: 'Inicio', icono: 'home' };
    let nodo: ActivatedRouteSnapshot | null = raiz;
    while (nodo) {
      const clave = nodo.paramMap.get('clave');
      const modulo = clave ? moduloPorClave(clave) : undefined;
      if (modulo) {
        actual = { titulo: modulo.nombre, icono: modulo.icono };
      } else if (nodo.data['titulo']) {
        actual = { titulo: nodo.data['titulo'] as string, icono: (nodo.data['icono'] as string) ?? 'circle' };
      }
      nodo = nodo.firstChild;
    }
    return actual;
  }
}
