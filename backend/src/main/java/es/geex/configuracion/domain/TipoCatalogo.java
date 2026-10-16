package es.geex.configuracion.domain;

import java.util.List;

/**
 * Listas desplegables de GEEX y sus valores iniciales, tomados del documento de
 * requisitos (borrador del programa). Cada despacho recibe una copia que puede
 * modificar; los valores iniciales no se pueden borrar (otras partes del programa
 * los usan por su código), solo renombrar, reordenar o desactivar.
 */
public enum TipoCatalogo {

    // ── Contactos ────────────────────────────────────────────────────────────
    TIPO_PERSONA("Tipo de persona", "Contactos", "Ficha del contacto (pág. 7).",
            List.of("Física", "Jurídica")),
    GENERO("Género", "Contactos", "Ficha del contacto (pág. 8).",
            List.of("Masculino", "Femenino")),
    TIPO_CLIENTE("Tipo de cliente", "Contactos", "Tipología de cliente (pág. 8).",
            List.of("Principal", "Vinculado", "Referido")),
    TIPO_DOCUMENTO_IDENTIDAD("Tipo de documento de identidad", "Contactos", "DOI del contacto (pág. 8).",
            List.of("DNI", "NIE", "Pasaporte")),
    REGIMEN("Régimen de extranjería", "Contactos",
            "Determina el plazo de aviso de renovación (págs. 8-9, 21-22).",
            List.of("COMUNITARIO|Régimen Comunitario", "GENERAL|Régimen General", "INVERSORES|Régimen Inversores",
                    "PROTECCION_INTERNACIONAL|Protección Internacional", "OTROS|Otros")),
    NACIONALIDAD("Nacionalidad", "Contactos", "Todas las nacionalidades (pág. 9).", List.of()),
    ESTADO_CIVIL("Estado civil", "Contactos", "Ficha del contacto (pág. 9).",
            List.of("Soltero/a", "Casado/a", "Pareja de hecho", "Separado/a", "Divorciado/a", "Viudo/a")),
    PARENTESCO("Parentesco del contacto vinculado", "Contactos",
            "Supuestos de parentesco a efectos de extranjería (pág. 12).",
            List.of("Cónyuge", "Pareja de hecho registrada", "Pareja no registrada",
                    "Pareja sin vínculo con hijos en común", "Hijo/a menor de 18 años", "Hijo/a menor de 21 años",
                    "Hijo/a mayor de 21 años", "Ascendiente menor de 65 años", "Ascendiente mayor de 65 años",
                    "Otros familiares")),
    TIPO_DIRECCION("Tipo de dirección", "Contactos",
            "Por defecto se usa la de empadronamiento (pág. 10).",
            List.of("Empadronamiento", "Notificación", "Otra")),
    SUPUESTO_NACIONALIDAD("Supuesto de nacionalidad por residencia", "Contactos",
            "Determina los años de residencia exigidos (págs. 24-25).",
            List.of("GENERAL_10|Caso general (10 años)",
                    "REFUGIADO_5|Refugiado o protección internacional (5 años)",
                    "IBEROAMERICA_2|Iberoamérica, Andorra, Filipinas, Guinea Ecuatorial, Portugal o sefardí (2 años)",
                    "NACIDO_ESPANA_1|Nacido en territorio español (1 año)",
                    "OPCION_NO_EJERCIDA_1|No ejerció su derecho de opción (1 año)",
                    "TUTELA_1|Tutela, guarda o acogimiento de español o institución española (1 año)",
                    "MATRIMONIO_1|Casado/a con español/a (1 año)",
                    "VIUDEDAD_1|Viudo/a de español/a (1 año)",
                    "ASCENDENCIA_ESPANOLA_1|Nacido fuera de España de padre, madre o abuelos originariamente españoles (1 año)")),

