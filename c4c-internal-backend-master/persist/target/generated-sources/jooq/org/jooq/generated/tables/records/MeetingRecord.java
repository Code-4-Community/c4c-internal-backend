/*
 * This file is generated by jOOQ.
 */
package org.jooq.generated.tables.records;


import java.sql.Timestamp;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.generated.tables.Meeting;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MeetingRecord extends UpdatableRecordImpl<MeetingRecord> implements Record4<String, String, Timestamp, Boolean> {

    private static final long serialVersionUID = 1820326067;

    /**
     * Setter for <code>meeting.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>meeting.id</code>.
     */
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>meeting.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>meeting.name</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>meeting.date</code>.
     */
    public void setDate(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>meeting.date</code>.
     */
    public Timestamp getDate() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>meeting.open</code>.
     */
    public void setOpen(Boolean value) {
        set(3, value);
    }

    /**
     * Getter for <code>meeting.open</code>.
     */
    public Boolean getOpen() {
        return (Boolean) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<String, String, Timestamp, Boolean> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<String, String, Timestamp, Boolean> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return Meeting.MEETING.ID;
    }

    @Override
    public Field<String> field2() {
        return Meeting.MEETING.NAME;
    }

    @Override
    public Field<Timestamp> field3() {
        return Meeting.MEETING.DATE;
    }

    @Override
    public Field<Boolean> field4() {
        return Meeting.MEETING.OPEN;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public Timestamp component3() {
        return getDate();
    }

    @Override
    public Boolean component4() {
        return getOpen();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public Timestamp value3() {
        return getDate();
    }

    @Override
    public Boolean value4() {
        return getOpen();
    }

    @Override
    public MeetingRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public MeetingRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public MeetingRecord value3(Timestamp value) {
        setDate(value);
        return this;
    }

    @Override
    public MeetingRecord value4(Boolean value) {
        setOpen(value);
        return this;
    }

    @Override
    public MeetingRecord values(String value1, String value2, Timestamp value3, Boolean value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MeetingRecord
     */
    public MeetingRecord() {
        super(Meeting.MEETING);
    }

    /**
     * Create a detached, initialised MeetingRecord
     */
    public MeetingRecord(String id, String name, Timestamp date, Boolean open) {
        super(Meeting.MEETING);

        set(0, id);
        set(1, name);
        set(2, date);
        set(3, open);
    }
}
