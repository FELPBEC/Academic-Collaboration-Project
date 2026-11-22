package co.edu.uptc.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import co.edu.uptc.Enum.InteresArea;
import co.edu.uptc.Enum.Program;
import co.edu.uptc.Enum.RelationType;
import co.edu.uptc.Enum.TypeUser;
import co.edu.uptc.Exceptions.UsuarioNoEncontradoException;
import co.edu.uptc.Model.Arista;
import co.edu.uptc.Model.Grafo;
import co.edu.uptc.Model.Nodo;
import co.edu.uptc.Model.User;

/**
 * Servicio encargado de gestionar las consultas de colaboración académica (RF05)
 * y el filtrado multicriterio sobre la red de usuarios (RF09).
 */
public class ServicioColaboracion {
    private Grafo<String> grafo;
    private Map<String, User> usuarios;

    /**
     * Construye el servicio de colaboración a partir de las estructuras en memoria.
     *
     * @param grafo    Grafo no dirigido que representa las conexiones entre usuarios.
     * @param usuarios Mapa indexado por correo electrónico con los perfiles de los usuarios.
     */
    public ServicioColaboracion(Grafo<String> grafo, Map<String, User> usuarios) {
        this.grafo = grafo;
        this.usuarios = usuarios;
    }

    /**
     * Clase auxiliar que asocia el correo de un usuario con su distancia acumulada
     * para ordenar los turnos de exploración dentro de la cola de prioridad de Dijkstra.
     */
    static class NodoDistancia implements Comparable<NodoDistancia> {
        String correo;
        double distancia;

