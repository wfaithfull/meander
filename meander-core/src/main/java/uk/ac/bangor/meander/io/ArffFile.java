package uk.ac.bangor.meander.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * @author Will Faithfull
 */
public class ArffFile {

    public ArffFile(String path) throws FileNotFoundException {
        Reader reader = new FileReader(new File(path));
        ArffParser parser = new ArffParser(reader);
    }

    ArffFile() {}
}
