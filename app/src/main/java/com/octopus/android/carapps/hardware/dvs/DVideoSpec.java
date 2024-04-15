package com.octopus.android.carapps.hardware.dvs;

import android.content.Intent;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class DVideoSpec {
	final static String TAG = "DVideoSpec";
	static {
		System.loadLibrary("DVD");
	}

	public DVideoSpec() {
		TQ = false;
	}

	public int mDVDStatus = 0;
	public int mDVDPower = 0;

	private boolean TQ = false;
	// public static final int MSC_SOURCE_INDEX_DVD = 0x1;
	// public static final int MSC_LOCK_BACKCAR = 0xb;
	//
	public static final int MSC_DVD = 0x1;
	public static final int MSC_CAMERA_DVD = 0x2;
	public static final int MSC_READ_SOURCE = 0x80;
	public static final int MSC_READ_LOCK = 0x81;

	public static final int MSC_DVD_LOCK = 0xf0;
	public static final int MSC_DVD_UNLOCK = 0xf1;
	public static final int MSC_DVD_LOCK2 = 0xfc;

	public static final int DVS_INITIALIZE = 0x01; // 0x00
	public static final int DVS_RC = 0x02;
	public static final int DVS_VS = 0x03;
	public static final int DVS_AS = 0x04;
	public static final int DVS_OS = 0x05;
	public static final int DVS_UP = 0x06;
	public static final int DVS_MPS = 0x07;
	public static final int DVS_SMC = 0x08;
	public static final int DVS_READ_INFO = 0x09;
	public static final int DVS_PO = 0x10; // 0x01
	public static final int DVS_SD = 0x11;
	public static final int DVS_STOP = 15;
	public static final int DVS_EJECT = 0x13;
	public static final int DVS_LOAD = 0x14;
	public static final int DVS_PLAY = 12;// 13; // is play pause function now
	public static final int DVS_PR = 0x16;
	public static final int DVS_PP = 0x17;
	public static final int DVS_PT = 0x18;
	public static final int DVS_PAUSE = 14;
	public static final int DVS_JF = 0x1a;
	public static final int DVS_JR = 0x1c;
	public static final int DVS_SF = 20; // fast forward
	public static final int DVS_SR = 21; // fast back
	public static final int DVS_NEXT = 23;
	public static final int DVS_PREVIOUS = 24;
	public static final int DVS_NAVIGATION_CMD = 0x21;
	public static final int DVS_NUMERIC_CMD = 0x22;
	public static final int DVS_ANGLE = 0x23;
	public static final int DVS_AUDIO = 19;
	public static final int DVS_TITLE = 31;
	public static final int DVS_SUBTITLE = 45;
	public static final int DVS_OD = 0x27;
	public static final int DVS_REPEAT = 29;
	public static final int DVS_SCAN = 0x29;
	public static final int DVS_SHUFFLERANDOM = 28;
	public static final int DVS_AIM = 0x2b;
	public static final int DVS_RAB = 0x2c;
	public static final int DVS_ZOOM = 0x2d;
	public static final int DVS_VOLUME = 0x2e;
	public static final int DVS_3D = 0x2f;
	public static final int DVS_AO = 0x30;
	public static final int DVS_TOUCH = 0x31;
	public static final int DVS_SET_SOURCE_CARD = 0x32;
	public static final int DVS_SET_SOURCE_DISC = 0x33;
	public static final int DVS_SET_SOURCE_USB = 0x34;
	public static final int DVS_GSR = 0x40; // 0x10
	public static final int DVS_MESR = 0x51; // 0x11
	public static final int DVS_MHVIDR = 0x60;
	public static final int DVS_GOTO_TITLE = 0x61;
	public static final int DVS_POWER_OFF = 0x62;
	public static final int DVS_RESUME = 0x63;
	public static final int DVS_PLAY_PTT = 0x64;
	public static final int DVS_CHANG_AREA = 0x65;
	public static final int DVS_UPGRADE_CH = 0x66;

	public static final int DVS_MEDIA_STATUS_REQ = 0x70;
	public static final int DVS_MEDIA_PTT_REQ = 0x71;
	public static final int DVS_AUDIO_STATE_REQ = 0x72;
	public static final int DVS_VERSION_REQ = 0x73;
	public static final int DVS_FOLDER_NAME_INFO_REQ = 0x75;
	public static final int DVS_FILE_NAME_INFO_REQ = 0x76;
	public static final int DVS_MEDIA_TYPE_FOLDER_FILE_NAME_INFO_REQ = 0x77;

	public static final int DVS_COMMON_CMD_1_PARAM = 0x7a;// param1=cmd,param2=1
	public static final int DVS_COMMON_CMD_2_PARAM = 0x7b;// param1=cmd,param2=2
	public static final int DVS_COMMON_CMD_3_PARAM = 0x7c;// param2=cmd<<8|1,param2=2

	public static final int DVS_OPEN = 0x80;
	public static final int DVS_CLOSE = 0xff;

	public static final int DVD_RETURN_TOUCH_MODE = 0x90;

	public static final int DVD_RETURN_DISK_MODE = 0x88;

	public static final int DVD_RETURN_PLAY_STATUS = 0x8a;

	public static final int DVD_RETURN_TRACK_NUM = 0x9c;

	public static final int DVD_RETURN_CUR_TRACK_NUM = 0x84;

	public static final int DVD_RETURN_CUR_TIME = 0x87;

	public static final int DVD_RETURN_REPEAT_MODE = 0x8b;
	public static final int DVD_RETURN_SHUFFLE_MODE = 0x8d;
	

	public static final int DVD_RETURN_DISK_ROLL_STATUS = 0xbe;
	public static final int DVD_RETURN_PLAY_RATE = 0x95;
	
	public static final int DVD_RETURN_TOTAL_TIME = 0x10003;

	public static final int DVD_POWER_STATUS = 0x10000;
	public static final int DVD_DISK_STATUS = 0x10001;

	public static final int DVS_READ_DISKSTATUS = 0x90;
	public static final int DVS_READ_PLAYSTATUS = 0x91;
	public static final int DVS_READ_MEDIATYPE = 0x92;
	public static final int DVS_READ_TRACKNUMBER = 0x93;
	public static final int DVS_READ_TOTALTRACK = 0x94;
	public static final int DVS_READ_PLAYTOTALTIME = 0x95;
	public static final int DVS_READ_PLAYCURTIME = 0x96;
	public static final int DVS_READ_FILETYPE = 0x97;

	public static final int DVS_READ_PLAYMODE = 0x98;

	public static final int DVS_READ_FILENAME = 0x99;
	public static final int DVS_READ_TOTALFOLDER = 0x9a;
	public static final int DVS_READ_FOLDERNUMBER = 0x9b;
	public static final int DVS_READ_REPEAT = 0x9c;
	public static final int DVS_READ_RANDOM = 0x9d;


	public static final int DVS_SET_MEMORY = 0x0300;

	public static final int DVD_DISK_TYPE_CD = 0x1;
	public static final int DVD_DISK_TYPE_DVD = 0x2;
	// timer
	public static final int DVS_PLAYMODE_SD = 0x1;
	public static final int DVS_PLAYMODE_DISC = 0x0;
	public static final int DVS_PLAYMODE_USB = 0x2;

	public static final int DVS_DISKSTATUS = 0x00000001;
	public static final int DVS_PLAYSTATUS = 0x00000002;
	public static final int DVS_TIMERSTATUS = 0x00000004;
	public static final int DVS_MEDIASTATUS = 0x00000008;
	public static final int DVS_MEDIAPTT = 0x00000010;
	public static final int DVS_PLAYTIME = 0x00000020;
	public static final int DVS_FILETYPE = 0x00000040;

	public static final int DVS_VERSION = 0x00000080;
	public static final int DVS_PLAYRANDOM = 0x00000100;
	public static final int DVS_PLAYREPEAT = 0x00000200;

	public static final int DVS_SOFTWARE_UPGRADE = 0x000000b5;
	public static final int DVS_FOLDER_NAME_REPORT = 0x000000d9;
	public static final int DVS_FILE_NAME_REPORT = 0x000000da;

	public static final int DVD_BIND_SERVICE_FINISH = 0x10000000;

	public static final int DVS_DISKSTATUS_OUT = 0x06;
	public static final int DVS_DISKSTATUS_NODISK = 0x07;
	public static final int DVS_DISKSTATUS_IN = 0x09;

	public static final int DVS_PLAYSTATUS_STOP = 0x00;
	public static final int DVS_PLAYSTATUS_PREPARE_STOP = 0x01;
	public static final int DVS_PLAYSTATUS_PLAY = 0x02;
	public static final int DVS_PLAYSTATUS_SR = 0x5;
	public static final int DVS_PLAYSTATUS_SF = 0x7;
	public static final int DVS_PLAYSTATUS_PAUSE = 0x08;
	public static final int DVS_PLAYSTATUS_FILELIST = 0x0a;

	public static final int DVS_MEDIA_TYPE_NONE = 0;
	public static final int DVS_MEDIA_TYPE_DVD_VIDEO = 0x1;
	public static final int DVS_MEDIA_TYPE_CD_VIDEO = 0x5;
	public static final int DVS_MEDIA_TYPE_SVCD_VIDEO = 0x6;
	public static final int DVS_MEDIA_TYPE_CD_TXT = 0x8;

	public static final int DVS_MEDIA_TYPE_CD = 0x84;
	public static final int DVS_MEDIA_TYPE_USB = 0x2;

	public static final int DVS_MEDIA_TYPE_CDDA = 0x84;
	public static final int DVS_MEDIA_TYPE_CDROM = 0x02;
	public static final int DVS_FILE_TYPE_IDLE = 0x00;
	public static final int DVS_FILE_TYPE_MP3WMA = 0x01;
	public static final int DVS_FILE_TYPE_PICTURE = 0x02;
	public static final int DVS_FILE_TYPE_VEDIO = 0x03;

	public int sendDVDControlCommand(int value) {
		if (0x0C == value) value=0x0D;
		value = ((TQ?0x0300:0x0200) | value);
		return sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
	}

	public int sendDVDLangCommand(int value) {
		value = ((TQ?0x5700:0x0F00) | value);
		return sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
	}
	
	public void sendDVDControlSelect(int index) {

		// int v;
		// while (true) {
		//
		// if (value < 10) {
		// break;
		// }
		//
		// sendDVDControlCommand(0x11);
		// Util.doSleep(1);
		// value = value - 10;
		// }
		++index;
		int value;

		value = (index & 0xff00) >> 8;
		value = ((TQ?0x6200:0x0b00) | value);
		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);

		value = (index & 0xff);
		value = ((TQ?0x6300:0x0c00) | value);
		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);

	}

	private final static int DVD_W = 250;
	private final static int DVD_H = 250;
	private int mScreenWidth = 1024;
	private int mScreenHeight = 600;

	public void updateScreen(int w, int h) {
		mScreenWidth = w;
		mScreenHeight = h;
	}

	private long mLastDVDCmdTime = 0;

	public void sendDVDTouchCommand(int x, int y) {
		if ((System.currentTimeMillis() - mLastDVDCmdTime) < 500) {
			return;
		}
		x = x * DVD_W / mScreenWidth;
		y = y * DVD_H / mScreenHeight;
		int value = (0x8000 | ((x & 0xfe) << 7) | (y & 0xff));

		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
//		Log.d("aa", x + ":" + y);
		Util.doSleep(1);

		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
		Util.doSleep(1);

		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
		Util.doSleep(1);

		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, value);
		Util.doSleep(1);

		sendDVSCommand(DVS_COMMON_CMD_1_PARAM, 0xfefe);
		mLastDVDCmdTime = System.currentTimeMillis();
	}

	private native final int nativeSendDVSCommand(int cmd);

	private native final int nativeSendDVSCommand(int cmd, int param1,
			int param2);

	public int sendDVSCommand(int value) {
		return nativeSendDVSCommand(value);
	}

	public int sendDVSCommand(int cmd, int param) {

		return nativeSendDVSCommand(cmd, param, 0);
	}

	public int sendDVSCommand(int cmd, int param1, int param2) {
		return nativeSendDVSCommand(cmd, param1, param2);
	}

	public int sendDVSSetMemoryCommand() {
		if (TQ) {
			return sendDVSCommand(DVS_COMMON_CMD_1_PARAM,
					0x1000);
		} else {
			return sendDVSCommand(DVS_COMMON_CMD_1_PARAM,
					DVS_SET_MEMORY);
		}
	}

	private IDVSCallback mServiceCallback;
	private int mNextCmd = 0;
	private static final int NEXT_CMD_TOTAL_TIME = 1;

	private void dvdVGQHandler(byte[] param, int len) {
		int ret = 0;
		if (mNextCmd == NEXT_CMD_TOTAL_TIME) {
			mTotalTime = (((param[0] & 0xff) << 8) | (param[1] & 0xff));
			// param[0]
			ret = DVD_RETURN_TOTAL_TIME;
			mNextCmd = 0;
		} else {
			ret = (int) (param[0] & 0xff);
			switch ((int) (param[0] & 0xff)) {
			case 0x7F:
				if ((0xff&param[1]) == 0x55) {
					Log.i(TAG, "We got a TQ DVD machine");
					TQ = true;
				}
				break;
			case DVD_RETURN_TOUCH_MODE:
				mTouchMode = param[1];
				// Log.d(TAG, "mTouchMode:" + param[1]);
				break;
			case DVD_RETURN_DISK_MODE:
				Log.d(TAG, "mDiskType:" + param[1]);
				mDiskType = (0xff&param[1]);
				notifyLauncherInfo();
				break;
			case DVD_RETURN_PLAY_STATUS:
				mPlayStatus = param[1];
				if (!(mPlayStatus == 3 || mPlayStatus == 4)) {
					mPlayRate = 0;
				}
				Log.d(TAG, "mPlayStatus:" + param[1]);
				notifyLauncherInfo();
				break;
			case 0x83:
				mTrackCurrent = (int) ((param[1] & 0xff) << 8);
				break;
			case DVD_RETURN_CUR_TRACK_NUM:
				mTrackCurrent |= (int) (param[1] & 0xff);
//				Log.d(TAG, "mTrackCurrent:" + mTrackCurrent);
				break;
			case 0x9b:
				mTrackNum = (int) ((param[1] & 0xff) << 8);
				break;
			case DVD_RETURN_TRACK_NUM:
				mTrackNum |= (int) (param[1] & 0xff);
//				Log.d(TAG, "mTrackNum:" + mTrackNum);
				break;
			case 0x85:
				mDvdHour = param[1];
				break;
			case 0x86:
				mDvdMinute = param[1];
				break;
			case DVD_RETURN_PLAY_RATE:
				mPlayRate = param[1];
				break;
			case DVD_RETURN_CUR_TIME:
				Log.d(TAG, "mDvdSecond:" + param[1]);
				mDvdSecond = param[1];
				notifyLauncherInfo();
				break;
			// //long cmd
			case 0xeb:
				mNextCmd = NEXT_CMD_TOTAL_TIME;
				break;
			case DVD_RETURN_REPEAT_MODE:
				mRepeatMode = param[1];
				break;
			case DVD_RETURN_SHUFFLE_MODE:
				mShuffleMode = param[1];
				break;
			}
		}
		if (mServiceCallback != null && ret != 0) {
			mServiceCallback.dvsCallback(ret, param[1] & 0xff);
		}
	}


	public void notifyLauncherInfo(){
		if(GlobalDef.mSource == MyCmd.SOURCE_DVD && MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef.getSystemUI())){
			if (GlobalDef.mContext != null) {
				Intent i = new Intent(MyCmd.BROADCAST_CMD_FROM_MUSIC);
				i.putExtra(MyCmd.EXTRA_COMMON_CMD,MyCmd.Cmd.MUSIC_SEND_PLAY_STATUS);
				
					i.putExtra(MyCmd.EXTRA_COMMON_DATA, (!(mPlayStatus == 3 || mPlayStatus == 0
							|| mPlayStatus == 4 || mPlayStatus == 5 || mPlayStatus == 13)) ? 1 : 0);

					i.putExtra(MyCmd.EXTRA_COMMON_DATA2,
							mDvdSecond+mDvdMinute*60+mDvdHour*3600);
					
					if (mDiskType == DVideoSpec.DVD_DISK_TYPE_CD){
						i.putExtra(MyCmd.EXTRA_COMMON_DATA3, mTrackCurrent);
					} else {
						i.putExtra(MyCmd.EXTRA_COMMON_DATA3, -1);
					}
					
					i.putExtra(MyCmd.EXTRA_COMMON_DATA4, 0xabc);
				i.setPackage(AppConfig.getLauncherPackage());
				GlobalDef.mContext.sendBroadcast(i);
				Log.d("abce",mPlayStatus+ ":"+mDvdSecond);
			}
		}
	}

	private void dvdTQHandler(byte[] param, int len) {
		int ret = 0;
		if (mNextCmd == NEXT_CMD_TOTAL_TIME) {
			// same with VGQ
			mTotalTime = (((param[0] & 0xff) << 8) | (param[1] & 0xff));
			// param[0]
			ret = DVD_RETURN_TOTAL_TIME;
			mNextCmd = 0;
		} else {
			ret = (int) (param[0] & 0xff);
			switch ((int) (param[0] & 0xff)) {
			case DVD_RETURN_TOUCH_MODE:	// same with VGQ
				mTouchMode = param[1];
				// Log.d(TAG, "mTouchMode:" + param[1]);
				break;
			case 0xeb:	//long cmd	// same with VGQ
				mNextCmd = NEXT_CMD_TOTAL_TIME;
				break;

			case 0x87:	// TQ disc type 
				mDiskType = ((0xff&param[1]) == 0x02)?DVD_DISK_TYPE_CD:DVD_DISK_TYPE_DVD;
				Log.d(TAG, "TQ mDiskType:" + mDiskType);
				ret = DVD_RETURN_DISK_MODE;
				param[1]=(byte)mDiskType;
				notifyLauncherInfo();
				break;
			case 0x89:	// TQ play status
				ret = DVD_RETURN_PLAY_STATUS;
				switch (param[1]) {
					case 0x01:	// play
						mPlayStatus = 2;
						break;
					case 0x02:	// pause
						mPlayStatus = 5;
						break;
					case 0x05:	// stop
						mPlayStatus = 13;
						break;
					case 0x03:	// fast forward
						mPlayStatus = 3;
						break;
					case 0x04:	// fast backward
						mPlayStatus = 4;
						break;
					default:
						ret = 0;
						break;
				}
				if (!(mPlayStatus == 3 || mPlayStatus == 4)) {
					mPlayRate = 0;
				}
				Log.d(TAG, "mPlayStatus:" + mPlayStatus);
				notifyLauncherInfo();
				break;
			case 0x83:	// TQ current track
				mTrackCurrent = (int) (param[1] & 0xff);
				ret = DVD_RETURN_CUR_TRACK_NUM;
				break;

			case 0x97:	// TQ total track
				mTrackNum = (int) (param[1] & 0xff);
				ret = DVD_RETURN_TRACK_NUM;
				break;
			case DVD_RETURN_CUR_TRACK_NUM:	// this is a VGQ current track cmd
				ret = 0;	// do not send this MSG to UI
				// but it is hour cmd in TQ machine ???
				Log.d(TAG, "mDvdHour:" + param[1]);
				mDvdHour = param[1];
				break;
			case 0x85:	// TQ minute
				Log.d(TAG, "mDvdMinute:" + param[1]);
				mDvdMinute = param[1];
				break;
			case 0x86:	// TQ second
				//Log.d(TAG, "mDvdSecond:" + param[1]);
				mDvdSecond = param[1];
				ret = DVD_RETURN_CUR_TIME;
				notifyLauncherInfo();
				break;
			case 0x98:	// TQ play rate
				mPlayRate = param[1];
				ret = DVD_RETURN_PLAY_RATE;
				break;
			case DVD_RETURN_PLAY_STATUS:	// this is a VGQ play status cmd, 
				// but repeat & shuttle mode in TQ machine
				switch (param[1]) {
					case 0x00:	// off
						ret = DVD_RETURN_REPEAT_MODE;
						mRepeatMode = 0;
						mShuffleMode = 0;
						break;
					case 0x04:	// shuffle on
						ret = DVD_RETURN_SHUFFLE_MODE;
						mRepeatMode = 0;
						mShuffleMode = 1;
						break;
					case 0x40:	// repeat 1
						ret = DVD_RETURN_REPEAT_MODE;
						mRepeatMode = 1;
						mShuffleMode = 0;
						break;
					case 0x20:	// repeat all
						ret = DVD_RETURN_REPEAT_MODE;
						mRepeatMode = 3;
						mShuffleMode = 0;
						break;
				}
				break;

			case DVD_RETURN_TRACK_NUM:	// this is a VGQ total track cmd, ignore it in our TQ machine
				ret = 0;	// do not send this MSG to UI
				break;
			case DVD_RETURN_PLAY_RATE:	// this is a VGQ play rate cmd, ignore it in our TQ machine
				ret = 0;	// do not send this MSG to UI
				break;
			case DVD_RETURN_DISK_MODE:	// this is a VGQ disc type cmd, ignore it in our TQ machine
				ret = 0;	// do not send this MSG to UI. otherwise, it will think us a DVD disc
				break;
			case DVD_RETURN_REPEAT_MODE:	// this is a VGQ repeat cmd, ignore it in our TQ machine
				ret = 0;	// do not send this MSG to UI. otherwise, it will think us a DVD disc
				break;
			case DVD_RETURN_SHUFFLE_MODE:	// this is a VGQ shuffle cmd, ignore it in our TQ machine
				ret = 0;	// do not send this MSG to UI. otherwise, it will think us a DVD disc
				break;
			}
		}
		if (mServiceCallback != null && ret != 0) {
			mServiceCallback.dvsCallback(ret, param[1] & 0xff);
		}
	}


