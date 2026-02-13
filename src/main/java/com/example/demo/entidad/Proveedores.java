package com.example.demo.entidad;

import com.example.demo.multitenancy.TenantAware;
import com.example.demo.multitenancy.TenantEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "proveedores")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Filter(name = "tenantFilter", condition = "empresa_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class Proveedores implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "el nombre es obligatorio")
    private String nombre;

    @Column(name = "razon_social")
    private String razonSocial;

    @Size(min = 10, max = 10)
    @NotBlank(message = "el telefono es obligatorio")
    private String telefono;

    private String direccion;

    @Email
    @NotBlank(message = "el email es obligatorio")
    private String email;

    @Column(name = "tipo_documento")
    @NotBlank(message = "el tipo de documento es obligatorio")
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false)
    @NotBlank(message = "el numero de Documento es Obligatorio")
    private String numeroDocumento;

    // Cómo se guarda el boolean:
    @Column(name = "estado")
    private boolean estado = true;

    @ManyToOne
    @JoinColumn(name = "empresa_id", unique = true)
    private Empresa empresa ;

    public Proveedores() {
    }

    public Proveedores(Long id, String nombre, String razonSocial, String telefono,
                       String direccion, String email, String tipoDocumento, String numeroDocumento, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.direccion = direccion;
        this.email = email;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
