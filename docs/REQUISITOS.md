# Trazabilidad de requisitos

Correspondencia entre el documento de requisitos original («Borrador programa
expedientes GEEX», 60 páginas) y las fases del plan de desarrollo. Las páginas
se refieren a ese documento.

**Estado:** ✅ hecho · 🟡 parcial (la base está, el resto llega en la fase indicada) · ⬜ pendiente

## Objetivos generales (págs. 1-2)

| Objetivo | Fase | Estado |
|---|---|---|
| Automatización de procesos y tareas | 2-3 | ⬜ |
| Gestión documental de los expedientes | 2 | ⬜ |
| Gestión eficaz de los plazos procesales | 0 (calendario) · 1-2 | 🟡 |
| Comunicación con el cliente (app, avisos, correos, chat, pagos) | 6-7 | ⬜ |
| Explotación comercial / venta cruzada (campañas) | 6 | ⬜ |
| Cumplimiento en protección de datos (LOPD/RGPD) | 0 (auditoría, aislamiento) · 1 · 6-7 | 🟡 |
| Cumplimiento del registro de jornada de los empleados | 8 | ⬜ |
| App propia para el cliente | 7-8 | ⬜ |

## Pantalla de inicio (pág. 3)

| Requisito | Fase | Estado |
|---|---|---|
| Los 24 apartados visibles «a golpe de vista» en la primera página | 0 | ✅ |
| El apartado en el que se trabaja siempre visible arriba (pág. 4) | 0 | ✅ |

## 1. Contactos (págs. 4-27)

| Requisito | Fase | Estado |
|---|---|---|
| Listado sin paginar, con buscador y filtros por cada columna; columnas configurables | 1 | ⬜ |
| Índice de 16 pestañas siempre visible a la izquierda de la ficha | 1 | ⬜ |
| Anverso y reverso de la tarjeta de residencia / pasaporte (jpg o pdf) | 1 | ⬜ |
| Datos genéricos: tipo de persona, código y fecha de alta automáticos, residente, género, tipo de cliente, nombre y dos apellidos, DOI, régimen, nacionalidad, nacimiento, padres, estado civil, procedencia, hijos | 1 | ⬜ (listas desplegables ✅ en fase 0) |
| Usuario de acceso del cliente creado al dar de alta | 7 | ⬜ |
| Medios de contacto múltiples con observaciones; dirección de empadronamiento por defecto; teléfono y correo obligatorios | 1 | ⬜ |
| Listados y exportación a Excel/PDF por nombre, DOI, tipo, teléfono, correo, nacionalidad, régimen, mes/año de nacionalidad y renovación, procedencia | 1 | ⬜ |
| Imprimir la carátula del cliente / expediente (plantilla Word) | 6 | ⬜ |
| Contactos vinculados con parentesco obligatorio (10 supuestos) | 1 | ⬜ (lista ✅) |
| Notas con fecha y usuario automáticos | 1 | ⬜ |
| Tareas del cliente | 3 | ⬜ |
| Avisos estandarizados al cliente (SMS / app) con constancia de lectura | 6 | ⬜ |
| Documentos con carpetas de clasificación; LOPD y consentimiento obligatorios | 2 | ⬜ |
| Expedientes del cliente e inicio de expediente con sus datos precargados | 2 | ⬜ |
| Facturación desde el contacto (provisión de fondos, suplidos, honorarios) | 5 | ⬜ |
| Residencia: fechas, aviso de renovación 90 / 60 días según régimen, 90 días tras la caducidad, histórico | 1 | ⬜ |
| Nacionalidad: supuesto (10/5/2/1 años), fecha calculada, segundo apellido | 1 | ⬜ (lista de supuestos ✅) |
| Correo bilingüe (español + idioma del cliente) | 6 | ⬜ |
| Chat privado (no WhatsApp) registrado | 7 | ⬜ |
| Difusión masiva | 6 | ⬜ |
| Firma remota de documentos | 6 | ⬜ |
| Plantilla LOPD firmada en la ficha | 6 | ⬜ |

## 2. Expedientes (págs. 28-43)

| Requisito | Fase | Estado |
|---|---|---|
| Listado con las columnas y filtros indicados; botón de alta arriba a la derecha | 2 | ⬜ |
| Editar, cerrar o borrar; borrar no afecta a la ficha del cliente | 2 | ⬜ |
| Índice izquierdo: General, Estado, Documentos, Tareas, Seguimiento, Facturación, Resolución | 2 | ⬜ |
| General: referencia automática, nº de Extranjería, tipo de expediente, trámite, procedimiento, descripción, rol | 2 | ⬜ (listas ✅) |
| Estado: 14 estados; requerido → Requerimientos; resuelto → estadística de éxito | 2 | ⬜ |
| No se puede cerrar sin hoja de encargo, mandato y LOPD firmados | 2 | ⬜ |
| Check list por trámite; sin los documentos obligatorios no se puede presentar | 2 | ⬜ |
| Fecha de caducidad de cada documento y avisos | 2 | ⬜ |
| Carpetas: check list, comunicaciones, correos, notificaciones (requerimientos, resoluciones), mandato, encargo, LOPD, suplidos, otros | 2 | ⬜ |
| Reglas automáticas de tareas entre usuarios | 3 | ⬜ |
| Seguimiento: fechas clave, 10 días hábiles del requerimiento, 30 días naturales del recurso, actuaciones predefinidas | 2 | ⬜ (calendario laboral ✅, actuaciones ✅) |
| Facturación del expediente con gráfico de cobro | 5 | ⬜ |

