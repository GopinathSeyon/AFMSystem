package com.agriculture.farm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "activities")
@Getter @Setter @NoArgsConstructor
public class Activity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ActivityType activityType;
    @Column(length = 255)
    private String description;
    @Column(nullable = false)
    private LocalDate activityDate;
    @Column(precision = 14, scale = 2)
    private BigDecimal labourCost;
    @Column(precision = 14, scale = 2)
    private BigDecimal quantity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "field_id")
    private Field field;
}
