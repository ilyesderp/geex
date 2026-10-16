import { InvitacionPeticion } from '../api/generated/model/invitacionPeticion';

export type Rol = InvitacionPeticion.RolEnum;

/** Roles del personal del despacho (las etiquetas coinciden con el backend). */
export const ROLES: { valor: Rol; nombre: string }[] = [
  { valor: 'TITULAR', nombre: 'Titular / Administrador' },
  { valor: 'ABOGADO', nombre: 'Abogado / Revisor' },
  { valor: 'GESTOR', nombre: 'Gestor / Tramitador' },
  { valor: 'ADMINISTRATIVO', nombre: 'Administrativo / Recepción' },
  { valor: 'FACTURACION', nombre: 'Facturación' },
  { valor: 'COLABORADOR', nombre: 'Colaborador externo' },
];
