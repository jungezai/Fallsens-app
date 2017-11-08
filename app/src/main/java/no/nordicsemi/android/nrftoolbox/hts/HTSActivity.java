/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package no.nordicsemi.android.nrftoolbox.hts;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cfx.cfxsemi.android.cfxtoolboxppscout.R;
import no.nordicsemi.android.nrftoolbox.hisdat.DateFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * HTSActivity is the main Health Thermometer activity. It implements {@link HTSManagerCallbacks} to receive callbacks from {@link HTSManager} class. The activity supports portrait and landscape
 * orientations.
 */


public class HTSActivity extends BleProfileActivity implements HTSManagerCallbacks,BleManagerCallbacks {
	private final String TAG = "HTSActivity";
	
	
	private TextView mHTSValue,mHTUValue,V_OX,V_OY,V_OZ,V_CX,V_CY,V_CZ,V_SVM;
	private EditText threshold_hth,ETX,ETY,ETZ,AL;
	private Button btn_calibrate;
	private RelativeLayout relalayout;
	private Handler mHandler = new Handler();
	private final static long SCAN_DURATION = 1000;
	//private boolean hasconnected = false;

	private boolean haseverbelow = true;
	private boolean isCancel = true;
	private boolean isReConnect = true;
	private boolean isMusicSatrted = false;
	
	
	
	private Calendar   c   =   Calendar.getInstance();
    private Date   date   =   c.getTime();
    private int times = 0;
    private int times_lost = 0;
    public static HTSActivity instance = null;
	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_feature_hts_myself);
		setGUI();
		//Intent intent = new Intent("fff");
