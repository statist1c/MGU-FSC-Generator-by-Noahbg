package dev.mgu.fsc;

import android.app.*;
import android.os.*;
import org.json.*;
import java.io.*;
import java.util.*;

/** Pass -PupdateTestAssets=/absolute/path to candidate.apk, current.apk and wrong-cert.apk fixtures. */
public class UpdateTest extends Instrumentation {
    int checks;
    interface Action {void run()throws Exception;}
    void check(boolean value){checks++;if(!value)throw new AssertionError("Check "+checks);}
    void refuses(Action action)throws Exception{boolean refused=false;try{action.run();}catch(Exception e){refused=true;}check(refused);}
    public void onCreate(Bundle b){super.onCreate(b);start();}
    JSONObject release(String tag,boolean prerelease,String digest,String url,String asset)throws Exception{
        JSONObject a=new JSONObject().put("name",asset).put("digest",digest).put("size",100).put("browser_download_url",url);
        return new JSONObject().put("tag_name",tag).put("prerelease",prerelease).put("draft",false).put("assets",new JSONArray().put(a));
    }
    Updater.Release metadata(File file,String tag)throws Exception{return new Updater.Release(new JSONObject().put("tag",tag).put("url","https://github.com/"+Updater.REPO+"/releases/download/"+tag+"/app.apk").put("digest",Fsc.hash(Fsc.read(new FileInputStream(file),Updater.MAX))).put("size",file.length()));}
    File fixture(String name)throws Exception{File f=new File(getTargetContext().getCacheDir(),name);try(OutputStream out=new FileOutputStream(f)){out.write(Fsc.read(getContext().getAssets().open(name),Updater.MAX));}return f;}
    public void onStart(){Bundle result=new Bundle();try{
        String digest="sha256:"+String.join("",Collections.nCopies(64,"a"));String url="https://github.com/"+Updater.REPO+"/releases/download/v0.1.1/app.apk";
        JSONObject newer=release("v0.1.1",false,digest,url,Updater.ASSET);
        check(Updater.choose(new JSONArray().put(newer),"0.1.0").tag.equals("v0.1.1"));
        check(Updater.choose(new JSONArray().put(newer),"0.1.1")==null);
        check(Updater.choose(new JSONArray().put(release("v0.2.0",true,digest,url,Updater.ASSET)),"0.1.0")==null);
        check(Updater.choose(new JSONArray().put(release("v0.2.0",false,"",url,Updater.ASSET)),"0.1.0")==null);
        check(Updater.choose(new JSONArray().put(release("v0.2.0",false,digest,url,"another.apk")),"0.1.0")==null);
        check(Updater.choose(new JSONArray().put(release("v0.2.0",false,digest,"https://evil.example/app.apk",Updater.ASSET)),"0.1.0")==null);
        check(Updater.choose(new JSONArray().put(release("v0.2.0",false,digest,"https://github.com/other/repo/releases/download/v0.2.0/a.apk",Updater.ASSET)),"0.1.0")==null);
        check(Updater.choose(new JSONArray().put(release("v0.10.0",false,digest,url,Updater.ASSET)).put(newer),"0.1.0").tag.equals("v0.10.0"));
        for(String s:new String[]{"v0.1.1-beta","0.1","01.2.3","-1.2.3","0.1.2.3","v99999999999999.0.0"})refuses(()->ReleaseVersion.parse(s));
        refuses(()->Updater.connect("http://github.com/"));refuses(()->Updater.connect("https://evil.example/"));
        File candidate=fixture("candidate.apk"),current=fixture("current.apk"),wrong=fixture("wrong-cert.apk");
        Updater.Release valid=metadata(candidate,"v0.1.1");Updater.validate(getTargetContext(),candidate,valid);check(true);
        refuses(()->Updater.validate(getTargetContext(),current,metadata(current,"v0.1.0")));
        refuses(()->Updater.validate(getTargetContext(),candidate,metadata(candidate,"v0.2.0")));
        refuses(()->Updater.validate(getTargetContext(),wrong,metadata(wrong,"v0.1.1")));
        byte[] changed=Fsc.read(new FileInputStream(candidate),Updater.MAX);changed[changed.length-1]^=1;try(OutputStream out=new FileOutputStream(candidate)){out.write(changed);}refuses(()->Updater.validate(getTargetContext(),candidate,valid));
        new UpdateProvider();Updater.schedule(getTargetContext());check(getTargetContext().getSystemService(android.app.job.JobScheduler.class).getPendingJob(Updater.JOB)!=null);
        result.putString("stream","PASS: "+checks+" updater checks: release policy, host restrictions, valid candidate, downgrade/version/digest/certificate rejection and background schedule.\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable e){result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}
