package co.umariana.modelo;

import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * El árbol genealógico: un SUJETO (posición "1") con sus ASCENDIENTES (padres, abuelos... en Ahnentafel)
 * y sus DESCENDIENTES (hijos, nietos... en d'Aboville). Ver la clase Posicion.
 *
 * Son en realidad dos árboles unidos en la raíz:
 *
 *     ascendientes:  árbol BINARIO. Cada persona tiene como mucho un padre (2n) y una madre (2n + 1).
 *     descendientes: árbol GENERAL. Cada persona tiene una lista de hijos (1.1, 1.2, ...) ordenada por número.
 *
 * El árbol solo guarda a los miembros que EXISTEN: no hay "huecos" ni personas vacías. Puede faltar el padre
 * de alguien, o el hijo 1.1 aunque exista el 1.2.
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 2)
 * ------------------------------------------
 * 1. Implemente el árbol con sus PROPIOS NODOS: una clase interna "Nodo" con la Persona, el nodo del
 *    padre, el nodo de la madre y la lista de hijos. NO se permite usar Map ni Set (las pruebas lo
 *    revisan): para buscar una posición recorra el árbol siguiendo su camino
 *      - Ahnentafel: los bits de n después del primer 1 dicen el camino (0 = padre, 1 = madre).
 *        Ej.: 5 = 101 -> desde el sujeto: padre (0), luego madre (1).
 *      - D'Aboville: cada número de la posición dice a qué hijo bajar. Ej.: 1.2.3 -> hijo 2, luego su hijo 3.
 *    Se permite usar List/ArrayList para la lista de hijos.
 * 2. Todos los errores de las reglas se informan con {@link ArbolInvalidoException} (un mensaje claro).
 *    Los datos inválidos de una Persona los detecta Persona.validar() (IllegalArgumentException).
 * 3. NO modifique las pruebas (src/test).
 *
 * REGLAS DEL ÁRBOL
 *   - La posición "1" (el sujeto) existe siempre y no se puede eliminar.
 *   - Una posición no puede repetirse.
 *   - Un ascendiente n solo se puede agregar si ya existe su hijo n / 2, y debe haber NACIDO ANTES
 *     (año de nacimiento menor) que ese hijo.
 *   - Un descendiente solo se puede agregar si ya existe su progenitor, y debe haber NACIDO DESPUÉS
 *     (año de nacimiento mayor) que su progenitor.
 */
public class ArbolGenealogico {

    // TODO Etapa 2: declare la clase interna Nodo y los atributos necesarios (raíz, cantidad de miembros...).

    //Crea un árbol que solo tiene al sujeto.
        private static class Nodo {
        final Persona persona;
        Nodo padre;
        Nodo madre;
        final List<Nodo> hijos = new ArrayList<>();
 
        Nodo(Persona persona) {
            this.persona = persona;
        }
    }
 
    private final Nodo raiz;
    private int cantidad;
     
    public ArbolGenealogico(Persona sujeto) {
        // TODO Etapa 2
        sujeto.validar();
        if (!Posicion.esSujeto(sujeto.getPosicion())) {
            throw new ArbolInvalidoException("El sujeto debe estar en la posición 1.");
        }
        raiz = new Nodo(sujeto);
        cantidad = 1;
    }

    /** @return el sujeto (la persona de la posición "1") */
    public Persona sujeto() {
        // TODO Etapa 2
        return raiz.persona;
    }

    /** @return cuántos miembros tiene el árbol (el sujeto cuenta; un árbol nuevo tiene 1) */
    public int tamano() {
        // TODO Etapa 2
        return cantidad;
    }
        private Nodo buscarNodo(String posicion) {
        if (!Posicion.esValida(posicion)) {
            return null;
        }
        Nodo actual = raiz;
        if (Posicion.esAhnentafel(posicion)) {
            int n = Integer.parseInt(posicion);
            int altura = 31 - Integer.numberOfLeadingZeros(n);
            // los bits después del primer 1: 0 = padre, 1 = madre
            for (int i = altura - 1; i >= 0; i--) {
                actual = ((n >> i) & 1) == 0 ? actual.padre : actual.madre;
                if (actual == null) {
                    return null;
                }
            }
            return actual;
        }
        String[] partes = posicion.split("\\.");
        for (int i = 1; i < partes.length; i++) {
            int orden = Integer.parseInt(partes[i]);
            Nodo siguiente = null;
            for (Nodo h : actual.hijos) {
                if (Posicion.orden(h.persona.getPosicion()) == orden) {
                    siguiente = h;
                    break;
                }
            }
            if (siguiente == null) {
                return null;
            }
            actual = siguiente;
        }
        return actual;
    }

    /**
     * @return la persona de esa posición, o null si no existe en el árbol o si la posición no es válida
     */
    public Persona buscar(String posicion) {
        // TODO Etapa 2
        Nodo n = buscarNodo(posicion);
        return n == null ? null : n.persona;
    }

