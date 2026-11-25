package tn.esprit.candidatservice.candidat.dto;

public class JobDTO {

    private Long id;
    private String name;
    private boolean available;

    // Constructeur sans paramètres
    public JobDTO() {
    }

    // Constructeur avec paramètres
    public JobDTO(Long id, String name, boolean available) {
        this.id = id;
        this.name = name;
        this.available = available;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}