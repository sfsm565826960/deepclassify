#!/usr/bin/env python
#encoding=utf-8
import os

invaild_regexp = r'^[^\w][ Μδζτφροψωπεйиж:-_$=±#@%^&!|{}()·,\\+\\*\\.\\?\\;\\[\\]"]+'
table = 'zh_oa_kw_filter'
print '''insert overwrite table %s 
            select t1.msisdn, t1.domainname, t1.k, t1.v from 
                (select msisdn, domainname, k, regexp_replace(v,'%s','') as v from %s) t1 
            where length(t1.v) > 0;''' % \
      (
          table,
          invaild_regexp,
          table
      )