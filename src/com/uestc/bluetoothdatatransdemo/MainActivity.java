package com.uestc.bluetoothdatatransdemo;

import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * 
 * @Description	three different fragment will be shown here
 * 
 * @Date		2015-10-22 下午7:30:37
 * @Author		SugarZ
 */
public class MainActivity extends FragmentActivity implements ChooseFragment.Callbacks {
	
	public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static final String NAME = "BluetoothDemo";
	public static final String TAG = "TestBluetoothDataTrans";
	public static final int CLIENT = 0;
	public static final int SERVER = 1;
	
	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// to get fm u should extends FragmentActivity
		fm = getSupportFragmentManager();
		// use ChooseFragment to replace the default 
		fm.beginTransaction().replace(R.id.main_activity, new ChooseFragment()).commit();
		
	}
	
	public void onButtonSelected(int id) {
		// change fragment by user choose
		FragmentTransaction transaction = fm.beginTransaction();
		if (id == MainActivity.CLIENT) {
			transaction.replace(R.id.main_activity, new ClientFragment());
			Log.i(TAG, "MainActivity: client fragment is selected ");
		} else {
			transaction.replace(R.id.main_activity, new ServerFragment());
			Log.i(TAG, "MainActivity: server fragment is selected ");
		}
		// and add the transaction to the back stack so the user can navigate back
		transaction.addToBackStack(null);
		transaction.commit();
	}

}
