package com.studyroom.booking.repository;

import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.StudyRoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyRoomImageRepository extends JpaRepository<StudyRoomImage, UUID> {

    List<StudyRoomImage> findByRoomOrderByDisplayOrderAscCreatedAtAsc(StudyRoom room);

    List<StudyRoomImage> findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(UUID roomId);

    void deleteByRoom(StudyRoom room);

    void deleteByRoom_Id(UUID roomId);
}