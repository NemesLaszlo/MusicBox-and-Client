package musicbox;

import java.util.ArrayList;
import java.util.List;

abstract class Part {

}

public class Music {
    
    private int id;
    private String title;
    private List<Part> sounds;
    private List<String> lyrics;

    public Music(String title, int id) {
        this.title = title;
        sounds = new ArrayList<>();
        lyrics = new ArrayList<>();
        this.id = id;
    }

    public int getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }

    public List<Part> getSounds() {
        return sounds;
    }

    public List<String> getLyrics() {
        return lyrics;
    }

    public void setSounds(List<Part> sounds) {
        this.sounds = sounds;
    }

    public void setLyrics(List<String> lyrics) {
        this.lyrics = lyrics;
    }    
}

class Repeat extends Part{
    private int previous;
    private int times;

    public Repeat(int previous, int times){
        this.previous = previous;
        this.times = times;
    }

    public int getPrevious() {
        return previous;
    }

    public int getTimes() {
        return times;
    }

    public void setPrevious(int previous) {
        this.previous = previous;
    }

    public void setTimes(int times) {
        this.times = times;
    }
    
    
}

class Sound extends Part{
    private String note;
    private int length;
    
    
    public Sound(String note, int length){
        this.note = note;
        this.length = length;
    }  

    public String getNote() {
        return note;
    }

    public int getLength() {
        return length;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
    
}
