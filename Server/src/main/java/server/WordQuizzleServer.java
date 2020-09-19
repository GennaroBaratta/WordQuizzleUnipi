package server;

import shared.Registration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * server.WordQuizzleServer
 */
public class WordQuizzleServer {
    public static final int TCP_PORT = 2919;

    public static void main(String[] args) throws RemoteException {
        int corePoolSize = 1;
        int maximumPoolSize = 4;
        long keepAliveTime = 5;
        BlockingQueue<Runnable> workQ = new LinkedBlockingQueue<>(20);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                TimeUnit.SECONDS, workQ);
        Registry r;

            RegistrationImpl registration = new RegistrationImpl();
            LocateRegistry.createRegistry(9999);
            r = LocateRegistry.getRegistry(9999);
            //r.unbind(Registration.SERVICE_NAME);
            r.rebind(Registration.SERVICE_NAME, registration);

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(TCP_PORT);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            Set<SelectionKey> readKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
                        ByteBuffer message = ByteBuffer.allocate(1024);
                        ByteBuffer[] bfs = { length, message };
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ, bfs);
                    } else if (key.isReadable()) {
                        System.out.println("READABLE");
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer[] bfs = (ByteBuffer[]) key.attachment();
                        long bytesRead =client.read(bfs);
                        if(bytesRead < 0){
                            client.close();
                            key.cancel();
                        }
                        if (!bfs[0].hasRemaining()) {
                            bfs[0].flip();
                            int l = bfs[0].getInt();
                            if (bfs[1].position() == l) {
                                bfs[1].flip();
                                executor.execute(new CommandHandler(key, bfs));
                                SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE, null);
                            }
                        }

                    } else if (key.isWritable() && (key.attachment() != null)) {
                        System.out.println("WRITABLE");
                        SocketChannel client = (SocketChannel) key.channel();
                        String reply = (String) key.attachment();
                        ByteBuffer respBf = ByteBuffer.wrap(reply.getBytes());
                        client.write(respBf);
                        if (!respBf.hasRemaining()) {
                            respBf.clear();
                            ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
                            ByteBuffer message = ByteBuffer.allocate(1024);
                            ByteBuffer[] bfs = { length, message };
                            SelectionKey key2 = client.register(selector, SelectionKey.OP_READ, bfs);// SOLO SE
                        }
                    }else if(!key.isValid()){
                        System.out.println("NOT VALID");
                        key.cancel();
                        key.channel().close();
                    }
                } catch (Exception e) {
                    String usr = Database.getInstance().getOnlineUser(key);
                    if(usr != null){
                        Database.getInstance().logout(usr);
                        e.printStackTrace();
                    }
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        cex.printStackTrace();
                    }
                }
            }
        }
    }
}