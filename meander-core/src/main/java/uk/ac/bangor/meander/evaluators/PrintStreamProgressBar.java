package uk.ac.bangor.meander.evaluators;


import java.io.PrintStream;

/**
 * @author Will Faithfull
 */
public class PrintStreamProgressBar implements ProgressReporter {

    final char[] SPINNER_CHARS = {'|', '/', '-', '\\'};
    int spindex = 0;

    private char character;
    private int width;
    private PrintStream printStream;
    private String lastMessage = "";

    public PrintStreamProgressBar(char character, int width) {
        this(character, width, System.out);
    }

    public PrintStreamProgressBar(char character, int width, PrintStream printStream) {
        this.character = character;
        this.width = width;
        this.printStream = printStream;
    }

    private StringBuilder builder = new StringBuilder(width);

    @Override
    public void update(long progress) {
        update(progress, lastMessage);
    }

    @Override
    public void update(long progress, String message) {
        update(progress, -1, message);
    }

    public void update(long progress, long total) {
        update(progress, total, lastMessage);
    }

    public char getSpinner() {
        if(spindex == SPINNER_CHARS.length)
            spindex = 0;

        return SPINNER_CHARS[spindex++];
    }

    private long lastTotal;

    public void update(long progress, long total, String message) {
        char spinner = getSpinner();

        if(total > 0) {
            long percent = (++progress * 100) / total;
            long extrachars = (int) ((percent / 2L) - this.builder.length());

            while (extrachars-- > 0) {
                builder.append(character);
            }

            String progressBar = String.format("(%c) %3d%% [%-" + width + "s] %s", spinner, percent, builder, message);
            printStream.printf("\r(%c) %3d%% [%-" + width + "s] %s", spinner, percent, builder, message);

            if (lastTotal != total) {
                reset();
            }

            if (progress == total) {
                printStream.flush();
                printStream.println();
                builder = new StringBuilder(width);
            }

            lastTotal = total;
        } else {
            printStream.printf("\r(%c) [%d] %s ", spinner, progress, message);
        }

        lastMessage = message;
    }

    public void reset() {
        printStream.flush();
        builder = new StringBuilder(width);
    }
}
