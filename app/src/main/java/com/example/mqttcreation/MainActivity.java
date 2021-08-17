package com.example.mqttcreation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final String TAG = MainActivity.class.getSimpleName();
    private Button buttonPublish;
    public Button connect;
    public TextView textView;
    public EditText editText;
    MqttClient mqttClient;
    IMqttToken token;
    String Topic = "example";
    String content      = "Message from MqttPublishSample";
    int qos             = 0;
    String brokerUrl       = "tcp://192.168.17.177:1883";
    String clientId     = "JavaSample";
    public HandlerThread mqttHandlerThread = null;
    public Handler mqttHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate() called ");
        buttonPublish = (Button) findViewById(R.id.btn_publish);
        connect =(Button)findViewById(R.id.connect);
        textView =(TextView)findViewById(R.id.text_view);
        editText = (EditText)findViewById(R.id.edit_text);
        //connect();
        connect.setOnClickListener(this);
        buttonPublish.setOnClickListener(this);

        mqttHandlerThread = new HandlerThread("mqtt-thread");
        mqttHandlerThread.start();
        mqttHandler = new Handler(mqttHandlerThread.getLooper());
    }
    public void connect()
    {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, null);
            mqttClient.setTimeToWait(5000);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.d(TAG,"Connecting to broker: "+brokerUrl);
            Log.d(TAG,"Publishing message: "+content);
            mqttClient.setCallback(msg);
            token = mqttClient.connectWithResult(connOpts);
            token.setActionCallback(mqtt);
            Log.d(TAG,"Connecting.......");
            token.waitForCompletion();
            Log.d(TAG,"connected........");
            mqttClient.subscribe(Topic, qos);
            Log.d(TAG , "subscribe............");
        } catch(MqttException me) {
            Log.d(TAG,"reason "+me.getReasonCode());
            Log.d(TAG,"msg "+me.getMessage());
            Log.d(TAG,"loc "+me.getLocalizedMessage());
            Log.d(TAG,"cause "+me.getCause());
            Log.d(TAG,"excep "+me);
            me.printStackTrace();
        }

    }

    MqttCallback msg = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
          Log.d(TAG ,"connectionLost()");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
           final String messageString = new String (message.getPayload());
            Log.d(TAG ,"messageArrived()::"+ messageString);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(messageString);
                }
            });

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG ,"deliveryComplete()");
        }
    };

    IMqttActionListener mqtt = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG ,"onSuccess()");
            try {
                asyncActionToken.getClient().subscribe(Topic, 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG ,"onFailure()");
        }
    };

    public void sendMessage(String topic, String message){

        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        try {
            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect:
                mqttHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                });
                break;
            case R.id.btn_publish:
                sendMessage(Topic, editText.getText().toString());
        }

    }

    @Override
    protected void onDestroy() {
        try {
            mqttClient.disconnectForcibly();
        } catch (MqttException e) {
            Log.d(TAG,"onDestroy()"+e );
            e.printStackTrace();
        }
        try {
            mqttClient.close();
        } catch (MqttException e) {
            Log.d(TAG,"onDestroy()"+e );
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
