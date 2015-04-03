package com.tmack.pocketsermons.model;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class Speaker extends Person {

    private Organization affiliation;

    public Organization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Organization affiliation) {
        this.affiliation = affiliation;
    }
}