/*		ComponentName componentName = new ComponentName("no.nordicsemi.android.nrftoolbox.hts.HTSManager","HTSManager");
		intent.setComponent(componentName);*/
		Intent intent = new Intent();
		intent.setAction("fff");         //你定义的service的action
		intent.setPackage(getPackageName());//这里你需要设置你应用的包名
		startService(intent);
	    DebugLogger.e(TAG,date.toString());
		instance = this;
	}


	private void setGUI() {
		mHTSValue = (TextView) findViewById(R.id.text_hts_value);
		mHTUValue = (TextView) findViewById(R.id.text_hth_value);
		relalayout = (RelativeLayout)findViewById(R.id.ffff);
		threshold_hth = (EditText)findViewById(R.id.editText1);
		ETX = (EditText) findViewById(R.id.editTextX);
		ETY = (EditText) findViewById(R.id.editTextY);
		ETZ = (EditText) findViewById(R.id.editTextZ);
		AL = (EditText) findViewById(R.id.alarmvalue);
        V_OX = (TextView) findViewById(R.id.odatax);
        V_OY = (TextView) findViewById(R.id.odatay);
        V_OZ = (TextView) findViewById(R.id.odataz);
        V_CX = (TextView) findViewById(R.id.cdatax);
        V_CY = (TextView) findViewById(R.id.cdatay);
        V_CZ = (TextView) findViewById(R.id.cdataz);
        V_SVM = (TextView) findViewById(R.id.svm);

		publicDATA.alernvalue=24.5;
		publicDATA.fallalern=false;
		publicDATA.time=0;
		publicDATA.count=0;
		publicDATA.count2=0;
		//btn_calibrate = (Button) findViewById(R.id.calibratebutton);
/*		write("324123435234253453\n");
		write("黄旭\n");*/
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hts_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hts_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HTSManager.HT_SERVICE_UUID;
	}

	@Override
	protected BleManager<HTSManagerCallbacks> initializeManager() {
		HTSManager manager = HTSManager.getHTSManager();
		manager.setGattCallbacks(this);
		return manager;
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or show some views			

	}

	@Override
	public void onHTIndicationEnabled() {
		// nothing to do here
	}
	//private Context mContext;

	//MediaPlayer mp = MediaPlayer.create(mContext, R.raw.yourbabyneedsyourhelp);
	//			mp.start();

    public void onV_SVMReceived(double value) {
        setV_SVMOnView(value);
    }
    private void setV_SVMOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_SVM.setText(formatedTemp.format(value));

                }
            });
			if(publicDATA.fallalern){
				V_SVM.setTextColor(Color.RED);
			}
			else if(publicDATA.psvm>=publicDATA.alernvalue){
				//DebugLogger.e(TAG, "------------------------------" );
				V_SVM.setTextColor(Color.GREEN);

			}
			else{
				//DebugLogger.e(TAG, "++++++++++++++++++++++++++++++" );
				V_SVM.setTextColor(Color.BLUE);
				}
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }

    public void onV_OXReceived(double value) {
        setV_OXOnView(value);
    }
    private void setV_OXOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_OX.setText(formatedTemp.format(value));

                }
            });
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }



    public void onV_OYReceived(double value) {
        setV_OYOnView(value);
    }
    private void setV_OYOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_OY.setText(formatedTemp.format(value));

                }
            });

        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }

    public void onV_OZReceived(double value) {
        setV_OZOnView(value);
    }
    private void setV_OZOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_OZ.setText(formatedTemp.format(value));

                }
            });
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }


    public void onV_CXReceived(double value) {
        setV_CXOnView(value);
    }
    private void setV_CXOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_CX.setText(formatedTemp.format(value));

                }
            });
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }


    public void onV_CYReceived(double value) {
        setV_CYOnView(value);
    }
    private void setV_CYOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_CY.setText(formatedTemp.format(value));

                }
            });
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }


    public void onV_CZReceived(double value) {
        setV_CZOnView(value);
    }
    private void setV_CZOnView(final double value) {
        if(value < 199) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DecimalFormat formatedTemp = new DecimalFormat("#0.00");
                    V_CZ.setText(formatedTemp.format(value));

                }
            });
        }
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
        //mp.release();

    }








    @Override
    public void onHTValueReceived(double value) {
        setHTSValueOnView(value);
    }

    private void setHTSValueOnView(final double value) {
		if(value < 199) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				DecimalFormat formatedTemp = new DecimalFormat("#0.00");
				mHTSValue.setText(formatedTemp.format(value));

			}
		});
		}
		//MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
		//mp.release();

	}




	@SuppressWarnings("deprecation")
	@Override
	public void onHTHuValueReceived(double value) {

		
		//double threshold = 70.0;
/*		BigDecimal data1 = new BigDecimal(value);
		BigDecimal data2 = new BigDecimal(HTSManager.threshold);
		
		if(data1.compareTo(data2) > 0) {
			if(!isMusicSatrted) {
			//mp.reset();
			final Timer mTimer = new Timer();	
			
			mp = MediaPlayer.create(this, R.raw.yourbabyneedsyourhelp);//重新设置要播放的音频
			mp.start();
			isMusicSatrted = true;
			
			
			mTimer.schedule(new TimerTask() {
				 @Override 
				public void run(){				 	
					 
						if((isMusicSatrted == false)||(times == 3)) {
							times = 0;
						mTimer.cancel();
						}
						MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.yourbabyneedsyourhelp);//重新设置要播放的音频
						mp.start();
						times++;
						
				 }

				},5000,5000);
			
	
			}
		} else {
			isMusicSatrted = false;
			Resources res = getResources(); 
			Drawable drawable = res.getDrawable(R.drawable.back);
			this.getWindow().setBackgroundDrawable(drawable);
			DebugLogger.e(TAG, "humi value < 80");
		}*/

		setHTUValueOnView(value);
		
	}
	
	private void setHTUValueOnView(final double value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				DecimalFormat formatedTemp = new DecimalFormat("#0.00");
				if(value == -100){
					mConnectButton.setClickable(false);
					mHTSValue.setText(R.string.not_available_value);
					DebugLogger.e(TAG, "humi value > 80");
					Resources res = getResources();//close the lost baby 
					Drawable drawable = res.getDrawable(R.drawable.lost);
					relalayout.setBackground(drawable);
				} else {
					mConnectButton.setClickable(true);
					mConnectButton.setText(R.string.action_disconnect);
				mHTUValue.setText(formatedTemp.format(value));
				if(HTSManager.isMusicSatrted) {
				
					DebugLogger.e(TAG, "humi value > 80");
					Resources res = getResources();
					Drawable drawable = res.getDrawable(R.drawable.awake);
					/* ImageView iv = new ImageView(this);*/
					relalayout.setBackground(drawable);
					//write();
					//date   =   c.getTime();
/*					if(haseverbelow) {
					c   =   Calendar.getInstance();
					date   =   c.getTime();
					write(c.getTime().toString()+"\n");
					haseverbelow = false;
					}*/
					}else {
						Resources res = getResources();
						Drawable drawable = res.getDrawable(R.drawable.back);
						relalayout.setBackground(drawable);
						/*haseverbelow = true;*/
					}
				}
			}
			
		});
		
	}	
	
	
	@Override
	protected void setDefaultUI() {
		mHTSValue.setText(R.string.not_available_value);
		mHTUValue.setText(R.string.not_available_value);
	}
	

/*	@Override
	public void onError(final String message, final int errorCode) {
		final Timer mTimer = new Timer();
		DebugLogger.e(TAG, "Error occured: " + message + ",  error code: " + errorCode);
		showToast(message + " (" + errorCode + ")");
		if(errorCode == 8) {
			if(errorCode > 0) {
		mp = MediaPlayer.create(this, R.raw.youhavelostyourbaby);//重新设置要播放的音频
		mp.start();
		}*/
