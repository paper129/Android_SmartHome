package com.example.user.smarthome;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    int i = 0;
    int o = 0;
    int u = 0;
    int y = 0;
    String message = "1";
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn1;
    TextView txtResult;
    private Runnable mMyRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            //Change state here
        }
    };
    private RequestQueue mQueue;
    public String TAG="MyTAG";
    private String userName = "";
    private String passWord = "";
    String clientId="";
    private int mq = 1;
    static String HOST = "tcp://mqtt.racinglog.pw:1883";
    MqttAndroidClient client;
    Button btn;
    String hi = "1";
    Vibrator vibrator;
    Ringtone Rt;
    private FirebaseAuth firebaseAuth ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            String ranID = UUID.randomUUID().toString();
            clientId = "Android-" + ranID;
            btn1 = (Button)findViewById(R.id.btn1);
            btn3 = (Button)findViewById(R.id.btn3);
            btn2 = (Button)findViewById(R.id.btn2);
            btn4 = (Button)findViewById(R.id.btn4);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
            mQueue = Volley.newRequestQueue(this);
            txtResult = (TextView) findViewById(R.id.txtResult);
            vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
            Log.d(clientId, "clientID");
            String url = "https://api.thingspeak.com/channels/620045/feeds/last.json?results";
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Log.d(TAG, response.toString());
                    try {
                        // Parsing the string response into json object
                        JSONObject jObj = new JSONObject(response);
                        String light = jObj.getString("field1");
                        String fan = jObj.getString("field2");
                        String curtain = jObj.getString("field3");
                        String tem = jObj.getString("field4");
                        String hum = jObj.getString("field5");
                        String jsonResponse = "";
                        jsonResponse += "Temperature: " + tem + " °C\n";
                        jsonResponse += "Humidity: " + hum + " %";
                        if(light == hi){
                            i = 1;
                            btn2.setBackground(getResources().getDrawable(R.drawable.elec2));
                        }
                        if(fan == hi){
                            u = 1;
                            btn3.setBackground(getResources().getDrawable(R.drawable.fan2));
                        }
                        if(curtain == hi){
                            o = 1;
                            btn4.setBackground(getResources().getDrawable(R.drawable.curtain2));
                        }
                        txtResult.setText(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
// Add the request to the RequestQueue.
            String clientId = MqttClient.generateClientId();
            client = new MqttAndroidClient(this.getApplicationContext(), HOST, clientId);
            btn = (Button) findViewById(R.id.btn2);
            //MqttConnectOptions options = new MqttConnectOptions();
            try {
                IMqttToken token = client.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        setSubscription();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        setSubscription();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String s, MqttMessage message) throws Exception {
                    Rt.play();
                    Intent intent = new Intent();
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);
                    Notification noti = new Notification.Builder(MainActivity.this)
                            .setTicker("TickerTitle")
                            .setContentTitle("SmartHome")
                            .setContentText("Changes have been made")
                            .setSmallIcon(R.drawable.lock1)
                            .setContentIntent(pendingIntent).getNotification();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });


            mQueue.add(strReq);
            btn1.setOnClickListener(new View.OnClickListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    final String topic = "esp";
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d(TAG, "Subscribe Successfully " + topic);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                Log.e(TAG, "Subscribe Failed " + topic);
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    y++;
                    pub ();
                    if ( y == 1 ) {
                        Button btn1 = (Button) findViewById(R.id.btn1);
                        btn1.setBackground(getResources().getDrawable(R.drawable.unlock1));
                        y = y - 2;
                    }
                    if ( y == 0 ){
                        Button btn1 = (Button) findViewById(R.id.btn1);
                        btn1.setBackground(getResources().getDrawable(R.drawable.lock1));
                        pub ();
                    }
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i++;
                    pub2 ();
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( i == 1 ) {
                        Button btn2 = (Button) findViewById(R.id.btn2);
                        btn2.setBackground(getResources().getDrawable(R.drawable.elec));
                        i = i - 2;
                    }
                    if ( i == 0 ){
                        Button btn2 = (Button) findViewById(R.id.btn2);
                        btn2.setBackground(getResources().getDrawable(R.drawable.elec2));
                    }
                }
            });
            btn3.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    u++;
                    pub3 ();
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( u == 1 ) {
                        Button btn3 = (Button) findViewById(R.id.btn3);
                        btn3.setBackground(getResources().getDrawable(R.drawable.fan2));
                        u = u - 2;
                    }
                    if ( u == 0 ){
                        Button btn3 = (Button) findViewById(R.id.btn3);
                        btn3.setBackground(getResources().getDrawable(R.drawable.fan));
                    }
                }
            });
            btn4.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    o++;
                    pub4 ();
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( o == 1 ) {
                        Button btn4 = (Button) findViewById(R.id.btn4);
                        btn4.setBackground(getResources().getDrawable(R.drawable.curtain2));
                        o = o - 2;
                    }
                    if ( o == 0 ){
                        Button btn4 = (Button) findViewById(R.id.btn4);
                        btn4.setBackground(getResources().getDrawable(R.drawable.curtain));
                    }
                }
            });


        }
        else
        {
            Intent intent = new Intent(this, login.class);
            startActivity(intent);
        }


    }

    public void pub ()
    {
        String topic = "esp";

        try {
            client.publish(topic, message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void pub2 ()
    {
        String topic = "light";

        try {
            client.publish(topic, message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void pub3 ()
    {
        String topic = "esp";

        try {
            client.publish(topic, message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void setSubscription(){
        try{
            client.subscribe("esp", 0);
            client.subscribe("light", 0);
            client.subscribe("fan", 0);
            client.subscribe("curtain", 0);
            client.subscribe("esp1", 0);
            client.subscribe("lock", 0);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
        }
    }
    public void pub4 ()
    {
        String topic = "curtain";

        try {
            client.publish(topic, message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


};