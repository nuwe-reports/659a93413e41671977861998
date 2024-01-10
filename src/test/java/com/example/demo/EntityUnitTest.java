package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.PersistenceException;

import org.h2.bnf.context.DbContents;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.example.demo.entities.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace=Replace.NONE)
@TestInstance(Lifecycle.PER_CLASS)
class EntityUnitTest {

	@Autowired
	private TestEntityManager entityManager;

	private Doctor d1;

	private Patient p1;

    private Room r1;

    private Appointment a1;
    private Appointment a2;
    private Appointment a3;

    private DateTimeFormatter formatter;

    // Setup method to initialize common data before all tests
    @BeforeEach
    void BeforeAll() {
        d1 = new Doctor("Perla", "Amalia", 24, "p.amalia@hospital.accwe");
        
        p1 = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");

        r1 = new Room("Dermatology");

        // Define a date-time formatter for parsinf and formatting dates
        formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    }

    @Test
    void shouldPersistAppointment(){
        // Create appointment data
        LocalDateTime startsAt = LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);
        
        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        
        // Persist the entities in the database
        entityManager.persist(p1);
        entityManager.persist(d1);
        entityManager.persist(r1);
        entityManager.persist(a1);

        // Flush changes to the database
        entityManager.flush();
        
        Appointment persistedAppointment = entityManager.find(Appointment.class, a1.getId());

