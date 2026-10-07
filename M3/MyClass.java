package M3;

public class MyClass {
    private final String name;
    private int score;
    private static int objectCount = 0;

    public MyClass(String name) {
        this.name = name;
        objectCount++;
    }

    public void addScore(int amount) {
        if (amount > 0) {
            score += amount;
        }
    }

    public void addScore() {
        addScore(1);
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public static int getObjectCount() {
        return objectCount;
    }

    public static void main(String[] args) {
        MyClass sample = new MyClass("Ada");
        MyClass sample2 = new MyClass("Lin");

        sample.addScore(10);
        sample2.addScore(4);

        System.out.println(sample.getName() + ": " + sample.getScore());
        System.out.println(sample2.getName() + ": " + sample2.getScore());
        System.out.println("Objects: " + MyClass.getObjectCount());
    }
}
