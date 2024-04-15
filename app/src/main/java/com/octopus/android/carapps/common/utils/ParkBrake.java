package com.octopus.android.carapps.common.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import com.common.util.Util;
import com.octopus.android.carapps.car.ui.GlobalDef;

public class ParkBrake {

	public static String readLine(String path) {

		File file = new File(path);

		String source = null;
		if (file.exists()) {
			BufferedReader buf;

			try {
				FileReader fr = new FileReader(file);
				buf = new BufferedReader(fr);
				source = buf.readLine();
				buf.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return source;
	}

	public static boolean isBrake() {

		String s = readLine("/sys/class/ak/source/reaksw");
		if ("0".equals(s)) {
			return true;
		} else {
			String source = readLine("/sys/class/ak/source/brake_status");
			Log.d("cckf", "" + source);
			try {
				int status = Integer.valueOf(source);

				if (((status & 0xFF) == 1) || 
						(((status & 0xFF00) >> 8) == 1)) {
					return true;
				}
			} catch (Exception e) {

			}

			return false;
		}

	}

	public static boolean checkCamera0IfFacing = false; 

	public static int dvrExist = 0; 
	
	public static void saveCameraStatasIfSleep(){
		Log.d("ParkBrake" , "saveCameraStatas");
		checkCamera0IfFacing = false;
		if (android.hardware.Camera.getNumberOfCameras()>=2){
			dvrExist = 2;
		}
	}
	
	public static int isSignal() {
		String source;

		// if(Util.isPX5()){
		source = readLine(GlobalDef.CAMERA_SIGNAL);
		// } else {
		// source = readLine("/sys/class/misc/mst701/device/lock");
		// }

		if (source != null && source.equals("1")) {
			/*if (!checkCamera0IfFacing && Util.isRKSystem()) {
				try {
					android.hardware.Camera.CameraInfo mCameraInfo = new android.hardware.Camera.CameraInfo();
					android.hardware.Camera.getCameraInfo(0, mCameraInfo);
					Log.w("ParkBrake", "camera_surfaceview "
							+ mCameraInfo.facing +":"+ dvrExist);
					if (mCameraInfo.facing == 1) {
						return 0;
					}			
					if (dvrExist > 0) {
						--dvrExist;
						return 0;
					}
					checkCamera0IfFacing = true;
				} catch (RuntimeException e) {
					Log.w("ParkBrake", "camera_surfaceview 0"
							+ " maybe doesn't exist");
					return 0;
				}
			}*/
			return 1;
		}

		return 0;

	}
}
