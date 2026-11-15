package dev.mgu.fsc;
import java.util.regex.*;
/** Stable three-component versions only; never installs prereleases or downgrades. */
public final class ReleaseVersion implements Comparable<ReleaseVersion> {
    private final int major,minor,patch;
    private ReleaseVersion(int a,int b,int c){major=a;minor=b;patch=c;}
    public static ReleaseVersion parse(String value){Matcher m=Pattern.compile("^v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$").matcher(value);if(!m.matches())throw new IllegalArgumentException("Expected a stable version such as v0.1.0.");return new ReleaseVersion(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));}
    public int compareTo(ReleaseVersion o){int c=Integer.compare(major,o.major);if(c==0)c=Integer.compare(minor,o.minor);if(c==0)c=Integer.compare(patch,o.patch);return c;}
}
