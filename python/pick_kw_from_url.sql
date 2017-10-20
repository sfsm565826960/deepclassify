add jar /home/rcgzz/cdc/HiveDecodeUri.jar;
drop temporary function decode_uri;
create temporary function decode_uri as 'hiveDecodeUri.HiveDecodeUri';
create table rcgzz.zh_4g_kw_tmp1 as 
select * from 
  (select url.msisdn, url.k, decode_uri(url.v) as v, url.domainname from 
      ( select msisdn 
      , regexp_extract(url,'%s',1) as k 
      , regexp_extract(url,'%s',2) as v 
      , domainname 
      from %s) url 
  where length(url.v) >= 9) kw 
where kw.v is not null and regexp_extract(kw.v, '%s', 1) > 0; 
drop temporary function decode_uri;

add jar /home/rcgzz/cdc/HiveDecodeUri.jar;
drop temporary function decode_uri;
create temporary function decode_uri as 'hiveDecodeUri.HiveDecodeUri';
create table rcgzz.zh_4g_kw_tmp1 as 
select * from 
  (select url.msisdn, url.k, decode_uri(url.v) as v, url.domainname from 
      ( select msisdn 
      , regexp_extract(url,'([a-zA-Z0-9_]+)=(([A-Za-z0-9+-_]{0,}(%[A-F0-9]{2}){6,}[A-Za-z0-9+-_]{0,})+)',1) as k 
      , regexp_extract(url,'([a-zA-Z0-9_]+)=(([A-Za-z0-9+-_]{0,}(%[A-F0-9]{2}){6,}[A-Za-z0-9+-_]{0,})+)',2) as v 
      , domainname 
      from rcgzz.zh_4gdpi) url 
  where length(url.v) >= 9) kw 
where kw.v is not null and regexp_extract(kw.v, '([^\\x00-\\xff]+)', 1) > 0; 
drop temporary function decode_uri;

# 用于判断无效字段
select domainname,v,count(*) as cnt from zh_4g_kw_tmp2 where k ='address' group by domainname,v order by cnt;

# 用于判断无效域名
select k,v,count(*) as cnt from zh_4g_kw_tmp2 where domainname ='statistic.3g.qq.com' group by k,v order by cnt;

# zh_4gdpi
URL数：159203222
用户数：96255

# zh_4g_kw
URL数：2919433
用户数：63035

# zh_4g_kw_filter
URL数：1701539
用户数：57676