/*		Resources res = getResources();
		Drawable drawable = res.getDrawable(R.drawable.lost);
		relalayout.setBackground(drawable);*/
		// refresh UI when connection failed、
	
	/*	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Resources res = getResources();
				Drawable drawable = res.getDrawable(R.drawable.lost);
				relalayout.setBackground(drawable);
				}
			
		});
	
		DebugLogger.e(TAG, "mBluetoothAdapter" + mBluetoothAdapter.getName());
		
		onDeviceDisconnected();
		if(errorCode == 8) {
			if(errorCode > 0) {
			if(isReConnect){
			hasconnected = false;
			isCancel = false;
		mTimer.schedule(new TimerTask() {
			 @Override 
			public void run(){				 	
					mBluetoothAdapter.startLeScan(mLEScanCallback);
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mBluetoothAdapter.stopLeScan(mLEScanCallback);
						}
					}, SCAN_DURATION);
					//mListValues.
					if(isCancel == true) {
					DebugLogger.e(TAG, "eeeeee");
					mTimer.cancel();
					}
			 }
			},5000,5000);
			}
		}
	}*/
	
	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();

		DebugLogger.e(TAG, "111111111111");
		//DebugLogger.e(TAG, "mBluetoothAdapter" + mBluetoothAdapter.getName());
		
	
		
			
/*			if(HTSManager.isReConnect){
				mConnectButton.setClickable(false);*/
/*				final Timer mTimer = new Timer();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Resources res = getResources();
						Drawable drawable = res.getDrawable(R.drawable.lost);
						relalayout.setBackground(drawable);
						}
					
				});	*/
				
				//mp = MediaPlayer.create(this, R.raw.youhavelostyourbaby);//重新设置要播放的音频
				//mp.start();
				

			

			//mDeviceConnected = false;
			//isCancel = false;
/*		mTimer.schedule(new TimerTask() {
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

					if(isCancel == true) {
					mConnectButton.setClickable(true);
					DebugLogger.e(TAG, "eeeeee");
					mTimer.cancel();
					times_lost = 0;
					}
					if((isCancel == false)&&(times_lost <= 3)) {
					MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.youhavelostyourbaby);//重新设置要播放的音频
					mp.start();
					times_lost++;
					} else {
						
					}
			 }
			 
			},5000,5000);*/
			
			/*}*/
		
		
		
		
	}


	
	
	
	/*
	
	
	
	private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null) {
				if(mDeviceName.equals(device.getName())&& (rssi > -80)) {
				
				if("CFX_DIAPER".equals(device.getName())) {
					DebugLogger.e(TAG, "gggggggggg");
					if(isCancel == false) {
					
					isCancel = true;
					DebugLogger.e(TAG, "ffffffffff");
					}
					if(mDeviceConnected == false) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mBleManager.connect(getApplicationContext(), device);
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
		
	};*/
	
	
	@Override
	public void onBackPressed() {
		HTSManager.isReConnect = false;
		Intent intent = new Intent();
		intent.setAction("fff");//你定义的service的action  
		intent.setPackage(getPackageName());//这里你需要设置你应用的包名
		stopService(intent);
		super.onBackPressed();
	}

	@Override
	public void onConnectClicked(final View view) {
		//DebugLogger.e(TAG, read());
		//read();
		if (isBLEEnabled()) {
			if (!HTSManager.mDeviceConnected) {/*蓝牙开了，没连接设备，想断了重新连*/
				HTSManager.isReConnect = true;
				setDefaultUI();
				showDeviceScanningDialog(getFilterUUID(), isCustomFilterUUID());
			} else {/*蓝牙开了，也连设备了，用户想断开，所以不重新连*/
				HTSManager.isReConnect = false;
				mBleManager.disconnect();
			}
		} else {/*蓝牙没开，没连设备，重新连*/
			HTSManager.isReConnect = true;
			showBLEDialog();
		}
	}


    public void onCalibrateClicked(final View view) {


        String SX=ETX.getText().toString();
        String SY=ETY.getText().toString();
        String SZ=ETZ.getText().toString();
        publicDATA.x=Double.parseDouble(SX);
        publicDATA.y=Double.parseDouble(SY);
        publicDATA.z=Double.parseDouble(SZ);
		publicDATA.alernvalue=Double.parseDouble(AL.getText().toString());
        DebugLogger.e(TAG, "onCalibrateClicked " );

    }


	public void onSetClicked(final View view) {
		//threshold_hth.setText("GGGGGGGGG");
		//Character.(threshold_hth.getText().toString())
		//char num[] = threshold_hth.getText().toString().toCharArray();
		try {
		HTSManager.threshold = Integer.parseInt(threshold_hth.getText().toString());
		}
		catch (NumberFormatException ex){
			//System.out.println("The String does not contain a parsable integer");
			showToast("The String does not contain a parsable integer");
		}
	}
	
	public void onCheckClicked(final View view) {
		showHistoryDataDialog();
	}
	
	private void showHistoryDataDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final DateFragment dialog = DateFragment.getInstance();
				dialog.show(getFragmentManager(), "hisdata_fragment");
			}
		});
	}


	
	
	
	
	


}