    // ── Expedientes ──────────────────────────────────────────────────────────
    TIPO_EXPEDIENTE("Tipo de expediente", "Expedientes", "Pág. 31.",
            List.of("Extranjería", "Nacionalidad", "Civil", "Penal", "Mercantil", "Laboral", "Fiscal",
                    "Administrativo", "Financiero", "Jurídico", "Seguros", "Policía", "Inmobiliario", "Inversores",
                    "Escrituras", "Gestoría")),
    TIPO_TRAMITE("Tipo de trámite", "Expedientes",
            "Inicial, modificación, renovación... (págs. 31 y 42).",
            List.of("Residencia inicial", "Modificación de residencia", "Renovación de residencia",
                    "Cancelación de antecedentes penales", "Cancelación de antecedentes policiales",
                    "Recurso de reposición", "Recurso contencioso-administrativo", "Consulta legal",
                    "Pareja de hecho")),
    TIPO_PROCEDIMIENTO("Tipo de procedimiento", "Expedientes",
            "Arraigo, reagrupación, no lucrativa... (págs. 32 y 42-43). Cada uno tendrá su check list.",
            List.of("Arraigo social", "Arraigo sociolaboral", "Arraigo socioformativo", "Arraigo familiar",
                    "Arraigo de segunda oportunidad", "Residencia no lucrativa", "Reagrupación familiar",
                    "Residencia y trabajo por cuenta ajena", "Residencia y trabajo por cuenta propia",
                    "Tarjeta de familiar de ciudadano de la Unión", "Certificado de registro de ciudadano de la Unión",
                    "Residencia de larga duración", "Residencia de larga duración UE", "Estancia por estudios",
                    "Búsqueda de empleo tras estudios", "Circunstancias excepcionales (razones humanitarias)",
                    "Protección internacional", "Nacionalidad por residencia")),
    ROL_EXPEDIENTE("Rol en el expediente", "Expedientes", "Cómo actúa cada interviniente (págs. 32-33).",
            List.of("Cliente principal", "Cónyuge", "Pareja de hecho registrada", "Pareja no registrada",
                    "Hijo/a menor de 21 años", "Hijo/a mayor de 21 años", "Ascendiente menor de 65 años",
                    "Ascendiente mayor de 65 años", "Otros familiares", "Colaborador", "Referido",
                    "Empresa contratante", "Asesoría empresa contratante", "Tercero")),
    TIPO_SEGUIMIENTO("Actuación de seguimiento", "Expedientes", "Actuaciones predefinidas (pág. 41).",
            List.of("Apertura de expediente", "Solicitud documentación cliente",
                    "Solicitud documentación empresa contratante",
                    "Solicitud documentación asesoría empresa contratante", "Solicitud informe de vivienda",
                    "Solicitud informe de inserción social", "Solicitud cita huella", "Enviada resolución alta SS",
                    "Enviada matrícula AFO", "Enviado contrato AFO", "Abono tasas", "Enviada traducción",
                    "Enviada alta en Seguridad Social a Extranjería", "Abonada traducción")),
    TIPO_REQUERIMIENTO("Tipo de requerimiento", "Expedientes", "Pág. 47.",
            List.of("Tasas", "Documentación")),
    TIPO_RECURSO("Tipo de recurso", "Expedientes", "Pág. 49.",
            List.of("Reposición", "Alzada", "Contencioso", "Escrito de alegaciones", "Otros escritos")),

    // ── Tareas ───────────────────────────────────────────────────────────────
    TIPO_TAREA("Tipo de tarea", "Tareas", "Listado de tareas básicas (págs. 44-45).",
            List.of("Alta cliente", "Llamada contacto cliente", "Solicitud documentación pendiente",
                    "Solicitud documentación adicional", "Solicitud traducción jurada", "Petición informe vivienda",
                    "Petición informe de arraigo", "Preparación de expediente", "Presentación de expediente",
                    "Información cliente presentación expediente", "Información cliente estado de expediente",
                    "Solicitud ampliación de plazo", "Escrito aportación documentación",
                    "Escrito aclaración expediente", "Escrito contestación requerimiento", "Pago de tasas",
                    "Aportación de tasas", "Envío mail / SMS / nota", "Preparación recurso / alegaciones / escritos",
                    "Presentación recurso / alegaciones / escritos", "Gestión interna", "Gestión consulados",
                    "Facturación expediente", "Cierre expediente", "Otras"));

    private final String nombre;
    private final String grupo;
    private final String descripcion;
    private final List<String> valoresIniciales;

    TipoCatalogo(String nombre, String grupo, String descripcion, List<String> valoresIniciales) {
        this.nombre = nombre;
        this.grupo = grupo;
        this.descripcion = descripcion;
        this.valoresIniciales = valoresIniciales;
    }

    public String nombre() {
        return nombre;
    }

    public String grupo() {
        return grupo;
    }

    public String descripcion() {
        return descripcion;
    }

    /** Valores iniciales como pares (código, etiqueta). */
    public List<ValorInicial> valoresIniciales() {
        if (this == NACIONALIDAD) {
            return Nacionalidades.todas();
        }
        return valoresIniciales.stream().map(TipoCatalogo::aValorInicial).toList();
    }

    /** "CODIGO|Etiqueta" fija el código explícitamente (valores que usarán las reglas); si no, se deriva de la etiqueta. */
    private static ValorInicial aValorInicial(String definicion) {
        int separador = definicion.indexOf('|');
        return separador < 0
                ? new ValorInicial(CodigoCatalogo.desdeEtiqueta(definicion), definicion)
                : new ValorInicial(definicion.substring(0, separador), definicion.substring(separador + 1));
    }

    public record ValorInicial(String codigo, String etiqueta) {
    }
}
