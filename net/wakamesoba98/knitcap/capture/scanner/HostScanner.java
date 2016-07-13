package net.wakamesoba98.knitcap.capture.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HostScanner {

    private static final boolean WINDOWS = "\\".equals(System.getProperty("file.separator"));

    public String getDefaultGateway() {
        if (WINDOWS) {
            return getDefaultGatewayWindows();
        } else {
            return getDefaultGatewayLinux();
        }
    }

    private String getDefaultGatewayWindows() {
        try {
            ProcessBuilder builder = new ProcessBuilder("netstat -rn");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            String result = "";
            while (line != null) {
                if (line.trim().startsWith("0.0.0.0")) {
                    result = line.trim().split("[\\s]+")[2];
                    break;
                }
                line = reader.readLine();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getDefaultGatewayLinux() {
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "ip route | grep '^default' | cut -d ' ' -f 3");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
