package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



/**
 * Test de integración de Registros/ Usuarios
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 409 Conflict con email duplicado
 */
//@WebMvcTest(ControladorREST.class)
//@AutoConfigureMockMvc(addFilters = false)



@DataJpaTest
class IntegrationTest {

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoUsuario repoUsuario;

    private static final String REGISTER = "/pistaPadel/auth/register";

    @Autowired
    private RepoPista repoPista;

    @Autowired
    private RepoDisponibilidad repoDisponibilidad;

    /*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }
    */


    @Test
    void registro_ok_201() throws Exception {

        Usuario user = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        repoUsuario.save(user);
        assertNotNull(repoUsuario.findById(user.getId()));
    }


    @Test
    void registro_emailDuplicado_409() throws Exception {
        Usuario user1 = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        Usuario user2 = new Usuario(null, "Martina", "Ortiz", "ana.integration@test.com", "789", "030", NombreRol.USER, LocalDateTime.now(),true);

        // Guardo por primera vez:
        repoUsuario.save(user1);
        // Guardo por segunda vez:
        DataIntegrityViolationException error = null;
        try{
            repoUsuario.save(user2);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        // Nos aseguramos que salte el error:
        assertNotNull(error);
    }

    @Test
    void borrado_registros() throws Exception{
        // Creo dos registros cualquiera para llenar mi repo:
        Usuario user1 = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        Usuario user2 = new Usuario(null, "Martina", "Ortiz", "skinny_legend.integration@test.com", "789", "030", NombreRol.USER, LocalDateTime.now(),true);
        repoUsuario.save(user1);
        repoUsuario.save(user2);

        // Probamos que los datos están en el repo:
        Assertions.assertTrue(repoUsuario.count() > 0);


        // Probamos a borrar toda la tabla:
        repoUsuario.deleteAll();
        // Pruebo que está vacío el repo:
        Assertions.assertEquals(0, repoUsuario.count());
    }

    // Buscar por email un usuario existente
    @Test
    void findByEmail_existe_usuario() {
        Usuario user = new Usuario(
                null, "Ana", "Ramos",
                "ana@test.com", "123", "456",
                NombreRol.USER, LocalDateTime.now(), true
        );

        repoUsuario.save(user);

        Usuario encontrado = repoUsuario.findByEmail("ana@test.com");

        assertNotNull(encontrado);
        Assertions.assertEquals("Ana", encontrado.getNombre());
    }

    // Con los siguientes test vamos a verificar el funcionamiento de las otras funciones del repo
    // Buscar por email un usuario no existente
    @Test
    void findByEmail_noExiste_usuario() {
        Usuario encontrado = repoUsuario.findByEmail("noexiste@test.com");

        Assertions.assertNull(encontrado);
    }

    // Verificar que existe un email:
    @Test
    void existsByEmail_true_siExiste() {
        repoUsuario.save(new Usuario(
                null, "Ana", "Ramos",
                "ana@test.com", "123", "456",
                NombreRol.USER, LocalDateTime.now(), true
        ));

        Assertions.assertTrue(repoUsuario.existsByEmail("ana@test.com"));
    }

    // Verificar que *no* existe un email:
    @Test
    void existsByEmail_false_siNoExiste() {
        Assertions.assertFalse(repoUsuario.existsByEmail("libre@test.com"));
    }

    // Verificar que un email pertenece a uno y no otro usuario:
    @Test
    void existsByEmailAndIdNot_true_siEmailEnOtroUsuario() {
        Usuario user1 = repoUsuario.save(new Usuario(
                null, "Ana", "Ramos",
                "duplicado@test.com", "123", "456",
                NombreRol.USER, LocalDateTime.now(), true
        ));

        Usuario user2 = repoUsuario.save(new Usuario(
                null, "Martina", "Ortiz",
                "martina@test.com", "123", "789",
                NombreRol.USER, LocalDateTime.now(), true
        ));

        boolean existe = repoUsuario.existsByEmailAndIdNot(
                "duplicado@test.com",
                user2.getId()
        );

        Assertions.assertTrue(existe);
    }

    // Verificar que el email e id pertenecen al mismo usuario:
    @Test
    void existsByEmailAndIdNot_false_siEsMismoUsuario() {
        Usuario user = repoUsuario.save(new Usuario(
                null, "Ana", "Ramos",
                "ana@test.com", "123", "456",
                NombreRol.USER, LocalDateTime.now(), true
        ));

        boolean existe = repoUsuario.existsByEmailAndIdNot(
                "ana@test.com",
                user.getId()
        );

        Assertions.assertFalse(existe);
    }

    // Verificar la correcta creación y registro de todos los usuarios:
    @Test
    void findAll_devuelve_todos_los_usuarios() {
        repoUsuario.save(new Usuario(null, "A", "A", "a@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(), true));
        repoUsuario.save(new Usuario(null, "B", "B", "b@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(), true));

        Iterable<Usuario> usuarios = repoUsuario.findAll();

        List<Usuario> lista = new ArrayList<>();
        usuarios.forEach(lista::add);

        Assertions.assertEquals(2, lista.size());
    }



     /*
     * Test de integración del endpoint PISTAS
     */

    @Test
    void createPista() {

        Pista pista = new Pista(
                "Central",
                "Madrid",
                20,
                true,
                "2026-03-08"
        );

        repoPista.save(pista);
        Pista encontrada = repoPista.findById(pista.id).orElse(null);

        assertNotNull(encontrada);
        assertThat(encontrada.nombre).isEqualTo("Central");
    }

    @Test
    void createPistaWithDuplicateNameShouldFail() {
        Pista pista1 = new Pista("central", "Madrid", 20, true, "2026-03-08");

        Pista pista2 = new Pista("central", "Barcelona", 25, true, "2026-03-09");

        repoPista.save(pista1);
        DataIntegrityViolationException error = null;
        try {
            repoPista.save(pista2);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        assertNotNull(error);
    }



    // Metodo privado para crear una pista de prueba
    private Long crearPistaPrueba() {
        Pista nuevaPista = new Pista(
                null,
                "Pista-Test-" + System.currentTimeMillis(),
                "Test Location",
                2800L,
                true,
                "2025-02-01"
        );
        Pista guardada = repoPista.save(nuevaPista);
        return guardada.id;
    }

/*
     * Test de integración del endpoint RESERVAS
     */

    @Test
    @DisplayName("Reprogramar reserva actualiza persistencia")
    void reprogramarReserva_actualizaPersistencia() {
        // Crear pista
        Long courtId = crearPistaPrueba();
        Pista pista = repoPista.findById(courtId).orElseThrow();

        // Crear usuario
        Usuario usuario = new Usuario(null, "User2", "Test", "user2@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);

        // Crear reserva original
        LocalDateTime inicioOriginal = LocalDateTime.of(2026, 7, 20, 14, 0);
        LocalDateTime finOriginal = LocalDateTime.of(2026, 7, 20, 15, 0);

        Reserva reservaOriginal = new Reserva();
        reservaOriginal.usuario = usuario;
        reservaOriginal.pista = pista;
        reservaOriginal.inicio = inicioOriginal;
        reservaOriginal.fin = finOriginal;
        reservaOriginal.fechaReservada = LocalDateTime.now();
        reservaOriginal.duracionMinutos = 60;
        reservaOriginal.estado = EstadoReserva.ACTIVA;
        reservaOriginal.fechaCreacion = LocalDateTime.now();

        Reserva reservaGuardada = repoReserva.save(reservaOriginal);
        Long reservaId = reservaGuardada.reservationId;

        // Reprogramar la reserva
        reservaGuardada.inicio = LocalDateTime.of(2026, 7, 20, 16, 0);
        reservaGuardada.fin = LocalDateTime.of(2026, 7, 20, 17, 0);
        repoReserva.save(reservaGuardada);

        // Verificar que la BD tiene los datos actualizados
        Optional<Reserva> reservaActualizada = repoReserva.findById(reservaId);
        assertThat(reservaActualizada).isPresent();
        assertThat(reservaActualizada.get().inicio).isEqualTo(LocalDateTime.of(2026, 7, 20, 16, 0));
        assertThat(reservaActualizada.get().fin).isEqualTo(LocalDateTime.of(2026, 7, 20, 17, 0));
    }

    @Test
    @DisplayName("Solapamiento de reservas → 409 Conflict")
    void solapamiento_da_409() {
        // Crear pista
        Long courtId = crearPistaPrueba();
        Pista pista = repoPista.findById(courtId).orElseThrow();

        // Crear usuarios
        Usuario usuario1 = new Usuario(null, "User10", "Test", "user10@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        Usuario usuario2 = new Usuario(null, "User11", "Test", "user11@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario1);
        repoUsuario.save(usuario2);

        // Primera reserva: 10:00 - 11:30
        Reserva r1 = new Reserva();
        r1.usuario = usuario1;
        r1.pista = pista;
        r1.inicio = LocalDateTime.of(2026, 10, 5, 10, 0);
        r1.fin = LocalDateTime.of(2026, 10, 5, 11, 30);
        r1.fechaReservada = LocalDateTime.now();
        r1.duracionMinutos = 90;
        r1.estado = EstadoReserva.ACTIVA;
        r1.fechaCreacion = LocalDateTime.now();

        repoReserva.save(r1);
        assertThat(repoReserva.count()).isEqualTo(1);

        // Segunda reserva: 10:45 - 11:45 (SOLAPADA)
        Reserva r2 = new Reserva();
        r2.usuario = usuario2;
        r2.pista = pista;
        r2.inicio = LocalDateTime.of(2026, 10, 5, 10, 45);
        r2.fin = LocalDateTime.of(2026, 10, 5, 11, 45);
        r2.fechaReservada = LocalDateTime.now();
        r2.duracionMinutos = 60;
        r2.estado = EstadoReserva.ACTIVA;
        r2.fechaCreacion = LocalDateTime.now();

        repoReserva.save(r2);

        assertThat(repoReserva.count()).isEqualTo(2);
    }

    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}

@Test
@DisplayName("Disponibilidad: Buscar por fecha devuelve todas las pistas de ese día")
void disponibilidad_multiple_pistas_test() {
    Long id1 = crearPistaPrueba();
    Long id2 = crearPistaPrueba();
    LocalDate hoy = LocalDate.now();

    repoDisponibilidad.save(new Disponibilidad(repoPista.findById(id1).get(), hoy, LocalTime.of(9,0), LocalTime.of(21,0), null));
    repoDisponibilidad.save(new Disponibilidad(repoPista.findById(id2).get(), hoy, LocalTime.of(9,0), LocalTime.of(21,0), null));

    List<Disponibilidad> lista = repoDisponibilidad.findByFecha(hoy);

    assertThat(lista.size()).isEqualTo(2);
}



@Test
@DisplayName("Disponibilidad: Recuperar reservas de un usuario específico")
void disponibilidad_reservas_usuario_test() {

    Usuario u = repoUsuario.save(new Usuario(null, "Pepe", "Test", "pepe@test.com", "1", "1", NombreRol.USER, LocalDateTime.now(), true));
    Pista p = repoPista.findById(crearPistaPrueba()).get();


    Reserva r = new Reserva();
    r.usuario = u;
    r.pista = p;
    r.inicio = LocalDateTime.of(2025, 12, 1, 10, 0);
    r.duracionMinutos = 60;

    r.fin = LocalDateTime.of(2025, 12, 1, 11, 0);
    r.duracionMinutos = 60;
    r.estado = EstadoReserva.ACTIVA;
    r.fechaReservada = LocalDateTime.now();
    r.fechaCreacion = LocalDateTime.now();
    repoReserva.save(r);
    List<Reserva> misReservas = repoReserva.findByUsuario_Id(u.getId());

    assertThat(misReservas.size()).isEqualTo(1);
    assertThat(misReservas.get(0).usuario.getEmail()).isEqualTo("pepe@test.com");
}
 @Test
    void crearDisponibilidad_persisteEnBD() {
        Long pistId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistId).get();
        LocalDate fecha = LocalDate.of(2026, 6, 1);

        Disponibilidad disp = new Disponibilidad(
                pista, fecha,
                LocalTime.of(8, 0), LocalTime.of(22, 0), null
        );

        repoDisponibilidad.save(disp);

        Optional<Disponibilidad> encontrada = repoDisponibilidad.findById(disp.getId());
        assertThat(encontrada.isPresent()).isTrue();
        assertThat(encontrada.get().getFecha()).isEqualTo(fecha);
    }

    @Test
    void disponibilidad_buscarPorPistaYFecha_ok() {
        Long pistaId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistaId).get();
        LocalDate fecha = LocalDate.of(2026, 6, 15);

        repoDisponibilidad.save(new Disponibilidad(
                pista, fecha,
                LocalTime.of(8, 0), LocalTime.of(22, 0), null
        ));

        Optional<Disponibilidad> resultado = repoDisponibilidad.findByPista_IdAndFecha(pistaId, fecha);

        assertThat(resultado.isPresent()).isTrue();
        assertThat(resultado.get().getFecha()).isEqualTo(fecha);
    }

    @Test
    void disponibilidad_buscarFechaInexistente_listaVacia() {
        LocalDate fechaSinDatos = LocalDate.of(2099, 1, 1);

        List<Disponibilidad> lista = repoDisponibilidad.findByFecha(fechaSinDatos);

        assertThat(lista.size()).isEqualTo(0);
    }

    @Test
    void borrarDisponibilidad_eliminaDeBD() {
        Long pistaId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistaId).get();

        Disponibilidad disp = new Disponibilidad(
                pista, LocalDate.of(2026, 7, 1),
                LocalTime.of(8, 0), LocalTime.of(22, 0), null
        );

        repoDisponibilidad.save(disp);
        Long id = disp.getId();

        repoDisponibilidad.deleteById(id);

        assertThat(repoDisponibilidad.findById(id).isPresent()).isFalse();
    }


    @Test
    @DisplayName("Buscar reservas por pista ID → findByPista_Id")
    void findByPistaId_devuelve_reservas_de_pista() {
        // Crear dos pistas
        Long pistaId1 = crearPistaPrueba();
        Long pistaId2 = crearPistaPrueba();
        
        Pista pista1 = repoPista.findById(pistaId1).orElseThrow();
        Pista pista2 = repoPista.findById(pistaId2).orElseThrow();

        // Crear usuarios
        Usuario usuario1 = new Usuario(null, "Carlos", "López", "carlos@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        Usuario usuario2 = new Usuario(null, "Diana", "García", "diana@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario1);
        repoUsuario.save(usuario2);

        // Crear 2 reservas en pista1 y 1 en pista2
        Reserva r1 = new Reserva();
        r1.usuario = usuario1;
        r1.pista = pista1;
        r1.inicio = LocalDateTime.of(2026, 8, 15, 9, 0);
        r1.fin = LocalDateTime.of(2026, 8, 15, 10, 0);
        r1.fechaReservada = LocalDateTime.now();
        r1.duracionMinutos = 60;
        r1.estado = EstadoReserva.ACTIVA;
        r1.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r1);

        Reserva r2 = new Reserva();
        r2.usuario = usuario2;
        r2.pista = pista1;
        r2.inicio = LocalDateTime.of(2026, 8, 15, 11, 0);
        r2.fin = LocalDateTime.of(2026, 8, 15, 12, 0);
        r2.fechaReservada = LocalDateTime.now();
        r2.duracionMinutos = 60;
        r2.estado = EstadoReserva.ACTIVA;
        r2.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r2);

        Reserva r3 = new Reserva();
        r3.usuario = usuario1;
        r3.pista = pista2;
        r3.inicio = LocalDateTime.of(2026, 8, 16, 9, 0);
        r3.fin = LocalDateTime.of(2026, 8, 16, 10, 0);
        r3.fechaReservada = LocalDateTime.now();
        r3.duracionMinutos = 60;
        r3.estado = EstadoReserva.ACTIVA;
        r3.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r3);

        // Buscar reservas de pista1
        List<Reserva> reservasPista1 = repoReserva.findByPista_Id(pistaId1);
        
        assertThat(reservasPista1).hasSize(2);
        reservasPista1.forEach(r -> assertThat(r.pista.id).isEqualTo(pistaId1));
    }

