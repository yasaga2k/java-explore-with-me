package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.requester.id = :userId")
    List<ParticipationRequest> findByRequesterId(@Param("userId") Long userId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.event.id = :eventId")
    List<ParticipationRequest> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.requester.id = :userId AND pr.event.id = :eventId")
    Optional<ParticipationRequest> findByRequesterIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = :status")
    List<ParticipationRequest> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.id IN :requestIds")
    List<ParticipationRequest> findByIdIn(@Param("requestIds") List<Long> requestIds);
}