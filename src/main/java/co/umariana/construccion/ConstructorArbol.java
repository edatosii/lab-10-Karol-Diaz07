package co.umariana.construccion;

import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ArbolInvalidoException;
import co.umariana.modelo.Persona;
import co.umariana.modelo.Posicion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Construye un árbol genealógico a partir de un texto: cada línea es una persona.
 *
 *     posicion;nombre;nacimiento;fallecimiento
 *
 * Ejemplo (el fallecimiento puede quedar vacío):
 *
 *     1;John Doe;2002;
 *     2;Henry Doe;1920;1990
 *     3;Maria Doe;1921;
 *     1.1;Timmy Doe;2021;
 *     1.2;Amy Doe;2026;
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 3)
 * ------------------------------------------
 * Reemplace el "throw new UnsupportedOperationException" por su implementación. Esta clase usa lo que ya
 * hizo en las etapas anteriores (Posicion, Persona y ArbolGenealogico). NO modifique las pruebas (src/test).
 *
 * REGLAS
 *   1. Si el texto es null o no tiene ninguna línea con contenido: ArbolInvalidoException (mensaje: "El texto está vacío...").
 *   2. Las líneas pueden terminar en \n o en \r\n. Las líneas en blanco se ignoran, pero los números de línea
 *      que se informan cuentan TODAS las líneas del texto (la primera es la línea 1).
 *   3. Cada línea tiene 4 campos separados por ';' (el cuarto, el fallecimiento, puede estar vacío). Se acepta
 *      también la línea con solo 3 campos y sin el último ';'. Los campos se recortan (trim).
 *      Si hay menos de 3 o más de 4 campos: error.
 *   4. El nacimiento y el fallecimiento deben ser números enteros (el fallecimiento vacío es "no tiene").
 *   5. Cada persona debe ser válida (Persona.validar) y no puede haber dos líneas con la misma posición.
 *   6. Debe existir la persona "1" (el sujeto), pero puede estar en cualquier línea.
 *   7. Las líneas pueden venir en CUALQUIER orden. El constructor debe agregarlas al árbol en un orden en el
 *      que cada persona ya tenga a la que necesita (por ejemplo, primero los ascendientes por número Ahnentafel
 *      ascendente y los descendientes por generación). Las reglas del árbol (ver ArbolGenealogico) las valida el árbol.
 *   8. Todos los errores asociados a una línea se informan con ArbolInvalidoException y un mensaje que EMPIEZA con
 *      "Línea N: " (N es el número de línea de la persona con el problema), por ejemplo:
 *          "Línea 3: la posición 2 está repetida."
 *      Los errores que no son de una línea (texto vacío, falta el sujeto) no llevan ese prefijo.
 *   9. Solo se muestran los miembros que están en el texto: no se crean personas vacías para los que faltan.
 */
public final class ConstructorArbol {

    private ConstructorArbol() {
    }
    private static final class Entrada {
        final int linea;
        final Persona persona;
 
        Entrada(int linea, Persona persona) {
            this.linea = linea;
            this.persona = persona;
        }
    }
    
    public static ArbolGenealogico construir(String texto) {
        // TODO Etapa 3
        // Regla 1
        if (texto == null || texto.isBlank()) {
            throw new ArbolInvalidoException("El texto está vacío: no hay ninguna persona.");
        }
 
        // Regla 2: \n o \r\n; el -1 conserva las líneas vacías para contar bien
        String[] lineas = texto.split("\r?\n", -1);
 
        List<Entrada> entradas = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
 
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            if (linea.isBlank()) {
                continue;
            }
            int n = i + 1;
 
            // Regla 3
            String[] c = linea.split(";", -1);
            if (c.length < 3 || c.length > 4) {
                throw new ArbolInvalidoException("Línea " + n + ": debe tener 3 o 4 campos separados por ';'.");
            }
            String pos = c[0].strip();
            String nombre = c[1].strip();
 
            // Regla 4
            int nacimiento;
            try {
                nacimiento = Integer.parseInt(c[2].strip());
            } catch (NumberFormatException e) {
                throw new ArbolInvalidoException("Línea " + n + ": el nacimiento no es un número entero.");
            }
            Integer fallecimiento = null;
            if (c.length == 4 && !c[3].isBlank()) {
                try {
                    fallecimiento = Integer.parseInt(c[3].strip());
                } catch (NumberFormatException e) {
                    throw new ArbolInvalidoException("Línea " + n + ": el fallecimiento no es un número entero.");
                }
            }
 
            // Regla 5
            Persona persona = new Persona(pos, nombre, nacimiento, fallecimiento);
            try {
                persona.validar();
            } catch (IllegalArgumentException e) {
                throw new ArbolInvalidoException("Línea " + n + ": " + e.getMessage());
            }
            if (!vistas.add(pos)) {
                throw new ArbolInvalidoException("Línea " + n + ": la posición " + pos + " está repetida.");
            }
            entradas.add(new Entrada(n, persona));
        }
 
        // Regla 6
        if (!vistas.contains(Posicion.SUJETO)) {
            throw new ArbolInvalidoException("Falta el sujeto (posición 1).");
        }
 
        // Regla 7: sujeto, ascendientes por número, descendientes por generación
        entradas.sort(Comparator
                .comparingInt((Entrada e) -> grupo(e.persona.getPosicion()))
                .thenComparingInt(e -> clave(e.persona.getPosicion()))
                .thenComparingInt(e -> e.linea));
 
        Entrada primera = entradas.get(0); // siempre es el sujeto
        ArbolGenealogico arbol = new ArbolGenealogico(primera.persona);
 
        for (int i = 1; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);
            try {
                arbol.agregar(e.persona);
            } catch (IllegalArgumentException ex) {
                throw new ArbolInvalidoException("Línea " + e.linea + ": " + ex.getMessage());
            }
        }
        return arbol;
    }
 
    /** 0 = sujeto, 1 = ascendientes, 2 = descendientes. */
    private static int grupo(String posicion) {
        if (Posicion.esSujeto(posicion)) {
            return 0;
        }
        return Posicion.esAscendiente(posicion) ? 1 : 2;
    }
 
    /** Ascendientes: el número Ahnentafel. Descendientes: la generación. */
    private static int clave(String posicion) {
        if (Posicion.esSujeto(posicion)) {
            return 0;
        }
        if (Posicion.esAscendiente(posicion)) {
            return Integer.parseInt(posicion);
        }
        return Posicion.generacion(posicion);
    }
}
