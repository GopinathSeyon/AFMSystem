package com.agriculture.farm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "crops")
@Getter @Setter @NoArgsConstructor
public class Crop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 100)
    private String variety;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    @Column(precision = 14, scale = 2)
    private BigDecimal plannedQuantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CropStatus status = CropStatus.PLANNED;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "field_id")
    private Field field;
    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Harvest> harvests = new ArrayList<>();
}
