package com.studyroom.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudyRoomBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyRoomBookingApplication.class, args);
    }
}