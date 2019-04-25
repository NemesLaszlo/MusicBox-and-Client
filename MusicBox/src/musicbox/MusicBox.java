package musicbox;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicBox {

    public static void main(String[] args) {

        List<Client> clients = new ArrayList<>();
        Accepter accepter = new Accepter(clients);
        accepter.start();

    }
}

class Accepter extends Thread {
    public ServerSocket ss;
    public List<Client> list;
    public List<Music> musics = new ArrayList<>();
    public static AtomicInteger counter = new AtomicInteger(0);
    public HashMap<Integer,ArrayList<Integer>> nowPlaying;

    public Accepter(List<Client> list) {
        this.list = list;
        this.nowPlaying = new HashMap<>();
        
    }

    @Override
    public void run() {
        System.out.println("Starting accepter");
        try{
            this.ss = new ServerSocket(40000);
            while (true) {
                Socket s = ss.accept();
                System.out.println(s.getRemoteSocketAddress());
                Client c = new Client(s,musics);
                c.nowPlaying = this.nowPlaying;
                c.start();
                synchronized(list) {
                    list.add(c);   
                }

            }
        } catch(Exception e) {
            System.out.println("Connection Error");
        } 
    }
}

class Client extends Thread {
    private Socket s;
    private PrintWriter out;
    private Scanner in;
    private List<Music> musics;
    public HashMap<Integer,ArrayList<Integer>> nowPlaying;
    
    public Client(Socket s, List<Music> musics) throws IOException {
        this.s = s;
        this.musics = musics;
        out = new PrintWriter(s.getOutputStream());
        in = new Scanner(s.getInputStream());
    }
   
    @Override
    public void run() {
        while(in.hasNextLine()) {
            String line = in.nextLine();
            String[] str = line.split(" ");
            switch(str[0]) {
                case "add":
                    add(str[1]);
                    break;
                case "addlyrics":
                    addlyrics(str[1]);
                    break;
                case "play":
                    try {
                        play(Integer.parseInt(str[1]), Integer.parseInt(str[2]), str[3]);
                    }catch(NumberFormatException | InterruptedException e) {
                        System.out.println("Playing Error");
                        out.println("Playing Error!");
                        out.flush();
                    }
                    break;
                case "change":
                    synchronized(nowPlaying){
                        int ind = Integer.parseInt(str[1]);
                        nowPlaying.get(ind).set(0, Integer.parseInt(str[2]));
                        if(str.length > 3){
                            nowPlaying.get(ind).set(1, Integer.parseInt(str[3]));
                        }
                    }
                    break;
                case "stop":
                    synchronized(nowPlaying){
                        int ind = Integer.parseInt(str[1]);
                        nowPlaying.get(ind).set(2, -1);
                    }
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
        System.out.println("Client disconnected");
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void close() throws Exception {
        out.close();
        in.close();
        s.close();
    }
    
    public void addToList(Music music ) {
        synchronized(musics) {
            musics.add(music);
        }
    }
    
    public void add(String title) {
        for(Music x : musics) {
            if(x.getTitle().equals(title)){
                out.println("This song is already exist!");
                out.flush();
            }
        }
        Music newMusic = new Music(title, musics.size());
        out.println("Give the sounds:");
        out.flush();
        ArrayList<Part> inputs = new ArrayList<>();
        String[] input = in.nextLine().split(" ");
        int i = 0;
        while(i<input.length){
            String note = input[i++];
            String tmp = input[i++];
            if(tmp.contains(";")){
                Repeat rep = new Repeat(Integer.parseInt(tmp.split(";")[0]), Integer.parseInt(tmp.split(";")[1]));
                inputs.add(rep);
            } else {
                Sound sound = new Sound(note, Integer.parseInt(tmp));
                inputs.add(sound);
            }
        }
        newMusic.setSounds(inputs);
        addToList(newMusic);
        System.out.println("New Song: " + title);
        System.out.println("Sounds added for song: " + title);
    }
    
    public void addlyrics(String title) {
        out.println("Give the lyrics parts:");
        out.flush();
        String[] tmp = in.nextLine().split(" ");
        List<String> list = Arrays.asList(tmp);
        for(Music x : musics) {
            if(x.getTitle().equals(title)) {
                x.setLyrics(list);
            }
        }
        System.out.println("Lyrics added for song: " + title);
    }
    
    public void play(int tempo, int transponate, String title ) throws InterruptedException {
        List<String> lyrics = null;
        List<Part> sound = null;
        for(Music x : musics) {
            if(!x.getTitle().equals(title)) {
                out.println("FIN");
                out.flush();
                break;
            }else {
                lyrics = x.getLyrics();
                sound = x.getSounds();
            }
        }
        
        int j = Accepter.counter.incrementAndGet();
        out.println("Playing " + j);
        out.flush();
        synchronized(nowPlaying){
            ArrayList<Integer> list = new ArrayList<>();
            list.add(tempo);
            list.add(transponate);
            list.add(0);
            nowPlaying.put(j, list);
            System.out.println(nowPlaying.get(j).get(0) + " "+ nowPlaying.get(j).get(1) + " "+nowPlaying.get(j).get(2) + " ");
        }
        ListIterator<String> iterator = lyrics.listIterator();
        for(Part x : sound){
            if(nowPlaying.get(j).get(2) == -1){
                break;
            }
            playSound(x,nowPlaying.get(j).get(0),nowPlaying.get(j).get(1),iterator,sound,j);
        }
        out.println("FIN");
        out.flush();
    }
    
    private void playSound(Part sound, int tempo, int transponate, ListIterator<String> iterator, List<Part> soundList, int num) throws InterruptedException {
        if(sound instanceof Repeat){
            int ind = soundList.indexOf(sound);
            for(int i = rep(((Repeat) sound).getPrevious(),ind,soundList); i < ((Repeat) sound).getPrevious() ; ++i){
                playSound(soundList.get(i), tempo,transponate, iterator,soundList,num);
            }
        } else {
            sleep(tempo * ((Sound) sound).getLength());
            if(nowPlaying.get(num).get(2) == -1){
                return;
            }
            String note = transpone(((Sound) sound).getNote(), nowPlaying.get(num).get(1));
            out.print(note);
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
    
    private String transpone(String baseNote, int trans){
        int[] note = { 69, 71, 60, 62, 64, 65, 67 };
        int nval = -1;
        String[] tmp = baseNote.split(" ");

        int c = tmp[0].charAt(0) - 65;
        if(c > -1 && c < 7){
            nval = note[c];
            if(tmp[0].indexOf('#') != -1){
                nval++;
            } else if (tmp[0].indexOf('b') != -1){
                nval--;
            }
            int ind = tmp[0].indexOf('/');
            if(ind != -1){
                nval += 12 * Character.getNumericValue(tmp[0].charAt(ind+1));
            }
            nval += trans;
            return convertMidiValueToNote(nval, true);
        }
        return "";
    }

    String convertMidiValueToNote(int value, boolean preferHigher) {
        HashMap<String, Integer> baseNotes = new HashMap<String, Integer>() {{
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

