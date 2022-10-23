package com.steward.nowpaid.api;

import com.steward.nowpaid.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class API
{
    public static API instance;
    public String host;
    public int timeout = 1000 * 10; // 10 seconds

    private class Request {
        int code;
        OnLoggingListener log;
        OnTestSession test;
        OnArrayResponse array;
        OnObjectResponse obj;
        JSONObject body;
        String param;
        InputStream is;
    }

    Request[] queue = new Request[5];

    APIEvent event;
    int index = 0;
    // global requested
    boolean error = false;
    String output = "";
    HttpURLConnection con;

    public static final int LOGIN = 0x763;
    public static final int GET_BUSSINESS_LIST = 0x764;
    public static final int TEST_SESSION = 0x745;
    public static final int PUBLISH_BUSSINESS = 0x765;
    public static final int EDIT_BUSSINESS = 0x8424;
    public static final int RATE_BUSSINESS = 0x766;
    public static final int UPLOAD_IMAGE = 0x873;
    public static final int LOGOUT = 0x768;
    public static final int SIGNIN = 0x7643;
    public static final int GET_USER_INFO = 0x3243;
    public static final int GET_BUSSINESS_DETAILS = 0x3343;

    private API() {
        new Thread(() -> {
            while(true) {
                for (int i = 0; i < queue.length; i++) {
                    if (queue[i] == null) continue;
                    try {
                        switch (queue[i].code) {
                            case LOGIN: {
                                post("login",queue[i].body);
                                if (!error) {
                                    JSONObject result = new JSONObject(output);
                                    switch (result.getString("message")) {
                                        case "done": {
                                            Session.Id = con.getHeaderField("set-cookie");
                                            Session.property = result.getBoolean("property");
                                            queue[i].log.sucessfully();
                                        }
                                        break;
                                        case "user_inv":
                                            queue[i].log.error(true, false);
                                            break;
                                        case "password_inv":
                                            queue[i].log.error(false, true);
                                            break;
                                    }
                                }
                            }
                            break;
                            case SIGNIN:{
                                post("signin",queue[i].body);
                                if (!error) {
                                    JSONObject result = new JSONObject(output);
                                    switch (result.getString("message")) {
                                        case "done": {
                                            Session.Id = con.getHeaderField("set-cookie");
                                            Session.property = result.getBoolean("property");
                                        }
                                        break;
                                    }
                                    queue[i].obj.successfully(result);
                                }
                            }
                            break;
                            case TEST_SESSION: {
                                get("test");
                                if (error) {
                                    queue[i].test.fail();
                                } else {
                                    queue[i].test.pass();
                                }
                            }
                            break;
                            case GET_BUSSINESS_LIST: {
                                get("get-list-buss");
                                if (!error) {
                                    queue[i].array.successfully(new JSONArray(output));
                                }
                            }
                            break;
                            case PUBLISH_BUSSINESS: {
                                post("publish-buss",queue[i].body);
                                if (!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                            case EDIT_BUSSINESS: {
                                post("edit-buss",queue[i].body);
                                if (!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                            case RATE_BUSSINESS: {
                                post("rate-buss",queue[i].body);
                                if (!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                            case LOGOUT:{
                                get("logout");
                                if(!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                            case UPLOAD_IMAGE: {
                                uploadImage("upload-image",queue[i].is,queue[i].param);
                                if(!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                            case GET_USER_INFO: {
                                get("get-user-info");
                                if(!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                                break;
                            case GET_BUSSINESS_DETAILS: {
                                post("get-details-buss", queue[i].body);
                                if(!error) {
                                    queue[i].obj.successfully(new JSONObject(output));
                                }
                            }
                            break;
                        }
                        queue[i] = null;
                    } catch (Exception e) {
                        queue[i] = null;
                        log(e.toString());
                        if (event != null) {
                            event.timeout();
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void init(String host) {
        instance = new API();
        instance.host = host;
    }

    private void add(Request req){
    if(index == 5){
        index = 0;
    }
    queue[index] = req;
    index++;
}

    public void login(String email,String password,OnLoggingListener listener) {
        Request req = new Request();
        req.code = LOGIN;
        req.log = listener;
        req.body = new JSONObject();
        try
        {
            req.body.put("email", email);
            req.body.put("password",password);
        }
        catch (JSONException e)
        {}
        add(req);
    }

    public void singin(String name,String phone,String image,int location, boolean property, String email,String password, OnObjectResponse listener) {
        Request req = new Request();
        req.code = SIGNIN;
        req.obj = listener;
        req.body = new JSONObject();
        try
        {
            req.body.put("email", email);
            req.body.put("name", name);
            req.body.put("password",password);
            req.body.put("property", property);
            req.body.put("location", location);
            req.body.put("phone", phone);
            req.body.put("image", image);
        }
        catch (JSONException e)
        {}
        add(req);
    }

    public void testSession(OnTestSession listener) {
        Request req = new Request();
        req.test = listener;
        req.code = TEST_SESSION;
        add(req);
    }

    public void getBussinessList(OnArrayResponse listener) {
        Request req = new Request();
        req.code = GET_BUSSINESS_LIST;
        req.array = listener;
        add(req);
    }

    public void publishBussiness(JSONObject body, OnObjectResponse listener) {
        Request req = new Request();
        req.code = PUBLISH_BUSSINESS;
        req.obj = listener;
        req.body = body;
        add(req);
    }

    public void editBussiness(JSONObject body, OnObjectResponse listener) {
        Request req = new Request();
        req.code = EDIT_BUSSINESS;
        req.obj = listener;
        req.body = body;
        add(req);
    }

    public void rateBussiness(String id, float stars,String text, OnObjectResponse listener) {
        Request req = new Request();
        req.code = RATE_BUSSINESS;
        req.obj = listener;
        req.body = new JSONObject();
        try
        {
            req.body.put("id", id);
            req.body.put("stars", stars);
            req.body.put("text", text);
        }
        catch (JSONException e)
        {}
        add(req);
    }

    public void getUserInfo(OnObjectResponse listener) {
        Request req = new Request();
        req.code = GET_USER_INFO;
        req.obj = listener;
        add(req);
    }

    public void requestDetailsBuss(String id, OnObjectResponse listener) {
        Request req = new Request();
        req.code = GET_BUSSINESS_DETAILS;
        req.obj = listener;
        req.body = new JSONObject();
        try
        {
            req.body.put("id", id);
        }
        catch (JSONException e)
        {}
        add(req);
    }

    public void logout(OnObjectResponse listener) {
        Request req = new Request();
        req.code = LOGOUT;
        req.obj = listener;
        add(req);
    }

    public void requestUploadImage(InputStream is,String ext, OnObjectResponse listener) {
        Request req = new Request();
        req.code = UPLOAD_IMAGE;
        req.obj = listener;
        req.is = is;
        req.param = ext;
        add(req);
    }

    public static void log(String text) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("/storage/emulated/0/dump.log", true));
            String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            out.append(time + " " + text);
            out.newLine();
            out.close();
        } catch (IOException e) {

        }
    }

    public void reportEvents(APIEvent event){
        this.event = event;
    }

    public void disposeEvent(){
        this.event = null;
    }

    private void post(String api_entry,JSONObject body) throws Exception {
        con = (HttpURLConnection)new URL(host + "/" + api_entry).openConnection();
        byte[] body_buffer = body.toString().getBytes("UTF-8");
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setRequestMethod("POST");
        con.setFixedLengthStreamingMode(body_buffer.length);
        con.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        con.setRequestProperty("Cookie", Session.Id);
        OutputStream out = con.getOutputStream();
        out.write(body_buffer);
        out.close();
        error = con.getResponseCode() != 200;
        if(con.getResponseCode() == 401 && event != null) {
            event.unautorized();
        }
        if(!error) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            output = "";
            while ((inputLine = in.readLine()) != null) {
                output += inputLine;
            }
            in.close();
        }
        con.disconnect();
    }

    private static final String EOL = "\r\n";

    private void uploadImage(String api_entry,InputStream is,String ext) throws Exception {
        con = (HttpURLConnection)new URL(host + "/" + api_entry).openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        final String boundary = UUID.randomUUID().toString();
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try (OutputStream out = con.getOutputStream()) {
            out.write(("--" + boundary + EOL +
                    "Content-Disposition: form-data; name=\"image\"; " +
                    "filename=\"result." + ext + "\"" + EOL +
                    "Content-Type: application/octet-stream" + EOL + EOL)
                    .getBytes(StandardCharsets.UTF_8)
            );
            byte[] buffer = new byte[2048];
            int size = -1;
            int length = is.available();
            while (-1 != (size = is.read(buffer))) {
                out.write(buffer, 0, size);
                out.flush();
            }
            out.write((EOL + "--" + boundary + "--" + EOL).getBytes(StandardCharsets.UTF_8));
            out.flush();
            con.connect();
        }
        error = con.getResponseCode() != 200;
        if(con.getResponseCode() == 401 && event != null) {
            event.unautorized();
        }
        if(!error) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            output = "";
            while ((inputLine = in.readLine()) != null) {
                output += inputLine;
            }
            in.close();
        }
        con.disconnect();
    }

    private void get(String api_entry) throws Exception {
        con = (HttpURLConnection)new URL(host + "/" + api_entry).openConnection();
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        con.setRequestProperty("Cookie", Session.Id);
        error = con.getResponseCode() != 200;
        if(con.getResponseCode() == 401 && event != null) {
            event.unautorized();
        }
        if(!error) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            output = "";
            while ((inputLine = in.readLine()) != null) {
                output += inputLine;
            }
            in.close();
        }
        con.disconnect();
    }
}