        /**
         * Crea un registro de nodo con su distancia acumulada.
         *
         * @param correo    Correo electrónico del usuario.
         * @param distancia Costo acumulado desde el nodo origen.
         */
        public NodoDistancia(String correo, double distancia) {
            this.correo = correo;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(NodoDistancia nodo) {
            int comparacionDistancia = Double.compare(this.distancia, nodo.distancia);
            if (comparacionDistancia != 0) {
                return comparacionDistancia;
            }
            return this.correo.compareTo(nodo.correo);
        }

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public double getDistancia() {
            return distancia;
        }

        public void setDistancia(double distancia) {
            this.distancia = distancia;
        }
    }
    /**
 * RF04: Consulta los participantes conectados directamente a un usuario.
 *
 * @param emailUsuario Correo del participante a consultar.
 * @return Lista de objetos {@link User} con relación directa (sin duplicados).
 * @throws UsuarioNoEncontradoException Si el correo no está registrado.
 */
public List<User> obtenerRelacionesDirectas(String emailUsuario) throws UsuarioNoEncontradoException {
    if (!usuarios.containsKey(emailUsuario)) {
        throw new UsuarioNoEncontradoException("El usuario con email " + emailUsuario + " no existe.");
    }

    Nodo<String> nodoOrigen = new Nodo<>(emailUsuario);
    List<Arista<String>> aristasIncidentes = grafo.obtenerAristas(nodoOrigen);

    // Usamos LinkedHashSet para evitar usuarios repetidos si tienen más de una arista entre sí
    Set<String> correosVecinos = new LinkedHashSet<>();
    for (Arista<String> arista : aristasIncidentes) {
        Nodo<String> nodoVecino = arista.getExtremoOpuesto(nodoOrigen);
        if (nodoVecino != null) {
            correosVecinos.add(nodoVecino.getContenido()); // Ajusta si tu getter en Nodo se llama getValor()
        }
    }

    List<User> colaboradoresDirectos = new ArrayList<>();
    for (String correo : correosVecinos) {
        User vecino = usuarios.get(correo);
        if (vecino != null) {
            colaboradoresDirectos.add(vecino);
        }
    }

    return colaboradoresDirectos;
}

public List<User> obtenerColaboradoresPorNivelInteraccion(String emailUsuario) throws UsuarioNoEncontradoException {
    if (!usuarios.containsKey(emailUsuario)) {
        throw new UsuarioNoEncontradoException("El usuario con email " + emailUsuario + " no existe.");
    }

    Nodo<String> nodoOrigen = new Nodo<>(emailUsuario);
    List<Arista<String>> aristasIncidentes = grafo.obtenerAristas(nodoOrigen);

    // Guardamos la menor distancia (relación más cercana) por cada correo vecino
    Map<String, Double> menorDistanciaPorVecino = new HashMap<>();
    for (Arista<String> arista : aristasIncidentes) {
        Nodo<String> vecino = arista.getExtremoOpuesto(nodoOrigen);
        if (vecino != null) {
            String correoVecino = vecino.getContenido();
            double peso = arista.getPeso();
            menorDistanciaPorVecino.merge(correoVecino, peso, Math::min);
        }
    }

    // Reutilizamos tu clase NodoDistancia y su compareTo()
    List<NodoDistancia> ranking = new ArrayList<>();
    for (Map.Entry<String, Double> entry : menorDistanciaPorVecino.entrySet()) {
        ranking.add(new NodoDistancia(entry.getKey(), entry.getValue()));
    }
    Collections.sort(ranking);

    // Convertimos el ranking ordenado a List<User>
    List<User> colaboradoresOrdenados = new ArrayList<>();
    for (NodoDistancia nd : ranking) {
        User vecino = usuarios.get(nd.getCorreo());
        if (vecino != null) {
            colaboradoresOrdenados.add(vecino);
        }
    }

    return colaboradoresOrdenados;
}

/**
 * Filtra los participantes que tienen relaciones relevantes y los ordena por actividad.
 * <p>
 * Se considera relevante una puntuación acumulada de al menos 2 puntos:
 * colaboración de investigación vale 3, mentoría vale 2 y participación conjunta
 * vale 1. La puntuación solo admite filtros mínimos y no reemplaza los filtros
 * por perfil gestionados por {@link ServicioUsuarios}.
 * </p>
 *
 * @param correoUsuarioActual correo del usuario que realiza la búsqueda
 * @return participantes relevantes excluyendo al usuario actual, ordenados por puntuación
 */
public List<User> filtrarUsuariosPorRelacionesRelevantes(String correoUsuarioActual) {
    return filtrarUsuariosPorRelacionesRelevantes(correoUsuarioActual, 2);
}

/**
 * Filtra participantes por relaciones relevantes y excluye obligatoriamente al usuario actual.
 *
 * @param correoUsuarioActual correo del usuario que realiza la búsqueda
 * @param puntuacionMinima puntaje mínimo requerido
 * @return participantes relevantes ordenados por puntuación y número de relaciones fuertes
 */
public List<User> filtrarUsuariosPorRelacionesRelevantes(
        String correoUsuarioActual,
        int puntuacionMinima) {
    if (correoUsuarioActual == null || correoUsuarioActual.isBlank()) {
        return new ArrayList<>();
    }
    if (puntuacionMinima < 0) {
        puntuacionMinima = 0;
    }

    Map<String, Integer> colaboraciones = new HashMap<>();
    Map<String, Integer> mentorias = new HashMap<>();
    Map<String, Integer> participaciones = new HashMap<>();
    Set<Arista<String>> aristasProcesadas = new HashSet<>();

    for (Nodo<String> nodo : grafo.obtenerTodosLosNodos()) {
        for (Arista<String> arista : grafo.obtenerAristas(nodo)) {
            if (!aristasProcesadas.add(arista)
                    || arista.getDatosRelacion() == null
                    || arista.getDatosRelacion().getRelationType() == null) {
                continue;
            }

            RelationType tipo = arista.getDatosRelacion().getRelationType();
            String correoInicial = arista.getNodoInicial().getContenido();
            String correoFinal = arista.getNodoFinal().getContenido();

            if (tipo == RelationType.InvestigationCollaboration) {
                colaboraciones.merge(correoInicial, 1, Integer::sum);
                colaboraciones.merge(correoFinal, 1, Integer::sum);
            } else if (tipo == RelationType.Mentoring) {
                mentorias.merge(correoInicial, 1, Integer::sum);
                mentorias.merge(correoFinal, 1, Integer::sum);
            } else if (tipo == RelationType.JointParticipation) {
                participaciones.merge(correoInicial, 1, Integer::sum);
                participaciones.merge(correoFinal, 1, Integer::sum);
            }
        }
    }

    List<User> resultado = new ArrayList<>();
    for (Map.Entry<String, User> entrada : usuarios.entrySet()) {
        String correo = entrada.getKey();
        User usuario = entrada.getValue();
        if (correo != null && correo.equals(correoUsuarioActual)) {
            continue;
        }

        int puntuacion = calcularPuntuacionRelevancia(
                colaboraciones.getOrDefault(correo, 0),
                mentorias.getOrDefault(correo, 0),
                participaciones.getOrDefault(correo, 0));

        if (puntuacion >= puntuacionMinima) {
            resultado.add(usuario);
        }
    }

    resultado.sort((usuarioUno, usuarioDos) -> {
        String correoUno = usuarioUno != null ? usuarioUno.getEmail() : "";
        String correoDos = usuarioDos != null ? usuarioDos.getEmail() : "";

        int colaboracionesUno = colaboraciones.getOrDefault(correoUno, 0);
        int colaboracionesDos = colaboraciones.getOrDefault(correoDos, 0);
        int mentoriasUno = mentorias.getOrDefault(correoUno, 0);
        int mentoriasDos = mentorias.getOrDefault(correoDos, 0);
        int participacionesUno = participaciones.getOrDefault(correoUno, 0);
        int participacionesDos = participaciones.getOrDefault(correoDos, 0);

        int puntuacionUno = calcularPuntuacionRelevancia(
                colaboracionesUno, mentoriasUno, participacionesUno);
        int puntuacionDos = calcularPuntuacionRelevancia(
                colaboracionesDos, mentoriasDos, participacionesDos);

        int comparacion = Integer.compare(puntuacionDos, puntuacionUno);
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(colaboracionesDos, colaboracionesUno);
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(mentoriasDos, mentoriasUno);
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(participacionesDos, participacionesUno);
        if (comparacion != 0) {
            return comparacion;
        }
        return correoUno.compareToIgnoreCase(correoDos);
    });

    return resultado;
}

/**
 * Calcula el puntaje ponderado de las relaciones de un participante.
 *
 * @param colaboraciones número de colaboraciones de investigación
 * @param mentorias número de mentorías
 * @param participaciones número de participaciones conjuntas
 * @return puntuación: 3 por colaboración, 2 por mentoría y 1 por participación
 */
public int calcularPuntuacionRelevancia(
        int colaboraciones,
        int mentorias,
        int participaciones) {
    return colaboraciones * 3 + mentorias * 2 + participaciones;
}

/**
 * Identifica participantes puente ordenados por los grupos académicos que conectan.
 * La separación de grupos se realiza por programa académico.
 *
 * @param correoUsuarioActual correo del usuario que realiza la consulta
 * @return participantes puente ordenados por grupos conectados y relaciones cruzadas
 */
public List<User> identificarParticipantesPuentePorPrograma(
        String correoUsuarioActual) {
    return identificarParticipantesPuentePorPrograma(
            correoUsuarioActual,
            TypeUser.Estudents,
            TypeUser.Teachers);
}

/**
 * Identifica estudiantes o docentes que conectan diferentes programas académicos.
 *
 * @param correoUsuarioActual correo excluido obligatoriamente
 * @param tiposParticipante tipos que pueden aparecer como candidatos
 * @return participantes puente encontrados
 */
public List<User> identificarParticipantesPuentePorPrograma(
        String correoUsuarioActual,
        TypeUser... tiposParticipante) {
    return identificarParticipantesPuente(
            correoUsuarioActual,
            true,
            tiposParticipante);
}

/**
 * Identifica participantes puente ordenados por las áreas académicas que conectan.
 *
 * @param correoUsuarioActual correo del usuario que realiza la consulta
 * @return participantes puente ordenados por áreas conectadas y relaciones cruzadas
 */
public List<User> identificarParticipantesPuentePorArea(
        String correoUsuarioActual) {
    return identificarParticipantesPuentePorArea(
            correoUsuarioActual,
            TypeUser.Estudents,
            TypeUser.Teachers);
}

/**
 * Identifica estudiantes o docentes que conectan diferentes áreas académicas.
 *
 * @param correoUsuarioActual correo excluido obligatoriamente
 * @param tiposParticipante tipos que pueden aparecer como candidatos
 * @return participantes puente encontrados
 */
public List<User> identificarParticipantesPuentePorArea(
        String correoUsuarioActual,
        TypeUser... tiposParticipante) {
    return identificarParticipantesPuente(
            correoUsuarioActual,
            false,
            tiposParticipante);
}

/**
 * Calcula participantes que conectan al menos dos grupos académicos diferentes.
 *
 * @param correoUsuarioActual correo excluido obligatoriamente
 * @param porPrograma {@code true} agrupa por programa; {@code false} por área
 * @return participantes puente ordenados de mayor a menor
 */
private List<User> identificarParticipantesPuente(
        String correoUsuarioActual,
        boolean porPrograma,
        TypeUser... tiposParticipante) {
    if (correoUsuarioActual == null || correoUsuarioActual.isBlank()) {
        return new ArrayList<>();
    }
    Set<TypeUser> tiposPermitidos = tiposParticipante != null
            ? new HashSet<>(java.util.Arrays.asList(tiposParticipante))
            : new HashSet<>();

    Map<String, Set<String>> gruposConectados = new HashMap<>();
    Map<String, Integer> relacionesCruzadas = new HashMap<>();
    Set<Arista<String>> aristasProcesadas = new HashSet<>();

    for (Nodo<String> nodo : grafo.obtenerTodosLosNodos()) {
        for (Arista<String> arista : grafo.obtenerAristas(nodo)) {
            if (!aristasProcesadas.add(arista)
                    || arista.getNodoInicial() == null
                    || arista.getNodoFinal() == null) {
                continue;
            }

            String correoInicial = arista.getNodoInicial().getContenido();
            String correoFinal = arista.getNodoFinal().getContenido();
            User usuarioInicial = usuarios.get(correoInicial);
            User usuarioFinal = usuarios.get(correoFinal);
            String grupoInicial = obtenerNombreGrupo(usuarioInicial, porPrograma);
            String grupoFinal = obtenerNombreGrupo(usuarioFinal, porPrograma);

            if (grupoInicial == null || grupoFinal == null
                    || grupoInicial.equals(grupoFinal)) {
                continue;
            }

            gruposConectados
                    .computeIfAbsent(correoInicial, correo -> new HashSet<>())
                    .add(grupoFinal);
            gruposConectados
                    .computeIfAbsent(correoFinal, correo -> new HashSet<>())
                    .add(grupoInicial);
            relacionesCruzadas.merge(correoInicial, 1, Integer::sum);
            relacionesCruzadas.merge(correoFinal, 1, Integer::sum);
        }
    }

    List<User> participantesPuente = new ArrayList<>();
    for (Map.Entry<String, User> entrada : usuarios.entrySet()) {
        String correo = entrada.getKey();
        User candidato = entrada.getValue();
        if (correo.equals(correoUsuarioActual)) {
            continue;
        }
        if (candidato == null
                || candidato.getTypeUser() == null
                || !tiposPermitidos.contains(candidato.getTypeUser())) {
            continue;
        }

        Set<String> grupos = gruposConectados.get(correo);
        int cruzadas = relacionesCruzadas.getOrDefault(correo, 0);
        if (grupos != null && grupos.size() >= 2 && cruzadas > 0) {
            participantesPuente.add(entrada.getValue());
        }
    }

    participantesPuente.sort((uno, dos) -> {
        Set<String> gruposUno = gruposConectados.getOrDefault(uno.getEmail(), new HashSet<>());
        Set<String> gruposDos = gruposConectados.getOrDefault(dos.getEmail(), new HashSet<>());
        int comparacion = Integer.compare(
                gruposDos.size(),
                gruposUno.size());
        if (comparacion != 0) {
            return comparacion;
        }
        comparacion = Integer.compare(
                relacionesCruzadas.getOrDefault(dos.getEmail(), 0),
                relacionesCruzadas.getOrDefault(uno.getEmail(), 0));
        if (comparacion != 0) {
            return comparacion;
        }
        return uno.getEmail().compareToIgnoreCase(dos.getEmail());
    });
    return participantesPuente;
}

/**
 * Obtiene la denominación del grupo académico usado para clasificar puentes.
 *
 * @param usuario participante asociado
 * @param porPrograma si es verdadero usa el programa; si no, el área
 * @return nombre del grupo o {@code null} si no está registrado
 */
private String obtenerNombreGrupo(User usuario, boolean porPrograma) {
    if (usuario == null) {
        return null;
    }
    if (porPrograma) {
        return usuario.getProgram() != null
                ? usuario.getProgram().getDisplayName()
                : null;
    }
    return usuario.getInteresArea() != null
            ? usuario.getInteresArea().name()
            : null;
}

