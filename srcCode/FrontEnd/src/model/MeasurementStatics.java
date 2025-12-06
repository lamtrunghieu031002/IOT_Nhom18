package model;

public class MeasurementStatics {
    public boolean success;
    public String message;
    public Data data;

    public static class Data {
        public int totalTests;
        public int violations;
        public double averageLevel;
        public ByLevel byLevel;
    }

    public static class ByLevel {
        public int high;
        public int low;
        public int none;
    }
}
