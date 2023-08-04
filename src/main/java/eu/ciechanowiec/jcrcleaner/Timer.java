package eu.ciechanowiec.jcrcleaner;

final class Timer {

    private final long startNanoTime;

    private Timer() {
        startNanoTime = System.nanoTime();
    }

    static Timer start() {
        return new Timer();
    }

    String renderedTimeFromStart() {
        long nowNanoTime = System.nanoTime();
        return toReadableDuration(startNanoTime, nowNanoTime);
    }

    @SuppressWarnings("MagicNumber")
    private String toReadableDuration(long startNanoTime, long endNanoTime) {
        long duration = endNanoTime - startNanoTime;

        // convert nanoseconds to other units
        long millis = duration / 1000000;
        long seconds = millis / 1000;
        long minutes = seconds / 60;

        // convert back for the remaining part
        seconds = seconds % 60;
        millis = millis % 1000;

        return String.format("%d min, %d sec, %d millis", minutes, seconds, millis);
    }
}