    /**
     * Evalúa si una conexión y su usuario vecino cumplen con los criterios de filtrado activos.
     * Los parámetros en {@code null} se consideran filtros no aplicados.
     *
     * @param arista       Arista que conecta al usuario actual con el vecino.
     * @param vecino       Perfil del usuario vecino en el extremo opuesto de la arista.
     * @param program      Programa académico requerido (opcional, puede ser {@code null}).
     * @param area         Área de interés requerida (opcional, puede ser {@code null}).
     * @param tipoRelacion Tipo de relación académica requerida (opcional, puede ser {@code null}).
     * @return {@code true} si la conexión y el vecino cumplen todos los filtros activos; {@code false} en caso contrario.
     */
    public boolean cumpleFiltros(Arista<String> arista, User vecino, Program program, InteresArea area, RelationType tipoRelacion) {
        if (arista == null || arista.getDatosRelacion() == null || vecino == null) {
            return false;
        }
        boolean cumpleFiltros = true;
        if (program != null) {
            cumpleFiltros = vecino.getProgram() == program;
            if (!cumpleFiltros) {
                return false;
            }
        }
        if (tipoRelacion != null) {
            cumpleFiltros = (arista.getDatosRelacion().getRelationType() == tipoRelacion);
            if (!cumpleFiltros) {
                return false;
            }
        }
        if (area != null) {
            cumpleFiltros = (vecino.getInteresArea() == area);
            if (!cumpleFiltros) {
                return false;
            }
        }
        return cumpleFiltros;
    }