    /** @return true si hay alguien en esa posición (false también si la posición no es válida) */
    public boolean contiene(String posicion) {
        // TODO Etapa 2
        return buscarNodo(posicion) != null;
    }

    /**
     * Agrega una persona en la posición que ella misma trae (la usa el constructor del árbol a partir del texto).
     * El orden de llamada importa: primero debe existir la persona de la que depende (ver REGLAS DEL ÁRBOL).
     *
     * @return la misma persona
     * @throws IllegalArgumentException si la persona no es válida (Persona.validar)
     * @throws ArbolInvalidoException si la posición ya existe (o es "1"), si falta la persona de la que depende,
     *         o si los años de nacimiento no cumplen las reglas
     */
    public Persona agregar(Persona persona) {
        // TODO Etapa 2
        persona.validar();
        String pos = persona.getPosicion();
 
        if (Posicion.esSujeto(pos) || buscarNodo(pos) != null) {
            throw new ArbolInvalidoException("la posición " + pos + " está repetida.");
        }
 
        Nodo nuevo = new Nodo(persona);
 
        if (Posicion.esAscendiente(pos)) {
            String posHijo = Posicion.posicionDelHijoEnLinea(pos);
            Nodo hijo = buscarNodo(posHijo);
            if (hijo == null) {
                throw new ArbolInvalidoException("para agregar la posición " + pos
                        + " primero debe existir la posición " + posHijo + ".");
            }
            if (persona.getNacimiento() >= hijo.persona.getNacimiento()) {
                throw new ArbolInvalidoException("la posición " + pos + " debe haber nacido antes que la posición "
                        + posHijo + ".");
            }
            if (Integer.parseInt(pos) % 2 == 0) {
                hijo.padre = nuevo;
            } else {
                hijo.madre = nuevo;
            }
        } else {
            String posProg = Posicion.posicionDelProgenitor(pos);
            Nodo prog = buscarNodo(posProg);
            if (prog == null) {
                throw new ArbolInvalidoException("para agregar la posición " + pos
                        + " primero debe existir la posición " + posProg + ".");
            }
            if (persona.getNacimiento() <= prog.persona.getNacimiento()) {
                throw new ArbolInvalidoException("la posición " + pos + " debe haber nacido después que la posición "
                        + posProg + ".");
            }
            // insertar manteniendo la lista de hijos ordenada por número
            int orden = Posicion.orden(pos);
            int indice = 0;
            while (indice < prog.hijos.size()
                    && Posicion.orden(prog.hijos.get(indice).persona.getPosicion()) < orden) {
                indice++;
            }
            prog.hijos.add(indice, nuevo);
        }
        cantidad++;
        return persona;
    }

