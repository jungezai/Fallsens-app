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
        DebugLogger.e(TAG,"����������������������������eeeee");
        super.onCreate();
        Intent intent = new Intent( this, HTSActivity. class);  
        intent.putExtra( "ficationId", 1);  
        Notification.Builder builder = new Notification.Builder(this);  
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);  
        builder.setContentIntent(contentIntent); //�����˴�������֮�󣬵��֪ͨ���������Ϣ������ת��MainActivity  
        //builder.setSmallIcon(R.drawable.ic_launcher);
//      builder.setTicker("Foreground Service Start");  
        builder.setContentTitle( "�����ܱ��Żݡ��������Wi-Fi" );  
        builder.setContentText( "���ǻ�������ֳ��е��ǻ�" );  
        Notification notification = builder.build();
        startForeground( 1, notification);
        DebugLogger.e(TAG,"����������������������������");
    }  
  
/*    @Override  
    public void onStart(Intent intent, int startId) {  
        Log.i(TAG, "ExampleService-onStart");  
        super.onStart(intent, startId);  
    } */ 
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        //ִ���ļ������ػ��߲��ŵȲ���  
        Log.i(TAG, "ExampleService-onStartCommand");  
        /*  
         * ���ﷵ��״̬������ֵ���ֱ���:  
         * 1��START_STICKY�����������������ʱ��ɱ����ϵͳ���������Ϊstarted״̬�����ǲ������䴫�ݵ�Intent����֮��ϵͳ�᳢�����´�������;  
         * 2��START_NOT_STICKY�����������������ʱ��ɱ��������û���µ�Intent���󴫵ݹ����Ļ���ϵͳ���������Ϊstarted״̬��  
         *   ����ϵͳ�������´�������ֱ��startService(Intent intent)�����ٴα�����;  
         * 3��START_REDELIVER_INTENT�����������������ʱ��ɱ�����������ڸ�һ��ʱ����Զ��������������һ�����ݵ�Intent���󽫻��ٴδ��ݹ�����  
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