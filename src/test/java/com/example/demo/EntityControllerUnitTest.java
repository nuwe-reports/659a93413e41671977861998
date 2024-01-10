
package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.time.format.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.demo.controllers.*;
import com.example.demo.repositories.*;
import com.example.demo.entities.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DoctorController.class)
class DoctorControllerUnitTest{

    @MockBean
    private DoctorRepository doctorRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateDoctor() throws Exception {
        Doctor doctor = new Doctor("Perla", "Amalia", 24, "p.amalia@hospital.accwe");

        String content = objectMapper.writeValueAsString(doctor);
        mockMvc.perform(post("/api/doctor").contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetDoctors() throws Exception {
        Doctor doctor1 = new Doctor("Perla", "Amalia", 24, "p.amalia@hospital.accwe");
        Doctor doctor2 = new Doctor("Miren", "Iniesta", 24, "m.iniesta@hospital.accwe");

        List<Doctor> doctors = new ArrayList<Doctor>();

        doctors.add(doctor1);
        doctors.add(doctor2);

        String contentExpected = objectMapper.writeValueAsString(doctors);

        when(doctorRepository.findAll()).thenReturn(doctors);
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(content().json(contentExpected));
    }

    @Test
    void shouldNotGetDoctors() throws Exception {
        List<Doctor> doctors = new ArrayList<Doctor>();
        when(doctorRepository.findAll()).thenReturn(doctors);

        mockMvc.perform(get("/api/doctors")).andExpect(status().isNoContent());
    }

    @Test
    void shouldGetDoctorById() throws Exception {
        Doctor doctor = new Doctor("Perla", "Amalia", 24, "p.amalia@hospital.accwe");
        Optional<Doctor> doctorOpc = Optional.of(doctor);

        when(doctorRepository.findById(doctor.getId())).thenReturn(doctorOpc);
        mockMvc.perform(get("/api/doctors/{id}", doctor.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Perla"))
                .andExpect(jsonPath("$.lastName").value("Amalia"))
                .andExpect(jsonPath("$.age").value(24))
                .andExpect(jsonPath("$.email").value("p.amalia@hospital.accwe"));
    }

    @Test
    void shouldNotGetAnyDoctorById() throws Exception {
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());
        long id = 3;
        mockMvc.perform(get("/api/doctors/{id}", id)).andExpect(status().isNotFound());

    }

    @Test
    void shouldDeleteDoctorById() throws Exception {
        long doctorIdToDelete = 3;
        Doctor doctor = new Doctor("Perla", "Amalia", 24, "p.amalia@hospital.accwe");
        Optional<Doctor> optionalDoctor = Optional.of(doctor);
        assertThat(optionalDoctor).isPresent();
        when(doctorRepository.findById(doctorIdToDelete)).thenReturn(optionalDoctor);
        doNothing().when(doctorRepository).deleteById(doctorIdToDelete);

        mockMvc.perform(delete("/api/doctors/{id}", doctorIdToDelete)).andExpect(status().isOk());

        verify(doctorRepository, times(1)).deleteById(doctorIdToDelete);
    }

    @Test
    void shouldNotDeleteDoctor() throws Exception {
        long doctorIdToDelete = 3;
        Optional<Doctor> emptyDoctorOptional = Optional.empty();
        when(doctorRepository.findById(doctorIdToDelete)).thenReturn(emptyDoctorOptional);

        mockMvc.perform(delete("/api/doctors/{id}", doctorIdToDelete)).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllDoctors() throws Exception {
        doNothing().when(doctorRepository).deleteAll();

        mockMvc.perform(delete("/api/doctors")).andExpect(status().isOk());

        verify(doctorRepository, times(1)).deleteAll();
    }
}


@WebMvcTest(PatientController.class)
class PatientControllerUnitTest{

    @MockBean
    private PatientRepository patientRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPatientCreate() throws Exception {
        Patient patient = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");

        String content = objectMapper.writeValueAsString(patient);
        mockMvc.perform(post("/api/patient").contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetPatients() throws Exception {
        Patient patient  = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");
        Patient patient1 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");
        List<Patient> patients = new ArrayList<Patient>();

        patients.add(patient);
        patients.add(patient1);

        String contentExpected = objectMapper.writeValueAsString(patients);

        when(patientRepository.findAll()).thenReturn(patients);
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(content().json(contentExpected));
    }

    @Test
    void shouldNoGetPatients() throws Exception {
        List<Patient> patients = new ArrayList<>();
        when(patientRepository.findAll()).thenReturn(patients);
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isNoContent());

    }

    @Test
    void shouldGetPatientById() throws  Exception {
        Patient patient = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");
        Optional<Patient> patientOptional = Optional.of(patient);

        String contentPatient = objectMapper.writeValueAsString(patient);
        when(patientRepository.findById(patient.getId())).thenReturn(patientOptional);
        mockMvc.perform(get("/api/patients/{id}", patient.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentPatient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jose Luis"))
                .andExpect(jsonPath("$.lastName").value("Olaya"))
                .andExpect(jsonPath("$.age").value(37))
                .andExpect(jsonPath("$.email").value("j.olaya@email.com"));
    }

    @Test
    void shouldNoGetAnyPatientById() throws Exception {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());
        long id = 8;
        mockMvc.perform(get("/api/patients/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePatientById() throws Exception {
        long patientIdToDelete = 3;
        Patient patient = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");
        Optional<Patient> optionalPatient = Optional.of(patient);
        assertThat(optionalPatient).isPresent();
        when(patientRepository.findById(patientIdToDelete)).thenReturn(optionalPatient);
        doNothing().when(patientRepository).deleteById(patientIdToDelete);

        mockMvc.perform(delete("/api/patients/{id}",patientIdToDelete)).andExpect(status().isOk());

        verify(patientRepository, times(1)).deleteById(patientIdToDelete);
    }

    @Test
   void shouldNoDeletePatientById() throws Exception {
        long patientIdToDelete = 8;
        Optional<Patient> emptyPatientOptional = Optional.empty();
        when(patientRepository.findById(patientIdToDelete)).thenReturn(emptyPatientOptional);

        mockMvc.perform(delete("/api/patients/{id}", patientIdToDelete)).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllDoctors() throws Exception {
        doNothing().when(patientRepository).deleteAll();

        mockMvc.perform(delete("/api/patients")).andExpect(status().isOk());

        verify(patientRepository, times(1)).deleteAll();
    }
}

@WebMvcTest(RoomController.class)
class RoomControllerUnitTest{

    @MockBean
    private RoomRepository roomRepository;

    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateRoom() throws Exception {
        Room room = new Room("Gynecology");

        String content = objectMapper.writeValueAsString(room);
        mockMvc.perform(post("/api/room").contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetRooms() throws Exception {

        Room room = new Room("Gynecology");
        Room room2 = new Room("Dermatology");
        List<Room> rooms = new ArrayList<Room>();

        rooms.add(room);
        rooms.add(room2);

        String contentExpected = objectMapper.writeValueAsString(rooms);

        when(roomRepository.findAll()).thenReturn(rooms);
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().json(contentExpected));
    }

    @Test
    void shouldNoGetRooms() throws Exception {
        
        List<Room> rooms = new ArrayList<>();
        when(roomRepository.findAll()).thenReturn(rooms);
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetRoom() throws Exception {
        
        Room room = new Room("Gynecology");
        Optional<Room> roomOptional = Optional.of(room);

        String contentRoom = objectMapper.writeValueAsString(room);
        when(roomRepository.findByRoomName(room.getRoomName())).thenReturn(roomOptional);
        mockMvc.perform(get("/api/rooms/{roomName}", room.getRoomName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentRoom))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomName").value("Gynecology"));
    }

    @Test
    void shouldNoGetAnyPatientById() throws Exception {
        when(roomRepository.findByRoomName(any())).thenReturn(Optional.empty());
        String roomName = "Gynecology";
        mockMvc.perform(get("/api/rooms/{roomName}", roomName)).andExpect(status().isNotFound());
    }

    @Test
     void shouldDeleteRoom() throws Exception {

        Room room = new Room("Gynecology");
        Optional<Room> optionalRoom = Optional.of(room);
        assertThat(optionalRoom).isPresent();
        when(roomRepository.findByRoomName(room.getRoomName())).thenReturn(optionalRoom);
        doNothing().when(roomRepository).deleteByRoomName(room.getRoomName());

        mockMvc.perform(delete("/api/rooms/{roomName}",room.getRoomName())).andExpect(status().isOk());

        verify(roomRepository, times(1)).deleteByRoomName(room.getRoomName());
    }

    @Test
    void shouldNotDeleteRoom() throws Exception {
        String roomNameToDelete = "Gynecology";
        Optional<Room> emptyRoomOptional = Optional.empty();
        when(roomRepository.findByRoomName(roomNameToDelete)).thenReturn(emptyRoomOptional);

        mockMvc.perform(delete("/api/rooms/{roomName}", roomNameToDelete)).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAllRooms() throws Exception {
        doNothing().when(roomRepository).deleteAll();

        mockMvc.perform(delete("/api/rooms")).andExpect(status().isOk());

        verify(roomRepository, times(1)).deleteAll();
    }
}
