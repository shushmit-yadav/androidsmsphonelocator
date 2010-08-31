package com.twolinessoftware.android.smslocator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {
	
	private static final String LOGNAME = SmsListener.class.getCanonicalName();

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")){
	
			// Check if the SMS matches our SOS message
			SmsMessage[] messages = getMessagesFromIntent(intent);
			if(messages != null){
				for(int i = 0; i < messages.length; i++){
					SmsMessage message = messages[i];
					if(matchesSosMessage(context,message.getDisplayMessageBody())){
						Log.i(LOGNAME, "Received SOS! Launching Service");
						
						Intent serviceIntent = new Intent(context,SmsLocatorService.class);
						serviceIntent.putExtra(SmsLocatorService.INTENT_DESTINATION_NUMBER, message.getOriginatingAddress());
						context.startService(serviceIntent);
						break;
					}
				}
			}
		}
		
	}

	
	private boolean matchesSosMessage(Context context, String message){
		
		SharedPreferences preferences = context.getSharedPreferences(MainSetting.PREFERENCES, Context.MODE_PRIVATE);
		
		String sosMessage = preferences.getString(MainSetting.PREFERENCES_SOS, MainSetting.PREFERENCES_SOS_DEFAULT);
		
		return sosMessage.equalsIgnoreCase(message);
		
	}
	
	
	
	/*
	 * Stolen from http://www.devx.com/wireless/Article/39495/1954
	 * 
	 * Thanks Chris Haseman. All credit to him since I think he is a 
	 * martial arts instructor and likely to do bad things to me.  
	 * 
	 * 
	 */
	private SmsMessage[] getMessagesFromIntent(Intent intent) {
		SmsMessage retMsgs[] = null;
		Bundle bdl = intent.getExtras();
		try {
			Object pdus[] = (Object[]) bdl.get("pdus");
			retMsgs = new SmsMessage[pdus.length];
			for (int n = 0; n < pdus.length; n++) {
				byte[] byteData = (byte[]) pdus[n];
				retMsgs[n] = SmsMessage.createFromPdu(byteData);
			}
		} catch (Exception e) {
			Log.e(LOGNAME, "fail", e);
		}
		return retMsgs;
	}

}
