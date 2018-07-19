# -*- coding: utf-8 -*-
# **********************************************************************
#
# Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
#
# This copy of Ice is licensed to you under the terms described in the
# ICE_LICENSE file included in this distribution.
#
# **********************************************************************
#
# Ice version 3.7.0
#
# <auto-generated>
#
# Generated from file `org.ice'
#
# Warning: do not edit this file.
#
# </auto-generated>
#

from sys import version_info as _version_info_
import Ice, IcePy

# Start of module svc
_M_svc = Ice.openModule('svc')
__name__ = 'svc'

if 'Org' not in _M_svc.__dict__:
    _M_svc.Org = Ice.createTempClass()
    class Org(object):
        def __init__(self, name='', juridical='', uscc='', tin='', registerNumber='', code='', category='', ra='', region='', nub='', term='', addr='', doa='', scope=''):
            self.name = name
            self.juridical = juridical
            self.uscc = uscc
            self.tin = tin
            self.registerNumber = registerNumber
            self.code = code
            self.category = category
            self.ra = ra
            self.region = region
            self.nub = nub
            self.term = term
            self.addr = addr
            self.doa = doa
            self.scope = scope

        def __hash__(self):
            _h = 0
            _h = 5 * _h + Ice.getHash(self.name)
            _h = 5 * _h + Ice.getHash(self.juridical)
            _h = 5 * _h + Ice.getHash(self.uscc)
            _h = 5 * _h + Ice.getHash(self.tin)
            _h = 5 * _h + Ice.getHash(self.registerNumber)
            _h = 5 * _h + Ice.getHash(self.code)
            _h = 5 * _h + Ice.getHash(self.category)
            _h = 5 * _h + Ice.getHash(self.ra)
            _h = 5 * _h + Ice.getHash(self.region)
            _h = 5 * _h + Ice.getHash(self.nub)
            _h = 5 * _h + Ice.getHash(self.term)
            _h = 5 * _h + Ice.getHash(self.addr)
            _h = 5 * _h + Ice.getHash(self.doa)
            _h = 5 * _h + Ice.getHash(self.scope)
            return _h % 0x7fffffff

        def __compare(self, other):
            if other is None:
                return 1
            elif not isinstance(other, _M_svc.Org):
                return NotImplemented
            else:
                if self.name is None or other.name is None:
                    if self.name != other.name:
                        return (-1 if self.name is None else 1)
                else:
                    if self.name < other.name:
                        return -1
                    elif self.name > other.name:
                        return 1
                if self.juridical is None or other.juridical is None:
                    if self.juridical != other.juridical:
                        return (-1 if self.juridical is None else 1)
                else:
                    if self.juridical < other.juridical:
                        return -1
                    elif self.juridical > other.juridical:
                        return 1
                if self.uscc is None or other.uscc is None:
                    if self.uscc != other.uscc:
                        return (-1 if self.uscc is None else 1)
                else:
                    if self.uscc < other.uscc:
                        return -1
                    elif self.uscc > other.uscc:
                        return 1
                if self.tin is None or other.tin is None:
                    if self.tin != other.tin:
                        return (-1 if self.tin is None else 1)
                else:
                    if self.tin < other.tin:
                        return -1
                    elif self.tin > other.tin:
                        return 1
                if self.registerNumber is None or other.registerNumber is None:
                    if self.registerNumber != other.registerNumber:
                        return (-1 if self.registerNumber is None else 1)
                else:
                    if self.registerNumber < other.registerNumber:
                        return -1
                    elif self.registerNumber > other.registerNumber:
                        return 1
                if self.code is None or other.code is None:
                    if self.code != other.code:
                        return (-1 if self.code is None else 1)
                else:
                    if self.code < other.code:
                        return -1
                    elif self.code > other.code:
                        return 1
                if self.category is None or other.category is None:
                    if self.category != other.category:
                        return (-1 if self.category is None else 1)
                else:
                    if self.category < other.category:
                        return -1
                    elif self.category > other.category:
                        return 1
                if self.ra is None or other.ra is None:
                    if self.ra != other.ra:
                        return (-1 if self.ra is None else 1)
                else:
                    if self.ra < other.ra:
                        return -1
                    elif self.ra > other.ra:
                        return 1
                if self.region is None or other.region is None:
                    if self.region != other.region:
                        return (-1 if self.region is None else 1)
                else:
                    if self.region < other.region:
                        return -1
                    elif self.region > other.region:
                        return 1
                if self.nub is None or other.nub is None:
                    if self.nub != other.nub:
                        return (-1 if self.nub is None else 1)
                else:
                    if self.nub < other.nub:
                        return -1
                    elif self.nub > other.nub:
                        return 1
                if self.term is None or other.term is None:
                    if self.term != other.term:
                        return (-1 if self.term is None else 1)
                else:
                    if self.term < other.term:
                        return -1
                    elif self.term > other.term:
                        return 1
                if self.addr is None or other.addr is None:
                    if self.addr != other.addr:
                        return (-1 if self.addr is None else 1)
                else:
                    if self.addr < other.addr:
                        return -1
                    elif self.addr > other.addr:
                        return 1
                if self.doa is None or other.doa is None:
                    if self.doa != other.doa:
                        return (-1 if self.doa is None else 1)
                else:
                    if self.doa < other.doa:
                        return -1
                    elif self.doa > other.doa:
                        return 1
                if self.scope is None or other.scope is None:
                    if self.scope != other.scope:
                        return (-1 if self.scope is None else 1)
                else:
                    if self.scope < other.scope:
                        return -1
                    elif self.scope > other.scope:
                        return 1
                return 0

        def __lt__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r < 0

        def __le__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r <= 0

        def __gt__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r > 0

        def __ge__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r >= 0

        def __eq__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r == 0

        def __ne__(self, other):
            r = self.__compare(other)
            if r is NotImplemented:
                return r
            else:
                return r != 0

        def __str__(self):
            return IcePy.stringify(self, _M_svc._t_Org)

        __repr__ = __str__

    _M_svc._t_Org = IcePy.defineStruct('::svc::Org', Org, (), (
        ('name', (), IcePy._t_string),
        ('juridical', (), IcePy._t_string),
        ('uscc', (), IcePy._t_string),
        ('tin', (), IcePy._t_string),
        ('registerNumber', (), IcePy._t_string),
        ('code', (), IcePy._t_string),
        ('category', (), IcePy._t_string),
        ('ra', (), IcePy._t_string),
        ('region', (), IcePy._t_string),
        ('nub', (), IcePy._t_string),
        ('term', (), IcePy._t_string),
        ('addr', (), IcePy._t_string),
        ('doa', (), IcePy._t_string),
        ('scope', (), IcePy._t_string)
    ))

    _M_svc.Org = Org
    del Org

# End of module svc
