package logica;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "pelicula_id", nullable = false)
    private Pelicula pelicula;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "estado", nullable = false)
    private String estado; // Por ejemplo: "ACTIVA", "FINALIZADA", "CANCELADA"

    @Column(name = "n_fila", nullable = false)
    private int nFila;

    @Column(name = "n_asiento", nullable = false)
    private int nAsiento;

    public Reserva() {
    }

    public Reserva(Integer id, Usuario usuario, Pelicula pelicula, LocalDate fechaReserva, String estado, int nFila, int nAsiento) {
        this.id = id;
        this.usuario = usuario;
        this.pelicula = pelicula;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.nFila = nFila;
        this.nAsiento = nAsiento;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getnFila() {
        return nFila;
    }

    public void setnFila(int nFila) {
        this.nFila = nFila;
    }

    public int getnAsiento() {
        return nAsiento;
    }

    public void setnAsiento(int nAsiento) {
        this.nAsiento = nAsiento;
    }
}