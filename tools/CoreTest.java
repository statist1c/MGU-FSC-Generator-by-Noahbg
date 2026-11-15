import dev.mgu.fsc.Fsc;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class CoreTest {
    interface Action {void run() throws Exception;}
    static int checks;
    static void check(boolean condition){checks++;if(!condition)throw new AssertionError("Check "+checks);}
    static void refuses(Action action)throws Exception{boolean rejected=false;try{action.run();}catch(Exception e){rejected=true;}check(rejected);}
    public static void main(String[] args)throws Exception{
        Path project=Paths.get(args[0]),fixtures=Paths.get(args[1]);Files.createDirectories(fixtures);
        Fsc f=new Fsc(Files.newInputStream(project.resolve("app/src/main/assets/signing_key.pem")));
        Instant stamp=Instant.parse("2026-11-13T12:00:00Z");
        for(Fsc.Preset p:Fsc.PRESETS){
            byte[] b=f.generate("WBA0000000AB12345",p.app,p.index,stamp,"123456","7654321");Fsc.Document d=f.verify(b);
            check(b.length==(p.index==65535?319:316));check(d.vin.equals("AB12345"));check(d.app==p.app&&d.index==p.index);check(d.stamp.equals(stamp));
            Files.write(fixtures.resolve(d.filename()),b);
            check(Arrays.equals(b,f.generate("ab12345",p.app,p.index,stamp,"123456","7654321")));
            // Every byte of every preset must be protected by structure or signature verification.
            for(int j=0;j<b.length;j++){byte[] mutated=b.clone();mutated[j]^=1;refuses(()->f.verify(mutated));}
        }
        for(String v:new String[]{"","123456","12345678","ABC123I","ABC123O","ABC123Q","AB 2345","ＡＢ12345","A!12345"})refuses(()->Fsc.vin(v));
        for(String h:new String[]{"","-1","10000","0x","12 34","GGGG"})refuses(()->Fsc.hex16(h));
        check(Fsc.hex16(" 0x017b ")==0x17b);
        refuses(()->f.generate("AB12345",0,1,stamp,"123456","7654321"));
        refuses(()->f.generate("AB12345",0x173,65535,stamp,"123456","7654321"));
        refuses(()->f.generate("AB12345",0x173,1,Instant.parse("1999-01-01T00:00:00Z"),"123456","7654321"));
        refuses(()->f.generate("AB12345",0x173,1,stamp,"12345é","7654321"));
        refuses(()->f.generate("AB12345",0x173,1,stamp,"short","7654321"));
        refuses(()->f.verify(new byte[320]));refuses(()->f.verify(new byte[315]));
        java.security.KeyPairGenerator kg=java.security.KeyPairGenerator.getInstance("RSA");kg.initialize(2048);
        String other="-----BEGIN PRIVATE KEY-----\n"+Base64.getEncoder().encodeToString(kg.generateKeyPair().getPrivate().getEncoded())+"\n-----END PRIVATE KEY-----";
        refuses(()->new Fsc(new java.io.ByteArrayInputStream(other.getBytes(java.nio.charset.StandardCharsets.US_ASCII))));
        System.out.println("PASS: "+checks+" native core checks; 34 signed fixtures written.");
    }
}
