package uk.ac.bangor.meander.evaluators;

/**
 * @author Will Faithfull
 */
public abstract class AbstractProgressReporter implements ProgressReporter {

    protected String lastMessage;

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

}
