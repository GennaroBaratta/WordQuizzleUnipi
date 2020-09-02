package org.baratta.logic;

import javafx.concurrent.Task;

public class ChallengeSender extends Task<Void> {
    private final String username;
    private final String friendname;

    public ChallengeSender(String username, String friendname) {
        this.username = username;
        this.friendname = friendname;
    }

    @Override
    protected Void call() {
        WordQuizzleClient client = WordQuizzleClient.getInstance();
        String response = client.sfida(username, friendname);
        updateMessage(response);
        return null;
    }
}
