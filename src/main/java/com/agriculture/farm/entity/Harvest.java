package com.agriculture.farm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "harvests")
@Getter @Setter @NoArgsConstructor
public class Harvest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate harvestDate;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;
    @Column(length = 20)
    private String unit;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pricePerUnit;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal totalRevenue;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "crop_id")
    private Crop crop;
}
