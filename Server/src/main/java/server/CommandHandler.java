package server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.StringTokenizer;

class CommandHandler implements Runnable {
    private final String request;
    private final SelectionKey key;

    public CommandHandler(SelectionKey key, ByteBuffer[] bfs) {
        request = new String(bfs[1].array()).trim();
        this.key = key;
    }

    @Override
    public void run() {
        System.out.println("Dentro handler");
        System.out.println(request);
        StringTokenizer tokenizer = new StringTokenizer(request);

        String command = tokenizer.nextToken();
        String reply;
        String username, friendname;
        switch (command) {
            case "LOGIN":
                username = tokenizer.nextToken();
                String password = tokenizer.nextToken();
                int port = Integer.parseInt(tokenizer.nextToken());
                reply = Database.getInstance().login(username, password, key, port);
                key.attach(reply);
                break;
            case "LOGOUT":
                username = tokenizer.nextToken();
                reply = Database.getInstance().logout(username);
                key.attach(reply);
                break;
            case "ADD_FRIEND":
                username = tokenizer.nextToken();
                friendname = tokenizer.nextToken();
                reply = Database.getInstance().add_friend(username, friendname, key);
                key.attach(reply);
                break;
            case "FRIEND_LIST":
                username = tokenizer.nextToken();
                reply = Database.getInstance().friend_list(username, key);
                key.attach(reply);
                break;
            case "CHALLENGE":
                username = tokenizer.nextToken();
                friendname = tokenizer.nextToken();
                reply = Database.getInstance().challenge(username, friendname, key);
                key.attach(reply);
                break;
            case "WORD":
                String word;
                try {
                    word = tokenizer.nextToken();
                } catch (Exception e) {
                    word = null;
                }
                reply = Database.getInstance().word(word, key);
                key.attach(reply);
                break;
            case "SCORE":
                username = tokenizer.nextToken();
                reply = Database.getInstance().getScore(username, key);
                key.attach(reply);
                break;
            case "RANK":
                username = tokenizer.nextToken();
                reply = Database.getInstance().getRank(username);
                key.attach(reply);
                break;
            default:
                reply = "Comando non valido";
                key.attach(reply);
                break;
        }

    }
}