    /**
     * Agrega el PADRE de la persona de esa posición, en la posición 2n (ver Posicion.posicionDelPadre).
     * Solo se pueden agregar padres al sujeto y a sus ascendientes.
     *
     * @return la persona creada
     * @throws ArbolInvalidoException si la posición no existe, si no es el sujeto ni un ascendiente, si ese padre
     *         ya existe o si no cumple las reglas del árbol
     * @throws IllegalArgumentException si los datos de la persona no son válidos
     */
    public Persona agregarPadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        return agregarProgenitorAscendiente(posicion, nombre, nacimiento, fallecimiento, true);
    }

    /**
     * Igual que agregarPadre, pero agrega la MADRE (posición 2n + 1).
     */
    public Persona agregarMadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        return agregarProgenitorAscendiente(posicion, nombre, nacimiento, fallecimiento, false);
    }
    private Persona agregarProgenitorAscendiente(String posicion, String nombre, int nacimiento,
                                                 Integer fallecimiento, boolean esPadre) {
        if (buscarNodo(posicion) == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe.");
        }
        if (!Posicion.esSujeto(posicion) && !Posicion.esAscendiente(posicion)) {
            throw new ArbolInvalidoException("la posición " + posicion
                    + " no es el sujeto ni un ascendiente: no se le pueden agregar padres.");
        }
        String nueva = esPadre ? Posicion.posicionDelPadre(posicion) : Posicion.posicionDeLaMadre(posicion);
        if (!Posicion.esAhnentafel(nueva)) {
            throw new ArbolInvalidoException("la posición " + nueva + " pasa del límite de "
                    + Posicion.MAX_AHNENTAFEL + ".");
        }
        if (buscarNodo(nueva) != null) {
            throw new ArbolInvalidoException("la posición " + nueva + " ya existe.");
        }
        return agregar(new Persona(nueva, nombre, nacimiento, fallecimiento));
    }

    /**
     * Agrega un HIJO a la persona de esa posición. Solo se pueden agregar hijos al sujeto y a sus descendientes.
     * Su posición es la del progenitor + "." + el número siguiente al MAYOR número de hijo que ya exista
     * (o 1 si todavía no tiene hijos). Ej.: si "1" tiene los hijos 1.1 y 1.3, el nuevo es 1.4.
     *
     * @return la persona creada
     * @throws ArbolInvalidoException si la posición no existe, si no es el sujeto ni un descendiente, o si no cumple
     *         las reglas del árbol
     * @throws IllegalArgumentException si los datos de la persona no son válidos
     */
    public Persona agregarHijo(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        // TODO Etapa 2
        Nodo prog = buscarNodo(posicion);
        if (prog == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe.");
        }
        if (!Posicion.esSujeto(posicion) && !Posicion.esDescendiente(posicion)) {
            throw new ArbolInvalidoException("la posición " + posicion
                    + " no es el sujeto ni un descendiente: no se le pueden agregar hijos.");
        }
        int mayor = 0;
        for (Nodo h : prog.hijos) {
            mayor = Math.max(mayor, Posicion.orden(h.persona.getPosicion()));
        }
        int siguiente = mayor + 1;
        if (siguiente > Posicion.MAX_ORDEN) {
            throw new ArbolInvalidoException("la posición " + posicion + " ya no admite más hijos.");
        }
        String nueva = posicion + "." + siguiente;
        if (nueva.length() > Posicion.MAX_LONGITUD) {
            throw new ArbolInvalidoException("la posición " + nueva + " pasa de " + Posicion.MAX_LONGITUD
                    + " caracteres.");
        }
        return agregar(new Persona(nueva, nombre, nacimiento, fallecimiento));
    }

    /**
     * Elimina a la persona de esa posición y TODO lo que depende de ella:
     *   - si es un ascendiente: ella y todos sus propios ascendientes (sus padres, abuelos...);
     *   - si es un descendiente: ella y todos sus propios descendientes (hijos, nietos...).
     * Las demás posiciones NO cambian de número.
     *
     * @return cuántas personas se eliminaron (ella incluida)
     * @throws ArbolInvalidoException si la posición es "1" (el sujeto no se puede eliminar) o si no existe
     */
    public int eliminar(String posicion) {
        // TODO Etapa 2
        if (Posicion.esSujeto(posicion)) {
            throw new ArbolInvalidoException("el sujeto (posición 1) no se puede eliminar.");
        }
        Nodo nodo = buscarNodo(posicion);
        if (nodo == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe.");
        }
        if (Posicion.esAscendiente(posicion)) {
            Nodo hijo = buscarNodo(Posicion.posicionDelHijoEnLinea(posicion));
            if (Integer.parseInt(posicion) % 2 == 0) {
                hijo.padre = null;
            } else {
                hijo.madre = null;
            }
        } else {
            Nodo prog = buscarNodo(Posicion.posicionDelProgenitor(posicion));
            prog.hijos.remove(nodo);
        }
        int eliminados = contar(nodo);
        cantidad -= eliminados;
        return eliminados;
    }
 
    /** Cuenta el nodo y todo lo que depende de él (sus ascendientes o sus descendientes). */
    private int contar(Nodo n) {
        if (n == null) {
            return 0;
        }
        int total = 1 + contar(n.padre) + contar(n.madre);
        for (Nodo h : n.hijos) {
            total += contar(h);
        }
        return total;
    }

    /**
     * @return todos los miembros en este orden: el sujeto; luego los ascendientes por número Ahnentafel
     *         ascendente (2, 3, 4, 5...); luego los descendientes en PREORDEN (cada persona y después sus hijos:
     *         1.1, 1.1.1, 1.1.2, 1.2, ...). La lista es una copia nueva: modificarla no cambia el árbol.
     *         Este orden garantiza que cada persona aparece DESPUÉS de la que necesita (útil para guardar y cargar).
     */
    public List<Persona> miembros() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();
        lista.add(raiz.persona);
        lista.addAll(ascendientes());
        lista.addAll(descendientes());
        return lista;
    }

    /** @return solo los ascendientes, por número Ahnentafel ascendente (sin el sujeto) */
    public List<Persona> ascendientes() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();
        Queue<Nodo> cola = new ArrayDeque<>();
        if (raiz.padre != null) {
            cola.add(raiz.padre);
        }
        if (raiz.madre != null) {
            cola.add(raiz.madre);
        }
        while (!cola.isEmpty()) {
            Nodo n = cola.poll();
            lista.add(n.persona);
            if (n.padre != null) {
                cola.add(n.padre);
            }
            if (n.madre != null) {
                cola.add(n.madre);
            }
        }
        return lista;
    }

    /** @return solo los descendientes, en preorden (sin el sujeto) */
    public List<Persona> descendientes() {
        // TODO Etapa 2
        List<Persona> lista = new ArrayList<>();
        for (Nodo h : raiz.hijos) {
            preorden(h, lista);
        }
        return lista;
    }
 
    private void preorden(Nodo n, List<Persona> lista) {
        lista.add(n.persona);
        for (Nodo h : n.hijos) {
            preorden(h, lista);
        }
    }
}
