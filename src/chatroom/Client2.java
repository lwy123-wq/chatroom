package chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class Client2 {
    private  final static String HOST="127.0.0.1";
    private final static int PORT=5556;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    public Client2()throws IOException{
        selector=Selector.open();
        socketChannel=SocketChannel.open(new InetSocketAddress("127.0.0.1",PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        username=socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username+"is ok ................");
    }
    public void sendInfo(String info){
        info=username+"say:"+info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readInfo(){
        try {
            int read=selector.select();
            if (read>0){
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key=iterator.next();
                    if(key.isReadable()){
                        SocketChannel sc=(SocketChannel) key.channel();
                        ByteBuffer buffer=ByteBuffer.allocate(1024);
                        sc.read(buffer);
                        String a=new String(buffer.array());
                        System.out.println(a.trim());
                    }
                    iterator.remove();
                }
            }else {
                System.out.println("没有可用的通道");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Client2 client1=new Client2();
        new Thread(){
            public void run(){
                while (true){
                    client1.readInfo();
                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
        Scanner sc=new Scanner(System.in);
        while (sc.hasNextLine()){
            String s=sc.nextLine();
            client1.sendInfo(s);
        }
    }
}
