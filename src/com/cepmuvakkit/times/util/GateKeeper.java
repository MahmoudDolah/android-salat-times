package com.cepmuvakkit.times.util;

import com.cepmuvakkit.times.VARIABLE;

import android.content.pm.PackageManager;

public class GateKeeper {
	public static String getVersionName() {
		String versionName = "undefined";
		try {
			versionName = VARIABLE.context.getPackageManager().getPackageInfo(VARIABLE.context.getPackageName(), PackageManager.GET_META_DATA).versionName;
		} catch(Exception ex) { }
		return versionName;
	}
}