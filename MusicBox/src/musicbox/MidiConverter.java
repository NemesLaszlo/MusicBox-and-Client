package musicbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class MidiConverter {
    private Map<String, Integer> baseNotes;
    private int octave = 12;
    private int smallB = -1;
    private int hashmark = 1;
    Random rand;

    MidiConverter() {
        baseNotes = new HashMap<String, Integer>() {{
            put("C", 60);
            put("C#/Db", 61);
            put("D", 62);
            put("D#/Eb", 63);
            put("E", 64);
            put("F", 65);
            put("F#/Gb", 66);
            put("G", 67);
            put("G#/Ab", 68);
            put("A", 69);
            put("A#/Bb", 70);
            put("B", 71);
        }};
        rand = new Random();
    }

     public int convertNoteToMidiValue(String note) {
        int value = 0;
        String[] noteParts = note.split("/");

        if (!"CDEFGAB".contains(Character.toString(noteParts[0].charAt(0)).toUpperCase())) {
            throw new IllegalArgumentException("Invalid music note!");
        }

        value += baseNotes.get(Character.toString(noteParts[0].charAt(0)));
        if (noteParts[0].length() > 1) {
            if (noteParts[0].charAt(1) == '#') {
                value += hashmark;
            } else if (noteParts[0].charAt(1) == 'b') {
                value += smallB;
            }
        }
        if (noteParts.length > 1) {
            value += Integer.parseInt(noteParts[1]) * octave;
        }
        return value;
    }


    /**
     * Converts Midi values to notes.
     *
     * @param value        Integer value of the note.
     * @param preferHigher True to prefer 'is notes, false to not.
     *                     
     * @return String representation of the note.
     */
    public String convertMidiValueToNote(int value, boolean preferHigher) {
        String baseNote;
        Optional<String> note = baseNotes.keySet().stream().filter(x -> baseNotes.get(x) == ((value % 12) + 60)).findFirst();
        if (note.isPresent()) {
            baseNote = note.get();
            if (baseNote.contains("/")) {
                baseNote = baseNote.split("/")[preferHigher ? 0 : 1];
            }
        } else {
            throw new IllegalArgumentException("Should never occur...");
        }
        String midi = baseNote;


        int octave = (value / 12) - 5;
        if (octave != 0)
            midi += String.format("/%d", octave);
        return midi;
    }
}
