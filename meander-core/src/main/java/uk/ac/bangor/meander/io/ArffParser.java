package uk.ac.bangor.meander.io;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * @author Will Faithfull
 */
public class ArffParser {

    static class ArffHeaders {
        static final String RELATION = "@relation";
        static final String ATTRIBUTE = "@attribute";
        static final String DATA = "@data";
        static final String END = "@end";
    }

    static class ArffAttributeTypes {
        static final String INTEGER = "integer";
        static final String REAL = "real";
        static final String NUMERIC = "numeric";
        static final String STRING = "string";
        static final String DATE = "date";
        static final String RELATIONAL = "relational";
    }

    private final StreamTokenizer tokenizer;

    public ArffParser(Reader reader) {
        tokenizer = new StreamTokenizer(new BufferedReader(reader));
        tokenizer.resetSyntax();
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars(' ' + 1, '\u00FF');
        tokenizer.whitespaceChars(',', ',');
        tokenizer.commentChar('%');
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.ordinaryChar('{');
        tokenizer.ordinaryChar('}');
        tokenizer.eolIsSignificant(true);
    }

}
