package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * server.Database
 */
final public class Database {
    //numero parole per sfida
    final int K=4;
    //porta per UDP 49152–65535
    private final AtomicInteger UDPport = new AtomicInteger(49152);
    private ConcurrentHashMap<String, User> users = null;
    private final ConcurrentHashMap<String, Connection> onlineUsers = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper = null;
    private final Dictionary dictionary = new Dictionary();

    private void merge() throws IOException {
        PrintWriter pw;
        try {
            pw = new PrintWriter("./users.json");
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        final File folder = new File("./users");
        String[] fileNames = folder.list();
        pw.println("{");
        int i;
        if (fileNames != null && fileNames.length > 0) {
            for (i = 0; i < fileNames.length - 1; i++) {
                File f = new File(folder, fileNames[i]);
                BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader(f));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
                String line = br.readLine();
                while (line != null) {
                    pw.println(line);
                    line = br.readLine();
                }
                pw.println(",");
                br.close();
            }

            File f = new File(folder, fileNames[i]);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line = br != null ? br.readLine() : null;
            while (line != null) {
                pw.println(line);
                line = br.readLine();
                br.close();
            }

        }
        pw.println("}");
        pw.flush();
    }

    private Database() {
        try {
            merge();
            File usersJSON = new File("./users.json");
            objectMapper = new ObjectMapper();
            users = objectMapper.readValue(usersJSON, new TypeReference<>() {
            });
        } catch (IOException e) {
            // TODO Auto-generated catch blockBufferedReader br = null;
            e.printStackTrace();
        }
    }

    // Bill Pugh singleton solution
    private static class SingletonHelper {
        private static final Database database = new Database();
    }

    public static Database getInstance() {
        return SingletonHelper.database;
    }

    boolean isRegistered(String name) {
        return users.containsKey(name);
    }

    boolean register(User user) {
        User alreadyExisting = users.putIfAbsent(user.getUsername(), user);
        if (alreadyExisting == null)
            update(user);
        return alreadyExisting == null;
    }

    public String login(String username, String password, SelectionKey key, Integer port) {
        if (port == null)
            return "Porta mancante";
        if (isRegistered(username)) {
            User user = users.get(username);
            if (Password.verifyPassword(password, user.getHash(), user.getSalt())) {
                if (!onlineUsers.containsKey(username)) {
                    Connection conn = new Connection(key, port);
                    Connection conn2 = onlineUsers.put(username, conn);
                    if (conn2 == null) {
                        return "OK";
                    } else {
                        return "Utente già online";
                    }
                }
                return "Utente già online";
            }
            return "Nome utente o password non corretti";
        }
        return "Utente non registrato";
    }

