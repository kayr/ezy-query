package io.github.kayr.ezyquery.gen;

public class NoTimeQueryGen extends QueryGen{

    public NoTimeQueryGen(String packageName, String className, String sql) {
        super(packageName, className, sql);
    }

    @Override
    protected String timeStamp() {
        return "0000-00-00 00:00:00";
    }
}
