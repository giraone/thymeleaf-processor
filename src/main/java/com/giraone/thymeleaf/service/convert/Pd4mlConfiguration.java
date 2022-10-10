package com.giraone.thymeleaf.service.convert;

public class Pd4mlConfiguration {

    /** Log level for PD4ML. Default = 5. Higher number means more detailed logs. */
    private int logLevel = 5;

    public Pd4mlConfiguration() {
    }

    public Pd4mlConfiguration(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }
}
