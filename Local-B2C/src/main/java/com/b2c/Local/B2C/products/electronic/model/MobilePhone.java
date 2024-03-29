package com.b2c.Local.B2C.products.electronic.model;

import com.b2c.Local.B2C.store.model.LocalStore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mobile_phone")
@Indexed
public class MobilePhone implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mobile_phone_id")
    private Long mobilePhoneId;

    @FullTextField
    private String model;

    @FullTextField
    private String brand;

    @FullTextField
    private String networkConnectivity;

    private String simType;

    @FullTextField
    private String displayType;

    private String displayResolution;

    @KeywordField
    private String displaySize;

    @FullTextField
    private String os;

    @KeywordField
    private String brandUi;

    @KeywordField
    private String chipset;

    @KeywordField
    private String cpuCore;

    private String cpuClockSpeed;

    private String gpu;

    private boolean memoryCordSlotSupported;

    @FullTextField
    private String internalMemorySize;

    private int mainCameraCount;

    @FullTextField
    private String mainCameraSpecs;

    private boolean frontCamera;

    @KeywordField
    private String frontCameraSpecs;

    @FullTextField
    private String batterySize;

    @KeywordField
    private String chargingType;

    private String chargerOutput;

    @FullTextField
    private String colour;

    private int weightInGrams;

    @GenericField
    private double price;

    private boolean bluetooth;

    private boolean gps;

    private boolean nfc;

    private boolean radio;

    @KeywordField
    private String usbType;

    private boolean audioJack;

    private boolean wlan;

    @FullTextField
    private String availability;

    private double discountPercentage;

    @JsonIgnore
    private Boolean active;

    @FullTextField
    private String warranty;

    @GenericField
    private int quantity;

    @ManyToOne @JsonIgnore  @IndexedEmbedded
    @JoinColumn(name = "local_store_id", referencedColumnName = "id")
    private LocalStore localStore;

    @Transient
    private String url;

    @PostLoad
    public void postLoad(){
        this.url = "http://localhost:8080/products/mobilePhone/"+this.mobilePhoneId+"/getMobilePhoneById";
    }
}
