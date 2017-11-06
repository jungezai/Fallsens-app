/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package no.nordicsemi.android.nrftoolbox.hisdat;

import java.util.Set;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.hts.HTSActivity;
import no.nordicsemi.android.nrftoolbox.hts.HTSManager;
import cfx.cfxsemi.android.cfxtoolboxppscout.R;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * ScannerFragment class scan required BLE devices and shows them in a list. This class scans and filter devices with standard BLE Service UUID and devices with custom BLE Service UUID It contains a
 * list and a button to scan/cancel. There is a interface {@link OnDeviceSelectedListener} which is implemented by activity in order to receive selected device. The scanning will continue for 5
 * seconds and then stop
 */
public class DateFragment extends DialogFragment {


	//private DeviceListAdapter mAdapter;



	private static AlertDialog mdialog;



	/**
	 * Static implementation of fragment so that it keeps data when phone orientation is changed For standard BLE Service UUID, we can filter devices using normal android provided command
	 * startScanLe() with required BLE Service UUID For custom BLE Service UUID, we will use class ScannerServiceParser to filter out required device
	 */
	public static DateFragment getInstance() {
		final DateFragment fragment = new DateFragment();
		return fragment;
	}


	/**
	 * When dialog is created then set AlertDialog with list and button views
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_data_history, null);
		//final ScrollView scrollview = (ScrollView) dialogView.findViewById(R.id.scrollView1);
		//scrollview.
		//listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
		//listview.setAdapter(mAdapter = new DeviceListAdapter(mContext));
		TextView textview = (TextView) dialogView.findViewById(R.id.textView1);
		
		textview.setMovementMethod(ScrollingMovementMethod.getInstance());
		HTSManager manager = HTSManager.getHTSManager();
		textview.setText(manager.read());
		Button mcanceldigbut = (Button) dialogView.findViewById(R.id.candig);
		//mcanceldigbut.setOnClickListener(dialogView.);
		//builder.setTitle(R.string.scanner_title);
		final AlertDialog dialog = builder.setView(dialogView).create();
		mdialog = dialog;
//		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
//				stopScan();
//				dialog.cancel();
//				mListener.onDeviceSelected(((ExtendedBluetoothDevice) mAdapter.getItem(position)).device);
//			}
//		});
//
//		mcanceldigbut = (Button) dialogView.findViewById(R.id.action_cancel);
		mcanceldigbut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDigcancelClicked(v);
			}
		});
//
//		addBondedDevices();
//		if (savedInstanceState == null)
//			startScan();
		return dialog;
	}

	public void onDigcancelClicked(final View view) {
		//threshold_hth.setText("GGGGGGGGG");
		mdialog.cancel();
	}

}
