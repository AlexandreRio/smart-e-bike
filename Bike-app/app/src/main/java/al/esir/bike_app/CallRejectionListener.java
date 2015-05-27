package al.esir.bike_app;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Listener for the application that rejects any incoming call.
 */
public class CallRejectionListener extends PhoneStateListener{

    // ----- ATTRIBUTES ----- //
    private ITelephony telephonyService;


    // ----- CONSTRUCTOR ----- //
    public CallRejectionListener(TelephonyManager tm){
        try{
            Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(tm);
        }
        catch(Exception e){
            Log.e(MainActivity.LOGS, e.getMessage());
            e.printStackTrace();
        }
    }

    // ----- METHODS ----- //
    public void onCallStateChanged(int state, String incomingNumber) {
        try{
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(MainActivity.LOGS, "PHONE STATE : RINGING");
                    telephonyService.endCall();
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(MainActivity.LOGS, "PHONE STATE : OFFHOOK");
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(MainActivity.LOGS, "PHONE STATE : IDLE");
                    break;

                default:
                    System.out.println("PHONE STATE : WHAAAT ?");
                    break;
            }
        }
        catch (Exception e) {
            Log.e(MainActivity.LOGS, e.getMessage());
            e.printStackTrace();
        }
    }


}