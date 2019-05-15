package musicbox;

abstract class Input {
}

class Repeat extends Input {
    public int previous;
    public int times;

    public Repeat(int previous, int times) {
        this.previous = previous;
        this.times = times;
    }
}

class Sound extends Input {
    public String note;
    public int length;

    public Sound(String note, int length) {
        this.note = note;
        this.length = length;
    }

}