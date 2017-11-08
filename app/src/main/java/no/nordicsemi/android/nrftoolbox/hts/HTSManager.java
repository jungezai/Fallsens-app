/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package no.nordicsemi.android.nrftoolbox.hts;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cfx.cfxsemi.android.cfxtoolboxppscout.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * HTSManager class performs BluetoothGatt operations for connection, service discovery, enabling indication and reading characteristics. All operations required to connect to device with BLE HT
 * Service and reading health thermometer values are performed here. HTSActivity implements HTSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class HTSManager extends Service implements BleManager<HTSManagerCallbacks>{
	private final String TAG = "HTSManager";
	private HTSManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;
	private BluetoothDevice mDevice;
	MediaPlayer mp;
	public static BluetoothAdapter mBluetoothAdapter;

	
	public final static UUID HT_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");

	private static final UUID HT_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
	private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";

	private BluetoothGattCharacteristic mHTCharacteristic, mBatteryCharacteritsic;
	private BluetoothGattService mHTService, mBatteryService;

	private boolean isHTServiceFound = false;
	private boolean isBatteryServiceFound = false;

	private final int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
	private final int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
	private final int SHIFT_LEFT_8BITS = 8;
	private final int SHIFT_LEFT_16BITS = 16;
	private final int GET_BIT24 = 0x00400000;
	private static final int FIRST_BIT_MASK = 0x01;

	private static HTSManager managerInstance = null;

	static boolean isMusicSatrted = false;
	static boolean haseverbelow = true;
	static boolean isCancel = true;
	static boolean isReConnect = true;
	static double threshold = 70.0;
	private Calendar   c   =   Calendar.getInstance();
    private Date   date   =   c.getTime();
    private int times = 0;
    private int times_lost = 0;
	public static boolean mDeviceConnected = false;

	public static String mDeviceName;
	private Handler mHandler = new Handler();
	private final static long SCAN_DURATION = 1000;
	public final static long ERROR_CODE = -100;
	public static boolean isExist = false;



	/**
	 * singleton implementation of HTSManager class
	 */

    public void onCreate() {
        Log.i(TAG, "ExampleService-onCreate");  
        DebugLogger.e(TAG,"威威威威威威威威威威威威威威eeeee");
        super.onCreate();

        Intent intent = new Intent(this, HTSActivity. class);
        intent.putExtra( "ficationId", 1);  
        Notification.Builder builder = new Notification.Builder(this);  
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(contentIntent); //设置了此项内容之后，点击通知栏的这个消息，就跳转到MainActivity  
        builder.setSmallIcon(R.drawable.gg,1);
        builder.setTicker("CFX Technology");
        builder.setContentTitle("PPscout");  
        builder.setContentText("BABY PEE AND HEALTH MOITOR");
        Notification notification = builder.build();
        startForeground( 1, notification);

        DebugLogger.e(TAG,"威威威威威威威威威威威威威威");
    }


	public static synchronized HTSManager getHTSManager() {
		if (managerInstance == null) {
			managerInstance = new HTSManager();
		}
		return managerInstance;
	}

	/**
	 * callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(HTSManagerCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	@Override
	public void connect(Context context, BluetoothDevice device) {
		mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
		mContext = context;
		mDevice = device;
		
	}

	@Override
	public void disconnect() {
		DebugLogger.d(TAG, "Disconnecting device");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			DebugLogger.d(TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

			// skip other devices
			if (!device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			if (bondState == BluetoothDevice.BOND_BONDED) {
				// We've read Battery Level, now enabling HT indications 
				if (mHTCharacteristic != null) {
					enableHTIndication();
				}
				mContext.unregisterReceiver(this);
				mCallbacks.onBonded();
			}
		}
	};

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					DebugLogger.d(TAG, "Device connected");
					mBluetoothGatt.discoverServices();
					//This will send callback to HTSActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					DebugLogger.d(TAG, "Device disconnected");
					//This will send callback to HTSActivity when device get disconnected
					mCallbacks.onDeviceDisconnected();
					//serveReconnect();
				}
			} else {
				mCallbacks.onError(ERROR_CONNECTION_STATE_CHANGE, status);
/*				if(status == 133) {
				DebugLogger.e(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				connect(mContext,mDevice);
				}*/
				serveReconnect();
			}
		}



		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			isHTServiceFound = false;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(HT_SERVICE_UUID)) {
						isHTServiceFound = true;
						mHTService = service;
					}
					if (service.getUuid().equals(BATTERY_SERVICE)) {
						mBatteryService = service;
						isBatteryServiceFound = true;
					}
				}
				if (isHTServiceFound) {
					mCallbacks.onServicesDiscovered(false);
				} else {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
				if (isBatteryServiceFound) {
					readBatteryLevel();
				} else if (isHTServiceFound) {
					enableHTIndication();
				}
			} else {
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status);
				serveReconnect();
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC)) {
					int batteryValue = characteristic.getValue()[0];
					batteryValue = batteryValue & 0xFF;
					//if(batteryValue > 100)
						//batteryValue = 100;
					double a = ((double)(batteryValue & 0x7f)/5);
					DebugLogger.e(TAG, "batt value: " + batteryValue);
					if((batteryValue & 0x80 )> 0)
						a = -a;
					DebugLogger.e(TAG, "batt value: " + batteryValue);
					

					//mCallbacks.onBatteryValueReceived(a);
					if (isHTServiceFound) {
						enableHTIndication();
					}
					//readBatteryLevel();
				}
			} else {
				DebugLogger.e(TAG, "Error on read characteristic " + " Error code: " + status);
				mBluetoothGatt.close();
				mCallbacks.onError(ERROR_READ_CHARACTERISTIC, status);
				serveReconnect();
				
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			double x_res = 0.0;
			double y_res = 0.0;
			double z_res = 0.0;
			double x_real;
			double y_real;
			double z_real;
			double SVM2 = 0.0;//SVM1 = 0.0,
			byte flag;
			if (characteristic.getUuid().equals(HT_MEASUREMENT_CHARACTERISTIC_UUID)) {
				if (isBatteryServiceFound)
					//readBatteryLevel();
				try {
					x_res = decodeTemperature(characteristic.getValue());
					y_res = decodeY(characteristic.getValue());
					z_res = decodeZ(characteristic.getValue());
					x_real = x_res+publicDATA.x;
					y_real = y_res+publicDATA.y;
					z_real = z_res+publicDATA.z;
					//svm1 原始svm
					//SVM1 = java.lang.Math.pow((java.lang.Math.pow(x_res,2) +
					//		  java.lang.Math.pow(y_res,2) +
					//		  java.lang.Math.pow(z_res,2)),0.5);
					//svm2 校正svm
					SVM2 = java.lang.Math.pow((java.lang.Math.pow(x_real,2) +
							java.lang.Math.pow(y_real,2) +
							java.lang.Math.pow( z_real,2)),0.5);
                    publicDATA.psvm = SVM2;


					mCallbacks.onHTValueReceived(x_res);
					mCallbacks.onHTHuValueReceived(y_res);
					mCallbacks.onBatteryValueReceived(z_res);
                    //需在HTSManagerCallbacks.java中声明
					mCallbacks.onV_OXReceived(x_res);
					mCallbacks.onV_OYReceived(y_res);
					mCallbacks.onV_OZReceived(z_res);
					mCallbacks.onV_CXReceived(x_real);
					mCallbacks.onV_CYReceived(y_real);
					mCallbacks.onV_CZReceived(z_real);

					mCallbacks.onV_SVMReceived(SVM2);
					//mCallbacks.onHTHuValueReceived(y_res);
					//mCallbacks.onBatteryValueReceived(z_res);


					//serverHTHuValueReceived(y_res);				
					DebugLogger.e(TAG, "x_res value: " + x_res);				
					DebugLogger.e(TAG, "y_res value: " + y_res);
					DebugLogger.e(TAG, "z_res value: " + z_res);
					//DebugLogger.e(TAG, "calib value: " + ETX.getText().toString());
					//DebugLogger.e(TAG, "y_res value: " + y_res);
					//DebugLogger.e(TAG, "z_res value: " + z_res);

					//write("Before:"+"\n");
					//write(Double.toString(x_res)+" "+"\n");
					//write(Double.toString(y_res)+" "+"\n");
					//write(Double.toString(z_res)+" "+"\n");
					//write("SVM:"+Double.toString(SVM1)+"\n");
					//write("After Calibrate"+"("+Double.toString(publicDATA.x)+","+
					//		                    Double.toString(publicDATA.y)+","+
					//		                    Double.toString(publicDATA.z)+"):"+"\n");
					write(Double.toString(x_real)+" ");
					write(Double.toString(y_real)+" ");
					write(Double.toString(z_real)+" ");
					write(Double.toString(SVM2)+"\n");

					//time相当于一个防止重复播放声音的延时
					//if(publicDATA.time>140) {//DebugLogger.e(TAG, "startmusic exist========== " );
					//	publicDATA.time=0;
					//}

					//svm超过临界值
					 if(publicDATA.psvm>=publicDATA.alernvalue){
						 publicDATA.count++;
						 //排除后7个数据
						 if(publicDATA.count>7)
						 {
							 //三次检测数据
							 if((!publicDATA.fallalern)&&(publicDATA.count2<3))
							 {
								 publicDATA.count2++;
								 if(  ((x_real>5)&&(y_real<5))||
									  ((z_real>5)&&(y_real<5))  )
								 //符合x>0.5g&&y<0.5g或者z>0.5g&&y<0.5g
								 {
									 publicDATA.fallalern=true;
									 //if(publicDATA.time==0||publicDATA.time==70){
										 //DebugLogger.e(TAG, "startmusic start========== "+publicDATA.time);
									 MediaPlayer mp = MediaPlayer.create(mContext, R.raw.youhavelostyourbaby);//重新设置要播放的音频
									 mp.start();

									 //}

								 }
								 else {
									 //publicDATA.fallalern=false;
									 //publicDATA.count=0;
									 //publicDATA.count2=0;
								 }
							 }
							 //超过三次或者已经判断为跌倒
							 else{

							 }
						 }
						 //publicDATA.time++;
						 //DebugLogger.e(TAG, "---------------------- "+publicDATA.time);
					}
					else {
						 publicDATA.fallalern=false;
						 //publicDATA.time = 0;
						 publicDATA.count=0;
						 publicDATA.count2=0;
					 }

				} catch (Exception e) {
					DebugLogger.e(TAG, "invalid temperature value");
				}
			}
		}


		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// HT indications has been enabled
				DebugLogger.e(TAG, "ssssssssssssssssssssssssssssss");
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
					mCallbacks.onBondingRequired();

					final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
					mContext.registerReceiver(mBondingBroadcastReceiver, filter);
				} else {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, ERROR_WRITE_DESCRIPTOR + " (" + status + ")");
				mCallbacks.onError(ERROR_WRITE_DESCRIPTOR, status);
				serveReconnect();
			}
		}
	};

	private void readBatteryLevel() {
		mBatteryCharacteritsic = mBatteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
		if (mBatteryCharacteritsic != null) {
			DebugLogger.e(TAG, "reading battery characteristic");
			mBluetoothGatt.readCharacteristic(mBatteryCharacteritsic);
		} else {
			DebugLogger.e(TAG, "Battery Level Characteristic is null");
		}
	}
	private void serveReconnect() {
		// TODO Auto-generated method stub
		DebugLogger.e(TAG, "22222");
		
		if(isReConnect){
			//mConnectButton.setClickable(false);
			final Timer mTimer = new Timer();

/*				runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Resources res = getResources();
					Drawable drawable = res.getDrawable(R.drawable.lost);
					relalayout.setBackground(drawable);
					}
				
			});	*/

			

		

		mDeviceConnected = false;
		isCancel = false;
	mTimer.schedule(new TimerTask() {
		 @Override 
		public void run(){				 	
				mBluetoothAdapter.startLeScan(mLEScanCallback);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mBluetoothAdapter.stopLeScan(mLEScanCallback);
						//mp.release();
					}
				}, SCAN_DURATION);
				//mListValues.
				mCallbacks.onHTHuValueReceived(-100);
				if((isCancel == true)||(isReConnect == false)) {
				//mConnectButton.setClickable(true);
				DebugLogger.e(TAG, "eeeeee");
				mTimer.cancel();
				times_lost = 0;
				}
				if((isCancel == false)&&(times_lost <= 3)) {
					if(isExist){
						MediaPlayer mp = MediaPlayer.create(mContext, R.raw.youhavelostyourbaby);//重新设置要播放的音频
						mp.start();
						times_lost++;
					}
				} else {
					
				}
		 }
		 
		},5000,5000);
		
		}
	}

	private void serverHTHuValueReceived(double tempValue) {
		// TODO Auto-generated method stub
		BigDecimal data1 = new BigDecimal(tempValue);
		BigDecimal data2 = new BigDecimal(threshold);
		
		if(data1.compareTo(data2) > 0) {
			if(!isMusicSatrted) {
			//mp.reset();
			final Timer mTimer = new Timer();	
			

			isMusicSatrted = true;
			if(isMusicSatrted){
				if(haseverbelow) {
				c   =   Calendar.getInstance();
				date   =   c.getTime();
				write(c.getTime().toString()+"\n");
				haseverbelow = false;
				} 
			}
			
			
			
			mTimer.schedule(new TimerTask() {
				 @Override 
				public void run(){				 	
					 
						if((isMusicSatrted == false)||(times == 3)) {
							times = 0;
						mTimer.cancel();
						}
						if(isExist){
							MediaPlayer mp = MediaPlayer.create(mContext, R.raw.yourbabyneedsyourhelp);//重新设置要播放的音频
						mp.start();
						times++;
						}
						

						
				 }

				},5000,5000);
			
	
			}
		} else {
			isMusicSatrted = false;
/*			Resources res = getResources(); 
			Drawable drawable = res.getDrawable(R.drawable.back);
			this.getWindow().setBackgroundDrawable(drawable);*/
			DebugLogger.e(TAG, "humi value < 80");
			haseverbelow = true;
		}
		
	}

	/**
	 * enable Health Thermometer indication on Health Thermometer Measurement characteristic
	 */
	private void enableHTIndication() {
		DebugLogger.e(TAG, "enableHTIndication()");
		mHTCharacteristic = mHTService.getCharacteristic(HT_MEASUREMENT_CHARACTERISTIC_UUID);
		mBluetoothGatt.setCharacteristicNotification(mHTCharacteristic, true);
		BluetoothGattDescriptor descriptor = mHTCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 * This method decode temperature value received from Health Thermometer device First byte {0} of data is flag and first bit of flag shows unit information of temperature. if bit 0 has value 1
	 * then unit is Fahrenheit and Celsius otherwise Four bytes {1 to 4} after Flag bytes represent the temperature value in IEEE-11073 32-bit Float format
	 */
	private double decodeTemperature(byte[] data) throws Exception {
		double temperatureValue = 0.0;
		byte flag = data[0];
		byte exponential = data[4];
		short firstOctet = convertNegativeByteToPositiveShort(data[1]);
		short secondOctet = convertNegativeByteToPositiveShort(data[2]);
		short thirdOctet = convertNegativeByteToPositiveShort(data[3]);
		int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
		mantissa = getTwosComplimentOfNegativeMantissa(mantissa);
		DebugLogger.e(TAG, "mantissa value: " + mantissa);
		temperatureValue = (mantissa * Math.pow(10, exponential));
		/*
		 * Conversion of temperature unit from Fahrenheit to Celsius if unit is in Fahrenheit
		 * Celsius = (98.6*Fahrenheit -32) 5/9
		 */
		
/*		if ((flag & FIRST_BIT_MASK) != 0) {
			temperatureValue = (float) ((98.6 * temperatureValue - 32) * (5 / 9.0));
		}*/
		return temperatureValue;

	}

	private double decodeY(byte[] data) throws Exception {
		double temperatureValue = 0.0;
		byte flag = data[0];
		byte exponential = data[8];
		short firstOctet = convertNegativeByteToPositiveShort(data[5]);
		short secondOctet = convertNegativeByteToPositiveShort(data[6]);
		short thirdOctet = convertNegativeByteToPositiveShort(data[7]);
		int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
		mantissa = getTwosComplimentOfNegativeMantissa(mantissa);
		DebugLogger.e(TAG, "mantissa value: " + mantissa);
		temperatureValue = (mantissa * Math.pow(10, exponential));
		/*
		 * Conversion of temperature unit from Fahrenheit to Celsius if unit is in Fahrenheit
		 * Celsius = (98.6*Fahrenheit -32) 5/9
		 */
		
/*		if ((flag & FIRST_BIT_MASK) != 0) {
			temperatureValue = (float) ((98.6 * temperatureValue - 32) * (5 / 9.0));
		}*/
		return temperatureValue;

	}
	
	private double decodeZ(byte[] data) throws Exception {
		double temperatureValue = 0.0;
		byte flag = data[0];
		byte exponential = data[12];
		short firstOctet = convertNegativeByteToPositiveShort(data[9]);
		short secondOctet = convertNegativeByteToPositiveShort(data[10]);
		short thirdOctet = convertNegativeByteToPositiveShort(data[11]);
		int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
		mantissa = getTwosComplimentOfNegativeMantissa(mantissa);
		DebugLogger.e(TAG, "mantissa value: " + mantissa);
		temperatureValue = (mantissa * Math.pow(10, exponential));
		/*
		 * Conversion of temperature unit from Fahrenheit to Celsius if unit is in Fahrenheit
		 * Celsius = (98.6*Fahrenheit -32) 5/9
		 */
		
/*		if ((flag & FIRST_BIT_MASK) != 0) {
			temperatureValue = (float) ((98.6 * temperatureValue - 32) * (5 / 9.0));
		}*/
		return temperatureValue;

	}
	
	private short convertNegativeByteToPositiveShort(byte octet) {
		if (octet < 0) {
			return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
		} else {
			return octet;
		}
	}

	private int getTwosComplimentOfNegativeMantissa(int mantissa) {
		if ((mantissa & GET_BIT24) != 0) {
			return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
		} else {
			return mantissa;
		}
	}

	@Override
	public void closeBluetoothGatt() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		resetStatus();
	}

	private void resetStatus() {
		isHTServiceFound = false;
		isBatteryServiceFound = false;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
/*	public void onError(final String message, final int errorCode) {
		DebugLogger.e(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		connect(mContext,mDevice);
	}*/
	public final String read() {
        try {
        	FileInputStream inStream = mContext.openFileInput("message.txt");
            byte[] buffer = new byte[1024];
            int hasRead = 0;
            StringBuilder sb = new StringBuilder();
            while ((hasRead = inStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, hasRead));
            }

            inStream.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return null;
    }
    
    public void write(String msg){
        // 步骤1：获取输入值
        if(msg == null) return;
        try {
        	
        	File file = new File(Environment.getExternalStorageDirectory(),
        			"message.txt");
        	
        	
        	
        	
        	
        	
        	
            // 步骤2:创建一个FileOutputStream对象,MODE_APPEND追加模式
            FileOutputStream fos = mContext.openFileOutput("message.txt",
                    MODE_APPEND);
            // 步骤3：将获取过来的值放入文件
            //FileOutputStream fos = null;
            fos = new FileOutputStream(file,true);
            fos.write(msg.getBytes());
            // 步骤4：关闭数据流
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null) {
				if(mDeviceName.equals(device.getName())&& (rssi > -80)) {
				
				/*if("CFX_DIAPER".equals(device.getName())) {*/
					DebugLogger.e(TAG, "gggggggggg");
					if(isCancel == false) {
					
					isCancel = true;
					DebugLogger.e(TAG, "ffffffffff");
					}
					if(mDeviceConnected == false) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							//connect(getApplicationContext(), device);
							if(isExist)
								connect(mContext, device);
							//else
								
						}
					}, 6000);
					
					mDeviceConnected = true;
					}
					
					//return;
				//mListValues.add(device);
				}

				DebugLogger.e(TAG, "Device" + device.getName() + ", rssi " + rssi);
				DebugLogger.e(TAG,"caonima");
			}
		}
		
	};
}
