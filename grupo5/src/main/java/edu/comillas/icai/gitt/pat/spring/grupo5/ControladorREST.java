package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class ControladorREST {

    Map<Long, Pista> pistas = new ConcurrentHashMap<Long, Pista>();
    private long idPistaContador = 0;
    private static final int N = 1000;


    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista crea(@RequestBody Pista pista) {

        boolean nombreDuplicado = pistas.values().stream()
                .anyMatch(p -> p.nombre().equalsIgnoreCase(pista.nombre()));

        if (nombreDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Otra pista con la igual nombre"
            );
        }

        if (idPistaContador > N){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "idPista es major de N"
            );
        }

        long idPista = idPistaContador++;

        Pista pistaNuevo = new Pista(
                idPista,
                pista.nombre(),
                pista.ubicacion(),
                pista.precioHora(),
                pista.activa(),
                pista.fechaAlta()
        );
        pistas.put(idPista, pistaNuevo);
        return pistaNuevo;
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts")
    public List<Pista> pistas() {
        ArrayList<Pista> pistaList = new ArrayList<>();
        for (Pista p : pistas.values()){
            pistaList.add(p);
        }
        return pistaList;
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista obtenerDetalle(@PathVariable long courtId){
        Pista pista = pistas.get(courtId);
        if(pista == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "no pista con este id"
            );
        }

        return pista;
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista modificarPista(@PathVariable long courtId, @RequestBody Pista pistaMod){
        Pista pista = pistas.get(courtId);
        if (pista == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Pista pistaActualizada = new Pista(
                courtId,
                pistaMod.nombre(),
                pistaMod.ubicacion(),
                pistaMod.precioHora(),
                pistaMod.activa(),
                pistaMod.fechaAlta());

        pistas.put(courtId, pistaActualizada);
        return pistaActualizada;
    }


    @DeleteMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void borrar(@PathVariable long courtId){
        Pista pista = pistas.get(courtId);
        if(pista == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "no pista con este id"
            );
        }
        pistas.remove(courtId);
    }

    private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
    private long idReservaContador = 0;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Reserva crearReserva(@RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (reservaNueva.inicio().isAfter(reservaNueva.fin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inicio posterior a fin");
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        // Detección solapes (409)
        boolean solapa = reservas.values().stream()
                .filter(r -> r.courtId() == reservaNueva.courtId())
                .anyMatch(r ->
                        reservaNueva.inicio().isBefore(r.fin()) &&
                                reservaNueva.fin().isAfter(r.inicio())
                );

        if (solapa) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Slot ocupado"
            );
        }

        long ReservationId = idReservaContador++;

        Reserva reservaCreada = new Reserva(
                ReservationId,
                reservaNueva.courtId(),
                reservaNueva.userId(), // luego vendrá del SecurityContext
                reservaNueva.inicio(),
                reservaNueva.fin()
        );

        reservas.put(ReservationId, reservaCreada);
        return reservaCreada;
    }

    @PatchMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva reprogramar(@PathVariable long reservationId, @RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Reserva actual = reservas.get(reservationId);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        // Detección solapes (409)
        boolean solapa = reservas.values().stream()
                .filter(r -> r.courtId() == actual.courtId())
                .filter(r -> r.reservationId() != reservationId)
                .anyMatch(r ->
                        reservaNueva.inicio().isBefore(r.fin()) &&
                                reservaNueva.fin().isAfter(r.inicio())
                );

        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Reserva actualizada = new Reserva(
                reservationId,
                actual.courtId(),
                actual.userId(),
                reservaNueva.inicio(),
                reservaNueva.fin()
        );

        reservas.put(reservationId, actualizada);
        return actualizada;
    }

    @GetMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva obtenerReserva(@PathVariable long reservationId) {
        Reserva reserva = reservas.get(reservationId);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }
        return reserva;
    }

    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> reservas() {
        ArrayList<Reserva> ReservaList = new ArrayList<>();
        for (Reserva r : reservas.values()){
            ReservaList.add(r);
        }
        return ReservaList;
    }

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void cancelar(@PathVariable long reservationId) {

        Reserva reserva = reservas.get(reservationId);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }
        reservas.remove(reservationId);
    }
    @GetMapping("/pistaPadel/availability")
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam(name = "date") String dateStr,
            @RequestParam(name = "courtId", required = false) Long courtId
    ) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dateStr); // Espera YYYY-MM-DD
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido (use YYYY-MM-DD)");
        }

        boolean disponible = false;

        if (courtId != null) {
            // A) Buscamos disponibilidad de UNA pista concreta
            // (Nota: Si la pista no existe, aquí podríamos dar 404, pero el requisito dice 200/400 en este endpoint)
            if (pistas.containsKey(courtId)) {
                disponible = isPistaLibreEnFecha(courtId, fecha);
            }
        } else {
            // B) Buscamos si hay ALGUNA pista libre
            for (Long id : pistas.keySet()) {
                if (isPistaLibreEnFecha(id, fecha)) {
                    disponible = true;
                    break; // Encontramos una libre, suficiente
                }
            }
        }

        String msg = disponible ? "Hay disponibilidad" : "Completo / No disponible";
        return new DisponibilidadResponse(fecha, courtId, disponible, msg);
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public DisponibilidadResponse disponibilidadPistaConcreta(
            @PathVariable Long courtId,
            @RequestParam(name = "date") String dateStr
    ) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido");
        }

        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista " + courtId + " no existe");
        }

        boolean disponible = isPistaLibreEnFecha(courtId, fecha);

        return new DisponibilidadResponse(fecha, courtId, disponible, disponible ? "Libre" : "Ocupada");
    }



    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listarMisReservas(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            // Simulamos Auth con una cabecera simple "X-User-Id"
            @RequestHeader(name = "X-User-Id", required = false) String userId
    ) {
        // 1. Validar Auth (401)
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado");
        }

        // 2. Parsear filtros opcionales
        LocalDate fromDate = (from != null) ? LocalDate.parse(from) : null;
        LocalDate toDate = (to != null) ? LocalDate.parse(to) : null;

        // 3. Filtrar reservas
        return reservas.values().stream()
                // A. Solo mis reservas
                .filter(r -> r.userId().equals(userId))
                // B. Filtro Desde (from)
                .filter(r -> fromDate == null || !r.inicio().toLocalDate().isBefore(fromDate))
                // C. Filtro Hasta (to)
                .filter(r -> toDate == null || !r.inicio().toLocalDate().isAfter(toDate))
                .collect(Collectors.toList());
    }
    private boolean isPistaLibreEnFecha(Long courtId, LocalDate fecha) {
        return reservas.values().stream()
                .filter(r -> r.courtId() == courtId)
                .noneMatch(r -> r.inicio().toLocalDate().equals(fecha));
    }
}

