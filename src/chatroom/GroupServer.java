package chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GroupServer {
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT=5556;

    public GroupServer() {
        try {
            selector=Selector.open();
            listenChannel=ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            listenChannel.configureBlocking(false);
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void listen(){
        try {
            while (true){
                int count=selector.select(2000);
                if(count>0){
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()){
                        SelectionKey key=iterator.next();
                        if(key.isAcceptable()){
                            SocketChannel sc=listenChannel.accept();
                            sc.configureBlocking(false);
                            sc.register(selector,SelectionKey.OP_READ);
                            System.out.println(sc.getRemoteAddress()+"high lines............");
                        }
                        if(key.isReadable()){
                            readDate(key);
                        }
                        iterator.remove();
                    }
                }else {
                    System.out.println("wait..................");
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {

        }
    }
    public void readDate(SelectionKey key){
        SocketChannel channel=null;
        try {
            channel=(SocketChannel) key.channel();
            ByteBuffer buffer=ByteBuffer.allocate(1024);
            int count=channel.read(buffer);
            if(count>0){
                String a=new String(buffer.array());
                System.out.println("from client"+a);
                sendToClient(a,channel);
            }
        } catch (IOException e) {
             try {
                 System.out.println(channel.getRemoteAddress()+"offline");
                 key.channel();
                 channel.close();
             }catch (IOException e1){
                 e1.printStackTrace();
             }
        }

    }
    private void sendToClient(String a,SocketChannel self)throws IOException{
        System.out.println("The server forwards the message");
        for (SelectionKey key:selector.keys()){
            Channel target=key.channel();
            if(target instanceof SocketChannel && target!=self){
                SocketChannel dest=(SocketChannel) target;
                ByteBuffer buffer=ByteBuffer.wrap(a.getBytes());
                dest.write(buffer);
            }
        }
    }
    public static void main(String[] args) {
        GroupServer groupServer=new GroupServer();
        groupServer.listen();
    }
}
