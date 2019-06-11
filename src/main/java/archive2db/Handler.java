package archive2db;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class Handler {
    private long lastPercent = -1;
    private DbAuthData authData;

    private final String url;
    private final String archived_file;
    private final String unarchived_file;

    public Handler(String url, String archived_file, String unarchived_file) {
        this.url = url;
        this.archived_file = archived_file;
        this.unarchived_file = unarchived_file;
    }

    public Handler setAuthData(DbAuthData authData) {
        this.authData = authData;
        return this;
    }

    public void doIt() throws IOException, SQLException {
        downloadFile();
        decompressFile();
        insertFileDataToDb();
    }

    private void downloadFile() throws IOException {
        System.out.println("Downloading...");
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES); // read timeout

        OkHttpClient httpClient = builder.build();
        Call call = httpClient.newCall(new Request.Builder().url(url).get().build()); //Call to server
        Response response = call.execute(); //

        if (response.code() == 200) { //Check Response code
            InputStream inputStream = null;
            FileOutputStream outputStream = null;

            try {
                inputStream = response.body().byteStream(); //Get stream of bytes
                byte[] buffer = new byte[1024 * 4]; //Creating buffer
                long downloaded = 0;
                long target = response.body().contentLength();
                outputStream = new FileOutputStream(new File(archived_file));
                printDownloadProcess(0L, target);
                while (true) {
                    int readed = inputStream.read(buffer);
                    if (readed == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, readed);
                    //write buff
                    downloaded += readed;
                    printDownloadProcess(downloaded, target);
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if(outputStream != null) {

                    outputStream.close();
                }
            }
        }
        System.out.println("File downloaded!");
    }


    private void decompressFile() throws IOException {
        System.out.println("Decompressing...");
        BZip2CompressorInputStream gzis = new BZip2CompressorInputStream(new FileInputStream(archived_file));
        Files.copy(gzis, Paths.get(unarchived_file), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File Decompressed!");
    }

    private void insertFileDataToDb() throws IOException, SQLException {
        System.out.println("Adding data to DB...");
        Connection conn = DriverManager.getConnection(authData.getUrl(), authData.getUser(), authData.getPassword()); //Creating connection to DB

        try(BufferedReader br = new BufferedReader(new FileReader(unarchived_file))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] item = line.split(","); //Split string by ","
                String sql = "INSERT INTO data (series, number) values (?, ?)";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, item[0]); //Set series instead "?"
                statement.setString(2, item[1]); //Set number instead "?"
                statement.executeUpdate();
            }
        }
        conn.close(); //Close connection to DB
        System.out.println("Data added to DB");
    }

    private void printDownloadProcess(long l1, long l2) {
        long currentPercent = (l1*100)/l2;
        if(lastPercent < currentPercent) {
            System.out.print("=");
            lastPercent = currentPercent;
        }
    }
}
