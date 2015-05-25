package com.tmack.pocketsermons.model;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class Church extends Organization {
    private String denomination;

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }
}
