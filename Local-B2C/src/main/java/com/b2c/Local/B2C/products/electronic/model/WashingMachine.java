package com.b2c.Local.B2C.products.electronic.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "washing_machine")
public class WashingMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "washing_machine_id")
    private Long washingMachineId;

    private String model;

    private String brand;

    private boolean dryer;

    private String functionType;

    private double capacityInKg;

    private double powerInStar;

    private boolean timer;

    private String colour;

    private String warranty;

    private boolean digitalDisplay;

    private boolean childLock;

    private boolean shockProof;

    private double weight;

    private double discountPercentage;

    private String availability;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonBackReference
    @JoinColumn(name = "local_store_id", referencedColumnName = "id")
    private LocalStore localStore;
}