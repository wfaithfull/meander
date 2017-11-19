package uk.ac.bangor.meander.streams;

import lombok.Data;

/**
 * Created by wfaithfull on 19/11/17.
 */
@Data
public class Pair<K, V> {

    private final K element0;
    private final V element1;

    public static <K, V> Pair<K, V> of(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

}