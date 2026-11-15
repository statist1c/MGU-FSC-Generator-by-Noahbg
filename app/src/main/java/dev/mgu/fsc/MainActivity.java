package dev.mgu.fsc;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.time.Instant;
import java.util.*;

public class MainActivity extends Activity {
    int BG,CARD,INK,MUTED,BLUE,PALE,LINE,ON,GREEN; boolean dark; int page=0;
    LinearLayout shell,body,nav; ScrollView scroll; TextView status; EditText vin,app,index,generator,order,stamp;
    String vinValue="",appValue="017B",indexValue="0001",generatorValue="123456",orderValue="7654321",stampValue="";
    byte[] generated,exporting,imported; Fsc.Document generatedDoc,importedDoc; String importError=""; Fsc engine;
    static final int EXPORT=21,IMPORT=22;
    boolean updateBusy; TextView updateStatus;
    @Override public void onCreate(Bundle saved){
        String theme=getPreferences(0).getString("theme","System");dark=theme.equals("Dark")||(theme.equals("System")&&(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES);
        setTheme(dark?R.style.AppTheme_Dark:R.style.AppTheme);super.onCreate(saved);
        BG=dark?0xFF11141C:0xFFFAF8FF;CARD=dark?0xFF1E2330:0xFFEFEDF5;INK=dark?0xFFEEF1FA:0xFF1D1C22;MUTED=dark?0xFFB1B9CA:0xFF5A5D6B;BLUE=dark?0xFFAFC3FF:0xFF30477F;PALE=dark?0xFF28385B:0xFFDFE5F6;LINE=dark?0xFF3C4558:0xFFD6D5DF;ON=dark?0xFF10254B:Color.WHITE;GREEN=dark?0xFF80DCC2:0xFF006B55;
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(CARD);getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        if(saved!=null){page=saved.getInt("page");vinValue=saved.getString("vin","");appValue=saved.getString("app","017B");indexValue=saved.getString("index","0001");generatorValue=saved.getString("generator","123456");orderValue=saved.getString("order","7654321");stampValue=saved.getString("stamp","");generated=saved.getByteArray("generated");exporting=saved.getByteArray("exporting");imported=saved.getByteArray("imported");importError=saved.getString("importError","");}
        try{engine=new Fsc(getAssets().open("signing_key.pem"));if(generated!=null)generatedDoc=engine.verify(generated);if(imported!=null)importedDoc=engine.verify(imported);}catch(Exception e){engine=null;}
        shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.setBackgroundColor(BG);setContentView(shell);
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);heading.setPadding(dp(20),dp(16),dp(20),dp(12));
        ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.ic_launcher);heading.addView(icon,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);titles.setPadding(dp(12),0,0,0);titles.addView(text("MGU FSC Generator",21,INK,true));titles.addView(text("by Noahbg",13,MUTED,false));heading.addView(titles);shell.addView(heading);
        status=text(engine==null?"Signing key unavailable · restart or reinstall":"Offline & private · FSC Patch key",12,engine==null?INK:GREEN,false);status.setPadding(dp(16),dp(10),dp(16),dp(10));status.setBackground(shape(CARD,22));LinearLayout.LayoutParams st=new LinearLayout.LayoutParams(-1,-2);st.setMargins(dp(20),0,dp(20),dp(8));shell.addView(status,st);
        scroll=new ScrollView(this);scroll.setFillViewport(true);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(14),dp(20),dp(24));scroll.addView(body);
        nav=new LinearLayout(this);nav.setBackgroundColor(CARD);nav.setPadding(dp(8),dp(8),dp(8),dp(8));shell.addView(nav);show(page);
        Updater.schedule(this);if(Updater.due(this))checkUpdates(false);else{Updater.Release available=Updater.cached(this);if(available!=null&&Updater.prefs(this).getBoolean("automatic",true))offerUpdate(available);}
    }
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(dp(2),1);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    void line(LinearLayout target,String value,int size,int color,boolean bold){TextView t=text(value,size,color,bold);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);target.addView(t,lp);}
    LinearLayout card(String title){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(18),dp(18),dp(18),dp(14));c.setBackground(shape(CARD,26));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(10));body.addView(c,lp);line(c,title,18,INK,true);return c;}
    EditText field(LinearLayout c,String label,String value,String hint){line(c,label,13,MUTED,false);EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setTextSize(15);e.setText(value);e.setHint(hint);e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);GradientDrawable g=shape(BG,14);g.setStroke(dp(1),LINE);e.setBackground(g);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);c.addView(e,lp);return e;}
    Button button(LinearLayout c,String label,boolean primary,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(primary?ON:BLUE);GradientDrawable g=shape(primary?BLUE:Color.TRANSPARENT,26);if(!primary)g.setStroke(dp(1),BLUE);b.setBackground(g);b.setMinHeight(dp(50));b.setPadding(dp(14),dp(10),dp(14),dp(10));b.setStateListAnimator(null);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(4),0,dp(8));c.addView(b,lp);b.setOnClickListener(v->action.run());return b;}
    void remember(){if(vin!=null){vinValue=vin.getText().toString();appValue=app.getText().toString();indexValue=index.getText().toString();generatorValue=generator.getText().toString();orderValue=order.getText().toString();stampValue=stamp.getText().toString();}}
    void show(int selected){remember();page=selected;vin=null;body.removeAllViews();nav.removeAllViews();String[] names={"Generate","Verify","Settings"};String[] symbols={"＋","✓","⚙"};for(int i=0;i<3;i++){final int n=i;LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setPadding(0,dp(4),0,dp(4));TextView icon=text(symbols[i],23,i==page?BLUE:MUTED,true);icon.setGravity(Gravity.CENTER);icon.setBackground(shape(i==page?PALE:Color.TRANSPARENT,20));item.addView(icon,new LinearLayout.LayoutParams(dp(64),dp(32)));TextView title=text(names[i],12,i==page?BLUE:MUTED,false);title.setGravity(Gravity.CENTER);title.setPadding(0,dp(4),0,0);item.addView(title);item.setContentDescription(names[i]);item.setOnClickListener(v->show(n));nav.addView(item,new LinearLayout.LayoutParams(0,-2,1));}
        if(page==0)generatePage();else if(page==1)verifyPage();else settingsPage();scroll.scrollTo(0,0);
    }
    void generatePage(){
        line(body,"Create your FSC",28,INK,true);line(body,"Applications & ID7 Live Maps",14,MUTED,false);
        LinearLayout c=card("1   Vehicle & application");vin=field(c,"Vehicle identification number",vinValue,"17-character VIN or last 7");vin.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button(c,"Choose application preset",false,()->{String[] labels=new String[Fsc.PRESETS.length];for(int i=0;i<labels.length;i++)labels[i]=Fsc.PRESETS[i].toString();new AlertDialog.Builder(this).setTitle("Application presets").setItems(labels,(d,n)->{Fsc.Preset p=Fsc.PRESETS[n];app.setText(String.format(Locale.ROOT,"%04X",p.app));index.setText(String.format(Locale.ROOT,"%04X",p.index));}).setNegativeButton("Cancel",null).show();});
        app=field(c,"Application ID · hexadecimal",appValue,"017B");index=field(c,"Upgrade index · hexadecimal",indexValue,"0001");line(c,"Live Maps use FFFF. Component protection variants use 0002 or 0003.",12,MUTED,false);
        LinearLayout advanced=card("2   Generation details");generator=field(advanced,"Generator ID · 6 printable characters",generatorValue,"123456");order=field(advanced,"Order ID · 7 printable characters",orderValue,"7654321");stamp=field(advanced,"UTC timestamp · optional",stampValue,"Automatic, or 2026-11-13T12:00:00Z");
        line(advanced,"Files are signed and verified on this device. Generating a file does not install or enable a feature; a compatible FSC Patch setup is required.",13,MUTED,false);
        Button b=button(advanced,"Generate & verify FSC",true,()->create());b.setEnabled(engine!=null);
        if(generatedDoc!=null){LinearLayout result=card("✓   Signature verified");line(result,generatedDoc.filename(),14,GREEN,true);TextView info=text(generatedDoc.details(),13,MUTED,false);info.setTextIsSelectable(true);result.addView(info);button(result,"Save FSC file…",true,()->save());}
    }
    void create(){remember();try{
        Instant timestamp=stampValue.trim().isEmpty()?Instant.now():Instant.parse(stampValue.trim());generated=engine.generate(vinValue,Fsc.hex16(appValue),Fsc.hex16(indexValue),timestamp,generatorValue,orderValue);generatedDoc=engine.verify(generated);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(shell.getWindowToken(),0);show(0);scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));status.setText("Verified · ready to save");
    }catch(Exception e){error(e);}}
    void save(){try{engine.verify(generated);exporting=generated.clone();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,generatedDoc.filename());startActivityForResult(i,EXPORT);}catch(Exception e){error(e);}}
    void verifyPage(){line(body,"Verify an FSC",28,INK,true);line(body,"Inspect a file before you use it",14,MUTED,false);LinearLayout c=card("Import from your files");line(c,"Open an application or ID7 Live map FSC. Every header byte and its RSA signature are checked against the bundled FSC Patch key.",14,MUTED,false);Button b=button(c,"Open FSC file…",true,()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,IMPORT);});b.setEnabled(engine!=null);
        if(importedDoc!=null){LinearLayout r=card("✓   Signature verified");line(r,importedDoc.filename(),14,GREEN,true);TextView info=text(importedDoc.details(),14,INK,false);info.setTextIsSelectable(true);r.addView(info);}else if(!importError.isEmpty()){LinearLayout r=card("Verification failed");line(r,importError,14,INK,false);}else{LinearLayout r=card("No file selected");line(r,"Supported sizes: 316 bytes (application) and 319 bytes (Live Maps).",13,MUTED,false);}
    }
    void settingsPage(){line(body,"Settings & credits",28,INK,true);line(body,"Make it yours. Keep the authors credited.",14,MUTED,false);LinearLayout c=card("Appearance");String current=getPreferences(0).getString("theme","System");button(c,"Theme: "+current,false,()->new AlertDialog.Builder(this).setTitle("Appearance").setItems(new String[]{"System","Light","Dark"},(d,n)->{getPreferences(0).edit().putString("theme",new String[]{"System","Light","Dark"}[n]).apply();recreate();}).setNegativeButton("Cancel",null).show());
        c=card("MGU FSC Generator by Noahbg");line(c,"Android v0.1.0 · upstream generator 1.3.0",13,MUTED,false);line(c,"FSC generation, formats, presets and intentionally public FSC Patch signing key: Noahbg and the mgu-apps contributors. Original copyrights remain with their authors.",14,INK,false);button(c,"Original project & source",false,()->open("https://github.com/Noahbg/mgu-apps/tree/main/scripts/fsc_generator"));line(c,"Visual styling inspired by statist1c/MGU-Store-Installer: rounded cards, blue accents and matching dark/light palettes. This Android UI is independently implemented.",13,MUTED,false);button(c,"MGU Store Installer",false,()->open("https://github.com/statist1c/MGU-Store-Installer"));button(c,"Open-source license",false,()->{try{new AlertDialog.Builder(this).setTitle("MIT License · Noahbg").setMessage(new String(Fsc.read(getAssets().open("LICENSE"),16384),java.nio.charset.StandardCharsets.UTF_8)).setPositiveButton("Close",null).show();}catch(Exception e){error(e);}});
        c=card("GitHub app updates");
        line(c,"Installed v"+BuildConfig.VERSION_NAME+"\n"+Updater.REPO,13,MUTED,false);
        updateStatus=text(Updater.prefs(this).getString("status","No update check yet"),14,INK,false);c.addView(updateStatus);
        CheckBox automatic=new CheckBox(this);automatic.setText("Automatically check for updates");automatic.setTextColor(INK);automatic.setChecked(Updater.prefs(this).getBoolean("automatic",true));c.addView(automatic);automatic.setOnCheckedChangeListener((v,checked)->{Updater.prefs(this).edit().putBoolean("automatic",checked).apply();Updater.schedule(this);});
        line(c,"Checks about once a day when Android allows background work, and when you open the app. Downloads and installation need your action.",12,MUTED,false);
        button(c,"Check for updates",false,()->checkUpdates(true));Updater.Release available=Updater.cached(this);if(available!=null)button(c,"View "+available.tag+" update",true,()->offerUpdate(available));
        c=card("Signing & privacy");line(c,"RSA-2048 · SHA-256 · PKCS#1 v1.5\n\nThe FSC Patch private key is intentionally public upstream. It is embedded unchanged and checked against the upstream modulus fingerprint. Signature verification confirms compatibility with this key; it is not proof of BMW authorization.\n\nFSC generation and verification work offline. Only the update checker contacts GitHub. File access uses Android’s document picker. VINs and FSCs are not sent anywhere.",13,MUTED,false);
    }
    void checkUpdates(boolean manual){
        if(updateBusy)return;updateBusy=true;if(manual&&updateStatus!=null)updateStatus.setText("Checking GitHub…");
        new Thread(()->{try{Updater.Release release=Updater.check(this);runOnUiThread(()->{updateBusy=false;if(isFinishing()||isDestroyed())return;if(page==2)show(2);if(release!=null)offerUpdate(release);else if(manual)Toast.makeText(this,"No newer compatible APK found",Toast.LENGTH_LONG).show();});}
        catch(Exception e){runOnUiThread(()->{updateBusy=false;if(isFinishing()||isDestroyed())return;if(manual){if(updateStatus!=null)updateStatus.setText(e.getMessage());error(e);}});}},"github-update-check").start();
    }
    void offerUpdate(Updater.Release release){
        String notes=release.notes.length()>2500?release.notes.substring(0,2500)+"…":release.notes;
        new AlertDialog.Builder(this).setTitle("Update available · "+release.tag).setMessage(notes.isEmpty()?"A newer APK is available from "+Updater.REPO:notes).setPositiveButton(Updater.apk(this).exists()?"Install update":"Download update",(d,n)->{
            if(Updater.apk(this).exists()){try{Updater.install(this,release);Toast.makeText(this,"If asked, allow app installs, then tap Install update again.",Toast.LENGTH_LONG).show();}catch(Exception e){Updater.apk(this).delete();error(e);}return;}
            if(updateBusy)return;updateBusy=true;status.setText("Downloading app update…");new Thread(()->{try{Updater.download(this,release);runOnUiThread(()->{updateBusy=false;if(isFinishing()||isDestroyed())return;status.setText("Update downloaded · verified");offerUpdate(release);});}catch(Exception e){runOnUiThread(()->{updateBusy=false;if(isFinishing()||isDestroyed())return;status.setText("App update download failed");error(e);});}},"github-apk-download").start();
        }).setNeutralButton("Release page",(d,n)->open("https://github.com/"+Updater.REPO+"/releases")).setNegativeButton("Later",null).show();
    }
    void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){error(new Exception("No browser is available to open this link."));}}
    void error(Exception e){new AlertDialog.Builder(this).setTitle("Could not complete action").setMessage(e.getMessage()==null?"Please check your input and try again.":e.getMessage()).setPositiveButton("OK",null).show();}
    @Override protected void onActivityResult(int request,int result,Intent intent){super.onActivityResult(request,result,intent);if(result!=RESULT_OK||intent==null||intent.getData()==null){if(request==EXPORT)exporting=null;return;}Uri uri=intent.getData();try{
        if(request==IMPORT){importedDoc=null;imported=null;importError="";try{byte[] bytes=Fsc.read(getContentResolver().openInputStream(uri),319);Fsc.Document doc=engine.verify(bytes);imported=bytes;importedDoc=doc;status.setText("Imported FSC · signature verified");}catch(Exception e){importError=e.getMessage();status.setText("Imported FSC · verification failed");}show(1);}
        if(request==EXPORT){if(exporting==null)throw new IOException("No pending FSC. Please generate and save again.");engine.verify(exporting);try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Cannot open the selected destination.");out.write(exporting);out.flush();}byte[] back=Fsc.read(getContentResolver().openInputStream(uri),319);engine.verify(back);if(!Arrays.equals(back,exporting))throw new IOException("Saved file differs from the verified FSC.");exporting=null;status.setText("Saved · readback & signature verified");Toast.makeText(this,"FSC saved and verified",Toast.LENGTH_LONG).show();}
    }catch(Exception e){exporting=null;error(e);}}
    @Override protected void onSaveInstanceState(Bundle out){remember();out.putInt("page",page);out.putString("vin",vinValue);out.putString("app",appValue);out.putString("index",indexValue);out.putString("generator",generatorValue);out.putString("order",orderValue);out.putString("stamp",stampValue);out.putByteArray("generated",generated);out.putByteArray("exporting",exporting);out.putByteArray("imported",imported);out.putString("importError",importError);super.onSaveInstanceState(out);}
}

