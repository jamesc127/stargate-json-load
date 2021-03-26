package org.example;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class App {
    public App() throws IOException, ParseException {
    }
    public static void main(String[] args) throws IOException, ParseException {
        String astraDbId        = ""; //Astra database ID
        String astraRegion      = "us-east1"; //Astra DB region
        String astraKeyspace    = ""; //Astra DB keyspace
        String astraCollection  = "something_interesting"; //Astra collection (think of it like a table) to create
        String astraAppToken    = ""; //App token

        //Set up HTTP client
        OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build();
        MediaType mediaType = MediaType.parse("application/json");
        JSONParser jsonParser = new JSONParser();

        //Read JSON data from file and create an array
        JSONArray json = (JSONArray) jsonParser.parse(new FileReader("./src/main/resources/MOCK_DATA.json"));

        //For each item in the array, load the document into Astra
        json.forEach(obj -> {
            RequestBody jsonBody = RequestBody.create(obj.toString(),mediaType);
            Request request = new Request.Builder()
                    .url("https://"+astraDbId+"-"+astraRegion+".apps.astra.datastax.com/api/rest/v2/namespaces/"
                            +astraKeyspace
                            +"/collections/"+astraCollection)
                    .method("POST", jsonBody)
                    .addHeader("X-Cassandra-Token", astraAppToken)
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                System.out.println(Objects.requireNonNull(response.body()).string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //Load car data too
        JSONArray carJson = (JSONArray) jsonParser.parse(new FileReader("./src/main/resources/MOCK_DATA_CAR.json"));
        carJson.forEach(obj -> {
            RequestBody carBody = RequestBody.create(obj.toString(),mediaType);
            Request request = new Request.Builder()
                    .url("https://"+astraDbId+"-us-east1.apps.astra.datastax.com/api/rest/v2/namespaces/"
                            +astraKeyspace
                            +"/collections/"+astraCollection)
                    .method("POST", carBody)
                    .addHeader("X-Cassandra-Token", astraAppToken)
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                System.out.println(Objects.requireNonNull(response.body()).string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
