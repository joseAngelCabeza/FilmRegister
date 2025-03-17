package logica;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="peliculas")

public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "Titulo")
    private String titulo;
    @Column(name = "Duracion")
    private Integer duracion;
    @Column(name = "Genero")
    private String genero;
    @Column(name = "Director")
    private String director;
    @Column(name = "Clasificacion")
    private String clasificacion; // Ejemplo: "PG-13", "R", etc.
    @Column(name = "Sinopsis", length = 500)
    private String sinopsis;
    @Column(name = "FechaEstreno")
    private LocalDate fechaEstreno;
    @Column(name = "Pais")
    private String pais;
    @Column(name = "Idioma")
    private String idioma;
    @Lob // Indica que es un objeto grande (Large Object)
    @Column(name = "Imagen", columnDefinition = "LONGBLOB")
    private byte[] Imagen;// Ruta o URL de la imagen del póster
    @Column(name = "Disponible")
    private Boolean disponible; // Indica si la película está disponible para reservar
    @Column(name = "PrecioReserva")
    private BigDecimal precioReserva; // Precio de reserva

    public Pelicula() {
    }

    public Pelicula(Integer id, String titulo, Integer duracion, String genero, String director, String clasificacion, String sinopsis, LocalDate fechaEstreno, String pais, String idioma, byte[] imagen, Boolean disponible, BigDecimal precioReserva) {
        this.id = id;
        this.titulo = titulo;
        this.duracion = duracion;
        this.genero = genero;
        this.director = director;
        this.clasificacion = clasificacion;
        this.sinopsis = sinopsis;
        this.fechaEstreno = fechaEstreno;
        this.pais = pais;
        this.idioma = idioma;
        Imagen = imagen;
        this.disponible = disponible;
        this.precioReserva = precioReserva;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public LocalDate getFechaEstreno() {
        return fechaEstreno;
    }

    public void setFechaEstreno(LocalDate fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public byte[] getImagen() {
        return Imagen;
    }

    public void setImagen(byte[] imagen) {
        Imagen = imagen;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public BigDecimal getPrecioReserva() {
        return precioReserva;
    }

    public void setPrecioReserva(BigDecimal precioReserva) {
        this.precioReserva = precioReserva;
    }
}
