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
    TIPO_PERSONA("Tipo de persona", "Contactos", "Si el contacto es una persona o una empresa.",
            List.of("Física", "Jurídica")),
    GENERO("Género", "Contactos", "Opciones del campo Género en la ficha del contacto.",
            List.of("Masculino", "Femenino")),
    TIPO_CLIENTE("Tipo de cliente", "Contactos", "Relación del contacto con el despacho.",
            List.of("Principal", "Vinculado", "Referido")),
    TIPO_DOCUMENTO_IDENTIDAD("Tipo de documento de identidad", "Contactos", "Documento con el que se identifica el contacto.",
            List.of("DNI", "NIE", "Pasaporte")),
    REGIMEN("Régimen de extranjería", "Contactos",
            "Régimen del permiso; determina con cuánta antelación se avisa de la renovación.",
            List.of("COMUNITARIO|Régimen Comunitario", "GENERAL|Régimen General", "INVERSORES|Régimen Inversores",
                    "PROTECCION_INTERNACIONAL|Protección Internacional", "OTROS|Otros")),
    NACIONALIDAD("Nacionalidad", "Contactos", "Países de nacionalidad de los contactos.", List.of()),
    ESTADO_CIVIL("Estado civil", "Contactos", "Opciones del campo Estado civil en la ficha del contacto.",
            List.of("Soltero/a", "Casado/a", "Pareja de hecho", "Separado/a", "Divorciado/a", "Viudo/a")),
    PARENTESCO("Parentesco del contacto vinculado", "Contactos",
            "Relación entre el cliente y sus familiares a efectos de extranjería.",
            List.of("Cónyuge", "Pareja de hecho registrada", "Pareja no registrada",
                    "Pareja sin vínculo con hijos en común", "Hijo/a menor de 18 años", "Hijo/a menor de 21 años",
                    "Hijo/a mayor de 21 años", "Ascendiente menor de 65 años", "Ascendiente mayor de 65 años",
                    "Otros familiares")),
    TIPO_DIRECCION("Tipo de dirección", "Contactos",
            "Tipos de dirección del contacto; por defecto se usa la de empadronamiento.",
            List.of("Empadronamiento", "Notificación", "Otra")),
    SUPUESTO_NACIONALIDAD("Supuesto de nacionalidad por residencia", "Contactos",
            "Supuesto aplicable; determina los años de residencia exigidos.",
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
    TIPO_EXPEDIENTE("Tipo de expediente", "Expedientes", "Área a la que pertenece cada expediente.",
            List.of("Extranjería", "Nacionalidad", "Civil", "Penal", "Mercantil", "Laboral", "Fiscal",
                    "Administrativo", "Financiero", "Jurídico", "Seguros", "Policía", "Inmobiliario", "Inversores",
                    "Escrituras", "Gestoría")),
    TIPO_TRAMITE("Tipo de trámite", "Expedientes",
            "Inicial, modificación, renovación…",
            List.of("Residencia inicial", "Modificación de residencia", "Renovación de residencia",
                    "Cancelación de antecedentes penales", "Cancelación de antecedentes policiales",
                    "Recurso de reposición", "Recurso contencioso-administrativo", "Consulta legal",
                    "Pareja de hecho")),
    TIPO_PROCEDIMIENTO("Tipo de procedimiento", "Expedientes",
            "Arraigo, reagrupación, no lucrativa… Cada uno tendrá su lista de documentos requeridos.",
            List.of("Arraigo social", "Arraigo sociolaboral", "Arraigo socioformativo", "Arraigo familiar",
                    "Arraigo de segunda oportunidad", "Residencia no lucrativa", "Reagrupación familiar",
                    "Residencia y trabajo por cuenta ajena", "Residencia y trabajo por cuenta propia",
                    "Tarjeta de familiar de ciudadano de la Unión", "Certificado de registro de ciudadano de la Unión",
                    "Residencia de larga duración", "Residencia de larga duración UE", "Estancia por estudios",
                    "Búsqueda de empleo tras estudios", "Circunstancias excepcionales (razones humanitarias)",
                    "Protección internacional", "Nacionalidad por residencia")),
    ROL_EXPEDIENTE("Rol en el expediente", "Expedientes", "Cómo actúa cada persona que interviene en el expediente.",
            List.of("Cliente principal", "Cónyuge", "Pareja de hecho registrada", "Pareja no registrada",
                    "Hijo/a menor de 21 años", "Hijo/a mayor de 21 años", "Ascendiente menor de 65 años",
                    "Ascendiente mayor de 65 años", "Otros familiares", "Colaborador", "Referido",
                    "Empresa contratante", "Asesoría empresa contratante", "Tercero")),
    TIPO_SEGUIMIENTO("Actuación de seguimiento", "Expedientes", "Actuaciones habituales que se registran en el seguimiento.",
            List.of("Apertura de expediente", "Solicitud documentación cliente",
                    "Solicitud documentación empresa contratante",
                    "Solicitud documentación asesoría empresa contratante", "Solicitud informe de vivienda",
                    "Solicitud informe de inserción social", "Solicitud cita huella", "Enviada resolución alta SS",
                    "Enviada matrícula AFO", "Enviado contrato AFO", "Abono tasas", "Enviada traducción",
                    "Enviada alta en Seguridad Social a Extranjería", "Abonada traducción")),
    TIPO_REQUERIMIENTO("Tipo de requerimiento", "Expedientes", "Qué pide la Administración en un requerimiento.",
            List.of("Tasas", "Documentación")),
    TIPO_RECURSO("Tipo de recurso", "Expedientes", "Recursos y escritos que se presentan contra una resolución.",
            List.of("Reposición", "Alzada", "Contencioso", "Escrito de alegaciones", "Otros escritos")),

    // ── Tareas ───────────────────────────────────────────────────────────────
    TIPO_TAREA("Tipo de tarea", "Tareas", "Tareas habituales del despacho.",
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