    /**
     * Obtiene las conexiones directas de un usuario que satisfacen los filtros especificados (RF09).
     *
     * @param correoUsuario Correo electrónico del usuario cuyas conexiones se desean consultar.
     * @param program       Programa académico para filtrar vecinos (opcional, puede ser {@code null}).
     * @param area          Área de interés para filtrar vecinos (opcional, puede ser {@code null}).
     * @param tipoRelacion  Tipo de relación para filtrar las aristas (opcional, puede ser {@code null}).
     * @return Lista de aristas directas que cumplen con los criterios establecidos.
     */
    public List<Arista<String>> filtrarConexionesDirectas(String correoUsuario, Program program, InteresArea area, RelationType tipoRelacion) {
        Nodo<String> nodo = new Nodo<>(correoUsuario);
        List<Arista<String>> listaAristas = grafo.obtenerAristas(nodo);
        List<Arista<String>> listaAristasExitosas = new ArrayList<>();
        if (listaAristas == null) {
            return listaAristasExitosas;
        }
        for (Arista<String> arista : listaAristas) {
            String correoVecino = arista.getExtremoOpuesto(nodo).getContenido();
            if (cumpleFiltros(arista, usuarios.get(correoVecino), program, area, tipoRelacion)) {
                listaAristasExitosas.add(arista);
            }
        }
        return listaAristasExitosas;
    }

