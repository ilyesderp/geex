import { DatosDespachoPeticion } from '../api/generated/model/datosDespachoPeticion';

export type ComunidadAutonoma = DatosDespachoPeticion.ComunidadAutonomaEnum;

/** Comunidades y ciudades autónomas (determinan los festivos autonómicos del calendario). */
export const COMUNIDADES: { valor: ComunidadAutonoma; nombre: string }[] = [
  { valor: 'ANDALUCIA', nombre: 'Andalucía' },
  { valor: 'ARAGON', nombre: 'Aragón' },
  { valor: 'ASTURIAS', nombre: 'Principado de Asturias' },
  { valor: 'BALEARES', nombre: 'Illes Balears' },
  { valor: 'CANARIAS', nombre: 'Canarias' },
  { valor: 'CANTABRIA', nombre: 'Cantabria' },
  { valor: 'CASTILLA_LA_MANCHA', nombre: 'Castilla-La Mancha' },
  { valor: 'CASTILLA_Y_LEON', nombre: 'Castilla y León' },
  { valor: 'CATALUNA', nombre: 'Cataluña' },
  { valor: 'COMUNITAT_VALENCIANA', nombre: 'Comunitat Valenciana' },
  { valor: 'EXTREMADURA', nombre: 'Extremadura' },
  { valor: 'GALICIA', nombre: 'Galicia' },
  { valor: 'MADRID', nombre: 'Comunidad de Madrid' },
  { valor: 'MURCIA', nombre: 'Región de Murcia' },
  { valor: 'NAVARRA', nombre: 'Comunidad Foral de Navarra' },
  { valor: 'PAIS_VASCO', nombre: 'País Vasco' },
  { valor: 'LA_RIOJA', nombre: 'La Rioja' },
  { valor: 'CEUTA', nombre: 'Ceuta' },
  { valor: 'MELILLA', nombre: 'Melilla' },
];
