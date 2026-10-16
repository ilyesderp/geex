import { FormControl, FormGroup } from '@angular/forms';
import { coinciden, contrasenaSegura } from './validadores';

describe('validadores', () => {
  it('exige contraseñas de al menos 12 caracteres, como el servidor', () => {
    expect(contrasenaSegura(new FormControl('corta-123'))).toEqual({ contrasenaCorta: true });
    expect(contrasenaSegura(new FormControl('una frase larga y segura'))).toBeNull();
  });

  it('rechaza contraseñas con muy pocos caracteres distintos', () => {
    expect(contrasenaSegura(new FormControl('aaaaaaaaaaaaaaaa'))).toEqual({ contrasenaSimple: true });
  });

  it('comprueba que la repetición coincide', () => {
    const grupo = new FormGroup(
      { contrasena: new FormControl('una frase larga'), repetir: new FormControl('otra frase larga') },
      { validators: coinciden('contrasena', 'repetir') },
    );
    expect(grupo.errors).toEqual({ noCoinciden: true });

    grupo.controls.repetir.setValue('una frase larga');
    expect(grupo.errors).toBeNull();
  });
});