/*
     * 字节数组转16进制字符串
     */
	public static String bytes2HexString(byte[] array) {
		StringBuilder builder = new StringBuilder();
 
		for (byte b : array) {
			String hex = Integer.toHexString(b & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			builder.append(hex);
		}
 
		return builder.toString().toUpperCase();
	}
	private void dvsCallback(byte[] param, int len) {
		 Log.d(TAG, len + "dataCallback:" + (bytes2HexString(param)));
	
		if (GlobalDef.mAutoTest && GlobalDef.mContext != null) {
			BroadcastUtil.sendToCarService(GlobalDef.mContext,	MyCmd.Cmd.AUTO_TEST_RESULT, MyCmd.SOURCE_DVD, 0);
		}
		
		if (len < 2) {
			return;
		}

		if (TQ) {
			dvdTQHandler(param, len);
		} else {
			dvdVGQHandler(param, len);
		}
	}

	public void clearAllData() {
		mTrackNum = 0;
		mCurTime = 0;
		mDiskType = 0;
		mTrackCurrent = 0;
		mPlayStatus = 0;
		mTouchMode = 0;
		mDvdHour = 0;
		mDvdMinute = 0;
		mRepeatMode = 0;
		mPlayRate = 0;
		mDvdSecond = 0;
		notifyLauncherInfo();
		Log.d(TAG, "clearAllData");
	}

	public void setServiceCallback(IDVSCallback dvsCallback) {
		mServiceCallback = dvsCallback;
	}

	// public void setCDActivityCallback(IDVSCallback dvsCallback) {
	// mCDActivityCallback = dvsCallback;
	// }

	private int mDVSMask = 0xffffffff;

	public void setDVSMask(int value) {
		if (value == 0) {
			value = 0xffffffff;
		}
		mDVSMask = value;
	}

	public boolean isPlaying() {
		if (mPlayStatus == 2) {
			return true;
		}
		return false;
	}

	public int mTouchMode = 0;

	public int mPlayStatus = 0;
	public int mDiskType = 0;

	public int mTrackNum = 0;
	public int mTrackCurrent = 0;
	public int mCurTime;
	public int mTotalTime;

	public byte mDvdHour;
	public byte mDvdMinute;
	public byte mDvdSecond;
	public byte mRepeatMode;
	public byte mPlayRate;
	public byte mShuffleMode;
	// //
	public short mRepeat = -1;
	public short mRandom = -1;

	public int mSourceIndex;
	public int mLockStatus;
	public int mDVSPlayTime;

	public short mDVSControlArea;
	public short mDVSTotalTrack;
	public short mDVSTotalFolder;
	public short mDVSTrackNumber;
	public short mDVSFolderNumber;
	public short mDVSGotoTitleTrackNumber;
	public byte mDVSDiskStatus;
	public byte mDVSPlayStatus;
	public byte mDVSMediaType;
	public byte mDVSPlayMode;
	public short mDVSTouchX;
	public short mDVSTouchY;
	public int mTimerValue;
	public int mDVSPlayTotalTime;
	public int mDVSPlayCurTime;
	public byte mDVSFileType;
	public int mVersion;
	public int mVersionDate;
	public String mDVSFileName;

}
