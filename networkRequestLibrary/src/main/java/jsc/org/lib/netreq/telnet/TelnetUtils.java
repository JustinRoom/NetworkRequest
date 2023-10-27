package jsc.org.lib.netreq.telnet;

import android.text.TextUtils;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class TelnetUtils {

    public static boolean telnet(String ip, int port) {
        if (TextUtils.isEmpty(ip)) return false;
        TelnetClient client = new TelnetClient();
        try {
            client.connect(ip, port);
            InputStream is = client.getInputStream();
            OutputStream os = client.getOutputStream();
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
            os.flush();
            byte[] bytes = new byte[is.available()];
            int len = is.read(bytes);
            if (len != -1) {
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            }
            return client.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
