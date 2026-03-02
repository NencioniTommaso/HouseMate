package com.housemate.backend.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "unavailabilities")
@Getter
@Setter
@NoArgsConstructor
public class Unavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public Unavailability(LocalDate startDate, LocalDate endDate, User user) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }
}