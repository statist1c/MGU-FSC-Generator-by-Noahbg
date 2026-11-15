package dev.mgu.fsc;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;

/** Grants the Android installer read-only access to one fixed app-private APK. */
public class UpdateProvider extends ContentProvider {
    public boolean onCreate(){return true;}
    private void check(Uri uri)throws FileNotFoundException{if(!"/update.apk".equals(uri.getPath())||!"dev.mgu.fsc.noahbg.updates".equals(uri.getAuthority()))throw new FileNotFoundException("Unknown update file.");}
    public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{check(uri);if(!"r".equals(mode))throw new FileNotFoundException("Read only.");return ParcelFileDescriptor.open(Updater.apk(getContext()),ParcelFileDescriptor.MODE_READ_ONLY);}
    public String getType(Uri uri){return "application/vnd.android.package-archive";}
    public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){try{check(uri);}catch(FileNotFoundException e){throw new IllegalArgumentException(e);}String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor c=new MatrixCursor(columns);Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)if(columns[i].equals(OpenableColumns.DISPLAY_NAME))row[i]=Updater.ASSET;else if(columns[i].equals(OpenableColumns.SIZE))row[i]=Updater.apk(getContext()).length();c.addRow(row);return c;}
    public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
    public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
