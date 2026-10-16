package es.geex.configuracion.domain;

import es.geex.configuracion.domain.TipoCatalogo.ValorInicial;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lista de nacionalidades a partir de los países ISO 3166 con su nombre en
 * español (los mismos nombres que usan los formularios oficiales EX). El código
 * es el ISO alfa-3, que es el que usan los modelos oficiales.
 */
final class Nacionalidades {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-ES");

    /** Territorios sin población permanente. */
    private static final Set<String> EXCLUIDOS = Set.of("AQ", "BV", "HM", "UM", "TF", "GS");

    private Nacionalidades() {
    }

    static List<ValorInicial> todas() {
        Collator orden = Collator.getInstance(ESPANOL);
        List<ValorInicial> lista = new ArrayList<>(Arrays.stream(Locale.getISOCountries())
                .filter(codigo -> !EXCLUIDOS.contains(codigo))
                .map(codigo -> new Locale.Builder().setRegion(codigo).build())
                .map(pais -> new ValorInicial(pais.getISO3Country(), pais.getDisplayCountry(ESPANOL)))
                .sorted((a, b) -> orden.compare(a.etiqueta(), b.etiqueta()))
                .toList());
        lista.add(new ValorInicial("APATRIDA", "Apátrida"));
        return lista;
    }
}
