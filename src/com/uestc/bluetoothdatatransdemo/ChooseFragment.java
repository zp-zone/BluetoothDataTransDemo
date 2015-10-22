package com.uestc.bluetoothdatatransdemo;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @Description	shown as a choose interface and this is the first UI to be shown 
 * 
 * @Date		2015-10-22 下午7:03:27
 * @Author		SugarZ
 */
public class ChooseFragment extends Fragment {

	private static final int REQUEST_BT_ENABLE = 2;
	
	private Callbacks mCallback = emptyCallback;
	private BluetoothAdapter btAdapter;
	private Button bt_client;
	private Button bt_server;
	private TextView tv_msg;

	public interface Callbacks {
		void onButtonSelected(int id);
	}

	// make output info 
	private void handleMsg(String msg) {
		tv_msg.append(msg + "\n");
	}

	/**
	 * 
	 * @Description start bluetooth on the device
	 * @param 
	 * @return void 
	 * @throws
	 */
	public void startBluetooth() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			handleMsg("This device does not support bluetooth");
			return;
		}
		if (!btAdapter.isEnabled()) {
			handleMsg("There is bluetooth, but turned off");
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
		} else {
			handleMsg("The bluetooth is ready to use.");
			searchPaired();
		}
	}

	/**
	 * 
	 * @Description search all paired bluetooth 
	 * 				cant list new bluetooth device, if need u can add the code
	 * @param 
	 * @return void 
	 * @throws
	 */
	private void searchPaired() {
		handleMsg("Paired Devices:");
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			final BluetoothDevice btDev[] = new BluetoothDevice[pairedDevices
					.size()];
			String devInfo;
			int i = 0;
			for (BluetoothDevice devicel : pairedDevices) {
				btDev[i] = devicel;
				devInfo = btDev[i].getName() + ": " + btDev[i].getAddress();
				handleMsg("Device: " + devInfo);
				i++;
			}

		} else {
			handleMsg("There are no paired devices");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_BT_ENABLE) {
			if (resultCode == Activity.RESULT_OK) {
				handleMsg("Bluetooth is on.");
				searchPaired();
			} else {
				handleMsg("Please turn the bluetooth on.");
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View myView = inflater.inflate(R.layout.fragment_choose, container,
				false);
		tv_msg = (TextView) myView.findViewById(R.id.tv_msg);

		bt_client = (Button) myView.findViewById(R.id.bt_client);
		bt_client.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onButtonSelected(MainActivity.CLIENT);
			}
		});

		bt_server = (Button) myView.findViewById(R.id.bt_server);
		bt_server.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onButtonSelected(MainActivity.SERVER);
			}
		});

		startBluetooth();

		return myView;
	}

	// these blow are functions for callback
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mCallback = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = emptyCallback;
	}

	private static Callbacks emptyCallback = new Callbacks() {

		@Override
		public void onButtonSelected(int id) {
			// nothing but like a null
		}
	};

}
