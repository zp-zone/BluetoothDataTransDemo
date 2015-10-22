package com.uestc.bluetoothdatatransdemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @Description will be shown if user choose the device as a bluetooth client
 * 
 * @Date 2015-10-22 下午7:05:44
 * @Author SugarZ
 */
public class ClientFragment extends Fragment {
	private Button bt_start_client;
	private Button bt_choose_device;
	private TextView tv_output;
	BluetoothAdapter btAdapter = null;
	BluetoothDevice device;
	BluetoothDevice remoteDevice;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			tv_output.append(msg.getData().getString("msg"));
		}

	};

	public void handleMsg(String str) {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View myView = inflater.inflate(R.layout.fragment_client, container,
				false);

		tv_output = (TextView) myView.findViewById(R.id.tv_output);
		bt_choose_device = (Button) myView.findViewById(R.id.bt_choose_device);
		bt_choose_device.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchPaired();
			}

		});
		
		bt_start_client = (Button) myView.findViewById(R.id.bt_start_client);
		bt_start_client.setEnabled(false);
		bt_start_client.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tv_output.append("Starting client\n");
				startClient();
			}

		});
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			tv_output.append("No bluetooth device.\n");
			bt_start_client.setEnabled(false);
			bt_choose_device.setEnabled(false);
		}
		Log.i(MainActivity.TAG, "ClientFragment: bluetooth");

		return myView;
	}

	/**
	 * 
	 * @Description search the paired device and list it in a AlertDialog
	 * 
	 * @param 
	 * @return void 
	 * @throws
	 */
	private void searchPaired() {
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			tv_output.append("at least 1 paired device\n");
			final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices
					.size()];
			String[] items = new String[blueDev.length];
			int i = 0;
			for (BluetoothDevice devicel : pairedDevices) {
				blueDev[i] = devicel;
				items[i] = blueDev[i].getName() + ": "
						+ blueDev[i].getAddress();
				tv_output.append("Device: " + items[i] + "\n");
				i++;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Choose Bluetooth:");
			builder.setSingleChoiceItems(items, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							if (item >= 0 && item < blueDev.length) {
								device = blueDev[item];
								bt_start_client.setText("device: "
										+ blueDev[item].getName());
								bt_start_client.setEnabled(true);
							}

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	/**
	 * 
	 * @Description start client thread to trans data
	 * 
	 * @param 
	 * @return void 
	 * @throws
	 */
	private void startClient() {
		if (device != null) {
			new Thread(new ConnectThread(device)).start();
		}
	}

	/**
	 * 
	 * @Description	client thread , connect to server device and establish a bluetooth socket
	 * 				then send and receive some simple data  
	 * 
	 * @Date		2015-10-22 下午7:21:16
	 * @Author		SugarZ
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket socket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket temp = null;
			// create a bluetooth socket with the choosed device
			try {
				temp = device
						.createRfcommSocketToServiceRecord(MainActivity.MY_UUID);
			} catch (IOException e) {
				handleMsg("Client connection failed: "
						+ e.getMessage().toString() + "\n");
			}
			socket = temp;
		}

		public void run() {
			handleMsg("Client running\n");
			// close discovery because its power consumed
			btAdapter.cancelDiscovery();

			try {
				socket.connect();
			} catch (IOException e) {
				handleMsg("Connect failed\n");
				try {
					socket.close();
					socket = null;
				} catch (IOException e1) {
					handleMsg("unable to close() socket during connection failure: "
							+ e1.getMessage().toString() + "\n");
					socket = null;
				}
			}
			// connect sucessed then we can use this socket to trans data
			if (socket != null) {
				handleMsg("Connection established \n");
				handleMsg("Remote device address: "
						+ socket.getRemoteDevice().getAddress().toString()
						+ "\n");
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					handleMsg("Attempting to send message ...\n");
					out.println("hello from Bluetooth Demo Client");
					out.flush();
					handleMsg("Message sent...\n");

					handleMsg("Attempting to receive a message ...\n");
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String str = in.readLine();
					handleMsg("received a message:\n" + str + "\n");
					handleMsg("We are done, closing connection\n");
				} catch (Exception e) {
					handleMsg("Error happened sending/receiving\n");
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						handleMsg("Unable to close socket" + e.getMessage()
								+ "\n");
					}
				}
			} else {
				handleMsg("Made connection, but socket is null\n");
			}
			handleMsg("Client ending \n");
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				handleMsg("close() of connect socket failed: "
						+ e.getMessage().toString() + "\n");
			}
		}
	}
}