    @Test
    @DisplayName("Cambiar estado de reserva a CANCELADA")
    void cambiarEstadoReserva_a_CANCELADA() {
        // Crear pista y usuario
        Long pistaId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistaId).orElseThrow();
        
        Usuario usuario = new Usuario(null, "Elena", "Martín", "elena@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);

        // Crear reserva activa
        Reserva reserva = new Reserva();
        reserva.usuario = usuario;
        reserva.pista = pista;
        reserva.inicio = LocalDateTime.of(2026, 9, 1, 15, 0);
        reserva.fin = LocalDateTime.of(2026, 9, 1, 16, 0);
        reserva.fechaReservada = LocalDateTime.now();
        reserva.duracionMinutos = 60;
        reserva.estado = EstadoReserva.ACTIVA;
        reserva.fechaCreacion = LocalDateTime.now();
        
        Reserva reservaGuardada = repoReserva.save(reserva);
        Long reservaId = reservaGuardada.reservationId;

        // Verificar que está ACTIVA
        Optional<Reserva> reservaActiva = repoReserva.findById(reservaId);
        assertThat(reservaActiva).isPresent();
        assertThat(reservaActiva.get().estado).isEqualTo(EstadoReserva.ACTIVA);

        // Cambiar estado a CANCELADA
        reservaGuardada.estado = EstadoReserva.CANCELADA;
        repoReserva.save(reservaGuardada);

        // Verificar que ahora está CANCELADA
        Optional<Reserva> reservaCancelada = repoReserva.findById(reservaId);
        assertThat(reservaCancelada).isPresent();
        assertThat(reservaCancelada.get().estado).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("Buscar reservas en rango de fechas → findByInicioBetween")
    void findByInicioBetween_devuelve_reservas_en_rango() {
        // Crear pista
        Long pistaId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistaId).orElseThrow();
        
        // Crear usuario
        Usuario usuario = new Usuario(null, "Fernando", "Díaz", "fernando@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);

        // Crear reservas en diferentes fechas
        // 1. Reserva el 1 de junio
        Reserva r1 = new Reserva();
        r1.usuario = usuario;
        r1.pista = pista;
        r1.inicio = LocalDateTime.of(2026, 6, 1, 10, 0);
        r1.fin = LocalDateTime.of(2026, 6, 1, 11, 0);
        r1.fechaReservada = LocalDateTime.now();
        r1.duracionMinutos = 60;
        r1.estado = EstadoReserva.ACTIVA;
        r1.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r1);

        // 2. Reserva el 15 de junio
        Reserva r2 = new Reserva();
        r2.usuario = usuario;
        r2.pista = pista;
        r2.inicio = LocalDateTime.of(2026, 6, 15, 14, 0);
        r2.fin = LocalDateTime.of(2026, 6, 15, 15, 0);
        r2.fechaReservada = LocalDateTime.now();
        r2.duracionMinutos = 60;
        r2.estado = EstadoReserva.ACTIVA;
        r2.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r2);

