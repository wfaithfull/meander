package uk.ac.bangor.meander.io;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Will Faithfull
 */
@Data
public class ArffAttribute implements Serializable {

    private final Type type;

    public enum Type {
        NUMERIC,
        CATEGORICAL,
        STRING,
        DATE
    }

    private double weight;
    private String name;
    private String description;

    public ArffAttribute(Type type, String name, String description, double weight) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    public ArffAttribute(Type type, String name, double weight) {
        this.type = type;
        this.name = name;
        this.weight = weight;
    }

    public ArffAttribute(Type type, String name) {
        this(type, name, 1.0);
    }

    public ArffAttribute(Type type, String name, String description) {
        this(type, name, description, 1.0);
    }

}
