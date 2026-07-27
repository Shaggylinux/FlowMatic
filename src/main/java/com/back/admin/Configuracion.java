package com.back.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "configuraciones")
public class Configuracion {

    @Id
    @Column(length = 100)
    private String clave;

    @Column(length = 500)
    private String valor;
}
