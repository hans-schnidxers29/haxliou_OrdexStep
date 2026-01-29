package com.example.demo.entidad;

import com.example.demo.entidad.Enum.RolEmpresa;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario_empresa")
public class UsuarioEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    private RolEmpresa rol ;

    public UsuarioEmpresa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public RolEmpresa getRol() {
        return rol;
    }

    public void setRol(RolEmpresa rol) {
        this.rol = rol;
    }
}


