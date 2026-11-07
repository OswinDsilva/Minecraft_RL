package dev.minecraftrl.integration.client.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;

import dev.minecraftrl.integration.client.Minecraftrl_integrationClient.PlayerState;

public class SocketProgram implements Runnable{
    private Socket soc = null;
    private ServerSocket server_soc = null;
    private BufferedReader in = null;
    Gson gson = new Gson();
    int port;

    private AtomicReference<PlayerState> sharedState;

    public SocketProgram(int port, AtomicReference<PlayerState> sharedState){
        this.port = port;
        this.sharedState = sharedState;
    }


    @Override 
    public void run(){
        try{
            server_soc = new ServerSocket(port);
            System.out.println("Server has started");
            
            soc = server_soc.accept();
            System.out.println("Got client");

            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            DataOutputStream out = new DataOutputStream(soc.getOutputStream());
            System.out.println("Data stream connected");

            String m = "";

            while(true){
                try{
                    m = in.readLine();
                    if(m ==  null || m.equals("Over")){
                        break;
                    }
                    System.out.println("From client: "+m);
                    String send_msg = gson.toJson(sharedState.get());
                    byte[] data = send_msg.getBytes("UTF-8");

                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();


                }catch(IOException i){
                    System.out.println(i);
                }
            }
            System.out.println("Connection closed");
            soc.close();
            server_soc.close();
            in.close();
        }catch(IOException i){
            System.out.println(i);
        }
    }
}
