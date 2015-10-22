package com.uestc.bluetoothdatatransdemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @Description will be shown if user choose the device as a bluetooth server
 * 
 * @Date 2015-10-22 下午7:06:12
 * @Author SugarZ
 */
public class ServerFragment extends Fragment {

	private BluetoothAdapter btAdapter = null;

	private TextView tv_output;
	private Button bt_start_server;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			tv_output.append(msg.getData().getString("msg"));
		}

	};

	public void handleMsg(String str) {
		// handler junk, because thread can't update screen!
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public void startServer() {
		new Thread(new AcceptThread()).start();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View myView = inflater.inflate(R.layout.fragment_server, container,
				false);
		tv_output = (TextView) myView.findViewById(R.id.tv_output);

		bt_start_server = (Button) myView.findViewById(R.id.bt_start_server);
		bt_start_server.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tv_output.append("Starting server\n");
				startServer();
			}
		});

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			tv_output.append("No bluetooth device.\n");
			tv_output.setEnabled(false);
		}
		return myView;
	}

	/**
	 * 
	 * @Description	server thread, wait for the client 
	 * 				then can exchange some data when connection established
	 * 
	 * @Date		2015-10-22 下午7:24:46
	 * @Author		SugarZ
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				// get the BluetoothServerSocket 
				tmp = btAdapter.listenUsingRfcommWithServiceRecord(
						MainActivity.NAME, MainActivity.MY_UUID);
			} catch (IOException e) {
				handleMsg("Failed to start server\n");
			}
			mServerSocket = tmp;
		}

		public void run() {
			handleMsg("waiting on accept");
			BluetoothSocket socket = null;
			try {
				// establish the socket connect with the client
				socket = mServerSocket.accept();
			} catch (IOException e) {
				handleMsg("Failed to accept\n");
			}

			if (socket != null) {
				handleMsg("Connection made\n");
				handleMsg("Remote device address: "
						+ socket.getRemoteDevice().getAddress().toString()
						+ "\n");
				try {
					handleMsg("Attempting to receive a message ...\n");
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String str = in.readLine();
					handleMsg("received a message:\n" + str + "\n");

					PrintWriter out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					handleMsg("Attempting to send message ...\n");
					out.println("Reponse from Bluetooth Demo Server");
					out.flush();
					handleMsg("Message sent...\n");

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
			handleMsg("Server ending \n");
		}

		public void cancel() {
			try {
				mServerSocket.close();
			} catch (IOException e) {
				handleMsg("close() of connect socket failed: "
						+ e.getMessage().toString() + "\n");
			}
		}
	}
}
