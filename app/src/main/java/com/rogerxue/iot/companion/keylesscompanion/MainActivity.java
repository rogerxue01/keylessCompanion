package com.rogerxue.iot.companion.keylesscompanion;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG = "main";
    private static final String DB_TAB_REQUEST = "request";
    private static final String DB_COL_TIMESTAMP = "timestamp";
    private static final String DB_COL_ADNROID_ID = "android_id";

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothDevice mDevice;
    private FloatingActionButton mFab1;
    private FloatingActionButton mFab2;
    private FloatingActionButton mFab3;
    private TextView mDeviceIdTextView;
    private FirebaseDatabase mDatabase;
    private String mAndroidId;


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothGattCharacteristic characteristic =
            new BluetoothGattCharacteristic(
                    DeviceProfile.CHARACTERISTIC_PIN_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(new UUID[] {DeviceProfile.SERVICE_UUID}, mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            mDevice = bluetoothDevice;
            mFab1.setEnabled(true);
            Log.d(TAG, "found BT device: " + bluetoothDevice);
        }
    };

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceIdTextView = findViewById(R.id.device_id);
        mFab1 = findViewById(R.id.fab1);
        mFab1.setEnabled(false);
        mBluetoothAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        mAndroidId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        mDeviceIdTextView.setText(mAndroidId);
        Log.d(TAG, "android id: " + mAndroidId);

        mFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevice != null) {
                    // sent pin
                    BluetoothGatt gatt = mDevice.connectGatt(MainActivity.this, true, mGattCallback);
                    characteristic.setValue("some value");
                    gatt.writeCharacteristic(characteristic);
                } else {
                    Log.w(TAG, "no device connected");
                }
            }
        });

        mFab2 = findViewById(R.id.fab2);
        mFab2.setEnabled(true);
        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference request = mDatabase.getReference(DB_TAB_REQUEST).push();
                request.child(DB_COL_TIMESTAMP).setValue(ServerValue.TIMESTAMP);
                request.child(DB_COL_ADNROID_ID).setValue(mAndroidId);
            }
        });

        mFab3 = findViewById(R.id.fab3);
        mFab3.setEnabled(false);

        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
