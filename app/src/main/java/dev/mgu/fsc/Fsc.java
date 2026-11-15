package dev.mgu.fsc;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/** Native port of Noahbg's fsc_generator 1.3.0. All signed bytes are reconstructed during verification. */
public final class Fsc {
    public static final String KEY_HASH="d4f1ebaece4e4dbc1536a28eaa15b6b9f80f50213bd485ba01495fb2c6c469e0";
    private static final DateTimeFormatter STAMP=DateTimeFormatter.ofPattern("uuuuMMddHHmm",Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    public static final class Preset {
        public final String name; public final int app,index;
        public Preset(String n,int a,int i){name=n;app=a;index=i;}
        public String toString(){return name+String.format(Locale.ROOT," · %04X / %04X",app,index);}
    }
    public static final Preset[] PRESETS={
        new Preset("Apple CarPlay",0x017B,1),new Preset("Android Auto",0x01CA,1),
        new Preset("Navigation Professional — Rest of World",0x0173,1),new Preset("Navigation Professional — Asia",0x0172,1),
        new Preset("Navigation Business — Rest of World",0x0191,1),new Preset("Navigation Business — Asia",0x0190,1),
        new Preset("Speech — Rest of World",0x017D,1),new Preset("Speech — China",0x017E,1),new Preset("Speech Global",0x01CC,1),
        new Preset("Touch Command",0x0171,1),new Preset("SiriusXM connected",0x01BE,1),new Preset("A4A",0x017A,1),
        new Preset("Component protection",0x01AA,1),new Preset("Component protection — variant 2",0x01AA,2),new Preset("Component protection — variant 3",0x01AA,3),
        new Preset("Diagnostic log and trace — external",0x0176,1),new Preset("Diagnostic log and trace — internal",0x0177,1),new Preset("RAM diagnostic log and trace",0x0179,1),
        new Preset("Europe Live",0x0155,65535),new Preset("North America Live",0x0156,65535),new Preset("North Africa Live",0x0157,65535),
        new Preset("South America Live",0x0158,65535),new Preset("Israel Live",0x0159,65535),new Preset("South Africa Live",0x015A,65535),
        new Preset("South East Asia Live",0x015B,65535),new Preset("Turkey Live",0x015C,65535),new Preset("Japan Live",0x015D,65535),
        new Preset("India Live",0x015E,65535),new Preset("Argentina Live",0x015F,65535),new Preset("Taiwan Live",0x0160,65535),
        new Preset("China Live",0x0161,65535),new Preset("Korea Live",0x0162,65535),new Preset("Africa / Middle East Live",0x0169,65535),new Preset("Australia / New Zealand Live",0x016C,65535)
    };
    private final PrivateKey privateKey; private final PublicKey publicKey;
    public Fsc(InputStream stream) throws Exception {
        String pem=new String(read(stream,8192),StandardCharsets.US_ASCII).replace("-----BEGIN PRIVATE KEY-----","").replace("-----END PRIVATE KEY-----","").replaceAll("\\s","");
        KeyFactory factory=KeyFactory.getInstance("RSA");
        privateKey=factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        RSAPrivateCrtKey rsa=(RSAPrivateCrtKey)privateKey;
        require(rsa.getModulus().bitLength()==2048 && rsa.getPublicExponent().equals(BigInteger.valueOf(65537)),"Expected RSA-2048 with exponent 65537.");
        byte[] big=rsa.getModulus().toByteArray(),little=new byte[256];for(int i=0;i<256;i++)little[i]=big[big.length-1-i];
        require(hash(little).equals(KEY_HASH),"Bundled signing key does not match FSC Patch.");
        publicKey=factory.generatePublic(new RSAPublicKeySpec(rsa.getModulus(),rsa.getPublicExponent()));
    }
    public static byte[] read(InputStream in,int limit) throws IOException {
        try(InputStream input=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[1024];int n;while((n=input.read(b))!=-1){if(out.size()+n>limit)throw new IOException("File exceeds the supported size.");out.write(b,0,n);}return out.toByteArray();}
    }
    static void require(boolean condition,String error){if(!condition)throw new IllegalArgumentException(error);}
    public static String vin(String text){String v=text.trim().toUpperCase(Locale.ROOT);require((v.length()==7||v.length()==17)&&v.matches("[A-HJ-NPR-Z0-9]+"),"Enter a 17-character VIN or its last 7 characters. I, O and Q are not VIN letters.");return v.substring(v.length()-7);}
    public static int hex16(String text){String t=text.trim().replaceFirst("(?i)^0x","");require(t.matches("[0-9A-Fa-f]{1,4}"),"Use 1 to 4 hexadecimal digits, such as 017B or 0001.");return Integer.parseInt(t,16);}
    static void ascii(byte[] b,int offset,String value,int length,String label){require(value.length()==length,label+" must contain exactly "+length+" printable ASCII characters.");for(int i=0;i<length;i++){char c=value.charAt(i);require(c>=32&&c<=126,label+" must use printable ASCII.");b[offset+i]=(byte)c;}}
    static boolean mapApp(int app){for(Preset p:PRESETS)if(p.app==app&&p.index==65535)return true;return false;}
    public static byte[] header(String vehicle,int app,int index,Instant timestamp,String generator,String order){
        String v=vin(vehicle);require(app>=1&&app<=65535,"Application ID must be between 0001 and FFFF.");require(index>=0&&index<=65535,"Upgrade index must be between 0000 and FFFF.");
        boolean map=index==65535;require(!map||mapApp(app),"FFFF is reserved for the supported ID7 Live map application IDs.");
        LocalDateTime utc=LocalDateTime.ofInstant(timestamp,ZoneOffset.UTC);require(utc.getYear()>=2000&&utc.getYear()<=9999,"FSC timestamps must be in years 2000 through 9999.");
        byte[] b=new byte[map?63:60];b[0]=5;b[1]=1;b[2]=(byte)(app>>8);b[3]=(byte)app;b[4]=(byte)(index>>8);b[5]=(byte)index;b[6]=b[7]=32;
        ascii(b,8,generator,6,"Generator ID");b[14]=(byte)(map?2:1);b[15]=32;ascii(b,16,order,7,"Order ID");
        if(map){ascii(b,23,"99991",5,"Map ID");b[28]=1;}else{boolean cp=app==0x1AA&&(index==2||index==3);b[23]=(byte)(cp?0x1e:7);b[24]=(byte)(cp?0x78:0xc4);b[25]=1;}
        int vo=map?29:26,so=map?48:45;ascii(b,vo,v,7,"VIN");b[vo+7]=1;b[so-2]=4;b[so-1]=18;ascii(b,so,STAMP.format(utc),12,"Timestamp");b[so+12]='Z';return b;
    }
    public byte[] generate(String v,int app,int index,Instant stamp,String generator,String order)throws Exception {
        byte[] h=header(v,app,index,stamp,generator,order);Signature s=Signature.getInstance("SHA256withRSA");s.initSign(privateKey);s.update(h);byte[] data=Arrays.copyOf(h,h.length+256);System.arraycopy(s.sign(),0,data,h.length,256);verify(data);return data;
    }
    public static final class Document {
        public final int app,index,length; public final String vin,generator,order,sha256;public final Instant stamp;
        Document(int a,int i,int l,String v,String g,String o,Instant t,String h){app=a;index=i;length=l;vin=v;generator=g;order=o;stamp=t;sha256=h;}
        public String filename(){return String.format(Locale.ROOT,"FSC_%s_%04X%04X.fsc",vin,app,index);}
        public String details(){return String.format(Locale.ROOT,"%s\nVIN  %s\nApplication  %04X   ·   Index  %04X\nUTC  %s\nGenerator  %s   ·   Order  %s\n%d bytes   ·   RSA-2048 / SHA-256\n\nSHA-256\n%s",length==63?"ID7 Live map":"Application",vin,app,index,stamp.toString(),generator,order,length+256,sha256);}
    }
    static String str(byte[] b,int start,int length){return new String(b,start,length,StandardCharsets.US_ASCII);}
    public Document verify(byte[] data)throws Exception {
        require(data.length==316||data.length==319,"Expected a 316-byte application or 319-byte extended-map FSC.");
        int length=data.length-256,vo=length==63?29:26,so=length==63?48:45;
        int app=(data[2]&255)*256+(data[3]&255),index=(data[4]&255)*256+(data[5]&255);
        String v=vin(str(data,vo,7)),g=str(data,8,6),o=str(data,16,7);Instant stamp;
        try{stamp=LocalDateTime.parse(str(data,so,12),STAMP).toInstant(ZoneOffset.UTC);}catch(DateTimeException e){throw new IllegalArgumentException("Invalid FSC timestamp.");}
        byte[] expected=header(v,app,index,stamp,g,o);require(Arrays.equals(expected,Arrays.copyOf(data,length)),"FSC fields do not match a supported generated layout.");
        Signature s=Signature.getInstance("SHA256withRSA");s.initVerify(publicKey);s.update(data,0,length);require(s.verify(Arrays.copyOfRange(data,length,data.length)),"The FSC signature does not verify with the FSC Patch key.");
        return new Document(app,index,length,v,g,o,stamp,hash(data));
    }
    public static String hash(byte[] bytes)throws NoSuchAlgorithmException{StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
}