    /**
     * Calcula la cadena de colaboración más corta (menor peso acumulado) entre dos usuarios
     * mediante el algoritmo de Dijkstra, aplicando restricciones de filtrado en cada salto (RF05 y RF09).
     *
     * @param correoOrigen  Correo electrónico del usuario de partida.
     * @param correoDestino Correo electrónico del usuario objetivo.
     * @param program       Programa académico exigido a los usuarios intermediarios (opcional, puede ser {@code null}).
     * @param area          Área de interés exigida a los usuarios intermediarios (opcional, puede ser {@code null}).
     * @param tipoRelacion  Tipo de relación exigida en cada conexión de la cadena (opcional, puede ser {@code null}).
     * @return Lista ordenada de objetos {@link User} desde el origen hasta el destino, o una lista vacía si no existe ruta válida.
     */
    public List<User> buscarCadenaColaboracion(String correoOrigen, String correoDestino, Program program, InteresArea area, RelationType tipoRelacion) {
        if (correoOrigen == null || correoDestino == null || !usuarios.containsKey(correoOrigen) || !usuarios.containsKey(correoDestino)) {
            return new ArrayList<>();
        }
        Map<String, Double> listaDistancias = new HashMap<>();
        Map<String, String> listaPredecesores = new HashMap<>();
        PriorityQueue<NodoDistancia> listaEspera = new PriorityQueue<>();
        Set<String> listaVisitados = new HashSet<>();

        for (String correo : usuarios.keySet()) {
            listaDistancias.put(correo, Double.POSITIVE_INFINITY);
        }
        listaDistancias.put(correoOrigen, 0.0);
        listaEspera.add(new NodoDistancia(correoOrigen, 0.0));

        while (!listaEspera.isEmpty()) {
            NodoDistancia nodoActualD = listaEspera.poll();
            String correoActual = nodoActualD.getCorreo();

            if (correoActual.equals(correoDestino)) {
                break;
            }
            if (!listaVisitados.contains(correoActual)) {
                listaVisitados.add(correoActual);
                Nodo<String> nodoActual = new Nodo<>(correoActual);
                List<Arista<String>> vecinos = grafo.obtenerAristas(nodoActual);
                if (vecinos == null) {
                    continue;
                }
                for (Arista<String> arista : vecinos) {
                    String correoVecino = arista.getExtremoOpuesto(nodoActual).getContenido();
                    User perfilVecino = usuarios.get(correoVecino);

                    if (correoVecino.equals(correoDestino)) {
                        if (!cumpleFiltros(arista, perfilVecino, null, null, tipoRelacion)) {
                            continue;
                        }
                    } else if (!cumpleFiltros(arista, perfilVecino, program, area, tipoRelacion)) {
                        continue;
                    }

                    double nuevaDistancia = listaDistancias.get(correoActual) + arista.getPeso();
                    if (nuevaDistancia < listaDistancias.get(correoVecino)) {
                        listaDistancias.put(correoVecino, nuevaDistancia);
                        listaPredecesores.put(correoVecino, correoActual);
                        listaEspera.add(new NodoDistancia(correoVecino, nuevaDistancia));
                    }
                }
            }
        }

        if (!correoOrigen.equals(correoDestino) && !listaPredecesores.containsKey(correoDestino)) {
            return new ArrayList<>();
        } else {
            List<User> cadena = new ArrayList<>();
            String paso = correoDestino;
            while (paso != null) {
                cadena.add(usuarios.get(paso));
                paso = listaPredecesores.get(paso);
            }
            Collections.reverse(cadena);
            return cadena;
        }
    }
}