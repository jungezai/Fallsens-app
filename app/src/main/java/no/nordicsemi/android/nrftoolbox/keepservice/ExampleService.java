package no.nordicsemi.android.nrftoolbox.keepservice;  
  
import no.nordicsemi.android.nrftoolbox.hts.HTSActivity;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;  
import android.content.Intent;  
import android.os.IBinder;  
import android.util.Log;  
  
public class ExampleService extends Service{  
    private static final String TAG = "ExampleService";
    //public static final String ACTION = "ExampleService";
  
    @Override  
    public void onCreate() {  
        Log.i(TAG, "ExampleService-onCreate");  
        DebugLogger.e(TAG,"威威威威威威威威威威威威威威eeeee");
        super.onCreate();
        Intent intent = new Intent( this, HTSActivity. class);  
        intent.putExtra( "ficationId", 1);  
        Notification.Builder builder = new Notification.Builder(this);  
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);  
        builder.setContentIntent(contentIntent); //设置了此项内容之后，点击通知栏的这个消息，就跳转到MainActivity  
        //builder.setSmallIcon(R.drawable.ic_launcher);
//      builder.setTicker("Foreground Service Start");  
        builder.setContentTitle( "发现周边优惠、畅享免费Wi-Fi" );  
        builder.setContentText( "【智慧生活】发现城市的智慧" );  
        Notification notification = builder.build();
        startForeground( 1, notification);
        DebugLogger.e(TAG,"威威威威威威威威威威威威威威");
    }  
  
/*    @Override  
    public void onStart(Intent intent, int startId) {  
        Log.i(TAG, "ExampleService-onStart");  
        super.onStart(intent, startId);  
    } */ 
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        //执行文件的下载或者播放等操作  
        Log.i(TAG, "ExampleService-onStartCommand");  
        /*  
         * 这里返回状态有三个值，分别是:  
         * 1、START_STICKY：当服务进程在运行时被杀死，系统将会把它置为started状态，但是不保存其传递的Intent对象，之后，系统会尝试重新创建服务;  
         * 2、START_NOT_STICKY：当服务进程在运行时被杀死，并且没有新的Intent对象传递过来的话，系统将会把它置为started状态，  
         *   但是系统不会重新创建服务，直到startService(Intent intent)方法再次被调用;  
         * 3、START_REDELIVER_INTENT：当服务进程在运行时被杀死，它将会在隔一段时间后自动创建，并且最后一个传递的Intent对象将会再次传递过来。  
         */  
        return super.onStartCommand(intent, flags, startId);  
    }  
  
    @Override  
    public IBinder onBind(Intent intent) {  
        Log.i(TAG, "ExampleService-onBind");  
        return null;  
    }  
      
    @Override  
    public void onDestroy() {  
        Log.i(TAG, "ExampleService-onDestroy");  
        super.onDestroy();  
    }  
  
}  