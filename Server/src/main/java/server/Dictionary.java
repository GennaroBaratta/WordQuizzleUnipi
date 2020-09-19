package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Dictionary {

    List<String> allwords;

    public Dictionary() {
        try {
            allwords = Files.readAllLines(Path.of("dictionary"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getWordsToTranslate(int toChose) {
        LinkedList<String> wordsToTranslate = new LinkedList<>();
        Random rand = new Random();
        int chosen = 0;
        do {
            int index = rand.nextInt(allwords.size());
            String word = allwords.get(index);
            if (!wordsToTranslate.contains(word)) {
                wordsToTranslate.add(word);
                chosen++;
            }
        } while (chosen < toChose);
        return wordsToTranslate;
    }

    public static void main(String[] args) throws IOException {
        Dictionary dic = new Dictionary();
        System.out.println(dic.getWordsToTranslate(4));

    }
}
