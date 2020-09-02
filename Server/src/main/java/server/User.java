package server;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;
import java.util.TreeSet;

/**
 * server.User
 */
public class User {
    private String username;
    private String hash;
    private String salt;
    private Set<String> friends;
    private int score;
    @JsonIgnore
    private Challenge challenge = null;

    public User() {

    }

    public User(String name, String password) {
        this.username = name;
        this.salt = Password.generateSalt(16);
        this.hash = Password.hashPassword(password, this.salt);
        this.friends = new TreeSet<>();
        score = 0;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHash() {
        return this.hash;
    }

    public String getSalt() {
        return this.salt;
    }

    public Set<String> getFriends() {
        return this.friends;
    }

    public boolean addFriend(String username) {
        return friends.add(username);
    }

    public Challenge getChallenge() {
        if (challenge == null)
            challenge = new Challenge();
        return challenge;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }
}