    public String logout(String username) {
        if (isRegistered(username)) {
            SelectionKey key = onlineUsers.remove(username).getKey();
            users.get(username).getChallenge().stop();
            if (key != null) {
                key.cancel();
                try {
                    key.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "OK";
            }
            return "Utente non connesso";
        }
        return "Utente non registrato";
    }

    public String getOnlineUser(SelectionKey key) {
        Iterator<String> iterator = onlineUsers.keySet().iterator();
        String username;
        while (iterator.hasNext()) {
            username = iterator.next();
            if (onlineUsers.get(username).getKey().equals(key)) {
                return username;
            }
        }
        return null;
    }

    public String add_friend(String username, String friendname, SelectionKey key2) {
        SelectionKey key = onlineUsers.get(username).getKey();
        if (key != null) {
            if (key.equals(key2)) {
                User friend = users.get(friendname);
                User user = users.get(username);
                if (friend != null) {
                    if (user.addFriend(friendname)) {
                        friend.addFriend(username);
                        update(user);
                        update(friend);
                        return "OK";
                    }
                    return "Amicizia già esistente";
                }
                return "Utente non esistente";
            }
            return "Non autorizzato";
        }
        return "Utente non connesso";
    }

    public void update(User user) {
        FileWriter fw;
        try {
            fw = new FileWriter(new File("./users/" + user.getUsername()));
            fw.write("\"" + user.getUsername() + "\"" + ":");
            fw.append(objectMapper.writeValueAsString(user));
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String friend_list(String username, SelectionKey key2) {
        SelectionKey key = onlineUsers.get(username).getKey();
        if (key != null) {
            if (key.equals(key2)) {
                User user = users.get(username);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    return objectMapper.writeValueAsString(user.getFriends());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return "Non autorizzato";
        }
        return "Utente non connesso";
    }

    public String getScore(String username, SelectionKey key2) {
        SelectionKey key = onlineUsers.get(username).getKey();
        if (key != null) {
            if (key.equals(key2)) {
                User user = users.get(username);
                return String.valueOf(user.getScore());
            }
            return "Non autorizzato";
        }
        return "Utente non connesso";
    }

    public String getRank(String username) {
        User user = users.get(username);
        if (user != null) {
            Set<String> friends = user.getFriends();
            Map<String, Integer> unsorted = new HashMap<>();
            unsorted.put(username, user.getScore());
            for (String friend : friends) {
                unsorted.put(friend, users.get(friend).getScore());
            }
            Map<String, Integer> rank = unsorted.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors
                            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(rank);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return "Utente non registrato";
    }

    public String challenge(String username, String friendname, SelectionKey key2) {
        SelectionKey key = onlineUsers.get(username).getKey();
        Connection friendConnection = onlineUsers.get(friendname);
        if (key != null && (friendConnection != null)) {
            User user = users.get(username);
            if (user.getChallenge().isStarted()) {
                return "Sfida in corso";
            }
            if (key.equals(key2) && user.getFriends().contains(friendname)) {
                int port = UDPport.getAndAdd(1);
                String name = "LocalHost";
                byte[] buffer = new byte[100];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                try (DatagramSocket server = new DatagramSocket(port)) {
                    server.setSoTimeout(15000);
                    String s = "CHALLENGE " + username;
                    buffer = s.getBytes();
                    DatagramPacket toSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(name),
                            friendConnection.getUDPport());
                    server.send(toSend);
                    server.receive(receivedPacket);
                    UDPport.decrementAndGet();
                    byte[] destBuf = new byte[receivedPacket.getLength()];
                    System.arraycopy(receivedPacket.getData(), receivedPacket.getOffset(), destBuf, 0, destBuf.length);
                    String replay = new String(destBuf);
                    if (replay.startsWith("OK")) {
                        String x="STARTED";
                        toSend.setData(x.getBytes());
                        server.send(toSend);
                        List<String> words = dictionary.getWordsToTranslate(K);
                        user.getChallenge().start(users.get(friendname),words);
                        users.get(friendname).getChallenge().start(user,words);
                    }
                    return replay;
                } catch (SocketTimeoutException e) {
                    return "Sfida non accettata";
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
            return "Non autorizzato";
        }
        return "Utente non connesso";
    }

    public String word(String word, SelectionKey key) {
        User user = users.get(getOnlineUser(key));
        Challenge challenge = user.getChallenge();
        if (challenge != null) {
            String isRight = "";
            if (word != null) {
                if (challenge.addWord(word) == 0) {
                    user.addScore(10);
                    isRight = "Risposta esatta, ";
                } else {
                    user.addScore(-5);
                    isRight = "Risposta sbagliata, ";
                }
            }
            String response = user.getChallenge().nextWordToTranslate();
            if (response.startsWith("Sfida terminata") || response.startsWith("Tempo scaduto")) {
                if (challenge.getFriend().getChallenge().getGuessed() < challenge.getGuessed()) {
                    user.addScore(10);
                }
                challenge.stop();
                update(user);
                return response+isRight;
            }
            return isRight + response;
        }
        return "Sfida non esistente";
    }
}