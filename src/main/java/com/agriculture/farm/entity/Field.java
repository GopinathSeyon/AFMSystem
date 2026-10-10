package com.agriculture.farm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "fields")
@Getter @Setter @NoArgsConstructor
public class Field {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal area;
    @Column(length = 50)
    private String soilType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private FieldStatus status = FieldStatus.AVAILABLE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "farm_id")
    private Farm farm;
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Crop> crops = new ArrayList<>();
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();
}
