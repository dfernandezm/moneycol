package com.moneycol.collections.app;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class HLSParser {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, CertificateException {

//        // Create a trust manager that does not validate certificate chains
//        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//            public void checkClientTrusted(X509Certificate[] certs, String authType) {
//            }
//            public void checkServerTrusted(X509Certificate[] certs, String authType) {
//            }
//        }
//        };
//
//        // Install the all-trusting trust manager
//        SSLContext sc = SSLContext.getInstance("SSL");
//        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//        // Create all-trusting host name verifier
//        HostnameVerifier allHostsValid = new HostnameVerifier() {
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        };
//
//        // Install the all-trusting host verifier
//        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); // PKIX
//        tmf.init((KeyStore) null);
//        SSLContext sslCtx = SSLContext.getInstance("TLS");
//        sslCtx.init(null, tmf.getTrustManagers(), null);
//        SSLSocketFactory sslSF = sslCtx.getSocketFactory();

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        URL url = new URL("https://tv4.live/api/stream/morenza@gmail.com/5498ad/livetv.epg/cnn.us.m3u8");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");


        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
            System.out.println(inputLine);
        }
        in.close();

        File file = new File("/Users/david/list.m3u8");
        //file.createNewFile();



        MasterPlaylistParser parser = new MasterPlaylistParser();

// Parse playlist
        MasterPlaylist playlist = parser.readPlaylist(Paths.get("/Users/david/Downloads/livetv.m3u"));

// Update playlist version
        MasterPlaylist updated = MasterPlaylist.builder()
                .from(playlist)
                .version(2)
                .build();

// Write playlist to standard out
        System.out.println(parser.writePlaylistAsString(updated));
    }
}
