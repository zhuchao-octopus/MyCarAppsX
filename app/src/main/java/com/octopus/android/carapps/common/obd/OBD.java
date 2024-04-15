package com.octopus.android.carapps.common.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class OBD {
	private final static String TAG = "OBD";
	private final static String TAG2 = "OBD2";
	private final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothDevice mRomoteDevice = null;
	private BluetoothSocket mBluetoothSocket = null;
	private InputStream mInputStream = null;
	private OutputStream mOutputStream = null;
	private Thread mThreadRecv = null;
	private Thread mThreadConnect = null;
	public String mDeviceAddress = null;
	public String mDeviceName = null;

	private static final int STATE_IDLE = 0;
	private static final int STATE_INIT = 1;
	private static final int STATE_NORMAL = 2;
	private int obd_recv_state =  STATE_IDLE;
	
	public final static int OBD_RPM = 1;
	public final static int OBD_SPEED = 2;
	
	public final static int OBD_QUERY_INFO = 1002;

	public final static int OBD_RPM_MAX = 16380;
	public final static int OBD_SPEED_MAX = 255;
	
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 100:
//				mTVInfo.setText("SPEED: " + mOBD.mVehicleInfo.speed + 
//						"\nRPM: " + mOBD.mVehicleInfo.rpm + 
//						"\nCOOLANT: " + mOBD.mVehicleInfo.coolant_temperature +
//						"\nFULE[0]: " + mOBD.mVehicleInfo.fuel[0] +
//						"\nFULE[1]: " + mOBD.mVehicleInfo.fuel[1] +
//						"\nFULE[2]: " + mOBD.mVehicleInfo.fuel[2] +
//						"\nFULE[3]: " + mOBD.mVehicleInfo.fuel[3]
//						);
				break;
			case 101:
//				String str = (String) msg.obj;
//				if (str != null)
//					Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
				break;
			case 102:
//				mBTNConnect.setText(mOBD.mConnected ? "Disconnect" : "Connect");
//				Toast.makeText(getActivity(),
//						mOBD.mConnected ? "Connected" : "Disconnected",
//						Toast.LENGTH_LONG).show();
				break;
			case 103:
//				if (!mBTNConnect.isEnabled())
//					mBTNConnect.setEnabled(true);
				break;
			}
		}
		
	};
	
	class CmdRespToken {
		boolean resp;

		CmdRespToken(boolean r) {
			resp = r;
		}
	}

	private CmdRespToken mCmdRespToken = new CmdRespToken(false);

	private void notifyCmdRespToken() {
		synchronized (mCmdRespToken) {
			mCmdRespToken.resp = true;
			mCmdRespToken.notify();
		}
	}

	class VehicleInfo {
		public float speed = 0.0f;
		public float rpm = 0.0f;
		public float coolant_temperature = 0.0f;
		public float[] fuel = { 0.0f, 0.00f, 0.00f, 0.00f };
	}

	public VehicleInfo mVehicleInfo = new VehicleInfo();

	/**
	 * reference to https://en.wikipedia.org/wiki/OBD-II_PIDs
	 * https://wenku.baidu.com/view/0451a26b10661ed9ad51f3d8.html
	 */
	public static final String CMD_PIDs_supported1 = "0100"; // 4bytes,PIDs
																// supported [01
																// - 20]
	public static final String CMD_Engine_coolant_temperature = "0105"; // 1bytes,-40-215,°C,A-40
	public static final String CMD_Short_term_fuel_trim1 = "0106"; // 1bytes,((100/128)*A)
																	// - 100
	public static final String CMD_Long_term_fuel_trim1 = "0107"; // 1bytes,((100/128)*A)
																	// - 100
	public static final String CMD_Short_term_fuel_trim2 = "0108"; // 1bytes,((100/128)*A)
																	// - 100
	public static final String CMD_Long_term_fuel_trim2 = "0109"; // 1bytes,((100/128)*A)
																	// - 100
	public static final String CMD_Engine_RPM = "010C"; // 2bytes, (256A + B) /
														// 4
	public static final String CMD_Vehicle_speed = "010D"; // 1bytes, A
	public static final String CMD_OBD_standards = "011C"; // 1bytes,OBD
															// standards this
															// vehicle conforms
															// to

	public static final String RESP_PIDs_supported1 = "4100";
	public static final String RESP_Engine_coolant_temperature = "4105";
	public static final String RESP_Short_term_fuel_trim1 = "4106";
	public static final String RESP_Long_term_fuel_trim1 = "4107";
	public static final String RESP_Short_term_fuel_trim2 = "4108";
	public static final String RESP_Long_term_fuel_trim2 = "4109";
	public static final String RESP_Engine_RPM = "410C";
	public static final String RESP_Vehicle_speed = "410D";
	public static final String RESP_OBD_standards = "411C";

	public boolean mConnected = false;

	public OBD() {
		mConnected = false;
		getBluetoothAdapter();
	}

	private boolean getBluetoothAdapter() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		return (mBluetoothAdapter != null);
	}

	/**
	 * @return :
	 * 	0: no bonded device 
	 * 	1: found default bonded device 
	 * 	2: not found default bonded device, but have bonded devices, need user to select one
	 */
	public int getDefultBondedDevices() {
		Set<BluetoothDevice> s = mBluetoothAdapter.getBondedDevices();
		if (s != null) {
			for (BluetoothDevice bd : s) {
				Log.d(TAG, bd.getName() + "," + bd.getAddress());
				if (bd.getName().startsWith("OBDII")) {
					mDeviceAddress = bd.getAddress();
					mDeviceName = bd.getName();
					return 1;
				}
			}
			for (BluetoothDevice bd : s) {
				if (bd.getName().startsWith("OBD")) {
					mDeviceAddress = bd.getAddress();
					mDeviceName = bd.getName();
					return 1;
				}
			}
			return 2;
		}
		return 0;
		/*
		 * Log.d(TAG, "address: " + mDeviceAddress + " ,name: " + mDeviceName +
		 * " ,state: " + mBluetoothAdapter.getState());
		 */
	}

	public Set<BluetoothDevice> getBondedDevices() {
		if (getBluetoothAdapter()) {
			return mBluetoothAdapter.getBondedDevices();
		}
		return null;
	}

	private BluetoothSocket getBluetoothSocket(BluetoothDevice bluetoothDevice) {
		try {
			BluetoothSocket insecureSocket = bluetoothDevice
					.createInsecureRfcommSocketToServiceRecord(UUID
							.fromString(SPP_UUID));
			return insecureSocket;
		} catch (IOException e1) {
			Log.e(TAG, "Create Insecure socket failed" + e1.toString());
			try {
				BluetoothSocket secureSocket = bluetoothDevice
						.createRfcommSocketToServiceRecord(UUID
								.fromString(SPP_UUID));
				return secureSocket;
			} catch (Exception e) {
				Log.e(TAG, "Create socket failed:" + e.toString());
				return null;
			}
		}
	}

	private final String[] mOBDQuerInfoCmds = {
			OBD.CMD_Engine_coolant_temperature,
			OBD.CMD_Engine_RPM,
			OBD.CMD_Vehicle_speed,
			// OBD.CMD_Short_term_fuel_trim1,
			// OBD.CMD_Long_term_fuel_trim1,
			// OBD.CMD_Short_term_fuel_trim2,
			// OBD.CMD_Long_term_fuel_trim2,
	};

	public boolean canQuerInfo = true;
	public void queryInfo() {
		if (mConnected && canQuerInfo) {
			canQuerInfo = false;
			sendCmds(mOBDQuerInfoCmds, 10000, 3,
				new Interface_SendCmdsCallback() {
					@Override
					public void callback(int arg0, String arg1) {
						// TODO Auto-generated method stub
						canQuerInfo = true;

					}

				});
		}
	}

	private boolean connectBT() {
		if (!getBluetoothAdapter())
			return false;

		if (mDeviceAddress == null || mDeviceAddress.isEmpty()) {
			int ret = getDefultBondedDevices();
			if (ret == 1) {
				Log.d(TAG, "get default bonded address ok " + mDeviceName + ":"
						+ mDeviceAddress + ":" + mBluetoothAdapter.getState());
				mHandler.sendMessage(mHandler.obtainMessage(101,
						"Find a default OBD device " + mDeviceName));
			} else if (ret == 2) {
				mHandler.sendMessage(mHandler.obtainMessage(101,
						"No OBDII bonded found"));
				return false;
			} else {
				mHandler.sendMessage(mHandler.obtainMessage(101,
						"No bonded address found"));
				return false;
			}
		}

		if (mDeviceAddress != null) {
			try {
				mBluetoothAdapter.cancelDiscovery();
				// mDeviceAddress = "00:1D:A5:00:04:36"; //OBDII
				mRomoteDevice = mBluetoothAdapter
						.getRemoteDevice(mDeviceAddress);
				Log.d(TAG,
						"get remote device ok, addr:"
								+ mRomoteDevice.getAddress() + " ,name:"
								+ mRomoteDevice.getName() + " ,bondstate:"
								+ mRomoteDevice.getBondState() + " ,type:"
								//+ mRomoteDevice.getType() + " ,uuids:"
							//	+ mRomoteDevice.getUuids()
								);
				if (mRomoteDevice.getAddress() == null
						|| mRomoteDevice.getName() == null) {
					Log.e(TAG, "get remote device addr and name error");
					return false;
				}
				mBluetoothSocket = getBluetoothSocket(mRomoteDevice);
				if (mBluetoothSocket == null)
					return false;
				Log.d(TAG, "create socket ok");
				mBluetoothSocket.connect();
				Log.d(TAG, "connect socket ok");
				mInputStream = mBluetoothSocket.getInputStream();
				Log.d(TAG, "get inputstream ok");
				mOutputStream = mBluetoothSocket.getOutputStream();
				Log.d(TAG, "get outputstream ok");

				startReceiveThread();

				return true;
			} catch (Exception e2) {
				Log.e(TAG, "connect failed1: " + e2.toString());
				try {
					if (mBluetoothSocket != null)
						mBluetoothSocket.close();
					mBluetoothSocket = null;
				} catch (IOException e) {
					Log.e(TAG, "connect failed2: " + e2.toString());
				}
			}
		}
		return false;
	}

			private final String[] mOBDInitCmds = { 
					"ATZ", 		//reset all general
					"AT@1", 	//display the device description general
					"ATL0",		//linefeeds off general(don't + 0x0a)					
					"ATE0",		//turn-off echo					
					"ATST62",	//timeout
					"ATSP0", 	//SP h: set protocol to h and save it. SP Ah: set protocol to auto, h and save it.
			};

	private void initOBD() {
		obd_recv_state = STATE_INIT;
		sendCmds(mOBDInitCmds, 5000, 5, new Interface_SendCmdsCallback() {
			@Override
			public void callback(int arg0, String arg1) {
				// TODO Auto-generated method stub
				if (arg0 >= mOBDInitCmds.length) {
					obd_recv_state = STATE_NORMAL;
					mConnected = true;
					mHandler.sendEmptyMessage(102);
					Log.d(TAG, "connect success");
				}
			}

		});
	}

	public void connect() {
		obd_recv_state = STATE_IDLE;
		if (mThreadConnect != null && mThreadConnect.isAlive()) {
			mThreadConnect.interrupt();
			mThreadConnect = null;
		}
		mThreadConnect = new Thread(new Runnable() {
			@Override
			public void run() {
				int i;
				for (i = 0; i < 10; i++) {
					if (connectBT())
						break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				if (i < 10)
					initOBD();
				mHandler.sendEmptyMessage(103);
			}
		});
		mThreadConnect.start();
	}

	public void disconnect() {
		try {
			if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
				// if (mInputStream != null)
				// mInputStream.close();
				// if (mOutputStream != null)
				// mOutputStream.close();
				mBluetoothSocket.close();
				mInputStream = null;
				mOutputStream = null;
				mBluetoothSocket = null;
				if (mThreadRecv != null) {
					mThreadRecv.interrupt();
					mThreadRecv = null;
				}
				Log.d(TAG, "disconnect ok");
			}
			obd_recv_state = STATE_IDLE;
			mConnected = false;
//			mBTNConnect.setText("Connect");
		} catch (Exception e) {
			Log.e(TAG, "disconnect failed:" + e.toString());
		}
	}

	private void startReceiveThread() {
		if (mThreadRecv != null && mThreadRecv.isAlive()) {
			mThreadRecv.interrupt();
			mThreadRecv = null;
		}
		mThreadRecv = new Thread(new Runnable() {
			@Override
			public void run() {
				receiver();
			}
		});
		mThreadRecv.start();
	}

	private int removeBeginWith0D(byte[] data, int len) {
		int n = 0;
		while (n < len && data[0] == (byte) 0x0d) {
			n++;
			System.arraycopy(data, 1, data, 0, len - n);
		}
		return len - n;
	}

	private byte[] hexStringToByte(String data) {
		if (data == null || data.isEmpty())
			return null;
		try {
			int n = data.length();
			if (n % 2 != 0)
				return null;
			n /= 2;
			byte[] d = new byte[n];
			for (int i = 0; i < n; i++) {
				int t = Integer.valueOf(data.substring(i * 2, i * 2 + 2), 16);
				d[i] = (byte) t;
			}
			return d;
		} catch (Exception e) {
			Log.e(TAG, "hexStringToByte faield: " + e.toString() + "," + data);
		}
		return null;
	}

	private void calculateFuel(int index, byte data) {
		mVehicleInfo.fuel[index] = (data & 0xff) / 1.28f - 100.0f;
		notifyCmdRespToken();
		Log.d(TAG, "fuel" + index + " [" + mVehicleInfo.fuel[index] + "]");
	}

	boolean bNextDataCanDeal = false;

	/**
	 * AT@1 determined data respon format, if send:
	 * D/OBDBT: recv str 21 [>41 0C FF D0 00 00 00 ]
	 * otherwise like this:
	 * D/OBDBT: recv str 5 [>010C]
	 * D/OBDBT: recv str 21 [41 0C FF D0 00 00 00 ]
	 */
	private void dealData(byte[] data) {
		try {
			if (data != null && data.length > 0) {
				String result = new String(data);
				Log.d(TAG, "recv " + data.length + " [" + result + "]");
				result = result.replace(" ", "");
				if (result.equals("STOPPED")) {
						/**
						 * 09-11 12:35:53.866 3045-3101/? W/OBDBT: >>>>>>>>>> send 010C
						 * 09-11 12:35:53.886 3045-3098/? D/OBDBT: recv 7 [STOPPED]
						 * 09-11 12:35:54.887 3045-3098/? D/OBDBT: recv 22 [>41 00 FF FF FF FF FF ]
						 * ww: when received STOPPED must to suspend for a while and then can to send
						 */
						Log.w(TAG, "recv STOPPED, pause 1s");
						Thread.sleep(1000);
						return;
				} else if (obd_recv_state == STATE_INIT) {
					if (result.startsWith(">"))
						notifyCmdRespToken();
				} else if (obd_recv_state == STATE_NORMAL) {
						if (result.equals(">" + CMD_Engine_RPM) ||
								result.equals(">" + CMD_Vehicle_speed) ||
								result.equals(">" + CMD_Engine_coolant_temperature) ||
								result.equals(">" + CMD_OBD_standards) ||
								result.equals(">" + CMD_Short_term_fuel_trim1) ||
								result.equals(">" + CMD_Long_term_fuel_trim1) ||
								result.equals(">" + CMD_Short_term_fuel_trim2) ||
								result.equals(">" + CMD_Long_term_fuel_trim2) ||
								result.equals(">" + RESP_PIDs_supported1) ||
								result.equals(">SEARCHING...")) {
								bNextDataCanDeal = true;
								return;
						} else {	//AT@1 format
								if (result.startsWith(">" + RESP_Engine_RPM) ||
										result.startsWith(">" + RESP_Vehicle_speed) ||
										result.startsWith(">" + RESP_Engine_coolant_temperature) ||
										result.startsWith(">" + RESP_OBD_standards) ||
										result.startsWith(">" + RESP_Short_term_fuel_trim1) ||
										result.startsWith(">" + RESP_Long_term_fuel_trim1) ||
										result.startsWith(">" + RESP_Short_term_fuel_trim2) ||
										result.startsWith(">" + RESP_Long_term_fuel_trim2) ||
										result.startsWith(">" + RESP_PIDs_supported1) ||
										result.startsWith(">SEARCHING...")) {
										bNextDataCanDeal = true;
										result = result.substring(1);
								}
						}
						
						if (bNextDataCanDeal && result != null) {
								byte[] hexValue = hexStringToByte(result);
								// if (hexValue != null)
								// Log.d(TAG, "hexStringToByte: " + hexValue.length +
								// " [" + toHex(hexValue, hexValue.length) + "]");
								// else
								// Log.d(TAG, "hexStringToByte: " + result);
								if (hexValue != null && hexValue.length >= 4) {
									if (result.startsWith(RESP_PIDs_supported1)) {
										notifyCmdRespToken();
									} else if (result.startsWith(RESP_OBD_standards)) {
										notifyCmdRespToken();
									} else if (result.startsWith(RESP_Engine_RPM)) {
										mVehicleInfo.rpm = ((hexValue[2] & 0xff) * 256 + (hexValue[3] & 0xff)) / 4.0f;
										notifyCmdRespToken();
		//								mHandler.sendEmptyMessage(100);
										callBack(OBD_SPEED, mVehicleInfo.speed); 
										Log.d(TAG, "RPM [" + mVehicleInfo.rpm + "]");
									} else if (result.startsWith(RESP_Vehicle_speed)) {
										mVehicleInfo.speed = hexValue[2] & 0xff;
										notifyCmdRespToken();
		//								mHandler.sendEmptyMessage(100);
		
										callBack(OBD_RPM, mVehicleInfo.rpm); 
										Log.d(TAG, "SPEED [" + mVehicleInfo.speed + "]");
									} else if (result
											.startsWith(RESP_Engine_coolant_temperature)) {
										mVehicleInfo.coolant_temperature = (hexValue[2] & 0xff) - 40;
										notifyCmdRespToken();
										mHandler.sendEmptyMessage(100);
										Log.d(TAG, "COOLANT TEMP ["
												+ mVehicleInfo.coolant_temperature
												+ "]");
									} else if (result
											.startsWith(RESP_Short_term_fuel_trim1)) {
										calculateFuel(0, hexValue[2]);
										mHandler.sendEmptyMessage(100);
									} else if (result
											.startsWith(RESP_Long_term_fuel_trim1)) {
										calculateFuel(1, hexValue[2]);
										mHandler.sendEmptyMessage(100);
									} else if (result
											.startsWith(RESP_Short_term_fuel_trim2)) {
										calculateFuel(2, hexValue[2]);
										mHandler.sendEmptyMessage(100);
									} else if (result
											.startsWith(RESP_Long_term_fuel_trim2)) {
										calculateFuel(3, hexValue[2]);
										mHandler.sendEmptyMessage(100);
									}
								}
						}
						bNextDataCanDeal = false;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "deal data failed:" + e.toString());
			e.printStackTrace();
		}
	}

	private void receiver() {
		if (mInputStream != null) {
			byte data[] = new byte[512];
			int byteOffset = 0;
			try {
				Log.d(TAG2, "start recv ...");
				while (true) {
					int len = mInputStream.read(data, byteOffset, data.length
							- byteOffset);
					if (len > 0) {
						byteOffset += len;
						Log.d(TAG2, "recv byte " + byteOffset + "," + len);
						byteOffset = removeBeginWith0D(data, byteOffset);
						// Log.d(TAG, "after rm  " + byteOffset + " [" +
						// toHex(data, byteOffset) + "]");
						if (byteOffset > 0) {
							int i;
							for (i = 0; i < byteOffset; i++) {
								if (data[i] == (byte) 0x0d)
									break;
							}
							if (i < byteOffset) {
								byte[] dataLine = new byte[i];
								System.arraycopy(data, 0, dataLine, 0, i);
								// Log.d(TAG, "after dataLine " + i + " [" +
								// toHex(dataLine, i) + "]");
								byteOffset -= (i + 1);
								System.arraycopy(data, i + 1, data, 0,
										byteOffset);
								// Log.d(TAG, "after data " + byteOffset + " ["
								// + toHex(data, byteOffset) + "]");
								dealData(dataLine);
							}
						}
					}
				}
			} catch (IOException e2) {
				Log.e(TAG, "recv failed1:" + e2.toString());
			} catch (Exception e) {
				Log.e(TAG, "recv failed2:" + e.toString());
			}
		}
	}

	public void sendCmds(final String[] cmds, final int timeout,
			final int retryTimes, final Interface_SendCmdsCallback callback) {
		if (cmds == null || cmds.length <= 0)
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int index = 0;
				int retry = 0;
				while (index < cmds.length && retry < retryTimes) {
					synchronized (mCmdRespToken) {
						try {
							mCmdRespToken.resp = false;
							sendCmd(cmds[index]);
							mCmdRespToken.wait(timeout);
							if (mCmdRespToken.resp) {
								retry = 0;
								index++;
							} else {
								retry++;
								Log.d(TAG, "sendCmds " + cmds[index]
										+ " timeout," + " retry " + retry);
							}
						} catch (InterruptedException e) {
							retry++;
							Log.e(TAG, "sendCmds failed: " + e.toString());
						}
					}
				}

				if (callback != null)
					callback.callback(index, null);

				if (index >= cmds.length) {
					Log.d(TAG, "cmds send success");
				}
			}
		}).start();
	}

	public void sendCmd(String cmd) {
		if (cmd == null || cmd.isEmpty())
			return;
		if (mOutputStream != null) {
			try {
				byte[] osBytes = (cmd + "\r").getBytes();
				mOutputStream.write(osBytes);
				mOutputStream.flush();
				// Log.d(TAG, "send " + osBytes.length + "[" + toHex(osBytes,
				// osBytes.length) + "]");
			//	Log.w(TAG, ">>>>>>>>>> send " + cmd);
			} catch (IOException e1) {
				Log.e(TAG, "send failed: " + e1.toString());
			} catch (Exception e) {
				Log.e(TAG, "send failed: " + e.toString());
			}
		}
	}
	
	private Handler mObdCallback;
	public void setCallback(Handler cb){
		mObdCallback = cb;
	}
	private void callBack(int what, Object obj){
		if(mObdCallback!=null){
			mObdCallback.sendMessage(mObdCallback.obtainMessage(what, obj));
		}
	}
	private void callBack(int what, int arg1, int arg2){
		if(mObdCallback!=null){
			mObdCallback.sendMessage(mObdCallback.obtainMessage(what, arg1, arg2));
		}
	}
	public interface Interface_SendCmdsCallback {
		void callback(int arg0, String arg1);
	}
}
