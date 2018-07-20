// **********************************************************************
//
// Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.7.0
//
// <auto-generated>
//
// Generated from file `org.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package com.thanos.model.svc;

public class ChangeRecord implements java.lang.Cloneable,
                                     java.io.Serializable
{
    public String time;

    public String category;

    public String before;

    public String after;

    public ChangeRecord()
    {
        this.time = "";
        this.category = "";
        this.before = "";
        this.after = "";
    }

    public ChangeRecord(String time, String category, String before, String after)
    {
        this.time = time;
        this.category = category;
        this.before = before;
        this.after = after;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        ChangeRecord r = null;
        if(rhs instanceof ChangeRecord)
        {
            r = (ChangeRecord)rhs;
        }

        if(r != null)
        {
            if(this.time != r.time)
            {
                if(this.time == null || r.time == null || !this.time.equals(r.time))
                {
                    return false;
                }
            }
            if(this.category != r.category)
            {
                if(this.category == null || r.category == null || !this.category.equals(r.category))
                {
                    return false;
                }
            }
            if(this.before != r.before)
            {
                if(this.before == null || r.before == null || !this.before.equals(r.before))
                {
                    return false;
                }
            }
            if(this.after != r.after)
            {
                if(this.after == null || r.after == null || !this.after.equals(r.after))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::svc::ChangeRecord");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, time);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, category);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, before);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, after);
        return h_;
    }

    public ChangeRecord clone()
    {
        ChangeRecord c = null;
        try
        {
            c = (ChangeRecord)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeString(this.time);
        ostr.writeString(this.category);
        ostr.writeString(this.before);
        ostr.writeString(this.after);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.time = istr.readString();
        this.category = istr.readString();
        this.before = istr.readString();
        this.after = istr.readString();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, ChangeRecord v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public ChangeRecord ice_read(com.zeroc.Ice.InputStream istr)
    {
        ChangeRecord v = new ChangeRecord();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<ChangeRecord> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, ChangeRecord v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<ChangeRecord> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(ChangeRecord.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final ChangeRecord _nullMarshalValue = new ChangeRecord();

    public static final long serialVersionUID = -744853442L;
}
