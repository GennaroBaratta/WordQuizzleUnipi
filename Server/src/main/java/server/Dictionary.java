package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Dictionary {

    public static List<String> getWordsToTranslate(int toChose) {
        AtomicInteger chosen= new AtomicInteger();
        LinkedList<String> wordsToTranslate = new LinkedList<>();
        try {
            Stream<String> stream = Files.lines(Path.of("dictionary"));
            while(chosen.get() <toChose){
                stream.forEach(s -> {
                    if(chosen.get() <toChose){
                        if(Math.round(Math.random()-0.2) == 0){
                            chosen.getAndIncrement();
                            wordsToTranslate.add(s);
                        }
                    }
                });
            }

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordsToTranslate;
    }

    public static LinkedList<String> getWordsToTranslateBase(int toChose) throws IOException {
        LinkedList<String> wordsToTranslate = new LinkedList<>();
        FileInputStream inputStream = null;
        Scanner sc = null;
        int chosen=0;
        try {
            inputStream = new FileInputStream("dictionary");
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            while(chosen <toChose) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (chosen < toChose) {
                        if (Math.round(Math.random() + 0.2) == 0) {
                            chosen++;
                            wordsToTranslate.add(line);
                        }
                    }
                    // System.out.println(line);
                }
                sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        return wordsToTranslate;
    }

    public static void main(String[] args) throws IOException {

            System.out.println(getWordsToTranslate(4));

    }
}
