package com.project.demo.logic.entity.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.startTime < :end AND a.endTime > :start")
    List<Appointment> findWindow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.startTime < :end AND a.endTime > :start")
    List<Appointment> findWindowByPatient(@Param("patientId") Long patientId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.startTime < :end AND a.endTime > :start")
    List<Appointment> findWindowByDoctor(@Param("doctorId") Long doctorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.patient.id = :patientId AND a.startTime < :end AND a.endTime > :start")
    List<Appointment> findWindowByDoctorAndPatient(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.startTime < :end AND a.endTime > :start")
    boolean existsOverlapForDoctor(@Param("doctorId") Long doctorId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId AND a.id <> :excludeId " +
            "AND a.startTime < :end AND a.endTime > :start")
    boolean existsOverlapForDoctorExcludingId(@Param("doctorId") Long doctorId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("excludeId") Long excludeId);

    @Query("SELECT DISTINCT a FROM Appointment a JOIN a.guests g WHERE g.id IN :userIds")
    List<Appointment> findAllByGuestIds(@Param("userIds") Set<Long> userIds);


    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
}