package musicbox;
/*
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.List;
import java.util.ListIterator;

public class Play {
    
    private final MidiConverter converter;
    private final Socket s;
    private final PrintWriter out;
    private static int id = 0;
    
    public Play(Socket s) throws IOException {
        this.s = s;
        out = new PrintWriter(s.getOutputStream());
        converter = new MidiConverter();
        ++id;
    }

    public int getId() {
        return id;
    }
    
    public void playSound(Part sound, long tempo, int transponate, ListIterator<String> iterator, List<Part> soundList) throws InterruptedException {
        if(sound instanceof Repeat){
            int ind = soundList.indexOf(sound);
            for(int i = rep(((Repeat) sound).getPrevious(),ind,soundList); i < ((Repeat) sound).getPrevious() ; ++i){
                playSound(soundList.get(i), tempo,transponate, iterator,soundList);
            }
        } else {
            sleep(tempo * ((Sound) sound).getLength());
            String note = ((Sound) sound).getNote();
            int transponated = converter.convertNoteToMidiValue(note) + transponate;
            String transponatedSound = converter.convertMidiValueToNote(transponated, true);
            out.print(transponatedSound);
            if(!((Sound) sound).getNote().equals("R")){
                String lyrics = iterator.hasNext() ? iterator.next() : "???";
                out.print(" " + lyrics);
            }
            out.println();
            out.flush();
        }
    }
    
    private int rep(int prev, int ind, List<Part> soundList){
        int k = prev;
        for(int j = ind; j>0; --j){
            if(soundList.get(j) instanceof Sound && !((Sound) soundList.get(j)).getNote().equals("R")){
                --k;
            }
            if(k==0){
                return j;
            }
        }
        return 0;
    }
    
}*/
