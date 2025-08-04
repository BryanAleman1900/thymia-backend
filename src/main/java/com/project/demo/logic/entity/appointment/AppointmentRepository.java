package com.project.demo.logic.entity.appointment;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE " +
            "(a.startTime BETWEEN :start AND :end) OR " +
            "(a.endTime BETWEEN :start AND :end) OR " +
            "(a.startTime <= :start AND a.endTime >= :end)")
    List<Appointment> findByStartTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT a FROM Appointment a JOIN a.guests g WHERE g.id IN :userIds")
    List<Appointment> findAllByGuestIds(@Param("userIds") Set<Long> userIds);



    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    //Luis agrego de aqui
    @Query("""
  SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
  FROM Appointment a
  LEFT JOIN a.guests g
  WHERE a.id = :appointmentId AND (
      a.patient.id = :userId OR
      a.doctor.id = :userId OR
      g.id = :userId
  )
""")
    boolean existsByIdAndParticipant(@Param("appointmentId") Long appointmentId,
                                     @Param("userId") Long userId);

    @Query("""
  SELECT a FROM Appointment a
  LEFT JOIN a.guests g
  WHERE (a.patient.id = :userId OR a.doctor.id = :userId OR g.id = :userId)
    AND (:start IS NULL OR a.startTime >= :start)
    AND (:end   IS NULL OR a.endTime   <= :end)
""")
    Page<Appointment> findAllVisibleTo(@Param("userId") Long userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);

    //hasta aqui
}
