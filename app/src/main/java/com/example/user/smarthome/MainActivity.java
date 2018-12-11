package com.example.user.smarthome;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
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


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    String message1 = "1" ,message2="";
    int y,u,p,o;
    private int bn[] = { R.id.btn2 , R.id.btn3 ,R.id.btn4,R.id.btn1 };
    private  Button btn_array[] = new Button[4];
    private  int image1[] ={R.drawable.elec,R.drawable.fan,R.drawable.curtain,R.drawable.lock1};
    private  int image2[] ={R.drawable.elec2,R.drawable.fan2,R.drawable.curtain2,R.drawable.unlock1};
    private String object1[] = new String[6];
    TextView txtResult;
    String label="";
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
    String clientId="";
    private int mq = 1;
    static String HOST = "tcp://mqtt.racinglog.pw:1883";
    MqttAndroidClient client;
    Button btn;
    String hi = "1";
    Vibrator vibrator;
    Ringtone Rt;

    //private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TEST_NOTIFY_ID = "test_notyfy_id";
    private static final int NOTYFI_REQUEST_ID = 300;
    private int testNotifyId = 11;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            String ranID = UUID.randomUUID().toString();
            clientId = "Android-" + ranID;
            for (int i=0; i<4 ;i++)
            {
                btn_array[i] = (Button)findViewById(bn[i]);
            }

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
            mQueue = Volley.newRequestQueue(this);
            txtResult = (TextView) findViewById(R.id.txtResult);
            vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
            Log.d(clientId, "clientID");
            final String field[] ={"1","2","3","4","5","6"};


                String url = "http://mqtt.racinglog.pw/merge.json";

                StringRequest strReq = new StringRequest(Request.Method.GET,
                        url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log.d(TAG, response.toString());
                        try {
                            // Parsing the string response into json object
                            JSONObject jObj = new JSONObject(response);
                            JSONArray array1 = jObj.getJSONArray("merge");
                            for(int i=0;i<array1.length();i++)
                            {
                                JSONObject oj = array1.getJSONObject(i);
                                object1[i] = oj.getString("field"+field[i]);
                                Log.d("System Info",object1[i]);
                            }
                            for (int i=0; i<=2; i++)
                            {


                                if(object1[i].equals("1"))
                                {
                                    btn_array[i].setBackgroundResource(image2[i]);
                                    if(i == 0)
                                    {
                                        p=-1;
                                    }
                                    if(i==1)
                                    {
                                        u=-1;
                                    }
                                    if(i==2)
                                    {
                                        o=-1;
                                    }
                                }
                                else {
                                    btn_array[i].setBackgroundResource(image1[i]);
                                }

                            }
                            if(object1[5].equals("1"))
                            {
                                y=-1;
                                btn_array[3].setBackgroundResource(image2[3]);
                            }
                            else {
                                btn_array[3].setBackgroundResource(image1[3]);
                            }

                            if(object1[3].equals("null"))
                            {
                                label+="Error temp\n";
                            }
                            else
                            {
                                label+="Temperature: "+object1[3]+"°C\n";
                            }
                            if(object1[4].equals("null"))
                            {
                                label+="Error humidity";
                            }
                            else
                            {
                                label+="Humidity: "+object1[4]+"%";
                            }
                            txtResult.setText(label);

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
                    showNotification(s,message);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });



            btn_array[3].setOnClickListener(new View.OnClickListener() {



                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    btn_array[3].setEnabled(false);
                    final String topic = "mqtt/door";
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
                                String toast = "Subscribe " + topic + " Failed " ;
                                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    y++;
                    if ( y == 1 ) {
                        message1 = "1";
                        btn_array[3].setBackground(getResources().getDrawable(R.drawable.unlock1));
                        y = y - 2;
                        String toast = "Unlocked!" ;
                        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                    }
                    if ( y == 0 ){
                        message1 = "1";
                        btn_array[3].setBackground(getResources().getDrawable(R.drawable.unlock1));
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            btn_array[3].setEnabled(true);
                        }
                    }, 5000);

                    pub ();

                }
            });
        btn_array[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn_array[0].setEnabled(false);
                    p++;
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( p == 1 ) {
                        message1 = "1";
                        btn_array[0].setBackground(getResources().getDrawable(R.drawable.elec2));
                        p = p - 2;
                    }
                    if ( p == 0 ){
                        message1 = "0";
                        btn_array[0].setBackground(getResources().getDrawable(R.drawable.elec));
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            btn_array[0].setEnabled(true);
                        }
                    }, 5000);
                    pub2 ();
                }
            });
        btn_array[1].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    btn_array[1].setEnabled(false);
                    u++;
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( u == 1 ) {
                        message1 = "1";
                        btn_array[1].setBackground(getResources().getDrawable(R.drawable.fan2));
                        u = u - 2;
                    }
                    if ( u == 0 ){
                        message1 = "0";
                        btn_array[1].setBackground(getResources().getDrawable(R.drawable.fan));
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            btn_array[1].setEnabled(true);
                        }
                    }, 5000);
                    pub3 ();
                }
            });
        btn_array[2].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    btn_array[2].setEnabled(false);
                    o++;
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 1000);
                    if ( o == 1 ) {
                        message1 = "1";
                        btn_array[2].setBackground(getResources().getDrawable(R.drawable.curtain2));
                        o = o - 2;
                    }
                    if ( o == 0 ){
                        message1 = "0";
                        btn_array[2].setBackground(getResources().getDrawable(R.drawable.curtain));
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            btn_array[2].setEnabled(true);
                        }
                    }, 5000);
                    pub4 ();
                }
            });
        mQueue.add(strReq);

        }

    public void pub ()
    {
        String topic = "mqtt/door";

        try {
            client.publish(topic, message1.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void pub2 ()
    {
        String topic = "mqtt/light";

        try {
            client.publish(topic, message1.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void pub3 ()
    {
        String topic = "mqtt/fan";

        try {
            client.publish(topic, message1.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void setSubscription(){

        try{
            client.subscribe("mqtt/light", 0);
            client.subscribe("mqtt/fan", 0);
            client.subscribe("mqtt/curtain", 0);
            client.subscribe("mqtt/door", 0);
            client.subscribe("mqtt/door/crack", 0);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
        }
    }
    public void pub4 ()
    {
        String topic = "mqtt/curtain";

        try {
            client.publish(topic, message1.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void showNotification(String s, MqttMessage message) {
        Log.d(TAG, "showNotification: ");

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                NOTYFI_REQUEST_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("System Info","...."+message+"....");
        if(s.equals("mqtt/light"))
        {
            if(message.toString().equals("1"))
            {
                s = "Light is On";
            }
            else
            {
                s = "Light is Off";
            }
        }
        if(s.equals("mqtt/fan"))
        {
            if(message.toString().equals("1"))
            {
                s = "Fan is On";
            }
            else
            {
                s = "Fan is Off";
            }
        }
        if(s.equals("mqtt/curtain"))
        {
            if(message.toString().equals("1"))
            {
                s = "Curtain is On";
            }
            else
            {
                s = "Curtain is Off";
            }
        }
        if(s.equals("mqtt/door"))
        {
            if(message.toString().equals("1"))
            {
                s = "Door Opened";
            }
        }
        if(s.equals("mqtt/door/crack"))
        {
            if(message.toString().equals("1"))
            {
                s = "Your door is cracked";
            }

        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Smart Home")
                .setContentText(s )
                .setSmallIcon(R.drawable.notify_big_icon)
                .setContentIntent(pendingIntent);
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(TEST_NOTIFY_ID, "Notify Test", NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId(TEST_NOTIFY_ID);
            manager.createNotificationChannel(channel);
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        manager.notify(testNotifyId,
                builder.build());
    }



}