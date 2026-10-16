import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Mismos requisitos que el backend (PoliticaContrasenas): 12-128 caracteres y no trivial. */
export const LONGITUD_MINIMA_CONTRASENA = 12;

export const contrasenaSegura: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = (control.value as string) ?? '';
  if (!valor) {
    return null;
  }
  if (valor.length < LONGITUD_MINIMA_CONTRASENA) {
    return { contrasenaCorta: true };
  }
  if (new Set(valor).size < 4) {
    return { contrasenaSimple: true };
  }
  return null;
};

/** Valida que dos campos de un grupo coincidan (contraseña y repetición). */
export function coinciden(campo: string, repeticion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(campo)?.value;
    const b = grupo.get(repeticion)?.value;
    return a && b && a !== b ? { noCoinciden: true } : null;
  };
}
