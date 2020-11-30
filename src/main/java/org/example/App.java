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
        String astraDbId        = "f82d3eb1-f99a-4111-bbd6-1bd594fbdfcd"; //Astra database ID
        String astraRegion      = "us-east1"; //Astra DB region
        String astraKeyspace    = "document"; //Astra DB keyspace
        String astraUser        = "james"; //Astra DB user name
        String astraPassword    = "SuperSecret"; //Astra DB password

        //Set up HTTP client
        OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build();
        MediaType mediaType = MediaType.parse("application/json");

        //Use the user name and password to request an auth token
        RequestBody authBody = RequestBody.create(
                "{\"username\": \""+astraUser+"\",\"password\": \""+astraPassword+"\"}",mediaType
        );
        //HTTP POST to request the token
        Request authRequest = new Request.Builder()
                .url("https://"+astraDbId+"-"+astraRegion+".apps.astra.datastax.com/api/rest/v1/auth")
                .method("POST", authBody)
                .addHeader("Content-Type", "application/json")
                .build();
        Response authResponse = client.newCall(authRequest).execute();
        String authTokenString = Objects.requireNonNull(authResponse.body()).string();
        JSONParser jsonParser = new JSONParser();
        JSONObject authTokenObj = (JSONObject) jsonParser.parse(authTokenString);
        //Grab the token from the JSON object
        String authToken = authTokenObj.get("authToken").toString();
        //Read JSON data from file and create an array
        JSONArray json = (JSONArray) jsonParser.parse(new FileReader("./src/main/resources/MOCK_DATA.json"));

        //For each item in the array, load the document into Astra
        json.forEach(obj -> {
            RequestBody jsonBody = RequestBody.create(obj.toString(),mediaType);
            Request request = new Request.Builder()
                    .url("https://"+astraDbId+"-"+astraRegion+".apps.astra.datastax.com/api/rest/v2/namespaces/"+astraKeyspace+"/collections/demo_collection")
                    .method("POST", jsonBody)
                    .addHeader("X-Cassandra-Token", authToken)
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
                    .url("https://"+astraDbId+"-us-east1.apps.astra.datastax.com/api/rest/v2/namespaces/"+astraKeyspace+"/collections/demo_collection")
                    .method("POST", carBody)
                    .addHeader("X-Cassandra-Token", authToken)
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
