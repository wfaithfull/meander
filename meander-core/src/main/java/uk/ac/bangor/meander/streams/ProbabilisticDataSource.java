package uk.ac.bangor.meander.streams;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
@Data
@AllArgsConstructor
public class ProbabilisticDataSource implements DataSource {

    double probability;
    Supplier<Double[]> source;

}