        // 3. Reserva el 30 de julio (fuera del rango)
        Reserva r3 = new Reserva();
        r3.usuario = usuario;
        r3.pista = pista;
        r3.inicio = LocalDateTime.of(2026, 7, 30, 10, 0);
        r3.fin = LocalDateTime.of(2026, 7, 30, 11, 0);
        r3.fechaReservada = LocalDateTime.now();
        r3.duracionMinutos = 60;
        r3.estado = EstadoReserva.ACTIVA;
        r3.fechaCreacion = LocalDateTime.now();
        repoReserva.save(r3);

        // Buscar reservas entre 1 y 30 de junio
        LocalDateTime inicioRango = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime finRango = LocalDateTime.of(2026, 6, 30, 23, 59);
        
        List<Reserva> reservasEnRango = repoReserva.findByInicioBetween(inicioRango, finRango);

        assertThat(reservasEnRango).hasSize(2);
        reservasEnRango.forEach(r -> {
            assertThat(r.inicio).isAfter(inicioRango);
            assertThat(r.inicio).isBefore(finRango);
        });
    }

    @Test
    @DisplayName("Verificar duración en minutos se calcula correctamente")
    void duracionMinutos_calculo_correcto() {
        // Crear pista y usuario
        Long pistaId = crearPistaPrueba();
        Pista pista = repoPista.findById(pistaId).orElseThrow();
        
        Usuario usuario = new Usuario(null, "Gisela", "Ruiz", "gisela@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);

        // Crear reserva de 90 minutos
        Reserva reserva = new Reserva();
        reserva.usuario = usuario;
        reserva.pista = pista;
        reserva.inicio = LocalDateTime.of(2026, 5, 20, 18, 0);
        reserva.fin = LocalDateTime.of(2026, 5, 20, 19, 30);
        reserva.fechaReservada = LocalDateTime.now();
        reserva.duracionMinutos = 90;
        reserva.estado = EstadoReserva.ACTIVA;
        reserva.fechaCreacion = LocalDateTime.now();

        Reserva reservaGuardada = repoReserva.save(reserva);

        // Verificar que la duración se guardó correctamente
        Optional<Reserva> reservaRecuperada = repoReserva.findById(reservaGuardada.reservationId);
        assertThat(reservaRecuperada).isPresent();
        assertThat(reservaRecuperada.get().duracionMinutos).isEqualTo(90);
        
        // Verificar que la diferencia entre fin e inicio es correcta
        long minutosCalculados = java.time.temporal.ChronoUnit.MINUTES.between(
            reservaRecuperada.get().inicio,
            reservaRecuperada.get().fin
        );
        assertThat(minutosCalculados).isEqualTo(90);
    }

    @Test
    @DisplayName("Reserva mantiene datos de usuario y pista después de guardar")
    void reserva_mantiene_relaciones_tras_guardar() {
        // Crear pista con características específicas
        Pista pista = new Pista(
            null,
            "Pista-Integración-" + System.currentTimeMillis(),
            "Barcelona",
            3500L,
            true,
            "2026-05-10"
        );
        Pista pistaGuardada = repoPista.save(pista);

        // Crear usuario con datos específicos
        Usuario usuario = new Usuario(
            null, 
            "Héctor", 
            "Sánchez", 
            "hector@test.com", 
            "password123", 
            "987654321", 
            NombreRol.USER, 
            LocalDateTime.now(), 
            true
        );
        Usuario usuarioGuardado = repoUsuario.save(usuario);

        // Crear reserva con relaciones
        Reserva reserva = new Reserva();
        reserva.usuario = usuarioGuardado;
        reserva.pista = pistaGuardada;
        reserva.inicio = LocalDateTime.of(2026, 10, 15, 19, 0);
        reserva.fin = LocalDateTime.of(2026, 10, 15, 20, 30);
        reserva.fechaReservada = LocalDateTime.now();
        reserva.duracionMinutos = 90;
        reserva.estado = EstadoReserva.ACTIVA;
        reserva.fechaCreacion = LocalDateTime.now();

        Reserva reservaGuardada = repoReserva.save(reserva);

        // Recuperar y verificar que mantiene las relaciones
        Optional<Reserva> reservaRecuperada = repoReserva.findById(reservaGuardada.reservationId);
        assertThat(reservaRecuperada).isPresent();
        
        Reserva r = reservaRecuperada.get();
        assertThat(r.usuario).isNotNull();
        assertThat(r.usuario.getNombre()).isEqualTo("Héctor");
        assertThat(r.usuario.getEmail()).isEqualTo("hector@test.com");
        
        assertThat(r.pista).isNotNull();
        assertThat(r.pista.nombre).startsWith("Pista-Integración-");
        assertThat(r.pista.ubicacion).isEqualTo("Barcelona");
        assertThat(r.pista.precioHora).isEqualTo(3500L);
    }
}
