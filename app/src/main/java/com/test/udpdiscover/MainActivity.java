package com.test.udpdiscover;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private DatagramSocket socket;
    private int broadcastAdress;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text= findViewById(R.id.text);
        WifiManager wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        System.out.println(dhcpInfo);
        log("dhcpInfo:"+dhcpInfo);
        broadcastAdress = (dhcpInfo.ipAddress&dhcpInfo.netmask)|(~dhcpInfo.netmask);
    }

    public void log(String format,Object ...args){
        final String logtxt = String.format(format, args);

        if(Looper.myLooper()!=Looper.getMainLooper()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText( text.getText().toString()+"\n"+logtxt);
                }
            });
        }else{
            text.setText( text.getText().toString()+"\n"+logtxt);
        }
    }

    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     * @param hostAddress an int corresponding to the IPv4 address in network byte order
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public void sendUdp(View view) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    getSocket();
                    byte[] buf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())
                            .getBytes();
                    InetAddress inetAddress = intToInetAddress(broadcastAdress);
                    System.out.println(inetAddress.getHostAddress());
                    log("SEND Address:"+inetAddress.getHostAddress());
                    DatagramPacket p = new DatagramPacket(buf, 0, buf.length,
                            inetAddress, 6666);
                    socket.send(p);
                    System.out.println("SEND:"+new String(buf));
                    log("SEND:"+new String(buf));
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    private void getSocket() throws SocketException {
        if (socket == null) {
            socket = new DatagramSocket(6666);
        }
    }

    public void recvUdp(View view) {
        new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    getSocket();
                    byte[] buf=new byte[1024];
                    DatagramPacket p = new DatagramPacket(buf, 0, buf.length);
                    socket.setSoTimeout(10_000);
                    System.out.println("=====Prepare Recv===");
                    log("=====Prepare Recv===");
                    socket.receive(p);
                    String data=new String(p.getData(),p.getOffset(),p.getLength());
                    String hostName = p.getAddress().getHostName();
                    int port = p.getPort();
                    System.out.printf("%s:%d RECV:%s\n",hostName,port,data);
                    log("%s:%d RECV:%s\n",hostName,port,data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
