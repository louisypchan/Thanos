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

public class Properties implements java.lang.Cloneable,
                                   java.io.Serializable
{
    public String pId;

    public Partner[] patents;

    public Brand[] brands;

    public CopyRight[] copyRights;

    public Properties()
    {
        this.pId = "";
    }

    public Properties(String pId, Partner[] patents, Brand[] brands, CopyRight[] copyRights)
    {
        this.pId = pId;
        this.patents = patents;
        this.brands = brands;
        this.copyRights = copyRights;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Properties r = null;
        if(rhs instanceof Properties)
        {
            r = (Properties)rhs;
        }

        if(r != null)
        {
            if(this.pId != r.pId)
            {
                if(this.pId == null || r.pId == null || !this.pId.equals(r.pId))
                {
                    return false;
                }
            }
            if(!java.util.Arrays.equals(this.patents, r.patents))
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.brands, r.brands))
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.copyRights, r.copyRights))
            {
                return false;
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::svc::Properties");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, pId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, patents);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, brands);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, copyRights);
        return h_;
    }

    public Properties clone()
    {
        Properties c = null;
        try
        {
            c = (Properties)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeString(this.pId);
        PartnerSeqHelper.write(ostr, this.patents);
        BrandSeqHelper.write(ostr, this.brands);
        CopyRightSeqHelper.write(ostr, this.copyRights);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.pId = istr.readString();
        this.patents = PartnerSeqHelper.read(istr);
        this.brands = BrandSeqHelper.read(istr);
        this.copyRights = CopyRightSeqHelper.read(istr);
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Properties v)
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

    static public Properties ice_read(com.zeroc.Ice.InputStream istr)
    {
        Properties v = new Properties();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Properties> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Properties v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Properties> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Properties.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Properties _nullMarshalValue = new Properties();

    public static final long serialVersionUID = 1436623622L;
}
