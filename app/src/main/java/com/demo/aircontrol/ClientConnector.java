package com.demo.aircontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnector {

    private Socket mClient;

    private String mDstName;

    private int mDesPort;

    private ConnectLinstener mListener;

    public ClientConnector(String dstName, int dstPort) {
        this.mDstName = dstName;
        this.mDesPort = dstPort;
    }

    public void connect() throws IOException {
        if (mClient == null) {
            mClient = new Socket(mDstName, mDesPort);
        }

        //获取其他客户端发送过来的数据
        InputStream inputStream = mClient.getInputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            String data = new String(buffer, 0, len);

            //通过回调接口将获取到的数据推送出去
            if (mListener != null) {
                mListener.onReceiveData(data);
            }
        }
    }


    public void send(final String data) throws IOException {

        if (mClient != null) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = mClient.getOutputStream();
                        //模拟内容格式：receiver+  # + content
                        outputStream.write((data).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        }
    }


    public void disconnect() throws IOException {
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }


    public void setOnConnectLinstener(ConnectLinstener linstener) {
        this.mListener = linstener;
    }


    public interface ConnectLinstener {
        void onReceiveData(String data);
    }
}
