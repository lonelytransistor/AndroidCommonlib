package net.lonelytransistor.commonlib.http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ServerRaw extends Thread {
    public static class Data {
        Map<String, String> headers = new HashMap<>();
        Query query = new Query();
        Body body = new Body();

        public static class Query {
            String method;
            float version;
            String path;
            Map<String, String> variables = new HashMap<>();
        }
        public static class Body {
            String mimetype;
            long size = 0;

            InputStream binary;
            JSONObject json;
        }
    }
    public static class Response {
        int code = 200;
        Map<String, String> headers = new HashMap<>();

        String mimetype;
        InputStream binary;
        JSONObject json;
    }
    public abstract static class Callback {
        private final Executor mExecutor;

        public Callback(Executor pExecutor) {
            mExecutor = pExecutor;
        }
        abstract public Response onPOST(Data data);
        abstract public Response onGET(Data data);
    }

    private static class StringBuilderInputStream extends InputStream {
        private int position = 0;
        private final StringBuilder stringBuilder;
        public StringBuilderInputStream(StringBuilder stringBuilder) {
            this.stringBuilder = stringBuilder;
        }
        @Override
        public int read() {
            if (position < stringBuilder.length()) {
                return stringBuilder.charAt(position++);
            } else {
                return -1; // End of stream
            }
        }
        @Override
        public int available() {
            return stringBuilder.length()-position;
        }
    }



    private final int mPort;
    private final String mCacheDir;
    private final Callback mCb;
    private final String mName;
    private boolean mRunning = false;
    public ServerRaw(int pPort, String pCacheDir, Callback pCb, String pName) {
        mPort = pPort;
        mCacheDir = pCacheDir;
        mCb = pCb;
        mName = pName;
    }
    @Override
    public void run() {
        mRunning = true;
        while (mRunning) {
            try {
                bind();
            } catch (IOException e) {
                if (e.getMessage().contains("EADDRINUSE")) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    Log.i("Server", "Address in use, retrying in 1s.");
                } else {
                    Log.w("ServerException", e);
                    break;
                }
            }
        }
    }
    public void end() {
        mRunning = false;
        Log.i("Server", "Stopping server.");
    }



    private void bind() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.setSoTimeout(5000);
        serverSocket.bind(new InetSocketAddress(mPort));
        while (mRunning) {
            try {
                Socket newSocket = serverSocket.accept();
                Thread newClient = new Receiver(newSocket);
                newClient.start();
            } catch (SocketTimeoutException ignored) {}
        }
    }
    private class Receiver extends Thread {
        private final Socket socket;
        public Receiver(Socket pSocket) {
            socket = pSocket;
        }
        @Override
        public void run() {
            try {
                DataInputStream in;
                DataOutputStream out;
                if (socket.isConnected()) {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } else {
                    return;
                }
                while (socket.isConnected()) {
                    recv(out, in);
                }
            } catch (IOException e){
                Log.w("ServerException", e);
            }
        }

        private static final String RESP_HEADER = "" +
                "HTTP/1.1 %s OK\r\n" +
                "Date: %s\r\n" +
                "Server: %s\r\n" +
                "Accept: */*\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Headers: *\r\n" +
                "Connection: Close\r\n" +
                "Content-Length: %s\r\n" +
                "Content-Type: %s\r\n" +
                "\r\n";
        private void send(OutputStream out, Response data) throws IOException {
            String mimetype = "text/html; charset=utf-8";
            String buffStr = null;
            long size = 0;
            if (data.json != null) {
                buffStr = data.json.toString();
                size = buffStr.length();
                mimetype = "application/json";
            } else if (data.binary != null) {
                size = data.binary.available();
                mimetype = data.mimetype;
            }
            String buff = String.format(RESP_HEADER,
                    data.code,
                    LocalDateTime.now(),
                    mName,
                    size,
                    mimetype);
            out.write(buff.getBytes());
            if (buffStr != null) {
                out.write(buffStr.getBytes());
            } else if (data.binary != null) {
                while (data.binary.available() > 0) {
                    out.write(data.binary.read());
                }
            }
            out.close();
        }
        private void recv(DataOutputStream out, DataInputStream in) throws IOException {
            Data data = recvData(new Data(), in);
            mCb.mExecutor.execute(()->{
                try {
                    send(out,
                            data.query.method.equals("GET") ?
                                    mCb.onGET(data) :
                                    mCb.onPOST(data)
                    );
                } catch (IOException e) {
                    Log.w("ServerException", e);
                }
            });
        }

        private Data recvData(Data data, DataInputStream in) throws IOException {
            String buffVar = "";
            String buff = "";
            while (socket.isConnected() && !socket.isInputShutdown()) {
                byte buffChar = in.readByte();

                if (buffChar==':' && buffVar.equals("")) {
                    buffVar = buff.replaceAll("^\\s+|\\s+$", "");
                    buff = "";
                } else if (buffChar=='\r' || buffChar=='\n') {
                    buffChar = in.readByte();
                    if (buffChar=='\r' || buffChar=='\n') {
                        buff = buff.replaceAll("^\\s+|\\s+$", "");
                        if (buffVar.equals("Content-Length")) {
                            data.body.size = Long.parseLong(buff);
                        } else if (buffVar.equals("Content-Type")) {
                            data.body.mimetype = buff.split(";")[0];
                        } else if (!buffVar.equals("")) {
                            data.headers.put(buffVar, buff);
                        } else if (buff.endsWith("HTTP/1.1") || buff.endsWith("HTTP/1.0")) {
                            String[] buffArr = buff.split("\\s+");
                            if (buffArr.length == 3) {
                                data.query.method = buffArr[0].toUpperCase();
                                data.query.version = Float.parseFloat(buffArr[2].split("/")[1]);
                                if (buffArr[1].contains("?")) {
                                    String[] pathData = buffArr[1].split("\\?");
                                    data.query.path = pathData[0];
                                    for (String varStr : pathData[1].split("&")) {
                                        String[] varArr = varStr.split("=");
                                        if (varArr.length > 1) {
                                            data.query.variables.put(varArr[0], varArr[1]);
                                        }
                                    }
                                } else {
                                    data.query.path = buffArr[1];
                                }
                            }
                        } else {
                            break;
                        }
                        buffVar = "";
                        buff = "";
                    }
                } else {
                    buff += new String(new byte[] {buffChar});
                }
            }
            if (data.body.mimetype.equals("application/json")) {
                recvJSON(data, in);
            } else {
                recvFile(data, in);
            }
            return data;
        }
        private void recvJSON(Data data, InputStream in) throws IOException {
            StringBuilder dataStr = new StringBuilder();
            byte[] buff = new byte[256];
            long lenNow = 0;
            long len = data.body.size;
            int nbytes = 0;
            while (in.available()>0 && nbytes!=-1 && lenNow<=len && socket.isConnected() && !socket.isInputShutdown()) {
                nbytes = in.read(buff, 0, Math.min(buff.length, (int) (len-lenNow)));
                lenNow += nbytes;
                dataStr.append(new String(buff));
            }

            try {
                data.body.json = new JSONObject(dataStr.toString());
            } catch (JSONException e) {
                Log.w("ServerException", e);
                data.body.binary = new StringBuilderInputStream(dataStr);
            }
        }
        private void recvFile(Data data, InputStream in) throws IOException {
            int ix = 0;
            File file = new File(mCacheDir, "tmp.bin");
            while (file.isFile()) {
                file = new File(mCacheDir, "tmp.bin" + ix);
                ix += 1;
            }

            OutputStream fStream = Files.newOutputStream(file.toPath());
            byte[] buff = new byte[256];
            long lenNow = 0;
            long len = data.body.size;
            int nbytes = 0;
            while (in.available()>0 && nbytes!=-1 && lenNow<=len && socket.isConnected() && !socket.isInputShutdown()) {
                nbytes = in.read(buff, 0, Math.min(buff.length, (int) (len-lenNow)));
                fStream.write(buff, 0, nbytes);
                lenNow += nbytes;
            }
            fStream.close();

            data.body.binary = Files.newInputStream(file.toPath());
        }
    }
}