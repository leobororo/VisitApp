package com.leandrobororo.visitapp.model;

import java.io.Serializable;

/**
 * Created by leandrobororo on 05/03/17.
 */

public class Amigo implements Serializable {

    private String id;
    private String nome;
    private Visita visita;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Visita getVisita() {
        return visita;
    }

    public void setVisita(Visita visita) {
        this.visita = visita;
    }
}