        // Assert that the persisted appointment is not null
        assertThat(persistedAppointment).isNotNull();
        // Assert that the properties of the persisted appointment match the expected values
        assertThat(persistedAppointment.getPatient()).isEqualTo(a1.getPatient());
        assertThat(persistedAppointment.getDoctor()).isEqualTo(a1.getDoctor());
        assertThat(persistedAppointment.getRoom()).isEqualTo(a1.getRoom());
        assertThat(persistedAppointment.getStartsAt()).isEqualTo(a1.getStartsAt());
        assertThat(persistedAppointment.getFinishesAt()).isEqualTo(a1.getFinishesAt());
    }

    @Test
    void shouldPersistAppointmentWithNullData() {
        a1 = new Appointment();

        entityManager.persist(a1);
        entityManager.flush();

        Appointment persistedAppointment = entityManager.find(Appointment.class, a1.getId());

        assertThat(persistedAppointment.getId()).isEqualTo(a1.getId());
        assertThat(persistedAppointment.getPatient()).isNull();
        assertThat(persistedAppointment.getDoctor()).isNull();
        assertThat(persistedAppointment.getRoom()).isNull();
        assertThat(persistedAppointment.getStartsAt()).isNull();
        assertThat(persistedAppointment.getFinishesAt()).isNull();
    }

    @Test
    void shouldDetectOverlapWhenA2StartsBeforeA1Ends() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        LocalDateTime startsAt= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAt2 = LocalDateTime.parse("19:45 24/04/2023", formatter);
        LocalDateTime finishesAt2 = LocalDateTime.parse("20:15 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt2, finishesAt2);
        
        assertThat(a1.overlaps(a2)).isTrue();
    }

    @Test
    void shouldDetectOverlapWhenA2EndsBeforeA1Ends() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        LocalDateTime startsAt= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAt2 = LocalDateTime.parse("20:15 24/04/2023", formatter);
        LocalDateTime finishesAt2 = LocalDateTime.parse("19:45 24/04/2023", formatter);
        
        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt2, finishesAt2);

        assertThat(a1.overlaps(a2)).isTrue();
    }

    @Test
    void overlapsShouldReturnTrueWhenAppointmentsStartAndEndTimesAreSame() {
        LocalDateTime startsAt = LocalDateTime.parse("20:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);

        assertThat(a1.overlaps(a1)).isTrue();
    }

    @Test
    void overlapsShouldReturnTrueWhenTwoAppointmentsWithSameFinishedAt() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        LocalDateTime startsAt = LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAt2 = LocalDateTime.parse("19:30 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt2, finishesAt);

        assertThat(a1.overlaps(a2)).isTrue();
    }

    @Test
    void overlapsShouldReturnTrueWhenTwoAppointmentsWithSameStartsAt() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        LocalDateTime startsAt = LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("19:45 24/04/2023", formatter);

        LocalDateTime finishesAt2 = LocalDateTime.parse("20:00 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt, finishesAt2);

        assertThat(a1.overlaps(a2)).isTrue();
    }

    @Test
    void overlapsShouldReturnTrueWhenThereTwoAppoimentsWithSameTime() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");
 
        LocalDateTime startsAt = LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:00 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt, finishesAt);

        assertThat(a1.overlaps(a2)).isTrue();
    }

    @Test
    void overlapsShouldReturnFalseWhenThreeAppointmentWithDistinctTime() {
        Patient p2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");
        Patient p3 = new Patient("Sandra", "Solis", 37, "p.sandra@email.com");
 
        LocalDateTime startsAt = LocalDateTime.parse("19:45 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAt2 = LocalDateTime.parse("20:15 24/04/2023", formatter);
        LocalDateTime finishesAt2 = LocalDateTime.parse("20:30 24/04/2023", formatter);

        LocalDateTime startsAt3 = LocalDateTime.parse("20:45 24/04/2023", formatter);
        LocalDateTime finishesAt3 = LocalDateTime.parse("21:00 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a2 = new Appointment(p2, d1, r1, startsAt2, finishesAt2);
        a3 = new Appointment(p3, d1, r1, startsAt3, finishesAt3);

        assertThat(a1.overlaps(a2)).isFalse();
        assertThat(a1.overlaps(a3)).isFalse();
        assertThat(a2.overlaps(a3)).isFalse();
    }

    @Test
    void shouldSetDoctorOnAppoitment() {
        LocalDateTime startsAt = LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);

        a1 = new Appointment(p1, d1, r1, startsAt, finishesAt);
        a1.setDoctor(new Doctor ("Miren", "Iniesta", 24, "m.iniesta@hospital.accwe"));

        entityManager.persist(p1);
        entityManager.persist(d1);
        entityManager.persist(r1);
        entityManager.persist(a1);
        entityManager.flush();

        Appointment persistedAppointment = entityManager.find(Appointment.class, a1.getId());
        
        assertThat(persistedAppointment).isNotNull();
        assertThat(persistedAppointment.getPatient()).isEqualTo(a1.getPatient());
        assertThat(persistedAppointment.getDoctor()).isEqualTo(a1.getDoctor());
        assertThat(persistedAppointment.getRoom()).isEqualTo(a1.getRoom());
        assertThat(persistedAppointment.getStartsAt()).isEqualTo(a1.getStartsAt());
        assertThat(persistedAppointment.getFinishesAt()).isEqualTo(a1.getFinishesAt());
    }

    @Test
    void shouldPersistDoctorWithAllData() {
        entityManager.persist(d1);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, d1.getId());

        assertThat(persistedDoctor).isNotNull();
        assertThat(persistedDoctor.getFirstName()).isEqualTo("Perla");
        assertThat(persistedDoctor.getLastName()).isEqualTo("Amalia");
        assertThat(persistedDoctor.getAge()).isEqualTo(24);
        assertThat(persistedDoctor.getEmail()).isEqualTo("p.amalia@hospital.accwe");
     }

    @Test
    void shouldPersistDoctorNull() {
        d1 = new Doctor();

        entityManager.persist(d1);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, d1.getId());

        assertThat(persistedDoctor).isNotNull();
        assertThat(persistedDoctor.getId()).isEqualTo(d1.getId());
        assertThat(persistedDoctor.getFirstName()).isNull();
        assertThat(persistedDoctor.getLastName()).isNull();
        assertThat(persistedDoctor.getAge()).isEqualTo(0);
        assertThat(persistedDoctor.getEmail()).isNull();
    }

    @Test
    void shouldGetterAndSetterDoctorWorkCorrectly() {
        d1.setAge(25);

        entityManager.persist(d1);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, d1.getId());

        assertThat(persistedDoctor).isNotNull();
        assertThat(persistedDoctor.getFirstName()).isEqualTo("Perla");
        assertThat(persistedDoctor.getLastName()).isEqualTo("Amalia");
        assertThat(persistedDoctor.getAge()).isEqualTo(25);
        assertThat(persistedDoctor.getEmail()).isEqualTo("p.amalia@hospital.accwe");
    }

    @Test
    void shouldPersistPatientWithAllData() {
        entityManager.persist(p1);
        entityManager.flush();

        Patient persistedPatient = entityManager.find(Patient.class, p1.getId());

        assertThat(persistedPatient).isNotNull();
        assertThat(persistedPatient.getFirstName()).isEqualTo(p1.getFirstName());
        assertThat(persistedPatient.getLastName()).isEqualTo(p1.getLastName());
        assertThat(persistedPatient.getAge()).isEqualTo(p1.getAge());
        assertThat(persistedPatient.getEmail()).isEqualTo(p1.getEmail());
    }

    @Test
    void shouldPersistPatientNull() {
        p1 = new Patient();

        entityManager.persist(p1);
        entityManager.flush();

        Patient persistedPatient = entityManager.find(Patient.class, p1.getId());

        assertThat(persistedPatient).isNotNull();
        assertThat(persistedPatient.getId()).isEqualTo(p1.getId());
        assertThat(persistedPatient.getLastName()).isNull();
        assertThat(persistedPatient.getLastName()).isNull();
        assertThat(persistedPatient.getAge()).isEqualTo(0);
        assertThat(persistedPatient.getEmail()).isNull();
    }

    @Test
    void shouldGetterAndSetterPatientWorkCorrectly() {        
        p1.setAge(38);
        
        entityManager.persist(p1);
        entityManager.flush();

        Patient persistedPatient = entityManager.find(Patient.class, p1.getId());
        
        assertThat(persistedPatient).isNotNull();
        assertThat(persistedPatient.getFirstName()).isEqualTo(p1.getFirstName());
        assertThat(persistedPatient.getLastName()).isEqualTo(p1.getLastName());
        assertThat(persistedPatient.getAge()).isEqualTo(p1.getAge());
        assertThat(persistedPatient.getEmail()).isEqualTo(p1.getEmail());
    }

    @Test
    void shouldPersistRoomWithAllData() {
        r1 = new Room("Gynecology");
        entityManager.persist(r1);
        entityManager.flush();

        Room persistedRoom = entityManager.find(Room.class, r1.getRoomName());

        assertThat(persistedRoom).isNotNull();
        assertThat(persistedRoom.getRoomName()).isEqualTo(r1.getRoomName());
    }

    @Test
    void shouldThrowExceptionWhenPersistingEmptyRoom() {
        // Create an empty room without setting any values
        Room r1 = new Room();

        // Use assertThrows to ensure that persisting an empty room throws the expected exception
        PersistenceException persistenceException = assertThrows(PersistenceException.class, () -> {
            entityManager.persist(r1);
            entityManager.flush();
        });

        // Verify the exception and its cause
        assertThat(persistenceException).isNotNull();
        assertThat(persistenceException.getCause()).isInstanceOf(IdentifierGenerationException.class);
    }
}