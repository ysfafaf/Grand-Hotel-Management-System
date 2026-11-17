package model;

public class KriteriaDiskon {
    private final boolean member;
    private final String kodePromo;

    public KriteriaDiskon(boolean member, String kodePromo) {
        this.member = member;
        this.kodePromo = (kodePromo != null) ? kodePromo.trim() : null;
    }

    public boolean isMember() {
        return member;
    }

    public String getKodePromo() {
        return kodePromo;
    }
}

