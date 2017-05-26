package uk.co.smartrak.smartpos;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;

import com.paydevice.smartpos.sdk.Device;
import com.paydevice.smartpos.sdk.printer.PrinterManager;
import com.paydevice.smartpos.sdk.SmartPosException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;

import java.util.Locale;

public class SmartPOSPlugin extends CordovaPlugin {

	private static final String LOG_TAG = "SmartPOSPlugin";

	private Device mDevice;
	private PrinterManager mPrinterManager;
	/** Application context */
	private Context mContext;
	
	CallbackContext pluginCallbackContext = null;

	public SmartPOSPlugin() {
	}

	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView)
	{
		Log.d(LOG_TAG, "Initializing");
		super.initialize(cordova, webView);
		Log.d(LOG_TAG, "Getting device instance");
		mDevice = Device.getInstance();
		Log.d(LOG_TAG, "Getting print manager");
		mPrinterManager 		= (PrinterManager)mDevice.getDeviceInstance(Device.DEV_TYPE_PRINTER);
		Log.d(LOG_TAG, "Preparing");
		this.prepare();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("print")) {
			Log.d(LOG_TAG, "Print called");
			this.pluginCallbackContext = callbackContext;
			String printString = args.getString(0);
			try {
				this.print(printString);
			} catch(SmartPosException e) {
				Log.d(LOG_TAG, "Print error", e);
				callbackContext.error("Exception caught");
			}
			return true;
		}
		return false;
	}
	
	private void print(String printString) throws SmartPosException {
		mPrinterManager.cmdSetHeatingParam(10, 80, 1);
		//NOTE: some printer board needs this command
		//mPrinterManager.cmdSetPrintDensity(15, 2);

		mPrinterManager.setStringEncoding("CP437");
		mPrinterManager.cmdSetPrinterLanguage(PrinterManager.CODE_PAGE_CP437);
		mPrinterManager.cmdLineFeed(1);
		mPrinterManager.sendData(printString);
		mPrinterManager.flush();
	}
	
	/**
	* @brief initialize printer and check the paper
	*
	* @return 0: success other: error code
	*/
	private int prepare() {
		try {
			Log.d(LOG_TAG, "Initializing printer");
			mPrinterManager.init();
		} catch (SmartPosException e) {
			Log.d(LOG_TAG, "We got an error initializing", e);
			return e.getErrorCode();
		}

		if (!mPrinterManager.paperStatus()) {
			Log.d(LOG_TAG, "No paper");
			return PrinterManager.PRINTER_ERR_NO_PAPER;
		}
		return 0;
	}

	/**
	* @brief deinitialize printer
	*
	* @return 
	*/
	private int destroy() {
		mPrinterManager.deinit();
		return 0;
	}
	
	private void sendUpdate(String barcode, boolean keepCallback) {
	    if (this.pluginCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, barcode);
			result.setKeepCallback(keepCallback);
			this.pluginCallbackContext.sendPluginResult(result);
		}
	}
	
	private void sendError(String error) {
		if (this.pluginCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
			result.setKeepCallback(true);
			this.pluginCallbackContext.sendPluginResult(result);
		}
	}
	
}
