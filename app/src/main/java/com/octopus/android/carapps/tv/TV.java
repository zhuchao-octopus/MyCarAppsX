package com.octopus.android.carapps.tv;

import android.content.Context;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.octopus.android.carapps.R;
import com.octopus.android.carapps.common.utils.ResourceUtil;

public class TV {

	private static Context mContext;
	public static int mSource;;
	
	public static int mType;
	

	public static int mTypeCmd;
	
	public static int mTouchW = 0;
	private static int mTouchH = 0;
	

	
	public static void init(Context c) {
		mType = MachineConfig.getPropertyIntOnce(MachineConfig.KEY_TV_TYPE);			
		
//		mType = 3;//test
		mContext = c;
		if(mType == 0){
			mSource = MyCmd.SOURCE_DTV;
		} else {
			mSource = MyCmd.SOURCE_DTV_CVBS;	
			switch(mType){
			case 1:
				mTypeCmd = 0xff;
				break;
			case 2:
				mTypeCmd = 0xef;
				ID_CMD = ID_CMD_EF;
				break;
			case 3:
				mTypeCmd = 0xaa55;
				ID_CMD = null;
				mTouchW = 0xff;
				mTouchH = 0xff;
				ID_CMD = ID_CMD_EF;
				break;
			}
		}
	}
	
	public static int getCmd(int id){
		if (ID_CMD!=null){
			for (int i = 0; i <ID_CMD.length; ++i){
				if(ID_CMD[i][0] == id){
					return ID_CMD[i][1];
				}
			}
		}
		return 0;
	}
	
	private static final int[][] ID_CMD_EF = new int[][] { 
		{R.id.dtv_up, 0x5},
		{R.id.dtv_down, 0xd},
		{R.id.dtv_left, 0x8},
		{R.id.dtv_right, 0xa},
		{R.id.num_ok, 0x9},
		{R.id.dtv_exit, 0xc},
		{R.id.dtv_menu, 0x4},
	};
	
	private static int[][] ID_CMD = null;
	
	public static void sendKey(int data) {
		byte key = (byte) (data & 0xff);
		byte key2 = (byte) (~key);
		int cmd = (TV.mTypeCmd << 16) | ((key & 0xff) << 8) | (key2 & 0xff);

		BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_DTV_CMD_EX, cmd);
	}

	private static void sendTouch(int data) {
		byte key = (byte) ((data & 0xff00)>>8);
		byte key2 = (byte) (data & 0xff);
		int cmd = (TV.mTypeCmd << 16) | ((key & 0xff) << 8) | (key2 & 0xff);

		BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_DTV_CMD_EX, cmd);
	}
	
	private static int toTVTouch(int x, int y){
		int data = -1;
		if (mTouchW > 0){
			try{
				x = x * mTouchW / ResourceUtil.mScreenWidth;
				y = y * mTouchH / ResourceUtil.mScreenHeight;
				data = ((x & 0xff) << 8) | (y & 0xff);
			}catch(Exception e){
				
			}
		
		}
		return data;
	}
	public static boolean sendTouch(int x, int y){
		int data = toTVTouch(x, y);
		if (data!=-1){
			sendTouch(data);
			return true;
		}
		return false;
	}
	
	public static void doKeyControl(int code) {
		int  id = 0;
		switch (code) {
		case MyCmd.Keycode.CH_UP:
		case MyCmd.Keycode.KEY_SEEK_NEXT:
		case MyCmd.Keycode.KEY_TURN_A:
		case MyCmd.Keycode.NEXT:
		case MyCmd.Keycode.RIGHT:
		case MyCmd.Keycode.KEY_DVD_UP:
		case MyCmd.Keycode.KEY_DVD_RIGHT:
			id = R.id.dtv_down;
			break;

		case MyCmd.Keycode.CH_DOWN:
		case MyCmd.Keycode.KEY_SEEK_PREV:
		case MyCmd.Keycode.KEY_TURN_D:
		case MyCmd.Keycode.PREVIOUS:
		case MyCmd.Keycode.LEFT:
		case MyCmd.Keycode.KEY_DVD_DOWN:
		case MyCmd.Keycode.KEY_DVD_LEFT:
			id = R.id.dtv_up;
			break;
		case MyCmd.Keycode.PLAY:
		case MyCmd.Keycode.PAUSE:
		case MyCmd.Keycode.ENTER:
		case MyCmd.Keycode.PLAY_PAUSE:
			id = R.id.num_ok;
			break;
		
		case MyCmd.Keycode.UP:
			id = R.id.dtv_up;
			break;
		case MyCmd.Keycode.DOWN:
			id = R.id.dtv_down;
			break;
		case MyCmd.Keycode.MENU:
			id = R.id.dtv_menu;
			break;			
		}
		
		int cmd = TV.getCmd(id);

		if (cmd != 0) {
			TV.sendKey(cmd);
		}
	}
		
}