## 3-25. Apartados generales (págs. 43-60)

| Apartado | Fase | Estado |
|---|---|---|
| 3. Documentos: listado cronológico con caducidad | 2 | ⬜ |
| 4. Tareas: listado por usuario y estado; tareas básicas | 3 | ⬜ (tipos de tarea ✅) |
| 5. Seguimientos | 2 | ⬜ |
| 6. Requerimientos: tipo, notificación, días hábiles o naturales, fecha límite, avisos | 2 | ⬜ |
| 7. Recursos: tipo, organismo, fechas, plazo, resultado | 2 | ⬜ |
| 8. Renovaciones: listado por mes/año y avisos masivos | 1 · 6 | ⬜ |
| 9. Nacionalidades: listado y aviso automático | 1 · 6 | ⬜ |
| 10. Agenda común, sincronizada con Outlook/Google; citas online | 3 · 7 · 8 | ⬜ |
| 11. Facturación: honorarios, provisiones, suplidos, gastos, abonos; cobrado / parcial / pendiente; exportación | 5 | ⬜ |
| 12. Check list de trámites (también visible en la app del cliente) | 2 · 7 | ⬜ |
| 13. Colaboradores: procedencia, expedientes aportados, comisiones y liquidación mensual | 5 | ⬜ |
| 14. Plantillas / escritos sin papel; campañas | 6 | ⬜ |
| 15. Honorarios predefinidos con descuentos | 5 | ⬜ |
| 16. Informes | 5 | ⬜ |
| 17. **Configuración**: totalmente parametrizable; datos del despacho; permisos de cada usuario | **0** | ✅ datos, usuarios, roles y permisos, listas desplegables, calendario, auditoría |
| 18. Enlaces a webs oficiales | 3 | ⬜ |
| 19. Avisos al cliente por reglas | 6 | ⬜ |
| 20. Mail desde el programa, eligiendo el destinatario | 6 | ⬜ |
| 21. Chat / WhatsApp | 7 · 8 | ⬜ |
| 22. Área de clientes: datos, expedientes, documentos compartidos, pagos, bloqueo por impago, consentimientos | 7 | ⬜ |
| 23. App | 8 | ⬜ |
| 24. @Firma | 6 | ⬜ |
| 25. **Plazos**: caducidad de tarjetas (fundamental), documentos, requerimientos, recursos, renovaciones, nacionalidad, envíos a Extranjería | 1-2 | 🟡 (calendario laboral ✅) |

## Fase 0: lo entregado

- Instalación inicial: crea el despacho y la cuenta del titular.
- Acceso: inicio de sesión, recuperación y restablecimiento de contraseña, activación de cuentas invitadas, cambio de contraseña, bloqueo temporal tras 5 intentos fallidos.
- Usuarios: invitación por correo, reenvío, cambio de rol, desactivación y reactivación; el despacho nunca se queda sin titular.
- Roles y permisos editables por despacho (6 roles; el titular siempre tiene todos).
- Listas desplegables parametrizables con los valores del documento (18 listas, 244 nacionalidades).
- Calendario laboral 2026: festivos nacionales, de las 19 comunidades y locales; Alicante precargado.
- Registro de auditoría inalterable.
- Multi-despacho desde el primer día, con aislamiento en la base de datos (Row Level Security).

## Decisiones de interpretación del documento

| Tema | Documento | Decisión | Motivo |
|---|---|---|---|
| Género y sexo | Aparecen los dos campos (págs. 8 y 9) | Un único campo «Género» (Masculino / Femenino) | Es el mismo dato repetido en el borrador |
| Tipo de trámite / procedimiento | La pág. 28 los intercambia respecto a las págs. 31 y 42 | **Trámite** = inicial, renovación, modificación…; **procedimiento** = arraigo, reagrupación… | Lo indican dos de las tres apariciones |
| Contraseña del cliente | Apellido + «1» (pág. 7) | Usuario = DOI; el cliente recibe un enlace para elegir su contraseña | Una contraseña predecible expondría datos personales sensibles |
| Edición de facturas | Número y fecha editables (pág. 42) | Solo en borradores e importaciones; las emitidas se corrigen con factura rectificativa | Normativa Verifactu |
| Avisos de requerimiento | «Cada 5 días antes de la fecha de notificación» (pág. 48) | Aviso cada 5 días hasta contestar + aviso final 2 días hábiles antes del vencimiento (configurable) | Intención: no perder el plazo |
| Plazo de los recursos | Siempre 30 días naturales (pág. 49) | Unidad configurable: días hábiles, naturales o **meses de fecha a fecha** | La reposición y la alzada son de 1 mes y el contencioso de 2 (Ley 39/2015, Ley 29/1998) |
| Reglas de extranjería | Plazos fijos (90/60 días…) | Reglas editables con valores iniciales del Reglamento vigente (RD 1155/2024) | El borrador es de 2024; el Reglamento cambió en 2025 |
| Registro de jornada | Solo citado como objetivo (pág. 1) | Fichaje con pausas, correcciones trazadas e informe mensual (fase 8) | Lo exige el art. 34.9 del Estatuto de los Trabajadores |
