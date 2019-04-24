package musicbox;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

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

    public Accepter(List<Client> list) {
        this.list = list;
        
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
                c.start();
                list.add(c);
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
    private MidiConverter converter;
    
    public Client(Socket s, List<Music> musics) throws IOException {
        this.s = s;
        this.musics = musics;
        out = new PrintWriter(s.getOutputStream());
        in = new Scanner(s.getInputStream());
        converter = new MidiConverter();
    }
   
    @Override
    public void run() {
        try{
        while(true) {
            out.println("Give your order");
            out.flush();
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
                        play(Long.parseLong(str[1]), Integer.parseInt(str[2]), str[3]);
                    }catch(NumberFormatException | InterruptedException e) {
                        System.out.println("Playing Error");
                        out.println("Playing Error!");
                        out.flush();
                    }
                    break;
                case "change":
                    break;
                case "stop":
                    break;
            }
        }
        }catch(Exception e){
            //e.printStackTrace();
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
    
    public void play(long tempo, int transponate, String title ) throws InterruptedException {
        out.println("Playing");
        out.flush();
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
        ListIterator<String> iterator = lyrics.listIterator();
        int i = 0;
        while( i < sound.size() ) {
            playSound(sound.get(i),tempo,transponate,iterator,sound);
            i++;
        }
        out.println("FIN");
        out.flush();
    }
    
    private void playSound(Part sound, long tempo, int transponate, ListIterator<String> iterator, List<Part> soundList) throws InterruptedException {
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
    
}

