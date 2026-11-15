package dev.mgu.fsc;

import android.app.*;
import android.os.*;
import android.content.*;
import java.time.Instant;

/** Dependency-free instrumentation: device crypto, UI generation, navigation and saved state. */
public class SmokeTest extends Instrumentation {
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){Bundle result=new Bundle();try{
        Fsc f=new Fsc(getTargetContext().getAssets().open("signing_key.pem"));
        for(Fsc.Preset p:Fsc.PRESETS){byte[] bytes=f.generate("AB12345",p.app,p.index,Instant.parse("2026-11-13T12:00:00Z"),"123456","7654321");if(f.verify(bytes).app!=p.app)throw new AssertionError("Device crypto mismatch");}
        Intent i=new Intent(getTargetContext(),MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);MainActivity activity=(MainActivity)startActivitySync(i);waitForIdleSync();
        runOnMainSync(()->{activity.vin.setText("ab12345");activity.stamp.setText("2026-11-13T12:00:00Z");activity.create();if(activity.generatedDoc==null||activity.generated.length!=316)throw new AssertionError("UI generation failed");activity.show(1);activity.show(2);activity.show(0);if(!activity.vin.getText().toString().equals("ab12345"))throw new AssertionError("VIN lost on navigation");Bundle saved=new Bundle();activity.onSaveInstanceState(saved);if(saved.getByteArray("generated").length!=316)throw new AssertionError("State lost");});
        result.putString("stream","PASS: Android API "+Build.VERSION.SDK_INT+"; all 34 presets sign/verify, UI generation, navigation and saved-state checks.\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable e){result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}
