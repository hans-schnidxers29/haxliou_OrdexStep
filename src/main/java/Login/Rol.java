package Login;

import jakarta.persistence.*;

@Entity
public class Rol{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @OneToOne(mappedBy = "rol")
    private Usuario usuarios;


    public Rol(){}

    public Rol(Usuario usuarios, String nombre) {
        this.usuarios = usuarios;
        this.nombre = nombre;
    }

    public Rol(String nombre){
        this.nombre = nombre;
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

    public Usuario getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Usuario usuarios) {
        this.usuarios = usuarios;
    }
}
